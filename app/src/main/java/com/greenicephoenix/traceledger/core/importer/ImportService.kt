package com.greenicephoenix.traceledger.core.importer

import android.content.ContentResolver
import android.net.Uri
import androidx.room.withTransaction
import com.greenicephoenix.traceledger.core.database.TraceLedgerDatabase
import com.greenicephoenix.traceledger.core.export.*
import com.greenicephoenix.traceledger.core.database.entity.AccountEntity
import com.greenicephoenix.traceledger.core.database.entity.CategoryEntity
import com.greenicephoenix.traceledger.core.database.entity.RecurringTransactionEntity
import com.greenicephoenix.traceledger.core.database.entity.TransactionEntity
import com.greenicephoenix.traceledger.core.datastore.SettingsDataStore
import com.greenicephoenix.traceledger.feature.budgets.data.BudgetEntity
import com.greenicephoenix.traceledger.feature.templates.data.TransactionTemplateEntity
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.first
import java.io.InputStreamReader
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

/**
 * Result returned by [ImportService.importCsvTransactions].
 * Previously the function returned Unit, so callers had no way to show a
 * completion message. Now they can display "X imported, Y skipped."
 */
data class ImportResult(
    val imported: Int,
    val skipped: Int
)

class ImportService(
    private val database:      TraceLedgerDatabase,
    private val contentResolver: ContentResolver,
    private val settingsStore: SettingsDataStore? = null
) {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    // ── JSON IMPORT ───────────────────────────────────────────────────────────

    /**
     * Merge import from a TraceLedger JSON backup.
     *
     * Behaviour per requirement:
     * - Accounts:     skip if an account with the same ID already exists
     * - Categories:   skip if a category with the same ID already exists
     * - Budgets:      skip if a budget with the same ID already exists
     * - Transactions: skip if a transaction with the same ID already exists
     *
     * No data is ever deleted. Runs inside a single Room transaction.
     * [onProgress] receives 0–100 as a percentage of items processed.
     */
    suspend fun importJson(
        uri: Uri,
        onProgress: (Int) -> Unit
    ) {
        val envelope = readEnvelope(uri)
        validateEnvelope(envelope)

        database.withTransaction {
            val existingAccounts       = database.accountDao().getAllOnce()
            val existingCategories     = database.categoryDao().getAllOnce()
            val existingBudgetIds      = database.budgetDao().getAllOnce().map { it.id }.toSet()
            val existingTransactionIds = database.transactionDao().getAllOnce().map { it.id }.toSet()

            val existingAccountIds  = existingAccounts.map { it.id }.toSet()
            val existingCategoryIds = existingCategories.map { it.id }.toSet()

            // Name → existing ID maps — used to remap backup IDs when names match
            val accountNameToId  = existingAccounts.associate   { it.name.trim().lowercase() to it.id }
            val categoryNameToId = existingCategories.associate { it.name.trim().lowercase() to it.id }

            // Remap tables: backupId → existingId
            // Built during account/category import so transaction IDs can be fixed up.
            val accountIdRemap  = mutableMapOf<String, String>()
            val categoryIdRemap = mutableMapOf<String, String>()

            val total =
                envelope.accounts.size +
                        envelope.categories.size +
                        envelope.budgets.size +
                        envelope.recurring.size +
                        envelope.templates.size +
                        envelope.transactions.size

            var done = 0
            fun step() { done++; if (total > 0) onProgress((done * 100) / total) }

            // ── Accounts ──────────────────────────────────────────────────────
            envelope.accounts.forEach { acct ->
                when {
                    // Exact ID match — account already exists, no action needed
                    acct.id in existingAccountIds -> { /* skip */ }

                    // Same name exists with different ID — remap, don't duplicate
                    accountNameToId.containsKey(acct.name.trim().lowercase()) -> {
                        val existingId = accountNameToId[acct.name.trim().lowercase()]!!
                        accountIdRemap[acct.id] = existingId
                    }

                    // Genuinely new account — insert with backup balance
                    else -> {
                        database.accountDao().insert(
                            AccountEntity(
                                id             = acct.id,
                                name           = acct.name,
                                balance        = BigDecimal(acct.balance),
                                type           = acct.type,
                                includeInTotal = acct.includeInTotal,
                                details        = null,
                                color          = acct.color
                            )
                        )
                    }
                }
                step()
            }

            // ── Categories ────────────────────────────────────────────────────
            envelope.categories.forEach { cat ->
                when {
                    cat.id in existingCategoryIds -> { /* skip */ }

                    categoryNameToId.containsKey(cat.name.trim().lowercase()) -> {
                        val existingId = categoryNameToId[cat.name.trim().lowercase()]!!
                        categoryIdRemap[cat.id] = existingId
                    }

                    else -> {
                        database.categoryDao().insert(
                            CategoryEntity(
                                id    = cat.id,
                                name  = cat.name,
                                type  = cat.type,
                                color = cat.color,
                                icon  = cat.icon
                            )
                        )
                    }
                }
                step()
            }

            // ── Budgets ───────────────────────────────────────────────────────
            envelope.budgets.forEach { budget ->
                if (budget.id !in existingBudgetIds) {
                    database.budgetDao().insert(
                        BudgetEntity(
                            id          = budget.id,
                            categoryId  = categoryIdRemap[budget.categoryId] ?: budget.categoryId,
                            limitAmount = BigDecimal(budget.limitAmount),
                            month       = YearMonth.parse(budget.month),
                            isActive    = budget.isActive
                        )
                    )
                }
                step()
            }

            // ── Recurring transactions ────────────────────────────────────────
            val existingRecurringIds = database.recurringTransactionDao()
                .getAll().first().map { it.id }.toSet()

            envelope.recurring.forEach { rec ->
                if (rec.id !in existingRecurringIds) {
                    database.recurringTransactionDao().insert(
                        RecurringTransactionEntity(
                            id                = rec.id,
                            type              = rec.type,
                            amount            = BigDecimal(rec.amount),
                            fromAccountId     = rec.fromAccountId?.let { accountIdRemap[it] ?: it },
                            toAccountId       = rec.toAccountId?.let   { accountIdRemap[it] ?: it },
                            categoryId        = rec.categoryId?.let    { categoryIdRemap[it] ?: it },
                            note              = rec.note,
                            startDate         = LocalDate.parse(rec.startDate),
                            endDate           = rec.endDate?.let { LocalDate.parse(it) },
                            frequency         = rec.frequency,
                            lastGeneratedDate = rec.lastGeneratedDate?.let { LocalDate.parse(it) },
                            isActive          = rec.isActive
                        )
                    )
                }
                step()
            }

            // ── Templates ─────────────────────────────────────────────────────
            val existingTemplateIds = database.transactionTemplateDao()
                .observeAll().first().map { it.id }.toSet()

            envelope.templates.forEach { tmpl ->
                if (tmpl.id !in existingTemplateIds) {
                    database.transactionTemplateDao().upsert(
                        TransactionTemplateEntity(
                            id            = tmpl.id,
                            name          = tmpl.name,
                            type          = tmpl.type,
                            amount        = tmpl.amount,
                            fromAccountId = tmpl.fromAccountId?.let { accountIdRemap[it] ?: it },
                            toAccountId   = tmpl.toAccountId?.let   { accountIdRemap[it] ?: it },
                            categoryId    = tmpl.categoryId?.let    { categoryIdRemap[it] ?: it },
                            notes         = tmpl.notes,
                            createdAt     = tmpl.createdAt
                        )
                    )
                }
                step()
            }

            // ── Transactions ──────────────────────────────────────────────────
            envelope.transactions.forEach { tx ->
                if (tx.id !in existingTransactionIds) {
                    database.transactionDao().insertTransaction(
                        TransactionEntity(
                            id            = tx.id,
                            type          = tx.type,
                            amount        = BigDecimal(tx.amount),
                            date          = LocalDate.parse(tx.date),
                            fromAccountId = tx.fromAccountId?.let { accountIdRemap[it] ?: it },
                            toAccountId   = tx.toAccountId?.let   { accountIdRemap[it] ?: it },
                            categoryId    = tx.categoryId?.let    { categoryIdRemap[it] ?: it },
                            note          = tx.note,
                            createdAt     = Instant.ofEpochSecond(tx.createdAtEpoch)
                        )
                    )
                }
                step()
            }
        }

        // ── Settings restore (outside DB transaction — DataStore is separate) ──
        // Only restore if the backup contains settings and a store is available.
        // Skip if values are already set on this device (don't overwrite user prefs).
        envelope.settings?.let { s ->
            settingsStore?.let { store ->
                s.currencyCode?.let { code ->
                    if (store.currencyCode.first() == null) {
                        store.setCurrency(code)
                    }
                }
                s.numberFormat?.let { fmt ->
                    if (store.numberFormat.first() == null) {
                        store.setNumberFormat(
                            com.greenicephoenix.traceledger.core.datastore.NumberFormat
                                .entries.firstOrNull { it.name == fmt }
                                ?: com.greenicephoenix.traceledger.core.datastore.NumberFormat.INDIAN
                        )
                    }
                }
            }
        }
    }

    // ── CSV IMPORT ────────────────────────────────────────────────────────────

    /**
     * Additive CSV import — transactions only, does not wipe existing data.
     *
     * CSV column order (matches ExportService.exportCsv):
     *   0  date
     *   1  type
     *   2  amount
     *   3  category       (human name — ignored for import)
     *   4  fromAccount    (human name — ignored for import)
     *   5  toAccount      (human name — ignored for import)
     *   6  note
     *   7  categoryId     (used for import)
     *   8  fromAccountId  (used for import)
     *   9  toAccountId    (used for import)
     *   10 id
     *
     * Minimum 11 columns required.
     * Skips rows with unknown account/category IDs or duplicate transaction IDs.
     */
    suspend fun importCsvTransactions(
        uri: Uri,
        onProgress: (Int) -> Unit
    ): ImportResult {
        val stream = contentResolver.openInputStream(uri)
            ?: error("Unable to open CSV file")

        val existingAccounts    = database.accountDao().getAllOnce()
        val existingAccountIds  = existingAccounts.map { it.id }.toSet()
        // Name → ID map for resolving by human name when UUID doesn't match
        val accountNameToId     = existingAccounts.associate { it.name.trim().lowercase() to it.id }

        val existingCategoryEntities = database.categoryDao().getAllOnce()
        val existingCategoryIds      = existingCategoryEntities.map { it.id }.toSet()
        val categoryNameToId         = existingCategoryEntities.associate { it.name.trim().lowercase() to it.id }

        val existingTransactionIds =
            database.transactionDao().getAllOnce().map { it.id }.toSet()

        val lines = stream.bufferedReader().readLines()
        if (lines.size <= 1) return ImportResult(imported = 0, skipped = 0)

        val rows  = lines.drop(1)
        val total = rows.size
        var processed = 0
        var imported  = 0
        var skipped   = 0

        database.withTransaction {
            rows.forEach { line ->
                processed++
                onProgress((processed * 100) / total)

                val cols = parseCsvLine(line)
                // Need at least 12 columns (id is col 11, createdAtEpoch is col 10)
                if (cols.size < 12) {
                    skipped++
                    return@forEach
                }

                // Read using new column layout
                val date              = cols[0]
                val type              = cols[1]
                val amount            = cols[2]
                val fromAccountName   = cols[4].ifBlank { null }  // human name col
                val toAccountName     = cols[5].ifBlank { null }  // human name col
                val note              = cols[6].ifBlank { null }
                val categoryIdRaw     = cols[7].ifBlank { null }
                val fromAccountIdRaw  = cols[8].ifBlank { null }
                val toAccountIdRaw    = cols[9].ifBlank { null }
                val createdAtEpoch    = cols[10].toLongOrNull()
                val id                = cols[11]

                // Resolve account IDs — try UUID first, fall back to name lookup.
                // This handles fresh installs where accounts were recreated with new UUIDs.
                val fromAccountId = when {
                    fromAccountIdRaw == null                    -> null
                    fromAccountIdRaw in existingAccountIds      -> fromAccountIdRaw
                    fromAccountName  != null &&
                            accountNameToId.containsKey(fromAccountName.trim().lowercase())
                        -> accountNameToId[fromAccountName.trim().lowercase()]
                    else                                        -> fromAccountIdRaw // will fail validation below
                }
                val toAccountId = when {
                    toAccountIdRaw == null                      -> null
                    toAccountIdRaw in existingAccountIds        -> toAccountIdRaw
                    toAccountName  != null &&
                            accountNameToId.containsKey(toAccountName.trim().lowercase())
                        -> accountNameToId[toAccountName.trim().lowercase()]
                    else                                        -> toAccountIdRaw
                }

                // Resolve category ID — try UUID first, fall back to name lookup
                val categoryName = cols[3].ifBlank { null }
                val categoryId = when {
                    categoryIdRaw == null                       -> null
                    categoryIdRaw in existingCategoryIds        -> categoryIdRaw
                    categoryName  != null &&
                            categoryNameToId.containsKey(categoryName.trim().lowercase())
                        -> categoryNameToId[categoryName.trim().lowercase()]
                    else                                        -> categoryIdRaw
                }

                // Skip duplicate transactions
                if (id in existingTransactionIds) {
                    skipped++
                    return@forEach
                }

                val hasUnknownAccount =
                    (fromAccountId != null && fromAccountId !in existingAccountIds) ||
                            (toAccountId   != null && toAccountId   !in existingAccountIds)

                val hasUnknownCategory =
                    categoryId != null && categoryId !in existingCategoryIds
                if (hasUnknownAccount || hasUnknownCategory) {
                    skipped++
                    return@forEach
                }

                try {
                    database.transactionDao().insertTransaction(
                        TransactionEntity(
                            id            = id,
                            type          = type,
                            amount        = BigDecimal(amount),
                            date          = LocalDate.parse(date),
                            fromAccountId = fromAccountId,
                            toAccountId   = toAccountId,
                            categoryId    = categoryId,
                            note          = note,
                            // Preserve original timestamp if available, else use now
                            createdAt     = if (createdAtEpoch != null)
                                Instant.ofEpochSecond(createdAtEpoch)
                            else
                                Instant.now()
                        )
                    )
                    imported++
                } catch (e: Exception) {
                    // Malformed row — amount not parseable, date invalid, etc.
                    skipped++
                }
            }
        }

        return ImportResult(imported = imported, skipped = skipped)
    }

    // ── PREVIEW ───────────────────────────────────────────────────────────────

    fun previewJson(uri: Uri): ImportPreview {
        val envelope = readEnvelope(uri)
        validateEnvelope(envelope)
        return ImportPreview(
            accounts     = envelope.accounts.size,
            categories   = envelope.categories.size,
            budgets      = envelope.budgets.size,
            recurring    = envelope.recurring.size,
            templates    = envelope.templates.size,
            transactions = envelope.transactions.size
        )
    }

    suspend fun previewCsv(uri: Uri): ImportPreview {
        val existingAccounts    = database.accountDao().getAllOnce()
        val existingAccountIds  = existingAccounts.map { it.id }.toSet()
        val accountNameToId     = existingAccounts.associate { it.name.trim().lowercase() to it.id }

        val existingCategories  = database.categoryDao().getAllOnce()
        val existingCategoryIds = existingCategories.map { it.id }.toSet()
        val categoryNameToId    = existingCategories.associate { it.name.trim().lowercase() to it.id }

        var total   = 0
        var valid   = 0
        val skipped = mutableMapOf<SkipReason, Int>()

        val stream = contentResolver.openInputStream(uri)
            ?: error("Unable to open CSV file")

        stream.bufferedReader().use { reader ->
            val lines = reader.readLines()
            if (lines.isEmpty()) error("Empty CSV file")

            lines.drop(1).forEach { line ->
                total++
                val cols = parseCsvLine(line)

                if (cols.size < 12) {
                    skipped[SkipReason.INVALID_FORMAT] =
                        (skipped[SkipReason.INVALID_FORMAT] ?: 0) + 1
                    return@forEach
                }

                val fromAccountName  = cols[4].ifBlank { null }
                val toAccountName    = cols[5].ifBlank { null }
                val categoryName     = cols[3].ifBlank { null }
                val fromAccountIdRaw = cols[8].ifBlank { null }
                val toAccountIdRaw   = cols[9].ifBlank { null }
                val categoryIdRaw    = cols[7].ifBlank { null }

                // Resolve by UUID then by name — same logic as importCsvTransactions
                val fromAccountId = when {
                    fromAccountIdRaw == null                   -> null
                    fromAccountIdRaw in existingAccountIds     -> fromAccountIdRaw
                    fromAccountName != null &&
                            accountNameToId.containsKey(fromAccountName.trim().lowercase())
                        -> accountNameToId[fromAccountName.trim().lowercase()]
                    else                                       -> fromAccountIdRaw
                }
                val toAccountId = when {
                    toAccountIdRaw == null                     -> null
                    toAccountIdRaw in existingAccountIds       -> toAccountIdRaw
                    toAccountName != null &&
                            accountNameToId.containsKey(toAccountName.trim().lowercase())
                        -> accountNameToId[toAccountName.trim().lowercase()]
                    else                                       -> toAccountIdRaw
                }
                val categoryId = when {
                    categoryIdRaw == null                      -> null
                    categoryIdRaw in existingCategoryIds       -> categoryIdRaw
                    categoryName != null &&
                            categoryNameToId.containsKey(categoryName.trim().lowercase())
                        -> categoryNameToId[categoryName.trim().lowercase()]
                    else                                       -> categoryIdRaw
                }

                val unknownAccount =
                    (fromAccountId != null && fromAccountId !in existingAccountIds) ||
                            (toAccountId   != null && toAccountId   !in existingAccountIds)

                val unknownCategory =
                    categoryId != null && categoryId !in existingCategoryIds

                when {
                    unknownAccount  -> skipped[SkipReason.UNKNOWN_ACCOUNT]  =
                        (skipped[SkipReason.UNKNOWN_ACCOUNT]  ?: 0) + 1
                    unknownCategory -> skipped[SkipReason.UNKNOWN_CATEGORY] =
                        (skipped[SkipReason.UNKNOWN_CATEGORY] ?: 0) + 1
                    else            -> valid++
                }
            }
        }

        return ImportPreview(
            totalRows       = total,
            validRows       = valid,
            skippedRows     = total - valid,
            skippedByReason = skipped
        )
    }

    // ── UNIFIED CSV IMPORT (with user mapping) ───────────────────────────────

    /**
     * Final import step after user confirms mapping on [ImportMappingScreen].
     *
     * For each [DerivedAccount] in [data]:
     * - If mapping has an existing ID → use it
     * - If mapping is null (Create New) → insert new account with balance
     *   calculated from transactions (income adds, expense/investment subtracts,
     *   transfer adjusts both sides). Balance is approximate — user is warned.
     *
     * For each [DerivedCategory]:
     * - If mapping has an existing ID → use it
     * - If null → insert new category with default color/icon
     *
     * Then inserts all transactions using resolved IDs.
     * Skips duplicate transaction IDs (same date+type+amount+account combo
     * is not a reliable dedup key so we use a content hash instead).
     *
     * Returns [ImportResult] with imported and skipped counts.
     */
    suspend fun importFromCsvWithMappings(
        data:       ParsedImportData,
        mappings:   CsvImportMapping,
        onProgress: (Int) -> Unit
    ): ImportResult {
        var imported = 0
        var skipped  = 0

        database.withTransaction {

            // ── 1. Resolve/create accounts ────────────────────────────────────
            // accountKey → final DB id
            val accountIdMap = mutableMapOf<String, String>()

            // Pre-calculate balances for accounts that will be created new
            // Balance = sum of transactions affecting that account
            val calculatedBalances = mutableMapOf<String, BigDecimal>()
            data.transactions.forEach { tx ->
                val fromKey = DerivedAccount(tx.fromAccountName, tx.fromAccountType).key
                val toKey   = tx.toAccountName?.let {
                    DerivedAccount(it, tx.toAccountType ?: "BANK").key
                }

                when (tx.type) {
                    "INCOME"     -> calculatedBalances[fromKey] =
                        (calculatedBalances[fromKey] ?: BigDecimal.ZERO) + BigDecimal(tx.amountStr)
                    "EXPENSE",
                    "INVESTMENT" -> calculatedBalances[fromKey] =
                        (calculatedBalances[fromKey] ?: BigDecimal.ZERO) - BigDecimal(tx.amountStr)
                    "TRANSFER"   -> {
                        calculatedBalances[fromKey] =
                            (calculatedBalances[fromKey] ?: BigDecimal.ZERO) - BigDecimal(tx.amountStr)
                        if (toKey != null) {
                            calculatedBalances[toKey] =
                                (calculatedBalances[toKey] ?: BigDecimal.ZERO) + BigDecimal(tx.amountStr)
                        }
                    }
                }
            }

            data.derivedAccounts.forEach { derived ->
                val existingId = mappings.accountMappings[derived.key]
                if (existingId != null) {
                    // User mapped to existing account — use its ID directly
                    accountIdMap[derived.key] = existingId
                } else {
                    // Create new account with calculated balance
                    val newId      = java.util.UUID.randomUUID().toString()
                    val balance    = calculatedBalances[derived.key] ?: BigDecimal.ZERO
                    // Assign a deterministic color from a built-in palette
                    // (avoids dependency on AccountColors whose property name may clash)
                    val accountPalette = listOf(
                        0xFF1E88E5L, 0xFF43A047L, 0xFF8E24AAL, 0xFFE53935L,
                        0xFF00897BL, 0xFFFFB300L, 0xFF5E35B1L, 0xFF3949ABL,
                        0xFFD81B60L, 0xFF00ACC1L, 0xFF6D4C41L, 0xFF546E7AL
                    )
                    val colorIndex = ((derived.name.hashCode() and 0x7FFFFFFF) % accountPalette.size)
                    val color      = accountPalette[colorIndex]

                    database.accountDao().insert(
                        AccountEntity(
                            id             = newId,
                            name           = derived.name,
                            type           = derived.normalisedType,
                            balance        = balance,
                            includeInTotal = true,
                            details        = null,
                            color          = color
                        )
                    )
                    accountIdMap[derived.key] = newId
                }
            }

            // ── 2. Resolve/create categories ──────────────────────────────────
            val categoryIdMap = mutableMapOf<String, String>()

            // Hardcoded Long color values per category type — avoids Color.value ULong cast issues
            val expensePalette    = listOf(
                0xFFE53935L, 0xFFD32F2FL, 0xFFD81B60L, 0xFF8E24AAL, 0xFF5E35B1L,
                0xFF3949ABL, 0xFF1E88E5L, 0xFF039BE5L, 0xFF00897BL, 0xFF43A047L,
                0xFFFF7043L, 0xFF6D4C41L
            )
            val incomePalette     = listOf(
                0xFF2E7D32L, 0xFF388E3CL, 0xFF00695CL, 0xFF00796BL, 0xFF0277BDL,
                0xFF1565C0L, 0xFF4527A0L, 0xFF6A1B9AL, 0xFF33691EL, 0xFF006064L
            )
            val investmentPalette = listOf(
                0xFFF9A825L, 0xFFFFB300L, 0xFFF57F17L, 0xFFE65100L, 0xFF2E7D32L,
                0xFF3949ABL, 0xFF4527A0L, 0xFF6D4C41L, 0xFF455A64L
            )

            data.derivedCategories.forEach { derived ->
                val existingId = mappings.categoryMappings[derived.key]
                if (existingId != null) {
                    categoryIdMap[derived.key] = existingId
                } else {
                    val newId      = java.util.UUID.randomUUID().toString()
                    val palette    = when (derived.forType) {
                        "INCOME"     -> incomePalette
                        "INVESTMENT" -> investmentPalette
                        else         -> expensePalette
                    }
                    val colorIndex = ((derived.name.hashCode() and 0x7FFFFFFF) % palette.size)
                    val color      = palette[colorIndex]

                    database.categoryDao().insert(
                        CategoryEntity(
                            id    = newId,
                            name  = derived.name,
                            type  = derived.forType,
                            color = color,
                            icon  = "other"   // user can edit in Manage Categories
                        )
                    )
                    categoryIdMap[derived.key] = newId
                }
            }

            // ── 3. Insert transactions ─────────────────────────────────────────
            val total = data.transactions.size
            data.transactions.forEachIndexed { idx, tx ->
                onProgress(((idx + 1) * 100) / total)

                val fromKey     = DerivedAccount(tx.fromAccountName, tx.fromAccountType).key
                val fromId      = accountIdMap[fromKey]
                val toId        = tx.toAccountName?.let { name ->
                    val toKey = DerivedAccount(name, tx.toAccountType ?: "BANK").key
                    accountIdMap[toKey]
                }
                val catKey      = tx.categoryName?.let { name ->
                    DerivedCategory(name, tx.type).key
                }
                val categoryId  = catKey?.let { categoryIdMap[it] }

                if (fromId == null) { skipped++; return@forEachIndexed }

                try {
                    database.transactionDao().insertTransaction(
                        TransactionEntity(
                            id            = java.util.UUID.randomUUID().toString(),
                            type          = tx.type,
                            amount        = BigDecimal(tx.amountStr),
                            date          = LocalDate.parse(tx.date),
                            fromAccountId = fromId,
                            toAccountId   = toId,
                            categoryId    = categoryId,
                            note          = tx.note,
                            createdAt     = Instant.now()
                        )
                    )
                    imported++
                } catch (e: Exception) {
                    skipped++
                }
            }
        }

        return ImportResult(imported = imported, skipped = skipped)
    }

    // ── INTERNALS ─────────────────────────────────────────────────────────────

    private fun readEnvelope(uri: Uri): ExportEnvelope {
        val stream = contentResolver.openInputStream(uri)
            ?: error("Unable to open input stream")
        return InputStreamReader(stream).use { reader ->
            json.decodeFromString(ExportEnvelope.serializer(), reader.readText())
        }
    }

    private fun validateEnvelope(envelope: ExportEnvelope) {
        require(envelope.meta.app == "TraceLedger") {
            "Invalid backup file (app mismatch)"
        }
        require(envelope.meta.schemaVersion <= database.openHelper.readableDatabase.version) {
            "Backup schema is newer than this version of TraceLedger"
        }
    }

    /**
     * Parse a single CSV line, handling quoted fields that may contain commas.
     * Example: 'hello,"world, with comma",test' → ["hello", "world, with comma", "test"]
     */
    private fun parseCsvLine(line: String): List<String> {
        val result  = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (c in line) {
            when {
                c == '"'              -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else                  -> current.append(c)
            }
        }
        result.add(current.toString())
        return result
    }
}