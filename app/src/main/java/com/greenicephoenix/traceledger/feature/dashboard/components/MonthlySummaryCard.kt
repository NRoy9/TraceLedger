package com.greenicephoenix.traceledger.feature.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.ui.theme.Grey850
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.core.ui.theme.SuccessGreen
import com.greenicephoenix.traceledger.core.ui.theme.TextSecondary

@Composable
fun MonthlySummaryCard(
    income: String,
    expense: String,
    net: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Grey850
        ),
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "THIS MONTH",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                SummaryItem(
                    label = "IN",
                    value = income,
                    color = SuccessGreen
                )

                SummaryItem(
                    label = "OUT",
                    value = expense,
                    color = NothingRed
                )

                SummaryItem(
                    label = "NET",
                    value = net,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color
        )
    }
}
