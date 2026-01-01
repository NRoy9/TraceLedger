package com.greenicephoenix.traceledger.feature.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.ui.theme.Grey850
import com.greenicephoenix.traceledger.core.ui.theme.TextSecondary

@Composable
fun BalanceCard(
    totalBalance: String
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
                text = "TOTAL BALANCE",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = totalBalance,
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}
