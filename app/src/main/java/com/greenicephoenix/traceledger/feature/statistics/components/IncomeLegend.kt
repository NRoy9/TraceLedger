package com.greenicephoenix.traceledger.feature.statistics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.currency.Currency
import com.greenicephoenix.traceledger.core.currency.CurrencyFormatter
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModel

@Composable
fun IncomeLegend(
    slices: List<StatisticsViewModel.CategorySlice>,
    categoryMap: Map<String, CategoryUiModel>
) {
    if (slices.isEmpty()) return

    val currency by CurrencyManager.currency.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        slices.forEach { slice ->
            val category = categoryMap[slice.categoryId]

            LegendRow(
                color = category?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.surfaceVariant,
                title = category?.name ?: "Unknown",
                percentage = slice.percentage,
                amount = slice.amount.toPlainString(),
                currency = currency
            )
        }
    }
}

@Composable
private fun LegendRow(
    color: Color,
    title: String,
    percentage: Float,
    amount: String,
    currency: Currency
) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = onSurface,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = String.format("%.1f%%", percentage),
            style = MaterialTheme.typography.bodyMedium,
            color = onSurface.copy(alpha = 0.6f)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = CurrencyFormatter.format(amount, currency),
            style = MaterialTheme.typography.bodyMedium,
            color = onSurface.copy(alpha = 0.6f)
        )
    }
}