package com.greenicephoenix.traceledger.feature.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.currency.CurrencyFormatter
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import com.greenicephoenix.traceledger.feature.statistics.components.BackHeader
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties

// Investment color used across all INVESTMENT type UI — dark gold, readable on both themes.
private val InvestmentGold = Color(0xFFB8860B)

@Composable
fun InvestmentTrendScreen(
    viewModel:   StatisticsViewModel,
    categoryMap: Map<String, CategoryUiModel>,
    onBack:      () -> Unit
) {
    val currency     by CurrencyManager.currency.collectAsState()
    val trendPoints  by viewModel.investmentMonthlyTrend.collectAsState()
    val invSlices    by viewModel.investmentCategorySlices.collectAsState()
    val invTotal     by viewModel.investmentTotal.collectAsState()

    // Summary stats derived from the 12-month trend
    val maxMonth = trendPoints.maxByOrNull { it.invested }
    val avgMonthly = if (trendPoints.isNotEmpty())
        trendPoints.sumOf { it.invested } / trendPoints.size
    else 0.0

    // Count months that had any investment activity
    val activeMonths = trendPoints.count { it.invested > 0.0 }

    val onSurface    = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {

        item {
            BackHeader(title = "INVESTMENT TREND", onBack = onBack)
        }

        item { Spacer(Modifier.height(8.dp)) }

        // ── Summary stat chips ────────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total invested over the 12-month window
                StatChip(
                    modifier = Modifier.weight(1f),
                    label    = "12-MONTH TOTAL",
                    value    = CurrencyFormatter.format(
                        trendPoints.sumOf { it.invested }.toString(), currency
                    ),
                    color    = InvestmentGold
                )
                // Average per active month (ignore zero months for a truer average)
                StatChip(
                    modifier = Modifier.weight(1f),
                    label    = "MONTHLY AVG",
                    value    = CurrencyFormatter.format(avgMonthly.toString(), currency),
                    color    = InvestmentGold
                )
            }
        }

        item { Spacer(Modifier.height(10.dp)) }

        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(
                    modifier = Modifier.weight(1f),
                    label    = "ACTIVE MONTHS",
                    value    = "$activeMonths / 12",
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                StatChip(
                    modifier = Modifier.weight(1f),
                    label    = "BEST MONTH",
                    value    = maxMonth?.let {
                        "${it.monthLabel} · ${CurrencyFormatter.format(it.invested.toString(), currency)}"
                    } ?: "--",
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        item { Spacer(Modifier.height(20.dp)) }

        // ── Line chart ───────────────────────────────────────────────────────
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(surfaceColor),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text  = "MONTHLY INVESTMENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "Last 12 months",
                        style      = MaterialTheme.typography.bodySmall,
                        color      = onSurface.copy(alpha = 0.35f)
                    )
                    Spacer(Modifier.height(20.dp))

                    if (trendPoints.all { it.invested == 0.0 }) {
                        // Empty state — no investment data yet
                        Box(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment  = Alignment.Center
                        ) {
                            Text(
                                text  = "No investments recorded yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurface.copy(alpha = 0.4f)
                            )
                        }
                    } else {
                        LineChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            data = listOf(
                                Line(
                                    label  = "Invested",
                                    values = trendPoints.map { it.invested },
                                    color  = SolidColor(InvestmentGold),
                                    firstGradientFillColor = InvestmentGold.copy(alpha = 0.3f),
                                    secondGradientFillColor = Color.Transparent,
                                    strokeAnimationSpec = androidx.compose.animation.core.tween(800),
                                    drawStyle = ir.ehsannarmani.compose_charts.models.DrawStyle.Stroke(2.dp),
                                    dotProperties = DotProperties(
                                        enabled   = true,
                                        radius    = 4.dp,
                                        color     = SolidColor(InvestmentGold),
                                        strokeWidth = 2.dp
                                    )
                                )
                            ),
                            labelHelperProperties = LabelHelperProperties(enabled = false),
                            indicatorProperties   = HorizontalIndicatorProperties(
                                enabled   = true,
                                textStyle = MaterialTheme.typography.labelSmall.copy(
                                    color = onSurface.copy(alpha = 0.5f)
                                )
                            ),
                            gridProperties = GridProperties(
                                xAxisProperties = GridProperties.AxisProperties(
                                    lineCount = 4,
                                    color     = SolidColor(
                                        onSurface.copy(alpha = 0.07f)
                                    )
                                ),
                                yAxisProperties = GridProperties.AxisProperties(enabled = false)
                            ),
                            popupProperties = PopupProperties(
                                textStyle = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        )

                        // X-axis month labels
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Show first, middle and last labels to avoid crowding
                            val labels = trendPoints.map { it.monthLabel }
                            val indices = listOf(0, labels.size / 2, labels.size - 1).distinct()
                            labels.forEachIndexed { idx, label ->
                                if (idx in indices) {
                                    Text(
                                        text  = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = onSurface.copy(alpha = 0.45f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }

        // ── Per-category breakdown for the 12-month window ───────────────────
        if (invSlices.isNotEmpty()) {
            item {
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(surfaceColor),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier            = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text  = "BREAKDOWN — SELECTED MONTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurface.copy(alpha = 0.5f)
                        )
                        invSlices.forEach { slice ->
                            val category = categoryMap[slice.categoryId]
                            val catColor = category?.color?.let { Color(it) } ?: InvestmentGold
                            val catName  = category?.name ?: slice.categoryId

                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color dot
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(catColor, shape = RoundedCornerShape(4.dp))
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text     = catName,
                                    style    = MaterialTheme.typography.bodyMedium,
                                    color    = onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text  = "%.1f%%".format(slice.percentage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurface.copy(alpha = 0.55f)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text       = CurrencyFormatter.format(
                                        slice.amount.toPlainString(), currency
                                    ),
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = InvestmentGold
                                )
                            }

                            // Progress bar showing this category's share of total invested
                            LinearProgressIndicator(
                                progress  = { (slice.percentage / 100f).coerceIn(0f, 1f) },
                                modifier  = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp),
                                color     = catColor,
                                trackColor = onSurface.copy(alpha = 0.08f)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

// ── Shared stat chip ─────────────────────────────────────────────────────────

@Composable
private fun StatChip(
    modifier: Modifier,
    label:    String,
    value:    String,
    color:    Color
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = color
            )
        }
    }
}