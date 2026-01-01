package com.greenicephoenix.traceledger.feature.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.ui.theme.Grey850
import com.greenicephoenix.traceledger.core.ui.theme.TextSecondary

@Composable
fun BudgetCard(
    onAddBudgetClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Grey850
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddBudgetClick() }
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "BUDGET",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Set monthly budgets to control spending",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
