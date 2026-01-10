package com.greenicephoenix.traceledger.feature.transactions.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.domain.model.TransactionUiModel
import com.greenicephoenix.traceledger.domain.model.TransactionType
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: TransactionUiModel,
    categoryName: String,
    accountName: String,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F0F0F),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = when (transaction.type) {
                    TransactionType.EXPENSE -> "Expense"
                    TransactionType.INCOME -> "Income"
                    TransactionType.TRANSFER -> "Transfer"
                },
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Text(
                text = accountName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = Color.White.copy(alpha = 0.1f)
            )

            Text(
                text = "Date",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Text(
                text = transaction.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                color = Color.White
            )

            transaction.note?.let {
                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    color = Color.White.copy(alpha = 0.1f)
                )
                Text("Note", color = Color.Gray)
                Text(it, color = Color.White)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}