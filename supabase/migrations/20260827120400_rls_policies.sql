-- RLS helpers and policies. Users can only access farms they belong to.
-- OWNER: full farm access.
-- MANAGER: update farm; invite WORKER; cannot delete farm or manage OWNER.
-- WORKER: read-only.

CREATE OR REPLACE FUNCTION public.current_user_farm_ids()
RETURNS SETOF uuid
LANGUAGE sql
SECURITY DEFINER
SET search_path = public
STABLE
AS $$
    SELECT farm_id
    FROM public.farm_members
    WHERE user_id = auth.uid();
$$;

CREATE OR REPLACE FUNCTION public.is_farm_member(_farm_id uuid)
RETURNS boolean
LANGUAGE sql
SECURITY DEFINER
SET search_path = public
STABLE
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM public.farm_members
        WHERE farm_id = _farm_id
          AND user_id = auth.uid()
    );
$$;

CREATE OR REPLACE FUNCTION public.user_has_farm_role(_farm_id uuid, _roles public.farm_role[])
RETURNS boolean
LANGUAGE sql
SECURITY DEFINER
SET search_path = public
STABLE
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM public.farm_members
        WHERE farm_id = _farm_id
          AND user_id = auth.uid()
          AND role = ANY (_roles)
    );
$$;

REVOKE ALL ON FUNCTION public.current_user_farm_ids() FROM PUBLIC;
REVOKE ALL ON FUNCTION public.is_farm_member(uuid) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.user_has_farm_role(uuid, public.farm_role[]) FROM PUBLIC;

GRANT EXECUTE ON FUNCTION public.current_user_farm_ids() TO authenticated;
GRANT EXECUTE ON FUNCTION public.is_farm_member(uuid) TO authenticated;
GRANT EXECUTE ON FUNCTION public.user_has_farm_role(uuid, public.farm_role[]) TO authenticated;

CREATE OR REPLACE FUNCTION public.prevent_unauthorized_owner_change()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF OLD.owner_id IS DISTINCT FROM NEW.owner_id
       AND NOT public.user_has_farm_role(OLD.id, ARRAY['OWNER']::public.farm_role[]) THEN
        RAISE EXCEPTION 'Only OWNER can transfer farm ownership';
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS farms_prevent_unauthorized_owner_change ON public.farms;
CREATE TRIGGER farms_prevent_unauthorized_owner_change
    BEFORE UPDATE ON public.farms
    FOR EACH ROW
    EXECUTE FUNCTION public.prevent_unauthorized_owner_change();

DROP POLICY IF EXISTS profiles_select_self_or_farm_peers ON public.profiles;
CREATE POLICY profiles_select_self_or_farm_peers
    ON public.profiles
    FOR SELECT
    TO authenticated
    USING (
        id = auth.uid()
        OR id IN (
            SELECT fm.user_id
            FROM public.farm_members fm
            WHERE fm.farm_id IN (SELECT public.current_user_farm_ids())
        )
    );

DROP POLICY IF EXISTS profiles_insert_self ON public.profiles;
CREATE POLICY profiles_insert_self
    ON public.profiles
    FOR INSERT
    TO authenticated
    WITH CHECK (id = auth.uid());

DROP POLICY IF EXISTS profiles_update_self ON public.profiles;
CREATE POLICY profiles_update_self
    ON public.profiles
    FOR UPDATE
    TO authenticated
    USING (id = auth.uid())
    WITH CHECK (id = auth.uid());

DROP POLICY IF EXISTS farms_select_members ON public.farms;
CREATE POLICY farms_select_members
    ON public.farms
    FOR SELECT
    TO authenticated
    USING (public.is_farm_member(id));

DROP POLICY IF EXISTS farms_insert_owner ON public.farms;
CREATE POLICY farms_insert_owner
    ON public.farms
    FOR INSERT
    TO authenticated
    WITH CHECK (owner_id = auth.uid());

DROP POLICY IF EXISTS farms_update_owner_manager ON public.farms;
CREATE POLICY farms_update_owner_manager
    ON public.farms
    FOR UPDATE
    TO authenticated
    USING (public.user_has_farm_role(id, ARRAY['OWNER', 'MANAGER']::public.farm_role[]))
    WITH CHECK (public.user_has_farm_role(id, ARRAY['OWNER', 'MANAGER']::public.farm_role[]));

DROP POLICY IF EXISTS farms_delete_owner ON public.farms;
CREATE POLICY farms_delete_owner
    ON public.farms
    FOR DELETE
    TO authenticated
    USING (public.user_has_farm_role(id, ARRAY['OWNER']::public.farm_role[]));

DROP POLICY IF EXISTS farm_members_select_members ON public.farm_members;
CREATE POLICY farm_members_select_members
    ON public.farm_members
    FOR SELECT
    TO authenticated
    USING (public.is_farm_member(farm_id));

DROP POLICY IF EXISTS farm_members_insert_by_role ON public.farm_members;
CREATE POLICY farm_members_insert_by_role
    ON public.farm_members
    FOR INSERT
    TO authenticated
    WITH CHECK (
        role <> 'OWNER'
        AND (
            (
                role IN ('MANAGER', 'WORKER')
                AND public.user_has_farm_role(farm_id, ARRAY['OWNER']::public.farm_role[])
            )
            OR (
                role = 'WORKER'
                AND public.user_has_farm_role(farm_id, ARRAY['MANAGER']::public.farm_role[])
            )
        )
    );

DROP POLICY IF EXISTS farm_members_update_owner ON public.farm_members;
CREATE POLICY farm_members_update_owner
    ON public.farm_members
    FOR UPDATE
    TO authenticated
    USING (public.user_has_farm_role(farm_id, ARRAY['OWNER']::public.farm_role[]))
    WITH CHECK (
        public.user_has_farm_role(farm_id, ARRAY['OWNER']::public.farm_role[])
        AND role IN ('MANAGER', 'WORKER')
    );

DROP POLICY IF EXISTS farm_members_delete_owner_or_self ON public.farm_members;
CREATE POLICY farm_members_delete_owner_or_self
    ON public.farm_members
    FOR DELETE
    TO authenticated
    USING (
        role <> 'OWNER'
        AND (
            user_id = auth.uid()
            OR public.user_has_farm_role(farm_id, ARRAY['OWNER']::public.farm_role[])
        )
    );
