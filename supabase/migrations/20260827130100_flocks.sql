-- Flocks belong to a shed on the same farm. Age is never stored; derive it from placement_date.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'flock_status') THEN
        CREATE TYPE public.flock_status AS ENUM ('PLANNED', 'ACTIVE', 'DEPLETED', 'CLOSED');
    END IF;
END
$$;

CREATE TABLE IF NOT EXISTS public.flocks (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id uuid NOT NULL,
    shed_id uuid NOT NULL,
    flock_code text NOT NULL CHECK (char_length(trim(flock_code)) > 0),
    breed text NOT NULL DEFAULT '',
    strain text NOT NULL DEFAULT '',
    placement_date date NOT NULL,
    initial_birds integer NOT NULL CHECK (initial_birds > 0),
    current_live_birds integer NOT NULL CHECK (current_live_birds >= 0),
    status public.flock_status NOT NULL DEFAULT 'PLANNED',
    target_production text NOT NULL DEFAULT '',
    notes text NOT NULL DEFAULT '',
    created_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    updated_at timestamptz NOT NULL DEFAULT timezone('utc', now()),
    CONSTRAINT flocks_live_birds_not_above_initial CHECK (current_live_birds <= initial_birds),
    CONSTRAINT flocks_shed_same_farm
        FOREIGN KEY (shed_id, farm_id)
        REFERENCES public.sheds (id, farm_id)
        ON DELETE RESTRICT,
    CONSTRAINT flocks_code_unique_per_farm UNIQUE (farm_id, flock_code)
);

CREATE INDEX IF NOT EXISTS flocks_farm_id_idx ON public.flocks (farm_id);
CREATE INDEX IF NOT EXISTS flocks_shed_id_idx ON public.flocks (shed_id);
CREATE INDEX IF NOT EXISTS flocks_status_idx ON public.flocks (status);
CREATE INDEX IF NOT EXISTS flocks_placement_date_idx ON public.flocks (placement_date);

DROP TRIGGER IF EXISTS flocks_set_updated_at ON public.flocks;
CREATE TRIGGER flocks_set_updated_at
    BEFORE UPDATE ON public.flocks
    FOR EACH ROW
    EXECUTE FUNCTION public.set_updated_at();

ALTER TABLE public.flocks ENABLE ROW LEVEL SECURITY;

GRANT SELECT, INSERT, UPDATE, DELETE ON public.flocks TO authenticated;
