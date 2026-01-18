package com.greenicephoenix.traceledger.feature.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.currency.CurrencyFormatter
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import com.greenicephoenix.traceledger.feature.statistics.components.BackHeader
import com.greenicephoenix.traceledger.feature.statistics.components.CashflowBarChart
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api


private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashflowScreen(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val currency by CurrencyManager.currency.collectAsState()
    val cashflow by viewModel.cashflowByDay.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    var selectedEntry by remember {
        mutableStateOf<StatisticsViewModel.CashflowEntry?>(null)
    }

    val sheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        item {
            BackHeader(
                title = "Cashflow",
                onBack = onBack
            )
        }

        item {
            CashflowBarChart(
                entries = cashflow,
                selectedDay = selectedEntry?.day,
                onDaySelected = { selectedEntry = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (cashflow.isEmpty()) {
            item {
                Text(
                    text = "No cashflow data for this month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            items(cashflow.size) { index ->
                val entry = cashflow[index]

                val date = LocalDate.of(
                    selectedMonth.year,
                    selectedMonth.month,
                    entry.day
                )

                CashflowRow(
                    date = date.format(dateFormatter),
                    income = CurrencyFormatter.format(
                        entry.income.toPlainString(),
                        currency
                    ),
                    expense = CurrencyFormatter.format(
                        entry.expense.toPlainString(),
                        currency
                    )
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (selectedEntry != null) {

        ModalBottomSheet(
            onDismissRequest = { selectedEntry = null },
            sheetState = sheetState
        ) {
            val entry = selectedEntry!!

            val date = LocalDate.of(
                selectedMonth.year,
                selectedMonth.month,
                entry.day
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = date.format(
                        DateTimeFormatter.ofPattern("dd MMM yyyy")
                    ),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Income: " + CurrencyFormatter.format(
                        entry.income.toPlainString(),
                        currency
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50)
                )

                Text(
                    text = "Expense: " + CurrencyFormatter.format(
                        entry.expense.toPlainString(),
                        currency
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE53935)
                )

                Text(
                    text = "Net: " + CurrencyFormatter.format(
                        entry.income.subtract(entry.expense).toPlainString(),
                        currency
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

}

@Composable
private fun CashflowRow(
    date: String,
    income: String,
    expense: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )

        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Text(
                text = "+ $income",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4CAF50)
            )
            Text(
                text = "− $expense",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE53935)
            )
        }
    }
}