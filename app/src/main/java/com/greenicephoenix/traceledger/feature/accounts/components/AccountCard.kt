package com.greenicephoenix.traceledger.feature.accounts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.domain.model.AccountUiModel
import com.greenicephoenix.traceledger.feature.accounts.accountTypeIcon

@Composable
fun AccountCard(
    account: AccountUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // LEFT: Account type icon
            Icon(
                imageVector = accountTypeIcon(account.type),
                contentDescription = null,
                tint = Color(account.color),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // CENTER: Name + type
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = account.type.name.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // RIGHT: Balance + include/exclude
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = account.balance,
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector =
                        if (account.includeInTotal)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint =
                        if (account.includeInTotal)
                            Color(0xFF4CAF50)
                        else
                            Color(0xFF9E9E9E),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
