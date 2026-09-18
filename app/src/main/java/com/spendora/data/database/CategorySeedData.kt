package com.spendora.data.database

import com.spendora.data.entity.AccountEntity
import com.spendora.data.entity.CategoryEntity
import com.spendora.data.model.AccountType
import com.spendora.data.model.CategoryType

object CategorySeedData {
    val defaultAccounts = listOf(
        AccountEntity(id = "acc_bank_default", name = "Bank Account", type = AccountType.BANK_ACCOUNT, balance = 0.0),
        AccountEntity(id = "acc_credit_card", name = "Credit Card", type = AccountType.CREDIT_CARD, balance = 0.0),
        AccountEntity(id = "acc_upi_wallet", name = "UPI / GPay", type = AccountType.UPI, balance = 0.0),
        AccountEntity(id = "acc_cash_wallet", name = "Cash Wallet", type = AccountType.CASH, balance = 0.0)
    )

    val defaultCategories = listOf(
        // Expense Categories
        CategoryEntity(id = "cat_food", name = "Food & Dining", type = CategoryType.EXPENSE, iconName = "restaurant", colorHex = "#F59E0B", isSystem = true),
        CategoryEntity(id = "cat_groceries", name = "Groceries", type = CategoryType.EXPENSE, iconName = "shopping_cart", colorHex = "#10B981", isSystem = true),
        CategoryEntity(id = "cat_transport", name = "Transport", type = CategoryType.EXPENSE, iconName = "directions_car", colorHex = "#3B82F6", isSystem = true),
        CategoryEntity(id = "cat_fuel", name = "Fuel", type = CategoryType.EXPENSE, iconName = "local_gas_station", colorHex = "#EF4444", isSystem = true),
        CategoryEntity(id = "cat_shopping", name = "Shopping", type = CategoryType.EXPENSE, iconName = "shopping_bag", colorHex = "#EC4899", isSystem = true),
        CategoryEntity(id = "cat_bills", name = "Bills & Utilities", type = CategoryType.EXPENSE, iconName = "receipt_long", colorHex = "#8B5CF6", isSystem = true),
        CategoryEntity(id = "cat_rent", name = "Rent", type = CategoryType.EXPENSE, iconName = "home", colorHex = "#6366F1", isSystem = true),
        CategoryEntity(id = "cat_health", name = "Health & Fitness", type = CategoryType.EXPENSE, iconName = "favorite", colorHex = "#14B8A6", isSystem = true),
        CategoryEntity(id = "cat_entertainment", name = "Entertainment", type = CategoryType.EXPENSE, iconName = "movie", colorHex = "#A855F7", isSystem = true),
        CategoryEntity(id = "cat_travel", name = "Travel", type = CategoryType.EXPENSE, iconName = "flight", colorHex = "#06B6D4", isSystem = true),

        // Income Categories
        CategoryEntity(id = "cat_salary", name = "Salary", type = CategoryType.INCOME, iconName = "payments", colorHex = "#10B981", isSystem = true),
        CategoryEntity(id = "cat_freelance", name = "Freelance & Consulting", type = CategoryType.INCOME, iconName = "work", colorHex = "#3B82F6", isSystem = true),
        CategoryEntity(id = "cat_investments", name = "Investments & Dividends", type = CategoryType.INCOME, iconName = "trending_up", colorHex = "#8B5CF6", isSystem = true),
        CategoryEntity(id = "cat_other_income", name = "Other Income", type = CategoryType.INCOME, iconName = "account_balance_wallet", colorHex = "#64748B", isSystem = true)
    )
}
