-- Phase 5: Feed is a ledger. No table stores a mutable on-hand balance.
CREATE TABLE public.feed_items (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(), farm_id uuid NOT NULL REFERENCES public.farms(id) ON DELETE RESTRICT,
    name text NOT NULL CHECK (char_length(trim(name)) > 0), feed_type text NOT NULL CHECK (feed_type IN ('LAYER_FEED','PRE_LAYER','STARTER','GROWER','CUSTOM_FEED')),
    unit text NOT NULL DEFAULT 'kg' CHECK (char_length(trim(unit)) > 0), opening_quantity_kg numeric(12,3) NOT NULL DEFAULT 0 CHECK (opening_quantity_kg >= 0),
    opening_cost_per_kg numeric(12,4), opening_date date NOT NULL DEFAULT CURRENT_DATE, is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT timezone('utc', now()), updated_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    UNIQUE(farm_id, name)
);
CREATE TABLE public.feed_purchases (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(), feed_item_id uuid NOT NULL REFERENCES public.feed_items(id) ON DELETE RESTRICT,
    farm_id uuid NOT NULL REFERENCES public.farms(id) ON DELETE RESTRICT, supplier text NOT NULL DEFAULT '', quantity_kg numeric(12,3) NOT NULL CHECK(quantity_kg > 0),
    unit text NOT NULL DEFAULT 'kg', price_per_kg numeric(12,4) NOT NULL CHECK(price_per_kg >= 0), batch text NOT NULL DEFAULT '', purchase_date date NOT NULL,
    expiry_date date, remarks text NOT NULL DEFAULT '', entered_by uuid NOT NULL DEFAULT auth.uid() REFERENCES public.profiles(id), created_at timestamptz NOT NULL DEFAULT timezone('utc', now()), updated_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    CHECK(expiry_date IS NULL OR expiry_date >= purchase_date)
);
CREATE TABLE public.feed_consumption (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(), feed_item_id uuid NOT NULL REFERENCES public.feed_items(id) ON DELETE RESTRICT,
    farm_id uuid NOT NULL REFERENCES public.farms(id) ON DELETE RESTRICT, flock_id uuid REFERENCES public.flocks(id) ON DELETE RESTRICT,
    quantity_kg numeric(12,3) NOT NULL CHECK(quantity_kg > 0), consumed_date date NOT NULL, remarks text NOT NULL DEFAULT '',
    entered_by uuid NOT NULL DEFAULT auth.uid() REFERENCES public.profiles(id), created_at timestamptz NOT NULL DEFAULT timezone('utc', now()), updated_at timestamptz NOT NULL DEFAULT timezone('utc', now())
);
CREATE TABLE public.feed_adjustments (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(), feed_item_id uuid NOT NULL REFERENCES public.feed_items(id) ON DELETE RESTRICT,
    farm_id uuid NOT NULL REFERENCES public.farms(id) ON DELETE RESTRICT, quantity_kg numeric(12,3) NOT NULL CHECK(quantity_kg <> 0), adjustment_date date NOT NULL,
    reason text NOT NULL CHECK(char_length(trim(reason)) > 0), allow_negative_stock boolean NOT NULL DEFAULT false,
    entered_by uuid NOT NULL DEFAULT auth.uid() REFERENCES public.profiles(id), created_at timestamptz NOT NULL DEFAULT timezone('utc', now()), updated_at timestamptz NOT NULL DEFAULT timezone('utc', now())
);
CREATE INDEX feed_purchases_item_date_idx ON public.feed_purchases(feed_item_id, purchase_date DESC);
CREATE INDEX feed_consumption_item_date_idx ON public.feed_consumption(feed_item_id, consumed_date DESC);
CREATE INDEX feed_consumption_flock_date_idx ON public.feed_consumption(flock_id, consumed_date DESC);
CREATE INDEX feed_adjustments_item_date_idx ON public.feed_adjustments(feed_item_id, adjustment_date DESC);

CREATE OR REPLACE FUNCTION public.feed_item_stock(p_feed_item_id uuid) RETURNS numeric LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public AS $$
 SELECT COALESCE((SELECT opening_quantity_kg FROM feed_items WHERE id = p_feed_item_id), 0)
      + COALESCE((SELECT sum(quantity_kg) FROM feed_purchases WHERE feed_item_id = p_feed_item_id), 0)
      + COALESCE((SELECT sum(quantity_kg) FROM feed_adjustments WHERE feed_item_id = p_feed_item_id), 0)
      - COALESCE((SELECT sum(quantity_kg) FROM feed_consumption WHERE feed_item_id = p_feed_item_id), 0)
$$;
CREATE OR REPLACE FUNCTION public.assert_feed_stock() RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE item uuid := COALESCE(NEW.feed_item_id, OLD.feed_item_id); allow_negative boolean := false;
BEGIN
  IF TG_TABLE_NAME = 'feed_adjustments' AND TG_OP <> 'DELETE' THEN allow_negative := NEW.allow_negative_stock; END IF;
  IF public.feed_item_stock(item) < 0 AND NOT allow_negative THEN RAISE EXCEPTION 'Feed inventory cannot be negative'; END IF;
  IF public.feed_item_stock(item) < 0 AND allow_negative AND NOT public.user_has_farm_role(COALESCE(NEW.farm_id, OLD.farm_id), ARRAY['OWNER']::public.farm_role[]) THEN RAISE EXCEPTION 'Only an owner may authorize negative inventory'; END IF;
  IF TG_OP = 'DELETE' THEN RETURN OLD; ELSE RETURN NEW; END IF;
END $$;
CREATE TRIGGER feed_purchase_stock_guard AFTER INSERT OR UPDATE OR DELETE ON public.feed_purchases FOR EACH ROW EXECUTE FUNCTION public.assert_feed_stock();
CREATE TRIGGER feed_consumption_stock_guard AFTER INSERT OR UPDATE OR DELETE ON public.feed_consumption FOR EACH ROW EXECUTE FUNCTION public.assert_feed_stock();
CREATE TRIGGER feed_adjustment_stock_guard AFTER INSERT OR UPDATE OR DELETE ON public.feed_adjustments FOR EACH ROW EXECUTE FUNCTION public.assert_feed_stock();

CREATE OR REPLACE FUNCTION public.record_feed_purchase(p_feed_item_id uuid, p_supplier text, p_quantity_kg numeric, p_unit text, p_price_per_kg numeric, p_batch text, p_purchase_date date, p_expiry_date date, p_remarks text) RETURNS uuid LANGUAGE plpgsql SECURITY INVOKER AS $$ DECLARE new_id uuid; item_farm uuid; BEGIN
 SELECT farm_id INTO item_farm FROM public.feed_items WHERE id=p_feed_item_id; IF item_farm IS NULL THEN RAISE EXCEPTION 'Feed item not found'; END IF;
 INSERT INTO public.feed_purchases(feed_item_id,farm_id,supplier,quantity_kg,unit,price_per_kg,batch,purchase_date,expiry_date,remarks) VALUES(p_feed_item_id,item_farm,p_supplier,p_quantity_kg,p_unit,p_price_per_kg,p_batch,p_purchase_date,p_expiry_date,p_remarks) RETURNING id INTO new_id; RETURN new_id; END $$;
CREATE OR REPLACE FUNCTION public.record_feed_consumption(p_feed_item_id uuid, p_flock_id uuid, p_quantity_kg numeric, p_consumed_date date, p_remarks text) RETURNS uuid LANGUAGE plpgsql SECURITY INVOKER AS $$ DECLARE new_id uuid; item_farm uuid; BEGIN
 SELECT farm_id INTO item_farm FROM public.feed_items WHERE id=p_feed_item_id; IF item_farm IS NULL THEN RAISE EXCEPTION 'Feed item not found'; END IF;
 IF p_flock_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM public.flocks WHERE id=p_flock_id AND farm_id=item_farm) THEN RAISE EXCEPTION 'Flock must belong to feed item farm'; END IF;
 INSERT INTO public.feed_consumption(feed_item_id,farm_id,flock_id,quantity_kg,consumed_date,remarks) VALUES(p_feed_item_id,item_farm,p_flock_id,p_quantity_kg,p_consumed_date,p_remarks) RETURNING id INTO new_id; RETURN new_id; END $$;
CREATE OR REPLACE FUNCTION public.record_feed_adjustment(p_feed_item_id uuid, p_quantity_kg numeric, p_adjustment_date date, p_reason text, p_allow_negative_stock boolean) RETURNS uuid LANGUAGE plpgsql SECURITY INVOKER AS $$ DECLARE new_id uuid; item_farm uuid; BEGIN
 SELECT farm_id INTO item_farm FROM public.feed_items WHERE id=p_feed_item_id; IF item_farm IS NULL THEN RAISE EXCEPTION 'Feed item not found'; END IF;
 INSERT INTO public.feed_adjustments(feed_item_id,farm_id,quantity_kg,adjustment_date,reason,allow_negative_stock) VALUES(p_feed_item_id,item_farm,p_quantity_kg,p_adjustment_date,p_reason,p_allow_negative_stock) RETURNING id INTO new_id; RETURN new_id; END $$;

ALTER TABLE public.feed_items ENABLE ROW LEVEL SECURITY; ALTER TABLE public.feed_purchases ENABLE ROW LEVEL SECURITY; ALTER TABLE public.feed_consumption ENABLE ROW LEVEL SECURITY; ALTER TABLE public.feed_adjustments ENABLE ROW LEVEL SECURITY;
GRANT SELECT,INSERT,UPDATE ON public.feed_items TO authenticated; GRANT SELECT,INSERT,UPDATE,DELETE ON public.feed_purchases,public.feed_consumption,public.feed_adjustments TO authenticated;
CREATE POLICY feed_items_members ON public.feed_items FOR SELECT TO authenticated USING(public.is_farm_member(farm_id));
CREATE POLICY feed_items_write ON public.feed_items FOR ALL TO authenticated USING(public.user_has_farm_role(farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[])) WITH CHECK(public.user_has_farm_role(farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[]));
CREATE POLICY feed_purchases_members ON public.feed_purchases FOR SELECT TO authenticated USING(public.is_farm_member(farm_id));
CREATE POLICY feed_purchases_write ON public.feed_purchases FOR ALL TO authenticated USING(public.user_has_farm_role(farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[])) WITH CHECK(entered_by=auth.uid() AND public.user_has_farm_role(farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[]));
CREATE POLICY feed_consumption_members ON public.feed_consumption FOR SELECT TO authenticated USING(public.is_farm_member(farm_id));
CREATE POLICY feed_consumption_write ON public.feed_consumption FOR ALL TO authenticated USING(public.user_has_farm_role(farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[])) WITH CHECK(entered_by=auth.uid() AND public.user_has_farm_role(farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[]));
CREATE POLICY feed_adjustments_members ON public.feed_adjustments FOR SELECT TO authenticated USING(public.is_farm_member(farm_id));
CREATE POLICY feed_adjustments_write ON public.feed_adjustments FOR ALL TO authenticated USING(public.user_has_farm_role(farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[])) WITH CHECK(entered_by=auth.uid() AND public.user_has_farm_role(farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[]));
