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
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties

private val InvestmentGold = Color(0xFFB8860B)

// Fallback palette for investment categories without explicit colors.
// Amber/gold tones to stay on-brand for investments.
private val PortfolioPalette = listOf(
    Color(0xFFB8860B), // dark gold
    Color(0xFFD4A017), // amber
    Color(0xFFE8C44A), // light gold
    Color(0xFF8B6914), // deep brown-gold
    Color(0xFFF0B429), // yellow-amber
    Color(0xFF7A5C00), // dark bronze
    Color(0xFFCC9900), // rich gold
    Color(0xFFFFCC44), // light amber
    Color(0xFF9B7A1A)  // muted gold
)

@Composable
fun PortfolioAllocationScreen(
    viewModel:   StatisticsViewModel,
    categoryMap: Map<String, CategoryUiModel>,
    onBack:      () -> Unit
) {
    val currency       by CurrencyManager.currency.collectAsState()
    val allocation     by viewModel.portfolioAllocationPoints.collectAsState()
    val invSlices      by viewModel.investmentCategorySlices.collectAsState()
    val invTotal       by viewModel.investmentTotal.collectAsState()
    val onSurface      = MaterialTheme.colorScheme.onSurface
    val surfaceColor   = MaterialTheme.colorScheme.surface

    // Collect all unique category IDs that appear anywhere in the 12-month window.
    // This determines how many bars we stack per month and what colors to use.
    val allCategoryIds: List<String> = remember(allocation) {
        allocation.flatMap { it.byCategory.keys }.distinct()
    }

    // Assign each category a stable color: prefer category's own color, else palette fallback.
    val categoryColors: Map<String, Color> = remember(allCategoryIds, categoryMap) {
        allCategoryIds.mapIndexed { idx, catId ->
            val explicit = categoryMap[catId]?.color?.let { Color(it) }
            catId to (explicit ?: PortfolioPalette[idx % PortfolioPalette.size])
        }.toMap()
    }

    // Check if there is any data at all
    val hasData = allocation.any { it.byCategory.isNotEmpty() }

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {

        item { BackHeader(title = "PORTFOLIO ALLOCATION", onBack = onBack) }

        item { Spacer(Modifier.height(8.dp)) }

        // ── Summary — total invested in selected month + category count ───────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryChip(
                    modifier = Modifier.weight(1f),
                    label    = "INVESTED (THIS MONTH)",
                    value    = CurrencyFormatter.format(invTotal.toPlainString(), currency),
                    color    = InvestmentGold
                )
                SummaryChip(
                    modifier = Modifier.weight(1f),
                    label    = "CATEGORIES",
                    value    = "${allCategoryIds.size}",
                    color    = onSurface.copy(alpha = 0.7f)
                )
            }
        }

        item { Spacer(Modifier.height(20.dp)) }

        // ── Stacked bar chart — one bar per month ─────────────────────────────
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
                        text  = "PORTFOLIO MIX — 12 MONTHS",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "How your investment allocation evolved",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurface.copy(alpha = 0.35f)
                    )

                    if (!hasData) {
                        Box(
                            modifier         = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = "No investment data yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurface.copy(alpha = 0.4f)
                            )
                        }
                    } else {
                        // Legend
                        Spacer(Modifier.height(14.dp))
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(allCategoryIds.size) { idx ->
                                val catId   = allCategoryIds[idx]
                                val catName = categoryMap[catId]?.name ?: catId
                                val color   = categoryColors[catId] ?: InvestmentGold
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(color, RoundedCornerShape(4.dp))
                                    )
                                    Text(
                                        text  = catName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Build stacked bars — each month = one Bars, each category = one Bars.Data
                        val bars = allocation.map { point ->
                            Bars(
                                label  = point.monthLabel,
                                values = allCategoryIds.map { catId ->
                                    Bars.Data(
                                        label = categoryMap[catId]?.name ?: catId,
                                        value = point.byCategory[catId] ?: 0.0,
                                        color = SolidColor(
                                            categoryColors[catId] ?: InvestmentGold
                                        )
                                    )
                                }
                            )
                        }

                        ColumnChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            data     = bars,
                            // stack = true makes this a stacked bar chart
                            // (each bar segment sits on top of the previous one)
                            barProperties = BarProperties(
                                thickness    = 18.dp,
                                spacing      = 3.dp,
                                cornerRadius = Bars.Data.Radius.Rectangle(topRight = 4.dp, topLeft = 4.dp)
                            ),
                            labelHelperProperties = LabelHelperProperties(enabled = false),
                            labelProperties = LabelProperties(
                                enabled   = true,
                                textStyle = MaterialTheme.typography.labelSmall.copy(
                                    color = onSurface.copy(alpha = 0.55f)
                                )
                            ),
                            indicatorProperties = HorizontalIndicatorProperties(
                                enabled   = true,
                                textStyle = MaterialTheme.typography.labelSmall.copy(
                                    color = onSurface.copy(alpha = 0.45f)
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
                    }
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }

        // ── Current month category breakdown ──────────────────────────────────
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
                            text  = "CURRENT ALLOCATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurface.copy(alpha = 0.5f)
                        )
                        invSlices.forEach { slice ->
                            val catColor = categoryColors[slice.categoryId]
                                ?: (categoryMap[slice.categoryId]?.color?.let { Color(it) } ?: InvestmentGold)
                            val catName  = categoryMap[slice.categoryId]?.name ?: slice.categoryId

                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(catColor, RoundedCornerShape(4.dp))
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
                                    color      = catColor
                                )
                            }

                            LinearProgressIndicator(
                                progress   = { (slice.percentage / 100f).coerceIn(0f, 1f) },
                                modifier   = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp),
                                color      = catColor,
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

@Composable
private fun SummaryChip(modifier: Modifier, label: String, value: String, color: Color) {
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