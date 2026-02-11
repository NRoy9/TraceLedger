package com.greenicephoenix.traceledger.feature.statistics.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import androidx.compose.ui.geometry.Size

@Composable
fun CashflowBarChart(
    entries: List<StatisticsViewModel.CashflowEntry>,
    selectedDay: Int?,
    onDaySelected: (StatisticsViewModel.CashflowEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
        Text(
            text = "No cashflow data",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        return
    }

    val maxValue =
        entries.fold(BigDecimal.ZERO) { acc, e ->
            maxOf(acc, e.income, e.expense)
        }

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 500),
        label = "cashflow-animation"
    )

    val axisColor =
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    val gridColor =
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

    val highlightAlpha by animateFloatAsState(
        targetValue = if (selectedDay != null) 0.10f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "cashflow-highlight"
    )

    val selectedBarScale by animateFloatAsState(
        targetValue = if (selectedDay != null) 1.08f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "cashflow-bar-scale"
    )

    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .pointerInput(entries) {
                detectTapGestures { offset ->
                    val leftPaddingPx = 40.dp.toPx()
                    val usableWidth = size.width - leftPaddingPx

                    if (offset.x < leftPaddingPx || entries.isEmpty()) return@detectTapGestures

                    val dayGroupWidth = usableWidth / entries.size
                    val index =
                        ((offset.x - leftPaddingPx) / dayGroupWidth).toInt()

                    if (index in entries.indices) {
                        onDaySelected(entries[index])
                    }
                }
            }
    ) {
    val leftPadding = 40.dp.toPx()
        val chartHeight = size.height * 0.75f
        val chartBottom = chartHeight
        val labelY = chartBottom + 22.dp.toPx()

        val dayGroupWidth = (size.width - leftPadding) / entries.size
        val barWidth = dayGroupWidth * 0.25f
        val intraBarGap = barWidth * 0.3f

        // Axes
        drawLine(axisColor, Offset(leftPadding, 0f), Offset(leftPadding, chartBottom), 1.dp.toPx())
        drawLine(axisColor, Offset(leftPadding, chartBottom), Offset(size.width, chartBottom), 1.dp.toPx())

        // Grid + Y labels
        val gridSteps = 4
        repeat(gridSteps) { step ->
            val fraction = (step + 1) / gridSteps.toFloat()
            val y = chartBottom - (chartBottom * fraction)
            val value = maxValue.multiply(BigDecimal(fraction.toDouble()))

            drawLine(gridColor, Offset(leftPadding, y), Offset(size.width, y), 1.dp.toPx())

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = axisColor.toArgb()
                    textSize = 11.sp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isAntiAlias = true
                }
                canvas.nativeCanvas.drawText(
                    formatCompactMagnitude(value),
                    leftPadding - 6.dp.toPx(),
                    y + 4.dp.toPx(),
                    paint
                )
            }
        }

        // Bars + X labels
        entries.forEachIndexed { index, entry ->
            val isSelected = selectedDay == entry.day


            if (isSelected && highlightAlpha > 0f) {
                drawRect(
                    color = axisColor.copy(alpha = highlightAlpha),
                    topLeft = Offset(
                        x = leftPadding + index * dayGroupWidth,
                        y = 0f
                    ),
                    size = Size(
                        width = dayGroupWidth,
                        height = chartBottom
                    )
                )
            }

            val incomeHeight =
                if (maxValue == BigDecimal.ZERO) 0f
                else (entry.income.toFloat() / maxValue.toFloat()) *
                        chartHeight * animatedProgress

            val expenseHeight =
                if (maxValue == BigDecimal.ZERO) 0f
                else (entry.expense.toFloat() / maxValue.toFloat()) *
                        chartHeight * animatedProgress

            val groupCenterX =
                leftPadding + index * dayGroupWidth + (dayGroupWidth / 2f)

            val incomeX = groupCenterX - barWidth - (intraBarGap / 2f)
            val expenseX = groupCenterX + (intraBarGap / 2f)

            drawRect(
                incomeColor,
                Offset(incomeX, chartBottom - incomeHeight),
                Size(barWidth, incomeHeight * if (isSelected) selectedBarScale else 1f)
            )

            drawRect(
                expenseColor,
                Offset(expenseX, chartBottom - expenseHeight),
                Size(barWidth, expenseHeight * if (isSelected) selectedBarScale else 1f)
            )

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = axisColor.toArgb()
                    textSize = 11.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.nativeCanvas.drawText(
                    entry.day.toString(),
                    groupCenterX,
                    labelY,
                    paint
                )
            }
        }
    }
}

private fun formatCompactMagnitude(value: BigDecimal): String {
    val abs = value.abs()
    return when {
        abs >= BigDecimal("100000") ->
            abs.divide(BigDecimal("100000"))
                .setScale(1, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString() + "L"
        abs >= BigDecimal("1000") ->
            abs.divide(BigDecimal("1000"))
                .setScale(1, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString() + "k"
        else ->
            abs.stripTrailingZeros().toPlainString()
    }
}