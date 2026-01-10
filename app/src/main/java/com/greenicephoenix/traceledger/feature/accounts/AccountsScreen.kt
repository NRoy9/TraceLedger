package com.greenicephoenix.traceledger.feature.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.domain.model.AccountUiModel
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import com.greenicephoenix.traceledger.core.currency.CurrencyFormatter
import androidx.compose.runtime.collectAsState

@Composable
fun AccountsScreen(
    accounts: List<AccountUiModel>,
    onBack: () -> Unit,
    onAddAccount: () -> Unit,
    onAccountClick: (AccountUiModel) -> Unit,
    onAccountLongPress: (AccountUiModel) -> Boolean
) {
    var accountPendingDeletion by remember {
        mutableStateOf<AccountUiModel?>(null)
    }

    var deleteBlockedReason by remember { mutableStateOf<String?>(null) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // ================= HEADER =================
        AccountsHeader(
            vaultCount = accounts.size,
            onBack = onBack,
            onAddAccount = onAddAccount
        )

        // ================= LIST =================
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(accounts) { account ->
                AccountRowCard(
                    account = account,
                    onClick = { onAccountClick(account) },
                    onLongPress = { accountPendingDeletion = account }
                )
            }
        }

        val accountToDelete = accountPendingDeletion

        if (accountToDelete != null) {
            AlertDialog(
                onDismissRequest = { accountPendingDeletion = null },

                title = {
                    Text("Delete account?")
                },

                text = {
                    Text(
                        "This will permanently delete “${accountToDelete.name}”. " +
                                "This action cannot be undone."
                    )
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            val deleted = onAccountLongPress(accountToDelete)

                            if (!deleted) {
                                deleteBlockedReason =
                                    "This account cannot be deleted because it has transactions linked to it."
                            }

                            accountPendingDeletion = null
                        }
                    )
                    {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = { accountPendingDeletion = null }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

    }

    if (deleteBlockedReason != null) {
        AlertDialog(
            onDismissRequest = { deleteBlockedReason = null },

            title = {
                Text("Cannot delete account")
            },

            text = {
                Text(deleteBlockedReason!!)
            },

            confirmButton = {
                TextButton(
                    onClick = { deleteBlockedReason = null }
                ) {
                    Text("OK")
                }
            }
        )
    }

}

@Composable
private fun AccountsHeader(
    vaultCount: Int,
    onBack: () -> Unit,
    onAddAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "ACCOUNTS",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "COUNT : $vaultCount",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            IconButton(onClick = onAddAccount) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Account",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun AccountRowCard(
    account: AccountUiModel,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141414)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(account.color),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + type
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = account.name.uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${account.type.name} · STATUS: ACTIVE",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Balance
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val currency by CurrencyManager.currency.collectAsState()

                Text(
                    text = CurrencyFormatter.format(
                        account.balance.toPlainString(),
                        currency
                    ),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "AVAILABLE",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}