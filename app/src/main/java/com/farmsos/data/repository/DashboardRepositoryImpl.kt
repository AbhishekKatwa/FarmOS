package com.farmsos.data.repository

import com.farmsos.data.remote.dto.*
import com.farmsos.domain.model.*
import com.farmsos.domain.repository.DashboardRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(private val db: Postgrest) : DashboardRepository {
    override suspend fun load(
        farmId: String?,
        shedId: String?,
        flockId: String?,
        start: String,
        end: String
    ) = runCatching {
        val production =
            db["dashboard_production_daily"].select { filter { if (farmId != null) DashboardDailyDto::farmId eq farmId; if (shedId != null) DashboardDailyDto::shedId eq shedId; if (flockId != null) DashboardDailyDto::flockId eq flockId; DashboardDailyDto::date gte start; DashboardDailyDto::date lte end } }
                .decodeList<DashboardDailyDto>().map {
                    DashboardDaily(
                        it.farmId,
                        it.shedId,
                        it.flockId,
                        it.date,
                        it.liveBirds,
                        it.eggs,
                        it.mortality,
                        it.feedKg,
                        it.henDay
                    )
                }
        val sales =
            db["dashboard_sales_daily"].select { filter { if (farmId != null) DashboardSaleDto::farmId eq farmId; if (flockId != null) DashboardSaleDto::flockId eq flockId; DashboardSaleDto::date gte start; DashboardSaleDto::date lte end } }
                .decodeList<DashboardSaleDto>().map {
                    DashboardSale(
                        it.farmId,
                        it.flockId,
                        it.date,
                        it.eggs,
                        it.trays,
                        it.revenue,
                        it.eggRate
                    )
                }
        val finance =
            db["dashboard_finance_daily"].select { filter { if (farmId != null) DashboardFinanceDto::farmId eq farmId; if (flockId != null) DashboardFinanceDto::flockId eq flockId; DashboardFinanceDto::date gte start; DashboardFinanceDto::date lte end } }
                .decodeList<DashboardFinanceDto>().map {
                    DashboardFinance(
                        it.farmId,
                        it.flockId,
                        it.date,
                        it.revenue,
                        it.expenses,
                        it.profit
                    )
                }
        val grades =
            db["dashboard_grade_daily"].select { filter { if (farmId != null) DashboardGradeDto::farmId eq farmId; if (shedId != null) DashboardGradeDto::shedId eq shedId; if (flockId != null) DashboardGradeDto::flockId eq flockId; DashboardGradeDto::date gte start; DashboardGradeDto::date lte end } }
                .decodeList<DashboardGradeDto>().map {
                    DashboardGrade(
                        it.farmId,
                        it.shedId,
                        it.flockId,
                        it.date,
                        it.grade,
                        it.eggs
                    )
                }
        val alerts = db["dashboard_critical_alerts"].select().decodeList<DashboardAlertDto>()
            .filter { farmId == null || it.farmId == farmId }
            .map { DashboardAlert(it.farmId, it.type, it.message, it.dueDate) }
        val stock = db["dashboard_feed_stock"].select().decodeList<DashboardFeedDto>()
            .firstOrNull { farmId == null || it.farmId == farmId }?.stockDays
        val outstanding =
            db["buyer_balances"].select().decodeList<com.farmsos.data.remote.dto.BuyerBalanceDto>()
                .filter { farmId == null || it.farmId == farmId }.sumOf { it.closingBalance }
        val today = production.maxByOrNull { it.date };
        val latestSales = sales.maxByOrNull { it.date };
        val f = finance.filter { it.date == end }
        DashboardSnapshot(
            today?.liveBirds ?: 0.0,
            today?.eggs ?: 0.0,
            today?.henDay,
            today?.mortality ?: 0.0,
            today?.feedKg ?: 0.0,
            0.0,
            latestSales?.eggRate,
            sales.filter { it.date == end }.sumOf { it.revenue },
            f.sumOf { it.expenses },
            f.sumOf { it.profit },
            outstanding,
            stock,
            alerts,
            production,
            sales,
            finance,
            grades
        )
    }
}
