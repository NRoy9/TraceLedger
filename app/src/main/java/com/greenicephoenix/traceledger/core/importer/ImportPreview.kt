package com.greenicephoenix.traceledger.core.importer

data class ImportPreview(
    val accounts: Int = 0,
    val categories: Int = 0,
    val budgets: Int = 0,
    val transactions: Int = 0,

    // CSV-specific
    val totalRows: Int = 0,
    val validRows: Int = 0,
    val skippedRows: Int = 0,
    val skippedByReason: Map<SkipReason, Int> = emptyMap()
)

enum class SkipReason {
    UNKNOWN_ACCOUNT,
    UNKNOWN_CATEGORY,
    INVALID_FORMAT
}
