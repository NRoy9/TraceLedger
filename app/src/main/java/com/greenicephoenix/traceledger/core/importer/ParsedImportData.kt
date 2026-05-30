package com.greenicephoenix.traceledger.core.importer

import kotlinx.serialization.Serializable

@Serializable
data class DerivedAccount(
    val name:    String,
    val rawType: String
) {
    val normalisedType: String get() = when (rawType.trim().lowercase()) {
        "wallet"                      -> "WALLET"
        "cash"                        -> "CASH"
        "credit_card", "credit card"  -> "CREDIT_CARD"
        else                          -> "BANK"
    }
    val key: String get() = "${name.trim().lowercase()}|${normalisedType}"
}

@Serializable
data class DerivedCategory(
    val name:    String,
    val forType: String
) {
    val key: String get() = "${name.trim().lowercase()}|${forType}"
}

@Serializable
data class RawCsvTransaction(
    val date:            String,   // ISO yyyy-MM-dd — stored as String for serialization
    val type:            String,
    val amountStr:       String,   // stored as String for serialization
    val fromAccountName: String,
    val fromAccountType: String,
    val toAccountName:   String?,
    val toAccountType:   String?,
    val categoryName:    String?,
    val note:            String?,
    val lineNumber:      Int
)

@Serializable
data class ParsedImportData(
    val derivedAccounts:   List<DerivedAccount>,
    val derivedCategories: List<DerivedCategory>,
    val transactions:      List<RawCsvTransaction>,
    val parseErrors:       List<String>
)

@Serializable
data class CsvImportMapping(
    val accountMappings:  Map<String, String?>,
    val categoryMappings: Map<String, String?>
)