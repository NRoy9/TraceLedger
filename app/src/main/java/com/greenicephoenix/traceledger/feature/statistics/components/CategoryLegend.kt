package com.greenicephoenix.traceledger.feature.statistics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.currency.CurrencyFormatter
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModel

@Composable
internal fun CategoryLegend(
    slices:             List<StatisticsViewModel.CategorySlice>,
    categoryMap:        Map<String, CategoryUiModel>,
    selectedCategoryId: String? = null,
    onItemClick:        ((categoryId: String) -> Unit)? = null
) {
    if (slices.isEmpty()) return

    val currency      by CurrencyManager.currency.collectAsState()
    // Determine if we're in a dark theme by checking background luminance
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        slices.forEach { slice ->
            val category   = categoryMap[slice.categoryId]
            val color      = category?.color?.let { Color(it) }
                ?: MaterialTheme.colorScheme.primary
            val isSelected = slice.categoryId == selectedCategoryId

            // Pill background: in light mode use higher alpha for visibility
            val pillBgAlpha   = if (isDark) 0.18f else 0.14f
            // Pill text: use the category color in dark, darken slightly in light
            val pillTextColor = if (isDark) color else color

            val rowModifier = if (onItemClick != null)
                Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isSelected)
                            color.copy(alpha = if (isDark) 0.12f else 0.08f)
                        else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onItemClick(slice.categoryId) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            else
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)

            Row(
                modifier          = rowModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, CircleShape)
                )

                Spacer(Modifier.width(12.dp))

                // Category name — always onSurface for guaranteed contrast
                Text(
                    text     = category?.name ?: "Unknown",
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Percentage pill
                Box(
                    modifier = Modifier
                        .background(
                            color = color.copy(alpha = pillBgAlpha),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text  = String.format("%.1f%%", slice.percentage),
                        style = MaterialTheme.typography.labelSmall,
                        // Ensure minimum contrast: use onSurface if color is too light
                        color = if (isDark) pillTextColor
                        else if (color.luminance() > 0.6f)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        else pillTextColor
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Amount
                Text(
                    text  = CurrencyFormatter.format(slice.amount.toPlainString(), currency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (isDark) 0.75f else 0.70f
                    )
                )
            }
        }
    }
}