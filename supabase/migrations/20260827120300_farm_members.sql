-- Farm membership. Role is the authorization source for a user on a farm.

CREATE TABLE IF NOT EXISTS public.farm_members (
    farm_id uuid NOT NULL REFERENCES public.farms (id) ON DELETE CASCADE,
    user_id uuid NOT NULL REFERENCES public.profiles (id) ON DELETE CASCADE,
    role public.farm_role NOT NULL,
    created_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    PRIMARY KEY (farm_id, user_id)
);

CREATE INDEX IF NOT EXISTS farm_members_user_id_idx ON public.farm_members (user_id);
CREATE INDEX IF NOT EXISTS farm_members_farm_id_role_idx ON public.farm_members (farm_id, role);

CREATE UNIQUE INDEX IF NOT EXISTS farm_members_one_owner_per_farm
    ON public.farm_members (farm_id)
    WHERE role = 'OWNER';

CREATE OR REPLACE FUNCTION public.handle_new_farm()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    INSERT INTO public.farm_members (farm_id, user_id, role)
    VALUES (NEW.id, NEW.owner_id, 'OWNER')
    ON CONFLICT (farm_id, user_id) DO UPDATE SET role = 'OWNER';
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS on_farm_created ON public.farms;
CREATE TRIGGER on_farm_created
    AFTER INSERT ON public.farms
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_farm();

ALTER TABLE public.farm_members ENABLE ROW LEVEL SECURITY;

GRANT SELECT, INSERT, UPDATE, DELETE ON public.farm_members TO authenticated;
