-- Shared farm role enum used by membership and RLS.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'farm_role') THEN
        CREATE TYPE public.farm_role AS ENUM ('OWNER', 'MANAGER', 'WORKER');
    END IF;
END
$$;

CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = timezone('utc', now());
    RETURN NEW;
END;
$$;
