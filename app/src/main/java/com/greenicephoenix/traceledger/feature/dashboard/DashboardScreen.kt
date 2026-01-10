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

@Composable
fun DashboardScreen(
    accounts: List<AccountUiModel>,
    onNavigate: (String) -> Unit,
    onAddAccount: () -> Unit,
    onAccountClick: (AccountUiModel) -> Unit
) {
    //val bankAccounts = accounts.filter { it.type == AccountType.BANK }
    val currency by CurrencyManager.currency.collectAsState()

    val totalBalanceAmount = accounts
        .filter { it.includeInTotal }
        .fold(BigDecimal.ZERO) { acc, account ->
            acc + account.balance
        }

    val netBalanceAmount = 0.0 // placeholder until logic exists

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ================= HEADER =================
        item(span = { GridItemSpan(2) }) {
            Column {
                Text(
                    text = "TRACE LEDGER",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = "STATUS: READY",
                    style = MaterialTheme.typography.labelSmall,
                    color = NothingRed
                )
            }
        }

        // ================= TOTAL BALANCE =================
        item(span = { GridItemSpan(2) }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF121212)
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
                        color = Color.Gray
                    )
                    Text(
                        text = CurrencyFormatter.format(
                            amount = totalBalanceAmount.toPlainString(),
                            currency = currency
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("IN  +0.00", color = Color(0xFF4CAF50))
                        Text("OUT  -0.00", color = NothingRed)
                    }
                }
            }
        }

        item(span = { GridItemSpan(2) }) {
            MonthlyBudgetCard(
                used = 0.0,
                limit = 0.0
            )
        }

        // ================= NET BALANCE =================
        item(span = { GridItemSpan(2) }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF121212)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "NET BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = CurrencyFormatter.format(
                            netBalanceAmount.toBigDecimal().toPlainString(),
                            currency
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }
            }
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
                    color = Color.White
                )
                Text(
                    text = "SEE ALL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
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
private fun MonthlyBudgetCard(
    used: Double = 0.0,
    limit: Double = 0.0
) {
    val currency by CurrencyManager.currency.collectAsState()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF121212)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "MONTHLY BUDGET",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            Text(
                text = "${CurrencyFormatter.format(
                    used.toBigDecimal().toPlainString(),
                    currency
                )} / ${CurrencyFormatter.format(
                    limit.toBigDecimal().toPlainString(),
                    currency
                )}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Text(
                text = "LOAD: 0%",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
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
            containerColor = Color(0xFF141414)
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
            containerColor = Color(0xFF141414)
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
                    color = Color.White,
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
                color = Color.White,
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

    val color = if (isIncluded) Color(0xFF4CAF50) else Color(0xFF757575)
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