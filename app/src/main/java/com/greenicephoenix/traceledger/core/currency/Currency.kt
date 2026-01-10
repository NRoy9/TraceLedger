package com.greenicephoenix.traceledger.core.currency

/**
 * UI-level currency model.
 * Single global currency for the app.
 */
enum class Currency(
    val symbol: String,
    val code: String
) {
    INR(symbol = "₹", code = "INR"),
    USD(symbol = "$", code = "USD"),
    EUR(symbol = "€", code = "EUR"),
    GBP(symbol = "£", code = "GBP"),
    JPY(symbol = "¥", code = "JPY"),
    AUD(symbol = "$", code = "AUD"),
    CAD(symbol = "$", code = "CAD"),
    SGD(symbol = "$", code = "SGD")
}
