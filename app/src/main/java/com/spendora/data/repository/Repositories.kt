package com.spendora.data.repository

import com.spendora.data.dao.AccountDao
import com.spendora.data.dao.BudgetDao
import com.spendora.data.dao.CategoryDao
import com.spendora.data.dao.FinancialCycleDao
import com.spendora.data.dao.TransactionDao
import com.spendora.data.entity.AccountEntity
import com.spendora.data.entity.BudgetEntity
import com.spendora.data.entity.CategoryEntity
import com.spendora.data.entity.FinancialCycleEntity
import com.spendora.data.entity.TransactionEntity
import com.spendora.data.model.FinancialCycle
import com.spendora.data.model.ReviewStatus
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    fun getConfirmedTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByStatus(ReviewStatus.CONFIRMED)

    fun getTransactionsInDateRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsInDateRange(startTime, endTime, ReviewStatus.CONFIRMED)

    fun getPendingReviewTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getPendingReviewTransactions()

    suspend fun getTransactionById(id: String): TransactionEntity? =
        transactionDao.getTransactionById(id)

    suspend fun findSimilarTransaction(amount: Double, startTime: Long, endTime: Long): TransactionEntity? =
        transactionDao.findSimilarTransaction(amount, startTime, endTime)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(id: String) =
        transactionDao.deleteTransactionById(id)

    suspend fun getTransactionCount(): Int =
        transactionDao.getTransactionCount()
}

class FinancialCycleRepository(private val financialCycleDao: FinancialCycleDao) {
    fun getPrimaryCycle(): Flow<FinancialCycleEntity?> =
        financialCycleDao.getPrimaryCycle()

    suspend fun getPrimaryCycleDirect(): FinancialCycleEntity? =
        financialCycleDao.getPrimaryCycleDirect()

    suspend fun updateStartDay(startDay: Int) {
        val clamped = startDay.coerceIn(1, 31)
        financialCycleDao.setPrimaryCycle(
            FinancialCycleEntity(
                id = "primary_cycle",
                startDay = clamped,
                label = "Monthly Cycle (Starts Day $clamped)"
            )
        )
    }
}

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllActiveCategories()

    suspend fun insertCategory(category: CategoryEntity) =
        categoryDao.insertCategory(category)

    suspend fun ensureDefaultCategories(defaultList: List<CategoryEntity>) {
        if (categoryDao.getCategoryCount() == 0) {
            categoryDao.insertCategories(defaultList)
        }
    }
}

class AccountRepository(private val accountDao: AccountDao) {
    fun getAllAccounts(): Flow<List<AccountEntity>> =
        accountDao.getAllActiveAccounts()

    suspend fun insertAccount(account: AccountEntity) =
        accountDao.insertAccount(account)

    suspend fun ensureDefaultAccounts(defaultList: List<AccountEntity>) {
        if (accountDao.getAccountCount() == 0) {
            accountDao.insertAccounts(defaultList)
        }
    }
}

class BudgetRepository(private val budgetDao: BudgetDao) {
    fun getBudgets(): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsForCycle("primary_cycle")

    suspend fun insertBudget(budget: BudgetEntity) =
        budgetDao.insertBudget(budget)

    suspend fun deleteBudget(budget: BudgetEntity) =
        budgetDao.deleteBudget(budget)
}
