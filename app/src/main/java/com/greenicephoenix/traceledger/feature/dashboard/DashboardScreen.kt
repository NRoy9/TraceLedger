package com.greenicephoenix.traceledger.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.greenicephoenix.traceledger.core.navigation.Routes
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.domain.model.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.getValue
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import com.greenicephoenix.traceledger.core.currency.CurrencyFormatter
import java.math.BigDecimal
import kotlin.math.min
import com.greenicephoenix.traceledger.feature.budgets.ui.BudgetColors
import com.greenicephoenix.traceledger.feature.dashboard.components.BudgetWarningBanner

@Composable
fun DashboardScreen(
    accounts: List<AccountUiModel>,
    statisticsViewModel: com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModel,
    budgetsViewModel: com.greenicephoenix.traceledger.feature.budgets.BudgetsViewModel,
    onNavigate: (String) -> Unit,
    onAddAccount: () -> Unit,
    onAccountClick: (AccountUiModel) -> Unit
) {
    //val bankAccounts = accounts.filter { it.type == AccountType.BANK }
    val currency by CurrencyManager.currency.collectAsState()

    val monthlyIncome by statisticsViewModel.totalIncome.collectAsState()
    val monthlyExpense by statisticsViewModel.totalExpense.collectAsState()
    val monthlyNet by statisticsViewModel.netAmount.collectAsState()

    val budgetStatuses by budgetsViewModel.budgetStatuses.collectAsState()
    val totalLimit = budgetStatuses.fold(BigDecimal.ZERO) { acc, b ->
        acc + b.limit
    }

    val totalUsed = budgetStatuses.fold(BigDecimal.ZERO) { acc, b ->
        acc + b.used
    }

    val budgetProgress =
        if (totalLimit > BigDecimal.ZERO)
            totalUsed.divide(totalLimit, 4, java.math.RoundingMode.HALF_UP).toFloat()
        else 0f

    val budgetState =
        when {
            budgetProgress >= 1f -> com.greenicephoenix.traceledger.feature.budgets.domain.BudgetState.EXCEEDED
            budgetProgress >= 0.7f -> com.greenicephoenix.traceledger.feature.budgets.domain.BudgetState.WARNING
            else -> com.greenicephoenix.traceledger.feature.budgets.domain.BudgetState.SAFE
        }

    val hasExceededBudgets by budgetsViewModel.hasExceededBudgets.collectAsState()
    val exceededBudgetsCount by budgetsViewModel.exceededBudgetsCount.collectAsState()

    val totalBalanceAmount = accounts
        .filter { it.includeInTotal }
        .fold(BigDecimal.ZERO) { acc, account ->
            acc + account.balance
        }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ================= HEADER =================
        item(span = { GridItemSpan(2) }) {
            Column {
                Text(
                    text = "OVERVIEW",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "This month",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

            }
        }

        if (hasExceededBudgets) {
            item(span = { GridItemSpan(2) }) {
                BudgetWarningBanner(
                    exceededCount = exceededBudgetsCount,
                    onClick = {
                        onNavigate(Routes.BUDGETS)
                    }
                )
            }
        }


        // ================= TOTAL BALANCE =================
        item(span = { GridItemSpan(2) }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TOTAL BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = CurrencyFormatter.format(
                            amount = totalBalanceAmount.toPlainString(),
                            currency = currency
                        ),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Income  ${CurrencyFormatter.format(monthlyIncome.toPlainString(), currency)}",
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "Expense  ${CurrencyFormatter.format(monthlyExpense.toPlainString(), currency)}",
                            color = NothingRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // ================= NET BALANCE =================
        item(span = { GridItemSpan(2) }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "NET BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = CurrencyFormatter.format(
                            monthlyNet.toPlainString(),
                            currency
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (monthlyNet >= BigDecimal.ZERO)
                            Color(0xFF4CAF50)
                        else
                            NothingRed
                    )
                }
            }
        }

        // ================= MONTHLY BUDGET =================
        item(span = { GridItemSpan(2) }) {
            if (budgetStatuses.isNotEmpty()) {
                MonthlyBudgetCard(
                    used = totalUsed.toDouble(),
                    limit = totalLimit.toDouble(),
                    state = budgetState,
                    onClick = {
                        onNavigate(Routes.BUDGETS)
                    }
                )
            } else {
                Text(
                    text = "Set monthly budgets to control spending",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // ✅ VISUAL SEPARATION (SECTION RHYTHM)
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ================= MY ACCOUNTS =================
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MY ACCOUNTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "SEE ALL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.clickable {
                        onNavigate(Routes.ACCOUNTS)
                    }
                )
            }
        }

// ---- prepare accounts for dashboard ----
        val dashboardAccounts = accounts
            .sortedByDescending { it.balance }
            .take(5)

// ---- grid ----
        // ADD ACCOUNT CARD
        item {
            AddAccountCard(onClick = onAddAccount)
        }

// ACCOUNT CARDS
        items(dashboardAccounts) { account ->
            DashboardAccountCard(
                account = account,
                onClick = { onAccountClick(account) }
            )
        }

        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun AddAccountCard(onClick: () -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Account",
                tint = NothingRed,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun DashboardAccountCard(
    account: AccountUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            /* ---------- TOP ROW ---------- */
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = account.type.icon(),
                    contentDescription = null,
                    tint = Color(account.color),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = account.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                IncludedBadge(isIncluded = account.includeInTotal)
            }

            val currency by CurrencyManager.currency.collectAsState()

            /* ---------- AMOUNT ---------- */
            Text(
                text = CurrencyFormatter.format(
                    account.balance.toPlainString(),
                    currency
                ),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )

            /* ---------- ACCENT LINE ---------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(2.dp)
                    .background(
                        color = Color(account.color),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
private fun IncludedBadge(isIncluded: Boolean) {

    val color = if (isIncluded) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val icon = if (isIncluded) Icons.Default.Check else Icons.Default.VisibilityOff

    Box(
        modifier = Modifier
            .size(18.dp)
            .background(
                color = color.copy(alpha = 0.15f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
fun MonthlyBudgetCard(
    used: Double = 0.0,
    limit: Double = 0.0,
    state: com.greenicephoenix.traceledger.feature.budgets.domain.BudgetState,
    onClick: () -> Unit
) {
    val currency by CurrencyManager.currency.collectAsState()

    val progress =
        if (limit > 0.0) (used / limit).toFloat() else 0f

    val clampedProgress = min(progress, 1f)

    val accentColor = BudgetColors.accent(state)


    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = "MONTHLY BUDGET",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Text(
                text =
                    "${CurrencyFormatter.format(
                        used.toBigDecimal().toPlainString(),
                        currency
                    )} / ${CurrencyFormatter.format(
                        limit.toBigDecimal().toPlainString(),
                        currency
                    )}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (limit > 0.0)
                    "LOAD: ${(clampedProgress * 100).toInt()}%"
                else
                    "LOAD: 0%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            /* ---------- ACCENT PROGRESS LINE ---------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(2.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(clampedProgress)
                        .height(4.dp)
                        .background(
                            color = accentColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}