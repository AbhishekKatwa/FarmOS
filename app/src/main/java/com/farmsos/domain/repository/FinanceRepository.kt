package com.farmsos.domain.repository
import com.farmsos.domain.model.*
interface FinanceRepository { suspend fun categories(farmId:String):Result<List<ExpenseCategory>>; suspend fun expenses(farmId:String):Result<List<Expense>>; suspend fun summaries(farmId:String):Result<List<DailyFinancialSummary>>; suspend fun addExpense(value:Expense):Result<Expense> }
