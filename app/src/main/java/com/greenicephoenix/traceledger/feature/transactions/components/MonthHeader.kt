package com.greenicephoenix.traceledger.feature.transactions.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthHeader(
    month: YearMonth,
    totalIn: String,
    totalOut: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = "←",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .clickable { onPrevious() }
                .padding(horizontal = 12.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )

            Text(
                text = "IN $totalIn  OUT $totalOut",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        Text(
            text = "→",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .clickable { onNext() }
                .padding(horizontal = 12.dp)
        )
    }
}