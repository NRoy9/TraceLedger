package com.greenicephoenix.traceledger.feature.statistics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModel
import kotlin.math.min

@Composable
fun IncomeDonutChart(
    slices: List<StatisticsViewModel.CategorySlice>,
    categoryMap: Map<String, CategoryUiModel>,
    modifier: Modifier = Modifier
) {
    if (slices.isEmpty()) {
        EmptyIncomeChartPlaceholder()
        return
    }

    val fallbackColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(220.dp)
        ) {
            val strokeWidth = 28.dp.toPx()
            val diameter = min(size.width, size.height)
            val arcSize = Size(diameter, diameter)

            var startAngle = -90f

            slices.forEach { slice ->
                val sweepAngle = slice.percentage * 3.6f

                val color =
                    categoryMap[slice.categoryId]
                        ?.color
                        ?.let { Color(it) }
                        ?: fallbackColor

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )

                startAngle += sweepAngle
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "INCOME",
                style = MaterialTheme.typography.labelSmall,
                color = onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "BREAKDOWN",
                style = MaterialTheme.typography.titleMedium,
                color = onSurface
            )
        }
    }
}

@Composable
private fun EmptyIncomeChartPlaceholder() {
    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No income data",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}