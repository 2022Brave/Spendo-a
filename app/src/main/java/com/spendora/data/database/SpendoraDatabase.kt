package com.spendora.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spendora.data.converter.Converters
import com.spendora.data.dao.AccountDao
import com.spendora.data.dao.AppSettingDao
import com.spendora.data.dao.BudgetDao
import com.spendora.data.dao.CategoryDao
import com.spendora.data.dao.FinancialCycleDao
import com.spendora.data.dao.MerchantRuleDao
import com.spendora.data.dao.SmsAuditDao
import com.spendora.data.dao.TransactionDao
import com.spendora.data.entity.AccountEntity
import com.spendora.data.entity.AppSettingEntity
import com.spendora.data.entity.BudgetEntity
import com.spendora.data.entity.CategoryEntity
import com.spendora.data.entity.FinancialCycleEntity
import com.spendora.data.entity.MerchantRuleEntity
import com.spendora.data.entity.SmsAuditEntity
import com.spendora.data.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        AccountEntity::class,
        CategoryEntity::class,
        FinancialCycleEntity::class,
        BudgetEntity::class,
        MerchantRuleEntity::class,
        SmsAuditEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SpendoraDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun financialCycleDao(): FinancialCycleDao
    abstract fun budgetDao(): BudgetDao
    abstract fun merchantRuleDao(): MerchantRuleDao
    abstract fun smsAuditDao(): SmsAuditDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: SpendoraDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): SpendoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpendoraDatabase::class.java,
                    "spendora.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed system categories, default accounts, and default financial cycle on fresh install
                        scope.launch {
                            val database = getDatabase(context, scope)
                            database.categoryDao().insertCategories(CategorySeedData.defaultCategories)
                            database.accountDao().insertAccounts(CategorySeedData.defaultAccounts)
                            database.financialCycleDao().setPrimaryCycle(
                                FinancialCycleEntity(
                                    id = "primary_cycle",
                                    startDay = 1,
                                    label = "Monthly Salary Cycle"
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
