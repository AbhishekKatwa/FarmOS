-- Phase 4: daily poultry production. Grades are rows (rather than an enum) so farms
-- can enable, rename, or add grades without an application release.
CREATE TABLE IF NOT EXISTS public.production_egg_grades (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code text NOT NULL UNIQUE CHECK (code = upper(code) AND char_length(trim(code)) > 0),
    display_name text NOT NULL CHECK (char_length(trim(display_name)) > 0),
    is_active boolean NOT NULL DEFAULT true,
    sort_order integer NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    updated_at timestamptz NOT NULL DEFAULT timezone('utc', now())
);

INSERT INTO public.production_egg_grades (code, display_name, sort_order)
VALUES
    ('JUMBO', 'Jumbo', 10), ('LARGE', 'Large', 20), ('MEDIUM', 'Medium', 30),
    ('SMALL', 'Small', 40), ('REJECT', 'Reject', 50), ('BROKEN', 'Broken', 60)
ON CONFLICT (code) DO NOTHING;

CREATE TABLE IF NOT EXISTS public.production_daily (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id uuid NOT NULL REFERENCES public.farms(id) ON DELETE RESTRICT,
    shed_id uuid NOT NULL,
    flock_id uuid NOT NULL,
    date date NOT NULL,
    opening_live_birds integer NOT NULL CHECK (opening_live_birds >= 0),
    mortality integer NOT NULL DEFAULT 0 CHECK (mortality >= 0),
    culls integer NOT NULL DEFAULT 0 CHECK (culls >= 0),
    closing_live_birds integer GENERATED ALWAYS AS (opening_live_birds - mortality - culls) STORED,
    eggs_collected integer NOT NULL DEFAULT 0 CHECK (eggs_collected >= 0),
    broken_eggs integer NOT NULL DEFAULT 0 CHECK (broken_eggs >= 0),
    dirty_eggs integer NOT NULL DEFAULT 0 CHECK (dirty_eggs >= 0),
    usable_eggs integer NOT NULL DEFAULT 0 CHECK (usable_eggs >= 0),
    rejected_eggs integer NOT NULL DEFAULT 0 CHECK (rejected_eggs >= 0),
    feed_consumed_kg numeric(12,3) NOT NULL DEFAULT 0 CHECK (feed_consumed_kg >= 0),
    remarks text NOT NULL DEFAULT '',
    entered_by uuid NOT NULL DEFAULT auth.uid() REFERENCES public.profiles(id) ON DELETE RESTRICT,
    created_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    updated_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    CONSTRAINT production_daily_shed_same_farm FOREIGN KEY (shed_id, farm_id)
        REFERENCES public.sheds(id, farm_id) ON DELETE RESTRICT,
    CONSTRAINT production_daily_flock_same_farm FOREIGN KEY (flock_id, farm_id)
        REFERENCES public.flocks(id, farm_id) ON DELETE RESTRICT,
    CONSTRAINT production_daily_losses_not_above_opening CHECK (mortality + culls <= opening_live_birds),
    CONSTRAINT production_daily_unique_flock_date UNIQUE (flock_id, date)
);

CREATE TABLE IF NOT EXISTS public.production_daily_egg_grade_entries (
    production_daily_id uuid NOT NULL REFERENCES public.production_daily(id) ON DELETE CASCADE,
    egg_grade_id uuid NOT NULL REFERENCES public.production_egg_grades(id) ON DELETE RESTRICT,
    quantity integer NOT NULL CHECK (quantity >= 0),
    PRIMARY KEY (production_daily_id, egg_grade_id)
);

CREATE TABLE IF NOT EXISTS public.mortality_records (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    production_daily_id uuid NOT NULL UNIQUE REFERENCES public.production_daily(id) ON DELETE CASCADE,
    farm_id uuid NOT NULL REFERENCES public.farms(id) ON DELETE RESTRICT,
    flock_id uuid NOT NULL REFERENCES public.flocks(id) ON DELETE RESTRICT,
    date date NOT NULL,
    mortality_count integer NOT NULL CHECK (mortality_count >= 0),
    cause text NOT NULL DEFAULT '',
    remarks text NOT NULL DEFAULT '',
    entered_by uuid NOT NULL DEFAULT auth.uid() REFERENCES public.profiles(id) ON DELETE RESTRICT,
    created_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    updated_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    CONSTRAINT mortality_records_flock_date_matches_production CHECK (mortality_count >= 0)
);

CREATE INDEX IF NOT EXISTS production_daily_farm_date_idx ON public.production_daily(farm_id, date DESC);
CREATE INDEX IF NOT EXISTS production_daily_flock_date_idx ON public.production_daily(flock_id, date DESC);
CREATE INDEX IF NOT EXISTS mortality_records_farm_date_idx ON public.mortality_records(farm_id, date DESC);

DROP TRIGGER IF EXISTS production_daily_set_updated_at ON public.production_daily;
CREATE TRIGGER production_daily_set_updated_at BEFORE UPDATE ON public.production_daily
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
DROP TRIGGER IF EXISTS production_egg_grades_set_updated_at ON public.production_egg_grades;
CREATE TRIGGER production_egg_grades_set_updated_at BEFORE UPDATE ON public.production_egg_grades
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
DROP TRIGGER IF EXISTS mortality_records_set_updated_at ON public.mortality_records;
CREATE TRIGGER mortality_records_set_updated_at BEFORE UPDATE ON public.mortality_records
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

ALTER TABLE public.production_daily ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.production_daily_egg_grade_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.production_egg_grades ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.mortality_records ENABLE ROW LEVEL SECURITY;
GRANT SELECT ON public.production_egg_grades TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.production_daily, public.production_daily_egg_grade_entries, public.mortality_records TO authenticated;

CREATE POLICY production_egg_grades_select_authenticated ON public.production_egg_grades FOR SELECT TO authenticated USING (true);
CREATE POLICY production_daily_select_members ON public.production_daily FOR SELECT TO authenticated USING (public.is_farm_member(farm_id));
CREATE POLICY production_daily_insert_owner_manager ON public.production_daily FOR INSERT TO authenticated WITH CHECK (
    entered_by = auth.uid() AND public.user_has_farm_role(farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[])
    AND EXISTS (SELECT 1 FROM public.flocks f WHERE f.id = flock_id AND f.farm_id = farm_id AND f.shed_id = shed_id)
);
CREATE POLICY production_daily_update_owner_manager ON public.production_daily FOR UPDATE TO authenticated
    USING (public.user_has_farm_role(farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[]))
    WITH CHECK (entered_by = auth.uid() AND public.user_has_farm_role(farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[]));
CREATE POLICY production_daily_delete_owner ON public.production_daily FOR DELETE TO authenticated USING (public.user_has_farm_role(farm_id, ARRAY['OWNER']::public.farm_role[]));
CREATE POLICY production_grade_entries_select_members ON public.production_daily_egg_grade_entries FOR SELECT TO authenticated USING (
    EXISTS (SELECT 1 FROM public.production_daily p WHERE p.id = production_daily_id AND public.is_farm_member(p.farm_id)));
CREATE POLICY production_grade_entries_write_owner_manager ON public.production_daily_egg_grade_entries FOR ALL TO authenticated USING (
    EXISTS (SELECT 1 FROM public.production_daily p WHERE p.id = production_daily_id AND public.user_has_farm_role(p.farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[])))
    WITH CHECK (EXISTS (SELECT 1 FROM public.production_daily p WHERE p.id = production_daily_id AND public.user_has_farm_role(p.farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[])));
CREATE POLICY mortality_records_select_members ON public.mortality_records FOR SELECT TO authenticated USING (public.is_farm_member(farm_id));
CREATE POLICY mortality_records_write_owner_manager ON public.mortality_records FOR ALL TO authenticated USING (
    entered_by = auth.uid() AND public.user_has_farm_role(farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[]))
    WITH CHECK (entered_by = auth.uid() AND public.user_has_farm_role(farm_id, ARRAY['OWNER','MANAGER']::public.farm_role[]));
