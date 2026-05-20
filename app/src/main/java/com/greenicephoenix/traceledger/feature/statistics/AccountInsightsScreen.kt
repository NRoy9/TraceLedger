package com.greenicephoenix.traceledger.feature.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.currency.CurrencyFormatter
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import com.greenicephoenix.traceledger.core.ui.components.MonthSelector
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.core.ui.theme.SuccessGreen
import com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModel.AccountCashflow
import com.greenicephoenix.traceledger.feature.statistics.components.BackHeader
import com.greenicephoenix.traceledger.feature.statistics.components.RunningBalanceChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie

@Composable
fun AccountInsightsScreen(
    viewModel: StatisticsViewModel,
    onBack:    () -> Unit
) {
    val currency      by CurrencyManager.currency.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val slices        by viewModel.accountSlices.collectAsState()
    val cashflows     by viewModel.accountCashflows.collectAsState()
    val runningBal    by viewModel.runningBalance.collectAsState()

    val fallback = Color(0xFF7C4DFF)

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding      = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { BackHeader(title = "Account Insights", onBack = onBack) }

        item {
            MonthSelector(selectedMonth, viewModel::previousMonth, viewModel::nextMonth)
        }

        // Balance distribution donut
        if (slices.isNotEmpty()) {
            item {
                Card(
                    shape    = RoundedCornerShape(20.dp),
                    colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("BALANCE DISTRIBUTION", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            PieChart(
                                modifier   = Modifier.size(200.dp),
                                data       = slices.map { slice ->
                                    Pie(
                                        label = slice.name,
                                        data  = slice.balance.toDouble(),
                                        color = Color(slice.color),
                                        style = Pie.Style.Stroke(width = 36.dp)
                                    )
                                },
                                spaceDegree        = 2f,
                                scaleAnimEnterSpec = androidx.compose.animation.core.tween(400),
                                scaleAnimExitSpec  = androidx.compose.animation.core.tween(400),
                                colorAnimEnterSpec = androidx.compose.animation.core.tween(400),
                                colorAnimExitSpec  = androidx.compose.animation.core.tween(400)
                            )
                        }

                        // Legend
                        slices.forEach { slice ->
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(slice.color))
                                )
                                Text(slice.name, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                Text(
                                    "${(slice.fraction * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(slice.color)
                                )
                                Text(
                                    CurrencyFormatter.format(slice.balance.toPlainString(), currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Running balance timeline
        if (runningBal.isNotEmpty()) {
            item {
                Card(
                    shape    = RoundedCornerShape(20.dp),
                    colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("RUNNING BALANCE (90 DAYS)", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        RunningBalanceChart(points = runningBal, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // Per-account cashflow
        if (cashflows.isNotEmpty()) {
            item {
                Text("ACCOUNT CASHFLOW — THIS MONTH", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
            items(cashflows.size) { i ->
                AccountCashflowCard(cashflow = cashflows[i], currency = currency)
            }
        }

        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
private fun AccountCashflowCard(
    cashflow: AccountCashflow,
    currency: com.greenicephoenix.traceledger.core.currency.Currency
) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                    .background(Color(cashflow.color).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(cashflow.color)))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(cashflow.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("In: ${CurrencyFormatter.format(cashflow.inflow.toPlainString(), currency)}",
                        style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
                    Text("Out: ${CurrencyFormatter.format(cashflow.outflow.toPlainString(), currency)}",
                        style = MaterialTheme.typography.labelSmall, color = NothingRed)
                }
            }
            Text(
                CurrencyFormatter.format(cashflow.net.toPlainString(), currency),
                style = MaterialTheme.typography.titleSmall,
                color = if (cashflow.net >= java.math.BigDecimal.ZERO) SuccessGreen else NothingRed
            )
        }
    }
}