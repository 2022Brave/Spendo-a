package com.spendora.data.converter

import androidx.room.TypeConverter
import com.spendora.data.model.AccountType
import com.spendora.data.model.CategoryType
import com.spendora.data.model.ReviewStatus
import com.spendora.data.model.TransactionSource
import com.spendora.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = try {
        TransactionType.valueOf(value)
    } catch (e: Exception) {
        TransactionType.EXPENSE
    }

    @TypeConverter
    fun fromReviewStatus(value: ReviewStatus): String = value.name

    @TypeConverter
    fun toReviewStatus(value: String): ReviewStatus = try {
        ReviewStatus.valueOf(value)
    } catch (e: Exception) {
        ReviewStatus.CONFIRMED
    }

    @TypeConverter
    fun fromTransactionSource(value: TransactionSource): String = value.name

    @TypeConverter
    fun toTransactionSource(value: String): TransactionSource = try {
        TransactionSource.valueOf(value)
    } catch (e: Exception) {
        TransactionSource.MANUAL
    }

    @TypeConverter
    fun fromCategoryType(value: CategoryType): String = value.name

    @TypeConverter
    fun toCategoryType(value: String): CategoryType = try {
        CategoryType.valueOf(value)
    } catch (e: Exception) {
        CategoryType.EXPENSE
    }

    @TypeConverter
    fun fromAccountType(value: AccountType): String = value.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = try {
        AccountType.valueOf(value)
    } catch (e: Exception) {
        AccountType.OTHER
    }
}
