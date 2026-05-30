package com.greenicephoenix.traceledger.core.importer

import android.content.ContentResolver
import android.net.Uri
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Parses a unified TraceLedger CSV file into [ParsedImportData].
 *
 * The parser is lenient:
 * - Comment lines starting with '#' are skipped
 * - Type values are case-insensitive
 * - account_type blank defaults to BANK
 * - Malformed rows are collected in [ParsedImportData.parseErrors] and skipped
 *
 * Accounts are derived as unique name+type combinations across all rows.
 * Categories are derived as unique name+transactionType combinations.
 * TRANSFER transactions produce no category entry.
 */
class UnifiedCsvParser(private val contentResolver: ContentResolver) {

    fun parse(uri: Uri): ParsedImportData {
        val stream = contentResolver.openInputStream(uri)
            ?: error("Unable to open CSV file")

        val lines = stream.bufferedReader().readLines()

        val transactions   = mutableListOf<RawCsvTransaction>()
        val parseErrors    = mutableListOf<String>()
        val accountSet     = mutableMapOf<String, DerivedAccount>()    // key → DerivedAccount
        val categorySet    = mutableMapOf<String, DerivedCategory>()   // key → DerivedCategory

        var headerSeen = false
        var lineNum    = 0

        for (rawLine in lines) {
            lineNum++
            val line = rawLine.trim()

            // Skip blank lines and comment lines
            if (line.isBlank() || line.startsWith("#")) continue

            // Skip the header row
            if (!headerSeen) {
                // Validate that this looks like our header
                if (line.lowercase().startsWith("date,type")) {
                    headerSeen = true
                    continue
                }
                // First non-comment, non-blank line must be the header
                parseErrors.add("Line $lineNum: expected header row, got: $line")
                continue
            }

            val cols = parseCsvLine(line)
            if (cols.size < UnifiedCsvSchema.MIN_COLUMNS) {
                parseErrors.add("Line $lineNum: expected ${UnifiedCsvSchema.MIN_COLUMNS} columns, found ${cols.size} — skipped")
                continue
            }

            // ── Parse each field ──────────────────────────────────────────────

            val dateStr          = cols[UnifiedCsvSchema.COL_DATE].trim()
            val typeStr          = cols[UnifiedCsvSchema.COL_TYPE].trim().uppercase()
            val amountStr        = cols[UnifiedCsvSchema.COL_AMOUNT].trim()
            val fromAccountName  = cols[UnifiedCsvSchema.COL_FROM_ACCOUNT].trim()
            val fromAccountType  = cols[UnifiedCsvSchema.COL_FROM_ACCOUNT_TYPE].trim()
            val toAccountName    = cols[UnifiedCsvSchema.COL_TO_ACCOUNT].trim().ifBlank { null }
            val toAccountType    = cols[UnifiedCsvSchema.COL_TO_ACCOUNT_TYPE].trim().ifBlank { null }
            val categoryName     = cols[UnifiedCsvSchema.COL_CATEGORY].trim().ifBlank { null }
            val note             = cols[UnifiedCsvSchema.COL_NOTE].trim().ifBlank { null }

            // Validate date
            val date = try {
                LocalDate.parse(dateStr)
            } catch (e: DateTimeParseException) {
                parseErrors.add("Line $lineNum: invalid date '$dateStr' (expected yyyy-MM-dd) — skipped")
                continue
            }

            // Validate type
            val normalisedType = when (typeStr) {
                "EXPENSE", "INCOME", "TRANSFER", "INVESTMENT" -> typeStr
                else -> {
                    parseErrors.add("Line $lineNum: unknown type '$typeStr' (expected EXPENSE/INCOME/TRANSFER/INVESTMENT) — skipped")
                    continue
                }
            }

            // Validate amount
            val amount = try {
                BigDecimal(amountStr).also {
                    if (it <= BigDecimal.ZERO) throw NumberFormatException("must be positive")
                }
            } catch (e: NumberFormatException) {
                parseErrors.add("Line $lineNum: invalid amount '$amountStr' — skipped")
                continue
            }

            // Validate from_account
            if (fromAccountName.isBlank()) {
                parseErrors.add("Line $lineNum: from_account is required — skipped")
                continue
            }

            // Validate TRANSFER has to_account
            if (normalisedType == "TRANSFER" && toAccountName.isNullOrBlank()) {
                parseErrors.add("Line $lineNum: TRANSFER requires to_account — skipped")
                continue
            }

            // ── Build derived account entries ─────────────────────────────────

            val fromAccount = DerivedAccount(
                name    = fromAccountName,
                rawType = fromAccountType
            )
            accountSet.putIfAbsent(fromAccount.key, fromAccount)

            val toAccountDerived = toAccountName?.let {
                DerivedAccount(
                    name    = it,
                    rawType = toAccountType ?: ""
                ).also { da -> accountSet.putIfAbsent(da.key, da) }
            }

            // ── Build derived category entries ────────────────────────────────
            // TRANSFER has no category; blank category is also allowed (skip)

            if (normalisedType != "TRANSFER" && !categoryName.isNullOrBlank()) {
                val cat = DerivedCategory(
                    name    = categoryName,
                    forType = normalisedType
                )
                categorySet.putIfAbsent(cat.key, cat)
            }

            // ── Store raw transaction ─────────────────────────────────────────

            transactions.add(
                RawCsvTransaction(
                    date            = date.toString(),
                    type            = normalisedType,
                    amountStr       = amount.toPlainString(),
                    fromAccountName = fromAccountName,
                    fromAccountType = fromAccount.normalisedType,
                    toAccountName   = toAccountName,
                    toAccountType   = toAccountDerived?.normalisedType,
                    categoryName    = categoryName,
                    note            = note,
                    lineNumber      = lineNum
                )
            )
        }

        return ParsedImportData(
            derivedAccounts   = accountSet.values.toList(),
            derivedCategories = categorySet.values.toList(),
            transactions      = transactions,
            parseErrors       = parseErrors
        )
    }

    // ── CSV line parser — handles quoted fields ───────────────────────────────

    private fun parseCsvLine(line: String): List<String> {
        val result   = mutableListOf<String>()
        val current  = StringBuilder()
        var inQuotes = false

        for (c in line) {
            when {
                c == '"'              -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> { result.add(current.toString()); current.clear() }
                else                  -> current.append(c)
            }
        }
        result.add(current.toString())
        return result
    }
}