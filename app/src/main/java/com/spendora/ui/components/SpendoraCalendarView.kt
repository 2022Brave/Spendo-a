package com.spendora.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpendoraExpenseRed
import com.example.ui.theme.SpendoraIncomeGreen
import com.example.ui.theme.SpendoraPurplePrimary
import com.spendora.data.entity.TransactionEntity
import com.spendora.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SpendoraCalendarView(
    transactions: List<TransactionEntity>,
    onTransactionClick: (TransactionEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var calendarMonth by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        })
    }

    var selectedDateMillis by remember {
        mutableStateOf<Long?>(System.currentTimeMillis())
    }

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthTitle = monthFormat.format(calendarMonth.time)

    // Build calendar days for the current month
    val daysInMonth = calendarMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendarMonth.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday...
    // Normalize to Monday = 0
    val offset = (firstDayOfWeek - Calendar.MONDAY + 7) % 7

    // Group transactions by (Year, Month, Day)
    val txByDay = remember(transactions, calendarMonth.timeInMillis) {
        val map = mutableMapOf<Int, MutableList<TransactionEntity>>()
        val tempCal = Calendar.getInstance()
        val currentYear = calendarMonth.get(Calendar.YEAR)
        val currentMonth = calendarMonth.get(Calendar.MONTH)

        for (tx in transactions) {
            tempCal.timeInMillis = tx.timestamp
            if (tempCal.get(Calendar.YEAR) == currentYear && tempCal.get(Calendar.MONTH) == currentMonth) {
                val day = tempCal.get(Calendar.DAY_OF_MONTH)
                map.getOrPut(day) { mutableListOf() }.add(tx)
            }
        }
        map
    }

    // Selected day transactions
    val selectedDayTransactions = remember(selectedDateMillis, transactions) {
        if (selectedDateMillis == null) emptyList()
        else {
            val selCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis!! }
            val selYear = selCal.get(Calendar.YEAR)
            val selMonth = selCal.get(Calendar.MONTH)
            val selDay = selCal.get(Calendar.DAY_OF_MONTH)

            val tempCal = Calendar.getInstance()
            transactions.filter { tx ->
                tempCal.timeInMillis = tx.timestamp
                tempCal.get(Calendar.YEAR) == selYear &&
                        tempCal.get(Calendar.MONTH) == selMonth &&
                        tempCal.get(Calendar.DAY_OF_MONTH) == selDay
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // Month navigation header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    calendarMonth = (calendarMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, -1)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(
                onClick = {
                    calendarMonth = (calendarMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, 1)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Weekday row (Mon to Sun)
        val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Calendar grid (up to 6 rows of 7 days)
        val totalCells = offset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - offset + 1

                    if (dayNumber in 1..daysInMonth) {
                        val dayCal = (calendarMonth.clone() as Calendar).apply {
                            set(Calendar.DAY_OF_MONTH, dayNumber)
                        }
                        val isSelected = selectedDateMillis?.let {
                            val selCal = Calendar.getInstance().apply { timeInMillis = it }
                            selCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                                    selCal.get(Calendar.MONTH) == dayCal.get(Calendar.MONTH) &&
                                    selCal.get(Calendar.DAY_OF_MONTH) == dayNumber
                        } ?: false

                        val hasExpenses = txByDay[dayNumber]?.any { it.type == TransactionType.EXPENSE } == true
                        val hasIncome = txByDay[dayNumber]?.any { it.type == TransactionType.INCOME } == true

                        DayCell(
                            day = dayNumber,
                            isSelected = isSelected,
                            hasExpenses = hasExpenses,
                            hasIncome = hasIncome,
                            onClick = {
                                selectedDateMillis = dayCal.timeInMillis
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Empty placeholder cell
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Expandable day detail card
        AnimatedVisibility(
            visible = selectedDateMillis != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val selectedDateStr = selectedDateMillis?.let {
                SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(it))
            } ?: ""

            val dayExpenseSum = selectedDayTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDateStr,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (dayExpenseSum > 0) "${formatRupee(dayExpenseSum)} spent" else "No spending",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (dayExpenseSum > 0) SpendoraExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedDayTransactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    selectedDayTransactions.forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onTransactionClick(tx) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tx.merchantName ?: tx.note ?: "Transaction",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(tx.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = (if (tx.type == TransactionType.INCOME) "+" else "-") + formatRupee(tx.amount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.type == TransactionType.INCOME) SpendoraIncomeGreen else SpendoraExpenseRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    hasExpenses: Boolean,
    hasIncome: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cellScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "cell_scale"
    )

    Column(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = SpendoraPurplePrimary),
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .scale(cellScale)
                .clip(CircleShape)
                .background(if (isSelected) SpendoraPurplePrimary else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }

        // Indicators row
        Row(
            modifier = Modifier.padding(top = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (hasExpenses) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else SpendoraExpenseRed)
                )
            }
            if (hasIncome) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else SpendoraIncomeGreen)
                )
            }
        }
    }
}
