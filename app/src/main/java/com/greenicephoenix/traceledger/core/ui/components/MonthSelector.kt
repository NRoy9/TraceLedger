package com.greenicephoenix.traceledger.core.ui.components

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
import java.time.format.DateTimeFormatter

private val monthFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy")

@Composable
fun MonthSelector(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "‹",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier
                .clickable { onPrevious() }
                .padding(8.dp)
        )

        Text(
            text = month.format(monthFormatter).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )

        Text(
            text = "›",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier
                .clickable { onNext() }
                .padding(8.dp)
        )
    }
}