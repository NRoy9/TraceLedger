package com.greenicephoenix.traceledger.core.ui.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

@Composable
fun AddTransactionFab(
    onClick: () -> Unit
) {
    FloatingActionButton(
        containerColor = NothingRed,
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Transaction"
        )
    }
}
