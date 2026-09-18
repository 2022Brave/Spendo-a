package com.spendora.data.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER,
    REFUND,
    CASH_WITHDRAWAL
}

enum class ReviewStatus {
    CONFIRMED,
    PENDING_REVIEW,
    REJECTED
}

enum class TransactionSource {
    MANUAL,
    SMS_LIVE,
    SMS_IMPORT
}

enum class CategoryType {
    EXPENSE,
    INCOME
}

enum class AccountType {
    BANK_ACCOUNT,
    CREDIT_CARD,
    CASH,
    UPI,
    WALLET,
    OTHER
}

data class FinancialCycle(
    val id: String = "default_cycle",
    val startDay: Int = 1,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val label: String
)

data class FinancialCycleSummary(
    val totalSpending: Double = 0.0,
    val totalIncome: Double = 0.0,
    val cashWithdrawals: Double = 0.0,
    val netCashFlow: Double = 0.0,
    val cycleLabel: String = "",
    val daysRemaining: Int = 0
)
