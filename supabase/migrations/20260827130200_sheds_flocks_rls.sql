-- RLS for sheds and flocks: members can read; OWNER/MANAGER write; OWNER can delete.
-- Access is always scoped to farms the user belongs to.

DROP POLICY IF EXISTS sheds_select_members ON public.sheds;
CREATE POLICY sheds_select_members
    ON public.sheds
    FOR SELECT
    TO authenticated
    USING (public.is_farm_member(farm_id));

DROP POLICY IF EXISTS sheds_insert_owner_manager ON public.sheds;
CREATE POLICY sheds_insert_owner_manager
    ON public.sheds
    FOR INSERT
    TO authenticated
    WITH CHECK (public.user_has_farm_role(farm_id, ARRAY['OWNER', 'MANAGER']::public.farm_role[]));

DROP POLICY IF EXISTS sheds_update_owner_manager ON public.sheds;
CREATE POLICY sheds_update_owner_manager
    ON public.sheds
    FOR UPDATE
    TO authenticated
    USING (public.user_has_farm_role(farm_id, ARRAY['OWNER', 'MANAGER']::public.farm_role[]))
    WITH CHECK (public.user_has_farm_role(farm_id, ARRAY['OWNER', 'MANAGER']::public.farm_role[]));

DROP POLICY IF EXISTS sheds_delete_owner ON public.sheds;
CREATE POLICY sheds_delete_owner
    ON public.sheds
    FOR DELETE
    TO authenticated
    USING (public.user_has_farm_role(farm_id, ARRAY['OWNER']::public.farm_role[]));

DROP POLICY IF EXISTS flocks_select_members ON public.flocks;
CREATE POLICY flocks_select_members
    ON public.flocks
    FOR SELECT
    TO authenticated
    USING (public.is_farm_member(farm_id));

DROP POLICY IF EXISTS flocks_insert_owner_manager ON public.flocks;
CREATE POLICY flocks_insert_owner_manager
    ON public.flocks
    FOR INSERT
    TO authenticated
    WITH CHECK (
        public.user_has_farm_role(farm_id, ARRAY['OWNER', 'MANAGER']::public.farm_role[])
        AND EXISTS (
            SELECT 1 FROM public.sheds s
            WHERE s.id = shed_id
              AND s.farm_id = farm_id
              AND s.is_active = true
        )
    );

DROP POLICY IF EXISTS flocks_update_owner_manager ON public.flocks;
CREATE POLICY flocks_update_owner_manager
    ON public.flocks
    FOR UPDATE
    TO authenticated
    USING (public.user_has_farm_role(farm_id, ARRAY['OWNER', 'MANAGER']::public.farm_role[]))
    WITH CHECK (
        public.user_has_farm_role(farm_id, ARRAY['OWNER', 'MANAGER']::public.farm_role[])
        AND EXISTS (
            SELECT 1 FROM public.sheds s
            WHERE s.id = shed_id
              AND s.farm_id = farm_id
        )
    );

DROP POLICY IF EXISTS flocks_delete_owner ON public.flocks;
CREATE POLICY flocks_delete_owner
    ON public.flocks
    FOR DELETE
    TO authenticated
    USING (public.user_has_farm_role(farm_id, ARRAY['OWNER']::public.farm_role[]));
