package com.spendora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpendoraExpenseRed
import com.example.ui.theme.SpendoraExpenseRedSubtle
import com.example.ui.theme.SpendoraIncomeGreen
import com.example.ui.theme.SpendoraIncomeGreenSubtle
import com.example.ui.theme.SpendoraPurplePrimary
import com.example.ui.theme.SpendoraSavingsPurple
import com.example.ui.theme.SpendoraSavingsPurpleSubtle
import java.text.NumberFormat
import java.util.Locale

fun formatRupee(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 2
    return "₹" + formatter.format(amount)
}

@Composable
fun SpendoraHeroCard(
    totalSpent: Double,
    totalIncome: Double,
    netSavings: Double,
    cycleLabel: String,
    modifier: Modifier = Modifier
) {
    val heroGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF35125E),
            Color(0xFF22113D),
            Color(0xFF13101C)
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(heroGradient)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF7C3AED).copy(alpha = 0.6f),
                        Color(0xFF2E1A47).copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFC4B5FD),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatRupee(totalSpent),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }

            // Cycle pill badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = cycleLabel.ifEmpty { "Current Cycle" },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFDDD6FE),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Stats Row: Income, Expenses, Savings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Income
            QuickStatBadge(
                label = "Income",
                amount = formatRupee(totalIncome),
                indicatorColor = SpendoraIncomeGreen,
                backgroundColor = SpendoraIncomeGreenSubtle,
                modifier = Modifier.weight(1f)
            )

            // Expenses
            QuickStatBadge(
                label = "Expenses",
                amount = formatRupee(totalSpent),
                indicatorColor = SpendoraExpenseRed,
                backgroundColor = SpendoraExpenseRedSubtle,
                modifier = Modifier.weight(1f)
            )

            // Savings
            QuickStatBadge(
                label = "Savings",
                amount = formatRupee(netSavings),
                indicatorColor = SpendoraSavingsPurple,
                backgroundColor = SpendoraSavingsPurpleSubtle,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickStatBadge(
    label: String,
    amount: String,
    indicatorColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F0D17).copy(alpha = 0.8f))
            .border(1.dp, Color(0xFF262136), RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
    }
}
