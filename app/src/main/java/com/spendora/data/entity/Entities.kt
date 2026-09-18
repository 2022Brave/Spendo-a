package com.spendora.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spendora.data.model.AccountType
import com.spendora.data.model.CategoryType
import com.spendora.data.model.ReviewStatus
import com.spendora.data.model.TransactionSource
import com.spendora.data.model.TransactionType
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val type: TransactionType,
    val categoryId: String? = null,
    val accountId: String? = null,
    val toAccountId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null,
    val merchantName: String? = null,
    val rawSms: String? = null,
    val reviewStatus: ReviewStatus = ReviewStatus.CONFIRMED,
    val source: TransactionSource = TransactionSource.MANUAL,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val confidenceScore: Float = 1.0f
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: AccountType,
    val balance: Double = 0.0,
    val lastFour: String? = null,
    val isArchived: Boolean = false
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: CategoryType,
    val iconName: String = "category",
    val colorHex: String = "#7C3AED",
    val isSystem: Boolean = false,
    val isArchived: Boolean = false
)

@Entity(tableName = "financial_cycles")
data class FinancialCycleEntity(
    @PrimaryKey
    val id: String = "primary_cycle",
    val startDay: Int = 1,
    val label: String = "Monthly Cycle",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String,
    val amount: Double,
    val cycleId: String = "primary_cycle"
)

@Entity(tableName = "merchant_rules")
data class MerchantRuleEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val merchantPattern: String,
    val categoryId: String,
    val autoCategorize: Boolean = true
)

@Entity(tableName = "sms_audits")
data class SmsAuditEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val smsBody: String,
    val sender: String,
    val timestamp: Long,
    val parseStatus: String,
    val extractedAmount: Double? = null
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
