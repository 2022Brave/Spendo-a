package com.spendora.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.spendora.data.entity.AccountEntity
import com.spendora.data.entity.AppSettingEntity
import com.spendora.data.entity.BudgetEntity
import com.spendora.data.entity.CategoryEntity
import com.spendora.data.entity.FinancialCycleEntity
import com.spendora.data.entity.MerchantRuleEntity
import com.spendora.data.entity.SmsAuditEntity
import com.spendora.data.entity.TransactionEntity
import com.spendora.data.model.ReviewStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE reviewStatus = :status ORDER BY timestamp DESC")
    fun getTransactionsByStatus(status: ReviewStatus = ReviewStatus.CONFIRMED): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE reviewStatus = :status AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getTransactionsInDateRange(
        startTime: Long,
        endTime: Long,
        status: ReviewStatus = ReviewStatus.CONFIRMED
    ): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE reviewStatus = 'PENDING_REVIEW' ORDER BY timestamp DESC")
    fun getPendingReviewTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE amount = :amount AND timestamp BETWEEN :startTime AND :endTime LIMIT 1")
    suspend fun findSimilarTransaction(amount: Double, startTime: Long, endTime: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllActiveAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountCount(): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}

@Dao
interface FinancialCycleDao {
    @Query("SELECT * FROM financial_cycles WHERE id = 'primary_cycle' LIMIT 1")
    fun getPrimaryCycle(): Flow<FinancialCycleEntity?>

    @Query("SELECT * FROM financial_cycles WHERE id = 'primary_cycle' LIMIT 1")
    suspend fun getPrimaryCycleDirect(): FinancialCycleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPrimaryCycle(cycle: FinancialCycleEntity)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE cycleId = :cycleId")
    fun getBudgetsForCycle(cycleId: String = "primary_cycle"): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)
}

@Dao
interface MerchantRuleDao {
    @Query("SELECT * FROM merchant_rules")
    fun getAllRules(): Flow<List<MerchantRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: MerchantRuleEntity)
}

@Dao
interface SmsAuditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: SmsAuditEntity)
}

@Dao
interface AppSettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    fun getSetting(key: String): Flow<String?>

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingDirect(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)
}
