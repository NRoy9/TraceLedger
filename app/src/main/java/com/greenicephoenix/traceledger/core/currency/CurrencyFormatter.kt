package com.greenicephoenix.traceledger.core.currency

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats numeric amounts for display based on currency.
 * UI-only responsibility.
 */
object CurrencyFormatter {

    fun format(
        amount: String,
        currency: Currency
    ): String {
        if (amount.isBlank()) return ""

        val number = amount.toBigDecimalOrNull() ?: return amount

        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            isGroupingUsed = true
            minimumFractionDigits = if (amount.contains(".")) 2 else 0
            maximumFractionDigits = 2
        }

        return "${currency.symbol}${formatter.format(number)}"
    }
}