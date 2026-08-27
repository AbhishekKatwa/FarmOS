package com.farmsos.data.repository

import com.farmsos.data.remote.dto.*
import com.farmsos.domain.model.*
import com.farmsos.domain.repository.FinanceRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepositoryImpl @Inject constructor(private val db: Postgrest) : FinanceRepository {
    private fun ExpenseCategoryDto.d() = ExpenseCategory(id.orEmpty(), farmId, name, active);
    private fun ExpenseDto.d() = Expense(
        id.orEmpty(),
        farmId,
        flockId,
        date,
        categoryId,
        amount,
        vendor,
        paymentMethod,
        reference,
        notes,
        attachmentPath
    );

    private fun DailyFinancialSummaryDto.d() = DailyFinancialSummary(
        farmId,
        flockId,
        date,
        revenue,
        feedCost,
        medicineCost,
        labourCost,
        transportCost,
        otherExpenses,
        grossProfit,
        netProfit,
        eggs,
        trays,
        averageLiveBirds
    )

    override suspend fun categories(farmId: String) = runCatching {
        db["expense_categories"].select().decodeList<ExpenseCategoryDto>()
            .filter { it.farmId == null || it.farmId == farmId }.map { it.d() }
    }

    override suspend fun expenses(farmId: String) = runCatching {
        db["expenses"].select { filter {ExpenseDto::farmId eq farmId }}.decodeList<ExpenseDto>()
            .map { it.d() }
    }

    override suspend fun summaries(farmId: String) = runCatching {
        db["daily_financial_summary"].select { filter {DailyFinancialSummaryDto::farmId eq farmId }}
            .decodeList<DailyFinancialSummaryDto>().map { it.d() }
    }

    override suspend fun addExpense(v: Expense) = runCatching {
        require(v.amount > 0) { "Expense amount must be greater than zero" }; db["expenses"].insert(
        ExpenseDto(
            farmId = v.farmId,
            flockId = v.flockId,
            date = v.date,
            categoryId = v.categoryId,
            amount = v.amount,
            vendor = v.vendor,
            paymentMethod = v.paymentMethod,
            reference = v.reference,
            notes = v.notes,
            attachmentPath = v.attachmentPath
        )
    ) { select() }.decodeSingle<ExpenseDto>().d()
    }
}
