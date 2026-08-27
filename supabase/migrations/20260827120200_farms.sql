-- Farms. Access is membership-based; OWNER is recorded on farm_members.

CREATE TABLE IF NOT EXISTS public.farms (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name text NOT NULL CHECK (char_length(trim(name)) > 0),
    location text NOT NULL DEFAULT '',
    owner_id uuid NOT NULL REFERENCES public.profiles (id) ON DELETE RESTRICT,
    created_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    updated_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    is_active boolean NOT NULL DEFAULT true
);

CREATE INDEX IF NOT EXISTS farms_owner_id_idx ON public.farms (owner_id);
CREATE INDEX IF NOT EXISTS farms_is_active_idx ON public.farms (is_active);

DROP TRIGGER IF EXISTS farms_set_updated_at ON public.farms;
CREATE TRIGGER farms_set_updated_at
    BEFORE UPDATE ON public.farms
    FOR EACH ROW
    EXECUTE FUNCTION public.set_updated_at();

ALTER TABLE public.farms ENABLE ROW LEVEL SECURITY;

GRANT SELECT, INSERT, UPDATE, DELETE ON public.farms TO authenticated;
