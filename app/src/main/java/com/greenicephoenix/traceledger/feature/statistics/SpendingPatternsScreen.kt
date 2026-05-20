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
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.currency.CurrencyFormatter
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import com.greenicephoenix.traceledger.core.ui.components.MonthSelector
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.core.ui.theme.SuccessGreen
import com.greenicephoenix.traceledger.core.ui.theme.WarningAmber
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import com.greenicephoenix.traceledger.feature.statistics.components.BackHeader
import com.greenicephoenix.traceledger.feature.statistics.components.DailyExpenseTrendChart

@Composable
fun SpendingPatternsScreen(
    viewModel:   StatisticsViewModel,
    categoryMap: Map<String, CategoryUiModel>,
    onBack:      () -> Unit
) {
    val currency      by CurrencyManager.currency.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val dailyTrend    by viewModel.dailyExpenseTrend.collectAsState()
    val patterns      by viewModel.spendingPatterns.collectAsState()

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding      = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { BackHeader(title = "Spending Patterns", onBack = onBack) }
        item { MonthSelector(selectedMonth, viewModel::previousMonth, viewModel::nextMonth) }

        // Daily expense trend
        item {
            Card(
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("DAILY EXPENSE TREND", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    DailyExpenseTrendChart(points = dailyTrend, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        // Weekend vs Weekday
        item {
            Card(
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("WEEKEND VS WEEKDAY", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PatternStatCard(
                            modifier = Modifier.weight(1f),
                            label    = "Weekdays",
                            value    = CurrencyFormatter.format(patterns.weekdayTotal.toString(), currency),
                            color    = MaterialTheme.colorScheme.primary
                        )
                        PatternStatCard(
                            modifier = Modifier.weight(1f),
                            label    = "Weekends",
                            value    = CurrencyFormatter.format(patterns.weekendTotal.toString(), currency),
                            color    = WarningAmber
                        )
                    }
                }
            }
        }

        // Month period spending
        item {
            Card(
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("SPENDING BY MONTH PERIOD (DAILY AVG)", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PatternStatCard(Modifier.weight(1f), "Days 1–10",
                            CurrencyFormatter.format(patterns.earlyMonthAvg.toString(), currency), SuccessGreen)
                        PatternStatCard(Modifier.weight(1f), "Days 11–20",
                            CurrencyFormatter.format(patterns.midMonthAvg.toString(), currency), WarningAmber)
                        PatternStatCard(Modifier.weight(1f), "Days 21+",
                            CurrencyFormatter.format(patterns.lateMonthAvg.toString(), currency), NothingRed)
                    }
                }
            }
        }

        // Transaction stats
        item {
            Card(
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("TRANSACTION STATS", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PatternStatCard(Modifier.weight(1f), "Count",
                            "${patterns.transactionCount}", MaterialTheme.colorScheme.primary)
                        PatternStatCard(Modifier.weight(1f), "Avg Value",
                            CurrencyFormatter.format(patterns.avgTransactionValue.toString(), currency),
                            MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Fastest growing categories
        if (patterns.fastestGrowingCategories.isNotEmpty()) {
            item {
                Card(
                    shape    = RoundedCornerShape(20.dp),
                    colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("FASTEST GROWING CATEGORIES", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        patterns.fastestGrowingCategories.forEach { (catId, growth) ->
                            val cat = categoryMap[catId]
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(cat?.name ?: catId.take(12),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    "+${(growth * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NothingRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Most frequent categories
        if (patterns.mostFrequentCategories.isNotEmpty()) {
            item {
                Card(
                    shape    = RoundedCornerShape(20.dp),
                    colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("MOST FREQUENT CATEGORIES", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        patterns.mostFrequentCategories.forEach { (catId, count) ->
                            val cat = categoryMap[catId]
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(cat?.name ?: catId.take(12),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface)
                                Text("$count txns", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
private fun PatternStatCard(modifier: Modifier, label: String, value: String, color: Color) {
    Card(modifier, RoundedCornerShape(14.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp), Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            Text(value, style = MaterialTheme.typography.titleSmall, color = color)
        }
    }
}