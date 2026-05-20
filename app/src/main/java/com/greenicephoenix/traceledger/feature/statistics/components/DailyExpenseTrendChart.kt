package com.greenicephoenix.traceledger.feature.statistics.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModel.DailyExpensePoint
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.LineProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties

/**
 * Day-by-day expense line chart for the selected month.
 */
@Composable
fun DailyExpenseTrendChart(
    points:   List<DailyExpensePoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val gridColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val axisColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    val line = Line(
        label                   = "Daily Expense",
        values                  = points.map { it.amount },
        color                   = SolidColor(NothingRed),
        firstGradientFillColor  = NothingRed.copy(alpha = 0.22f),
        secondGradientFillColor = Color.Unspecified,
        drawStyle               = DrawStyle.Stroke(width = 2.dp),
        strokeAnimationSpec     = tween(600),
        gradientAnimationSpec   = tween(600),
        dotProperties           = DotProperties(
            enabled     = true,
            radius      = 3.dp,
            color       = SolidColor(NothingRed),
            strokeColor = SolidColor(NothingRed),
            strokeWidth = 2.dp
        ),
        curvedEdges = true
    )

    LineChart(
        modifier            = modifier.fillMaxWidth().height(180.dp),
        data                = listOf(line),
        indicatorProperties = HorizontalIndicatorProperties(
            textStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor),
            padding   = 8.dp
        ),
        labelProperties     = LabelProperties(
            enabled   = true,
            textStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor),
            labels    = points.map { if (it.day % 5 == 0) "${it.day}" else "" }
        ),
        gridProperties      = GridProperties(
            xAxisProperties = GridProperties.AxisProperties(color = SolidColor(gridColor), thickness = 0.5.dp),
            yAxisProperties = GridProperties.AxisProperties(color = SolidColor(gridColor), thickness = 0.5.dp)
        ),
        dividerProperties   = DividerProperties(
            xAxisProperties = LineProperties(color = SolidColor(axisColor), thickness = 1.dp),
            yAxisProperties = LineProperties(enabled = false)
        ),
        popupProperties     = PopupProperties(
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            textStyle      = MaterialTheme.typography.labelSmall.copy(
                color      = MaterialTheme.colorScheme.inverseOnSurface
            )
        ),
        animationMode = AnimationMode.Together()
    )
}