package com.greenicephoenix.traceledger.feature.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.currency.CurrencyFormatter
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import com.greenicephoenix.traceledger.domain.model.AccountUiModel
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import com.greenicephoenix.traceledger.domain.model.TransactionType
import com.greenicephoenix.traceledger.domain.model.TransactionUiModel
import com.greenicephoenix.traceledger.feature.transactions.components.MonthHeader
import com.greenicephoenix.traceledger.feature.transactions.components.TransactionRow
import com.greenicephoenix.traceledger.feature.categories.CategoryIcons
import com.greenicephoenix.traceledger.feature.transactions.components.TransactionDetailSheet

@Composable
fun HistoryScreen(
    viewModel: TransactionsViewModel,
    categories: List<CategoryUiModel>,
    accounts: List<AccountUiModel>,
    onBack: () -> Unit,
    onEditTransaction: (String) -> Unit   // ✅ ADD THIS
) {

    var selectedTransaction by remember {
        mutableStateOf<TransactionUiModel?>(null)
    }
    val currency by CurrencyManager.currency.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()
    val transactions by viewModel.visibleTransactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val totalIn by viewModel.totalIn.collectAsState()
    val totalOut by viewModel.totalOut.collectAsState()

    val typeFilter by viewModel.typeFilter.collectAsState()

    LaunchedEffect(accounts) { viewModel.setAccounts(accounts) }
    LaunchedEffect(categories) { viewModel.setCategories(categories) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {

        Text(
            text = "TRANSACTIONS",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier
                .clickable { onBack() }
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::updateSearch,
            placeholder = { Text("Search history…") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
            )
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                null to "ALL",
                TransactionType.EXPENSE to "EXPENSE",
                TransactionType.INCOME to "INCOME",
                TransactionType.TRANSFER to "TRANSFER"
            ).forEach { (type, label) ->

                val selected = typeFilter == type

                FilterChip(
                    selected = selected,
                    onClick = { viewModel.updateTypeFilter(type) },
                    label = { Text(label) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        com.greenicephoenix.traceledger.core.ui.components.MonthSelector(
            month = month,
            onPrevious = { viewModel.goToPreviousMonth() },
            onNext = { viewModel.goToNextMonth() }
        )
        /*
        MonthHeader(
            month = month,
            totalIn = CurrencyFormatter.format(totalIn.toPlainString(), currency),
            totalOut = CurrencyFormatter.format(totalOut.toPlainString(), currency),
            onPrevious = { viewModel.goToPreviousMonth() },
            onNext = { viewModel.goToNextMonth() }
        )
        */

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(transactions.size) { index ->
                val tx = transactions[index]

                val category = categories.firstOrNull { it.id == tx.categoryId }
                val account = when (tx.type) {
                    TransactionType.EXPENSE,
                    TransactionType.TRANSFER ->
                        accounts.firstOrNull { it.id == tx.fromAccountId }

                    TransactionType.INCOME ->
                        accounts.firstOrNull { it.id == tx.toAccountId }
                }

                // SAFE FALLBACKS (NO CRASH)
                val displayTitle =
                    if (tx.type == TransactionType.TRANSFER) {
                        val toAccount =
                            accounts.firstOrNull { it.id == tx.toAccountId }?.name ?: "Account"
                        "Transfer to $toAccount"
                    } else {
                        category?.name ?: "Category"
                    }

                val categoryIcon =
                    if (tx.type == TransactionType.TRANSFER) {
                        androidx.compose.material.icons.Icons.Default.SyncAlt
                    } else {
                        CategoryIcons.all[category?.icon] ?: CategoryIcons.all["default"]!!
                    }

                val isTransfer = tx.type == TransactionType.TRANSFER

                val iconAndAccentColor =
                    if (isTransfer) {
                        Color.Gray
                    } else {
                        Color(category?.color ?: 0xFF9E9E9E)
                    }


                val accountName = account?.name ?: "Account"

                TransactionRow(
                    transaction = tx,
                    categoryName = displayTitle,
                    categoryIcon = categoryIcon,
                    categoryColor = iconAndAccentColor,
                    accountName = accountName,
                    amountText = CurrencyFormatter.format(
                        tx.amount.toPlainString(),
                        currency
                    ),
                    onClick = {
                        onEditTransaction(tx.id)
                    }
                )
            }
        }

        selectedTransaction?.let { tx ->

            val categoryName =
                if (tx.type == TransactionType.TRANSFER) {
                    val toAccount =
                        accounts.firstOrNull { it.id == tx.toAccountId }?.name ?: "Account"
                    "Transfer to $toAccount"
                } else {
                    categories.firstOrNull { it.id == tx.categoryId }?.name ?: "Category"
                }

            val accountName =
                when (tx.type) {
                    TransactionType.EXPENSE ->
                        accounts.firstOrNull { it.id == tx.fromAccountId }?.name
                    TransactionType.INCOME ->
                        accounts.firstOrNull { it.id == tx.toAccountId }?.name
                    TransactionType.TRANSFER ->
                        accounts.firstOrNull { it.id == tx.fromAccountId }?.name
                } ?: "Account"

            TransactionDetailSheet(
                transaction = tx,
                categoryName = categoryName,
                accountName = accountName,
                onDismiss = { selectedTransaction = null }
            )
        }
    }
}