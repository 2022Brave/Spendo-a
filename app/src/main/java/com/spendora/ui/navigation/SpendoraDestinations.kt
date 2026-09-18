package com.spendora.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object AddTransaction : Screen("add_transaction")
    object Analytics : Screen("analytics")
    object Budgets : Screen("budgets")
    object More : Screen("more")
    object Accounts : Screen("accounts")
    object FinancialCycleSettings : Screen("financial_cycle_settings")
    object PendingReview : Screen("pending_review")
    object HistoricalImport : Screen("historical_import")
    object PrivacyGuarantee : Screen("privacy_guarantee")
}
