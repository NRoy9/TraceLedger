package com.greenicephoenix.traceledger.feature.transactions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.domain.model.TransactionType
import com.greenicephoenix.traceledger.domain.model.TransactionUiModel

@Composable
fun TransactionRow(
    modifier: Modifier = Modifier,
    transaction: TransactionUiModel,
    categoryName: String,
    categoryIcon: ImageVector,
    categoryColor: Color,
    accountName: String,
    amountText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A), // top highlight
                            Color(0xFF0F0F0F)  // base
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            Column(
                //modifier = Modifier.padding(16.dp),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // ───── ICON (SPANS BOTH ROWS) ─────
                    Box(
                        modifier = Modifier
                            .size(46.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // ───── TEXT CONTENT ─────
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        // CATEGORY · NOTE
                        Text(
                            text = buildString {
                                append(categoryName)
                                transaction.note?.let {
                                    append(" · ")
                                    append(it)
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )

                        // ACCOUNT · DATE (slightly larger)
                        Text(
                            text = "$accountName · ${transaction.date}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = MaterialTheme.typography.bodySmall.fontSize * 1.1
                            ),
                            color = Color.Gray
                        )
                    }

                    // ───── AMOUNT (slightly larger) ─────
                    Text(
                        text = amountText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.15
                        ),
                        color =
                            when (transaction.type) {
                                TransactionType.EXPENSE -> Color(0xFFE53935)
                                TransactionType.INCOME -> Color(0xFF4CAF50)
                                TransactionType.TRANSFER -> Color.Gray
                            }
                    )
                }

                // ───── ACCENT LINE (SHORTER) ─────
                Box(
                    modifier = Modifier
                        .padding(start = 52.dp)
                        .fillMaxWidth(0.7f)
                        .height(1.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = Color.Black,
                            spotColor = Color.Black
                        )
                        .background(
                            color = categoryColor.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}