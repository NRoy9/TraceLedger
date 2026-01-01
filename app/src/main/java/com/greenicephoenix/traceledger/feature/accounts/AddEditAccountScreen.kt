package com.greenicephoenix.traceledger.feature.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.ui.theme.AccountColors
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.domain.model.AccountType
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.HorizontalDivider
import com.greenicephoenix.traceledger.domain.model.AccountUiModel


/**
 * Add / Edit Account Screen
 *
 * - Cancel (✕) and Save (✓) in TopAppBar
 * - Fully scrollable
 * - Credit card extra fields hidden behind expandable "Optional details"
 * - Credit card fields are informational only
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountScreen(
    onCancel: () -> Unit,
    onSave: (AccountUiModel) -> Unit
) {
    // ---------------- STATE ----------------

    var accountType by remember { mutableStateOf(AccountType.BANK) }
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var includeInTotal by remember { mutableStateOf(true) }
    var selectedColor by remember { mutableStateOf(AccountColors.first()) }

    // Credit card optional state
    var showCreditCardDetails by remember { mutableStateOf(false) }
    var creditLimit by remember { mutableStateOf("") }
    var billingDay by remember { mutableStateOf("") }
    var dueDay by remember { mutableStateOf("") }

    // ---------------- SCAFFOLD ----------------

    Scaffold { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // =========================
            // COMPACT CUSTOM HEADER
            // =========================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color.Black)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Add Account",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                IconButton(
                    onClick = {

                        // 1) Parse numeric balance safely
                        val numericBalance = balance.toDoubleOrNull() ?: 0.0

                        // 2) Format balance for display (₹)
                        val formattedBalance = "₹%,.2f".format(numericBalance)

                        // 3) Color is ALWAYS valid — AccountColors.first() is non-zero
                        val resolvedColor: Long = selectedColor.value.toLong()

                        // 4) Create account (MATCHES YOUR MODEL EXACTLY)
                        val account = AccountUiModel(
                            id = System.currentTimeMillis().toString(),
                            name = name.trim(),
                            balance = formattedBalance,
                            type = accountType,
                            includeInTotal = includeInTotal,
                            color = resolvedColor
                        )

                        onSave(account)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = NothingRed
                    )
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color.White.copy(alpha = 0.15f)
            )

            // =========================
            // FORM CONTENT
            // =========================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ---------------- ACCOUNT TYPE ----------------

                item {
                    Text("Account Type", style = MaterialTheme.typography.labelMedium)
                }

                item {
                    AccountTypeSelector(
                        selected = accountType,
                        onSelected = { accountType = it }
                    )
                }

                // ---------------- BASIC FIELDS ----------------

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Account Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = balance,
                        onValueChange = { balance = it },
                        label = { Text("Opening Balance") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ---------------- INCLUDE IN TOTAL ----------------

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Include in total balance")
                        Switch(
                            checked = includeInTotal,
                            onCheckedChange = { includeInTotal = it }
                        )
                    }
                }

                // ---------------- DETAILS ----------------

                item {
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Details (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                // ---------------- COLOR PICKER ----------------

                item {
                    Text("Account Color", style = MaterialTheme.typography.labelMedium)
                }

                item {
                    FlowRow(
                        maxItemsInEachRow = 6,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AccountColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (color == selectedColor) 3.dp else 1.dp,
                                        color = if (color == selectedColor)
                                            NothingRed
                                        else
                                            Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                }

                // ---------------- CREDIT CARD OPTIONAL SECTION ----------------

                if (accountType == AccountType.CREDIT_CARD) {

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showCreditCardDetails = !showCreditCardDetails
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Optional details",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = "Informational only",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector =
                                    if (showCreditCardDetails)
                                        Icons.Default.ExpandLess
                                    else
                                        Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    }

                    if (showCreditCardDetails) {

                        item {
                            OutlinedTextField(
                                value = creditLimit,
                                onValueChange = { creditLimit = it },
                                label = { Text("Total credit limit (optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = billingDay,
                                    onValueChange = { billingDay = it },
                                    label = { Text("Billing day (optional)") },
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = dueDay,
                                    onValueChange = { dueDay = it },
                                    label = { Text("Due day (optional)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Bottom spacer to avoid nav overlap
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* ------------------------ ACCOUNT TYPE SELECTOR ---------------------------- */
/* -------------------------------------------------------------------------- */

@Composable
private fun AccountTypeSelector(
    selected: AccountType,
    onSelected: (AccountType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AccountType.entries.forEach { type ->
            val isSelected = type == selected

            Text(
                text = type.name.replace("_", " "),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) NothingRed.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .clickable { onSelected(type) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                color = if (isSelected) NothingRed
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}