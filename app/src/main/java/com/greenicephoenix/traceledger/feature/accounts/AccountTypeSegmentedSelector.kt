package com.greenicephoenix.traceledger.feature.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.domain.model.AccountType

@Composable
fun AccountTypeSegmentedSelector(
    selected: AccountType,
    onSelected: (AccountType) -> Unit
) {

    Column {

        Text(
            text = "Account Type",
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            AccountType.values().forEach { type ->

                val isSelected = type == selected

                TextButton(
                    onClick = { onSelected(type) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (isSelected)
                            NothingRed.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected)
                            NothingRed
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = type.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
