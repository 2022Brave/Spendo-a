package com.spendora.data.engine

import com.spendora.data.entity.TransactionEntity
import com.spendora.data.model.FinancialCycle
import com.spendora.data.model.FinancialCycleSummary
import com.spendora.data.model.ReviewStatus
import com.spendora.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FinancialCycleCalculator {

    fun calculateCycle(
        startDay: Int,
        referenceTimeMillis: Long = System.currentTimeMillis()
    ): FinancialCycle {
        val clampedStartDay = startDay.coerceIn(1, 31)
        val cal = Calendar.getInstance().apply {
            timeInMillis = referenceTimeMillis
        }

        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            val maxDaysInCurrentMonth = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, clampedStartDay.coerceAtMost(maxDaysInCurrentMonth))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (currentDay < clampedStartDay) {
            // We are before the cycle start day in the current month, so cycle started last month
            startCal.add(Calendar.MONTH, -1)
            val maxDaysInPrevMonth = startCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            startCal.set(Calendar.DAY_OF_MONTH, clampedStartDay.coerceAtMost(maxDaysInPrevMonth))
        }

        // End date is 1 month ahead minus 1 millisecond
        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            val maxDaysInNextMonth = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, clampedStartDay.coerceAtMost(maxDaysInNextMonth))
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val startFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        val endFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val label = "${startFormat.format(startCal.time)} – ${endFormat.format(endCal.time)}"

        return FinancialCycle(
            startDay = clampedStartDay,
            startDateMillis = startCal.timeInMillis,
            endDateMillis = endCal.timeInMillis,
            label = label
        )
    }

    fun calculateSummary(
        transactions: List<TransactionEntity>,
        cycle: FinancialCycle
    ): FinancialCycleSummary {
        var totalSpending = 0.0
        var totalIncome = 0.0
        var cashWithdrawals = 0.0

        for (tx in transactions) {
            if (tx.reviewStatus == ReviewStatus.REJECTED) continue
            if (tx.timestamp in cycle.startDateMillis..cycle.endDateMillis) {
                when (tx.type) {
                    TransactionType.EXPENSE -> totalSpending += tx.amount
                    TransactionType.INCOME -> totalIncome += tx.amount
                    TransactionType.REFUND -> totalIncome += tx.amount
                    TransactionType.CASH_WITHDRAWAL -> cashWithdrawals += tx.amount
                    TransactionType.TRANSFER -> {
                        // Internal transfer does NOT count towards general spending
                    }
                }
            }
        }

        val now = System.currentTimeMillis()
        val remainingMillis = (cycle.endDateMillis - now).coerceAtLeast(0L)
        val daysRemaining = (remainingMillis / (1000 * 60 * 60 * 24)).toInt()

        return FinancialCycleSummary(
            totalSpending = totalSpending,
            totalIncome = totalIncome,
            cashWithdrawals = cashWithdrawals,
            netCashFlow = totalIncome - totalSpending,
            cycleLabel = cycle.label,
            daysRemaining = daysRemaining
        )
    }
}
