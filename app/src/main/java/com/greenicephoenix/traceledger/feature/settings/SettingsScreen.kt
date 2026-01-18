package com.greenicephoenix.traceledger.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.navigation.Routes
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.greenicephoenix.traceledger.core.currency.Currency
import com.greenicephoenix.traceledger.core.currency.CurrencyManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBudgetsClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val currentCurrency by CurrencyManager.currency.collectAsState()
    var showCurrencySheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ───────── Currency ─────────
        SettingsItem(
            title = "Currency",
            subtitle = "${currentCurrency.code} (${currentCurrency.symbol})",
            onClick = { showCurrencySheet = true }
        )

        // ───────── Categories (existing) ─────────
        SettingsItem(
            title = "Categories",
            subtitle = "Manage income and expense categories",
            onClick = { onNavigate(Routes.CATEGORIES) }
        )

        // ───────── Budgets ─────────
        SettingsItem(
            title = "Budgets",
            subtitle = "Monthly spending limits",
            onClick = onBudgetsClick
        )

    }

    if (showCurrencySheet) {
        CurrencyPickerBottomSheet(
            selected = currentCurrency,
            onSelect = {
                CurrencyManager.setCurrency(it)
                showCurrencySheet = false
            },
            onDismiss = { showCurrencySheet = false }
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141414)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = title, color = Color.White)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerBottomSheet(
    selected: Currency,
    onSelect: (Currency) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Currency.entries.forEach { currency ->
                val isSelected = currency == selected

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else Color.Transparent,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { onSelect(currency) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currency.code} (${currency.symbol})",
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else Color.White
                    )
                }
            }
        }
    }
}

