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
import com.greenicephoenix.traceledger.core.ui.theme.SuccessGreen
import com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModel.RunningBalancePoint
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
import java.time.format.DateTimeFormatter

/**
 * Running balance timeline — cumulative net over last 90 days.
 * Color: green when positive trend, red when negative.
 */
@Composable
fun RunningBalanceChart(
    points:   List<RunningBalancePoint>,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return

    val isPositive = (points.lastOrNull()?.balance ?: 0.0) >= 0.0
    val lineColor  = if (isPositive) SuccessGreen else NothingRed
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val gridColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val axisColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    val formatter  = DateTimeFormatter.ofPattern("d MMM")

    val line = Line(
        label                   = "Balance",
        values                  = points.map { it.balance },
        color                   = SolidColor(lineColor),
        firstGradientFillColor  = lineColor.copy(alpha = 0.25f),
        secondGradientFillColor = Color.Unspecified,
        drawStyle               = DrawStyle.Stroke(width = 2.dp),
        strokeAnimationSpec     = tween(700),
        gradientAnimationSpec   = tween(700),
        dotProperties           = DotProperties(enabled = false),
        curvedEdges             = true
    )

    LineChart(
        modifier            = modifier.fillMaxWidth().height(200.dp),
        data                = listOf(line),
        indicatorProperties = HorizontalIndicatorProperties(
            textStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor),
            padding   = 8.dp
        ),
        labelProperties     = LabelProperties(
            enabled   = true,
            textStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor),
            labels    = points.mapIndexed { i, pt ->
                if (i % 15 == 0) pt.date.format(formatter) else ""
            }
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
        animationMode       = AnimationMode.Together()
    )
}