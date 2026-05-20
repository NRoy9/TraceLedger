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
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.core.ui.theme.SuccessGreen
import com.greenicephoenix.traceledger.core.ui.theme.WarningAmber
import com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModel.RecurringItem
import com.greenicephoenix.traceledger.feature.statistics.components.BackHeader
import java.time.format.DateTimeFormatter

@Composable
fun RecurringAnalyticsScreen(
    viewModel: StatisticsViewModel,
    onBack:    () -> Unit
) {
    val currency  by CurrencyManager.currency.collectAsState()
    val summary   by viewModel.recurringSummary.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding      = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { BackHeader(title = "Recurring Analytics", onBack = onBack) }

        // Summary KPIs
        item {
            Card(
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("MONTHLY COMMITMENT", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(
                        CurrencyFormatter.format(summary.totalMonthlyCommitment.toPlainString(), currency),
                        style = MaterialTheme.typography.displaySmall,
                        color = NothingRed
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RecurringKpiCard(Modifier.weight(1f), "Active",   "${summary.activeCount}",   MaterialTheme.colorScheme.primary)
                        RecurringKpiCard(Modifier.weight(1f), "Expenses", "${summary.expenseCount}", NothingRed)
                        RecurringKpiCard(Modifier.weight(1f), "Income",   "${summary.incomeCount}",   SuccessGreen)
                    }
                }
            }
        }

        // Upcoming this month
        if (summary.upcomingThisMonth.isNotEmpty()) {
            item {
                Text("UPCOMING THIS MONTH", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
            items(summary.upcomingThisMonth.size) { i ->
                RecurringItemCard(item = summary.upcomingThisMonth[i], currency = currency, formatter = formatter)
            }
        } else {
            item {
                Card(
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                        Text("No upcoming recurring transactions this month",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
            }
        }

        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
private fun RecurringKpiCard(modifier: Modifier, label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(modifier, RoundedCornerShape(14.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp), Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            Text(value, style = MaterialTheme.typography.titleMedium, color = color)
        }
    }
}

@Composable
private fun RecurringItemCard(
    item:      RecurringItem,
    currency:  com.greenicephoenix.traceledger.core.currency.Currency,
    formatter: DateTimeFormatter
) {
    val typeColor = when (item.type) {
        "INCOME"   -> SuccessGreen
        "EXPENSE"  -> NothingRed
        else       -> WarningAmber
    }
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
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    item.note ?: item.type.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${item.frequency.lowercase().replaceFirstChar { it.uppercase() }} · ${item.nextDate?.format(formatter) ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Text(
                CurrencyFormatter.format(item.amount.toPlainString(), currency),
                style = MaterialTheme.typography.titleSmall,
                color = typeColor
            )
        }
    }
}