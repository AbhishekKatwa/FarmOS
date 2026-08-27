-- Sheds belong to a farm. Composite unique (id, farm_id) lets flocks enforce the same farm.

CREATE TABLE IF NOT EXISTS public.sheds (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id uuid NOT NULL REFERENCES public.farms (id) ON DELETE CASCADE,
    name text NOT NULL CHECK (char_length(trim(name)) > 0),
    capacity integer CHECK (capacity IS NULL OR capacity > 0),
    notes text NOT NULL DEFAULT '',
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    updated_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    UNIQUE (id, farm_id)
);

CREATE INDEX IF NOT EXISTS sheds_farm_id_idx ON public.sheds (farm_id);
CREATE INDEX IF NOT EXISTS sheds_farm_id_active_idx ON public.sheds (farm_id, is_active);

DROP TRIGGER IF EXISTS sheds_set_updated_at ON public.sheds;
CREATE TRIGGER sheds_set_updated_at
    BEFORE UPDATE ON public.sheds
    FOR EACH ROW
    EXECUTE FUNCTION public.set_updated_at();

ALTER TABLE public.sheds ENABLE ROW LEVEL SECURITY;

GRANT SELECT, INSERT, UPDATE, DELETE ON public.sheds TO authenticated;
