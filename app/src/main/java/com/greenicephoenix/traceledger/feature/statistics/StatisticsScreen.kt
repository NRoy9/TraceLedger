package com.greenicephoenix.traceledger.feature.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.currency.CurrencyFormatter
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import com.greenicephoenix.traceledger.core.navigation.Routes
import com.greenicephoenix.traceledger.core.ui.components.MonthSelector
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    categoryMap: Map<String, CategoryUiModel>,
    onNavigate: (String) -> Unit
) {
    // categoryMap is intentionally UNUSED in Phase 2

    val currency by CurrencyManager.currency.collectAsState()

    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val netAmount by viewModel.netAmount.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        item {
            Text(
                text = "STATISTICS",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }

        item {
            MonthSelector(
                month = selectedMonth,
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth
            )
        }

        item {
            StatRow(
                label = "TOTAL INCOME",
                value = CurrencyFormatter.format(
                    totalIncome.toPlainString(),
                    currency
                ),
                valueColor = Color(0xFF4CAF50)
            )
        }

        item {
            StatRow(
                label = "TOTAL EXPENSE",
                value = CurrencyFormatter.format(
                    totalExpense.toPlainString(),
                    currency
                ),
                valueColor = Color(0xFFE53935)
            )
        }

        item {
            StatRow(
                label = "NET",
                value = CurrencyFormatter.format(
                    netAmount.toPlainString(),
                    currency
                ),
                valueColor =
                    if (netAmount.signum() >= 0)
                        Color(0xFF4CAF50)
                    else
                        Color(0xFFE53935)
            )
        }

        item {
            SectionCard(
                title = "Expense Breakdown",
                subtitle = "See where your money goes",
                onClick = { onNavigate(Routes.STATISTICS_BREAKDOWN) }
            )
        }

        item {
            SectionCard(
                title = "Income Breakdown",
                subtitle = "View income sources",
                onClick = { onNavigate(Routes.STATISTICS_INCOME) }
            )
        }

        item {
            SectionCard(
                title = "Cashflow",
                subtitle = "Income vs Expense over time",
                onClick = { onNavigate(Routes.STATISTICS_CASHFLOW) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color(0xFF121212))
            .padding(20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = valueColor
        )
    }
}
