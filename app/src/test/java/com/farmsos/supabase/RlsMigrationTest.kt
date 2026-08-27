package com.farmsos.supabase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RlsMigrationTest {

    private val sql = loadMigrations()

    @Test
    fun tablesHavePrimaryKeysForeignKeysAndIndexes() {
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS public.profiles"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS public.farms"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS public.farm_members"))
        assertTrue(sql.contains("REFERENCES auth.users (id)"))
        assertTrue(sql.contains("REFERENCES public.profiles (id)"))
        assertTrue(sql.contains("REFERENCES public.farms (id)"))
        assertTrue(sql.contains("PRIMARY KEY (farm_id, user_id)"))
        assertTrue(sql.contains("profiles_email_idx"))
        assertTrue(sql.contains("farms_owner_id_idx"))
        assertTrue(sql.contains("farm_members_user_id_idx"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS public.sheds"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS public.flocks"))
        assertTrue(sql.contains("CONSTRAINT flocks_shed_same_farm"))
        assertTrue(sql.contains("REFERENCES public.sheds (id, farm_id)"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS public.production_daily"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS public.production_egg_grades"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS public.mortality_records"))
        assertTrue(sql.contains("production_daily_unique_flock_date UNIQUE (flock_id, date)"))
        assertTrue(sql.contains("production_daily_losses_not_above_opening"))
    }

    @Test
    fun rlsIsEnabledAndMembershipScoped() {
        assertTrue(sql.contains("ENABLE ROW LEVEL SECURITY"))
        assertTrue(sql.contains("is_farm_member"))
        assertTrue(sql.contains("farms_select_members"))
        assertTrue(sql.contains("USING (public.is_farm_member(id))"))
        assertTrue(sql.contains("farm_members_select_members"))
        assertTrue(sql.contains("sheds_select_members"))
        assertTrue(sql.contains("flocks_select_members"))
        assertTrue(sql.contains("USING (public.is_farm_member(farm_id))"))
        assertTrue(sql.contains("production_daily_select_members"))
        assertTrue(sql.contains("mortality_records_select_members"))
    }

    @Test
    fun ownerManagerWorkerAreEnforced() {
        assertTrue(sql.contains("CREATE TYPE public.farm_role AS ENUM ('OWNER', 'MANAGER', 'WORKER')"))
        assertTrue(sql.contains("farms_update_owner_manager"))
        assertTrue(sql.contains("ARRAY['OWNER', 'MANAGER']"))
        assertTrue(sql.contains("farms_delete_owner"))
        assertTrue(sql.contains("ARRAY['OWNER']"))
        assertTrue(sql.contains("farm_members_insert_by_role"))
        assertTrue(sql.contains("role = 'WORKER'"))
        assertTrue(sql.contains("AND public.user_has_farm_role(farm_id, ARRAY['MANAGER']"))
        assertFalse(sql.contains("service_role"))
        assertTrue(sql.contains("sheds_insert_owner_manager"))
        assertTrue(sql.contains("flocks_insert_owner_manager"))
    }

    @Test
    fun helpersAreSecurityDefinerToAvoidRlsRecursion() {
        assertTrue(sql.contains("CREATE OR REPLACE FUNCTION public.current_user_farm_ids()"))
        assertTrue(sql.contains("SECURITY DEFINER"))
        assertTrue(sql.contains("WHERE user_id = auth.uid()"))
    }

    @Test
    fun dailyProductionPreventsDuplicatesAndInvalidCountsInDatabase() {
        assertTrue(sql.contains("CONSTRAINT production_daily_unique_flock_date UNIQUE (flock_id, date)"))
        assertTrue(sql.contains("CHECK (mortality + culls <= opening_live_birds)"))
        assertTrue(sql.contains("CHECK (eggs_collected >= 0)"))
        assertTrue(sql.contains("CHECK (feed_consumed_kg >= 0)"))
        assertTrue(sql.contains("closing_live_birds integer GENERATED ALWAYS AS (opening_live_birds - mortality - culls) STORED"))
    }

    @Test
    fun feedLedgerHasDerivedStockProtectionAndRls() {
        assertTrue(sql.contains("CREATE TABLE public.feed_items"))
        assertTrue(sql.contains("CREATE TABLE public.feed_purchases"))
        assertTrue(sql.contains("CREATE TABLE public.feed_consumption"))
        assertTrue(sql.contains("CREATE TABLE public.feed_adjustments"))
        assertTrue(sql.contains("CREATE OR REPLACE FUNCTION public.feed_item_stock"))
        assertTrue(sql.contains("Feed inventory cannot be negative"))
        assertTrue(sql.contains("Only an owner may authorize negative inventory"))
        assertTrue(sql.contains("feed_consumption_members"))
    }

    @Test
    fun medicineAndVaccinationTablesHaveMemberScopedRls() {
        assertTrue(sql.contains("CREATE TABLE public.medicines"))
        assertTrue(sql.contains("CREATE TABLE public.medicine_purchases"))
        assertTrue(sql.contains("CREATE TABLE public.medicine_usage"))
        assertTrue(sql.contains("CREATE TABLE public.vaccines"))
        assertTrue(sql.contains("CREATE TABLE public.vaccination_schedules"))
        assertTrue(sql.contains("CREATE TABLE public.vaccination_records"))
        assertTrue(sql.contains("upcoming_vaccination_reminders"))
        assertTrue(sql.contains("medicine_expiry_alerts"))
        assertTrue(sql.contains("medicine_usage_select_members"))
        assertTrue(sql.contains("vaccination_records_select_members"))
    }

    @Test
    fun buyerDispatchLedgerUsesTransactionsAndProtectsSaleableEggs() {
        assertTrue(sql.contains("CREATE TABLE public.buyers"))
        assertTrue(sql.contains("CREATE TABLE public.dispatches"))
        assertTrue(sql.contains("CREATE TABLE public.dispatch_items"))
        assertTrue(sql.contains("CREATE TABLE public.buyer_ledger"))
        assertTrue(sql.contains("CREATE TABLE public.buyer_payments"))
        assertTrue(sql.contains("Dispatch exceeds available saleable eggs"))
        assertTrue(sql.contains("Only an owner may authorize an oversell"))
        assertTrue(sql.contains("buyer_outstanding_aging"))
        assertTrue(sql.contains("buyer_ledger_members"))
    }

    @Test
    fun financeKeepsActualsSeparateFromPlans() {
        assertTrue(sql.contains("CREATE TABLE public.expense_categories"))
        assertTrue(sql.contains("CREATE TABLE public.expenses"))
        assertTrue(sql.contains("CREATE TABLE public.financial_transactions"))
        assertTrue(sql.contains("CREATE TABLE public.daily_financial_summary"))
        assertTrue(sql.contains("CREATE TABLE public.financial_plan_entries"))
        assertTrue(sql.contains("plan_type IN ('BUDGET','FORECAST','SIMULATION')"))
        assertTrue(sql.contains("source_type='DISPATCH' AND transaction_type='REVENUE'"))
        assertTrue(sql.contains("transactions_members"))
    }

    private fun loadMigrations(): String {
        val candidates = listOf(
            File("supabase/migrations"),
            File("../supabase/migrations"),
            File("../../supabase/migrations")
        )
        val dir = candidates.firstOrNull { it.isDirectory }
            ?: error("Could not locate supabase/migrations")
        val files = dir.listFiles { file -> file.extension == "sql" }?.sortedBy { it.name }
            ?: error("No SQL migrations found in ${dir.absolutePath}")
        return files.joinToString("\n") { it.readText() }
    }
}
