package com.greenicephoenix.traceledger.feature.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.currency.CurrencyFormatter
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import com.greenicephoenix.traceledger.core.ui.components.MonthSelector
import com.greenicephoenix.traceledger.core.ui.theme.ErrorRed
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.core.ui.theme.SuccessGreen
import com.greenicephoenix.traceledger.core.ui.theme.WarningAmber
import com.greenicephoenix.traceledger.feature.statistics.components.BackHeader
import com.greenicephoenix.traceledger.feature.statistics.components.BurnRateChart
import com.greenicephoenix.traceledger.feature.statistics.components.NetCashflowChart
import java.time.format.DateTimeFormatter

@Composable
fun ForecastingScreen(
    viewModel: StatisticsViewModel,
    onBack:    () -> Unit
) {
    val currency      by CurrencyManager.currency.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val forecast      by viewModel.forecastData.collectAsState()
    val burnRate      by viewModel.burnRatePoints.collectAsState()
    val netCashflow   by viewModel.netCashflowTrend.collectAsState()

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding      = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { BackHeader(title = "Forecasting", onBack = onBack) }
        item { MonthSelector(selectedMonth, viewModel::previousMonth, viewModel::nextMonth) }

        // On-track status card
        item {
            Card(
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = if (forecast.isOnTrack)
                        SuccessGreen.copy(alpha = 0.10f) else ErrorRed.copy(alpha = 0.10f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            if (forecast.isOnTrack) "On Track" else "Over Pace",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (forecast.isOnTrack) SuccessGreen else ErrorRed
                        )
                        Text(
                            "${forecast.daysRemaining} days remaining this month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("SAFE TO SPEND / DAY", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(
                            CurrencyFormatter.format(forecast.dailySafeToSpend.toString(), currency),
                            style = MaterialTheme.typography.titleLarge,
                            color = if (forecast.isOnTrack) SuccessGreen else WarningAmber
                        )
                    }
                }
            }
        }

        // KPI cards
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ForecastKpiCard(Modifier.weight(1f), "Projected Expense",
                    CurrencyFormatter.format(forecast.projectedMonthEndExpense.toString(), currency), NothingRed)
                ForecastKpiCard(Modifier.weight(1f), "Budget Remaining",
                    CurrencyFormatter.format(forecast.budgetRemaining.toString(), currency),
                    if (forecast.budgetRemaining > 0) SuccessGreen else ErrorRed)
            }
        }

        // Burn rate chart
        if (burnRate.isNotEmpty()) {
            item {
                Card(
                    shape    = RoundedCornerShape(20.dp),
                    colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("BURN RATE — ACTUAL VS BUDGET", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text("Red = actual spend, Green dashed = budget pace",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        BurnRateChart(points = burnRate, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // Net cashflow trend
        if (netCashflow.isNotEmpty()) {
            item {
                Card(
                    shape    = RoundedCornerShape(20.dp),
                    colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("NET CASHFLOW TREND (12 MONTHS)", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        NetCashflowChart(points = netCashflow, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // Spending spikes
        if (forecast.spendingSpikes.isNotEmpty()) {
            item {
                Card(
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.10f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("SPENDING SPIKES DETECTED", style = MaterialTheme.typography.labelSmall,
                            color = WarningAmber)
                        val formatter = DateTimeFormatter.ofPattern("d MMM")
                        Text(
                            forecast.spendingSpikes.joinToString(" · ") { it.format(formatter) },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
private fun ForecastKpiCard(modifier: Modifier, label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(modifier, RoundedCornerShape(16.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            Text(value, style = MaterialTheme.typography.titleSmall, color = color)
        }
    }
}