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
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.core.ui.theme.SuccessGreen
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

@Composable
fun InvestmentVsExpenseScreen(
    viewModel: StatisticsViewModel,
    onBack:    () -> Unit
) {
    val currency      by CurrencyManager.currency.collectAsState()
    val comparePoints by viewModel.investmentComparePoints.collectAsState()
    val onSurface     = MaterialTheme.colorScheme.onSurface
    val surfaceColor  = MaterialTheme.colorScheme.surface

    // Aggregate totals across the 6-month window for the summary row
    val totalIncome   = comparePoints.sumOf { it.income }
    val totalExpense  = comparePoints.sumOf { it.expense }
    val totalInvested = comparePoints.sumOf { it.invested }

    // Investment rate = invested / income × 100 — how much of income goes to investments
    val investmentRate: Int? = if (totalIncome > 0)
        ((totalInvested / totalIncome) * 100).toInt() else null

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {

        item { BackHeader(title = "INVEST vs EARN vs SPEND", onBack = onBack) }

        item { Spacer(Modifier.height(8.dp)) }

        // ── 3-metric summary row ──────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryPill(
                    modifier = Modifier.weight(1f),
                    label    = "EARNED",
                    value    = CurrencyFormatter.format(totalIncome.toString(), currency),
                    color    = SuccessGreen
                )
                SummaryPill(
                    modifier = Modifier.weight(1f),
                    label    = "SPENT",
                    value    = CurrencyFormatter.format(totalExpense.toString(), currency),
                    color    = NothingRed
                )
                SummaryPill(
                    modifier = Modifier.weight(1f),
                    label    = "INVESTED",
                    value    = CurrencyFormatter.format(totalInvested.toString(), currency),
                    color    = InvestmentGold
                )
            }
        }

        item { Spacer(Modifier.height(10.dp)) }

        // Investment rate pill
        if (investmentRate != null) {
            item {
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = InvestmentGold.copy(alpha = 0.10f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text  = "INVESTMENT RATE",
                                style = MaterialTheme.typography.labelSmall,
                                color = onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text  = "% of income put to work",
                                style = MaterialTheme.typography.bodySmall,
                                color = onSurface.copy(alpha = 0.35f)
                            )
                        }
                        Text(
                            text       = "$investmentRate%",
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color      = InvestmentGold
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(10.dp)) }
        }

        // ── Grouped bar chart ─────────────────────────────────────────────────
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
                        text  = "6-MONTH COMPARISON",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "Income · Expense · Invested — per month",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurface.copy(alpha = 0.35f)
                    )

                    // Legend row
                    Spacer(Modifier.height(14.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        LegendDot("Income",   SuccessGreen)
                        LegendDot("Expense",  NothingRed)
                        LegendDot("Invested", InvestmentGold)
                    }

                    Spacer(Modifier.height(16.dp))

                    if (comparePoints.all { it.income == 0.0 && it.expense == 0.0 && it.invested == 0.0 }) {
                        Box(
                            modifier         = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = "No data for the last 6 months",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurface.copy(alpha = 0.4f)
                            )
                        }
                    } else {
                        // Build Bars list — one Bars entry per month,
                        // each with 3 grouped bars (income, expense, invested).
                        val bars = comparePoints.map { point ->
                            Bars(
                                label  = point.monthLabel,
                                values = listOf(
                                    Bars.Data(
                                        label = "Income",
                                        value = point.income,
                                        color = SolidColor(SuccessGreen)
                                    ),
                                    Bars.Data(
                                        label = "Expense",
                                        value = point.expense,
                                        color = SolidColor(NothingRed)
                                    ),
                                    Bars.Data(
                                        label = "Invested",
                                        value = point.invested,
                                        color = SolidColor(InvestmentGold)
                                    )
                                )
                            )
                        }

                        ColumnChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            data     = bars,
                            barProperties = BarProperties(
                                spacing  = 1.dp,
                                thickness = 8.dp,
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

        // ── Per-month breakdown table ─────────────────────────────────────────
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text  = "MONTH-BY-MONTH",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.5f)
                    )

                    // Header row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("MONTH",    style = MaterialTheme.typography.labelSmall, color = onSurface.copy(alpha = 0.4f), modifier = Modifier.weight(1f))
                        Text("INCOME",   style = MaterialTheme.typography.labelSmall, color = SuccessGreen.copy(alpha = 0.7f), modifier = Modifier.weight(1.4f))
                        Text("EXPENSE",  style = MaterialTheme.typography.labelSmall, color = NothingRed.copy(alpha = 0.7f), modifier = Modifier.weight(1.4f))
                        Text("INVESTED", style = MaterialTheme.typography.labelSmall, color = InvestmentGold.copy(alpha = 0.7f), modifier = Modifier.weight(1.4f))
                    }

                    HorizontalDivider(color = onSurface.copy(alpha = 0.08f))

                    comparePoints.reversed().forEach { point ->
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text     = point.monthLabel,
                                style    = MaterialTheme.typography.bodySmall,
                                color    = onSurface,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text     = CurrencyFormatter.format(point.income.toString(), currency),
                                style    = MaterialTheme.typography.bodySmall,
                                color    = SuccessGreen,
                                modifier = Modifier.weight(1.4f)
                            )
                            Text(
                                text     = CurrencyFormatter.format(point.expense.toString(), currency),
                                style    = MaterialTheme.typography.bodySmall,
                                color    = NothingRed,
                                modifier = Modifier.weight(1.4f)
                            )
                            Text(
                                text     = CurrencyFormatter.format(point.invested.toString(), currency),
                                style    = MaterialTheme.typography.bodySmall,
                                color    = InvestmentGold,
                                modifier = Modifier.weight(1.4f)
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
private fun SummaryPill(modifier: Modifier, label: String, value: String, color: Color) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text       = value,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = color
            )
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
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
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}