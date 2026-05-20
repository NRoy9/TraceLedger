package com.greenicephoenix.traceledger.feature.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.feature.statistics.components.BackHeader
import com.greenicephoenix.traceledger.feature.statistics.components.SavingsRateTrendChart

@Composable
fun SavingsRateTrendScreen(viewModel: StatisticsViewModel, onBack: () -> Unit) {
    val points by viewModel.savingsRateTrend.collectAsState()
    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding      = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { BackHeader(title = "Savings Rate Trend", onBack = onBack) }
        item { Text("Monthly savings rate — last 12 months", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) }
        item {
            Card(
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                val allZero = points.isEmpty() || points.all { it.rate == 0f }
                if (allZero) {
                    // Empty state — no income recorded yet
                    Box(
                        modifier           = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment   = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier            = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text  = "No income recorded yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text      = "Savings rate = (income − expense) ÷ income. Add income transactions to see your trend.",
                                style     = MaterialTheme.typography.bodySmall,
                                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    SavingsRateTrendChart(
                        points   = points,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
            }
        }
        // Latest rate callout
        val latest = points.lastOrNull()
        if (latest != null) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), Arrangement.spacedBy(4.dp)) {
                        Text("This month", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        Text(
                            "${String.format("%.1f", latest.rate * 100f)}% savings rate",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (latest.rate >= 0) com.greenicephoenix.traceledger.core.ui.theme.SuccessGreen else com.greenicephoenix.traceledger.core.ui.theme.NothingRed
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(48.dp)) }
    }
}