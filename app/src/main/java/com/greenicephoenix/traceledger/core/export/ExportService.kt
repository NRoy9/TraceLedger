package com.greenicephoenix.traceledger.core.export

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import com.greenicephoenix.traceledger.BuildConfig
import com.greenicephoenix.traceledger.core.database.TraceLedgerDatabase
import com.greenicephoenix.traceledger.core.importer.UnifiedCsvSchema
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.time.Instant
import java.time.format.DateTimeFormatter

enum class ExportFormat { JSON, CSV }

/**
 * Result returned after a successful export.
 * [fileSizeBytes] lets the UI show "Backup saved (1.2 MB)".
 */
data class ExportResult(
    val fileSizeBytes: Long,
    val fileName:      String
)

class ExportService(
    private val database:        TraceLedgerDatabase,
    private val contentResolver: ContentResolver,
    private val settingsStore:   com.greenicephoenix.traceledger.core.datastore.SettingsDataStore? = null
) {

    private val json = Json {
        prettyPrint    = true
        encodeDefaults = true
        explicitNulls  = false
    }

    /**
     * Export to [uri], return [ExportResult] with file size and name.
     * Throws on IO failure.
     */
    suspend fun export(format: ExportFormat, uri: Uri): ExportResult {
        return when (format) {
            ExportFormat.JSON -> exportJson(uri)
            ExportFormat.CSV  -> exportCsv(uri)
        }
    }

    /**
     * Export JSON directly to a folder tree URI (used by AutoBackupWorker).
     * Creates a file named with today's date inside [treeFolderUri].
     * Returns [ExportResult] with the file name and size.
     */
    suspend fun exportJsonToFolder(treeFolderUri: Uri, context: Context): ExportResult {
        val date     = java.time.LocalDate.now().toString()          // 2026-05-27
        val fileName = "TraceLedger-backup-$date.json"

        // DocumentsContract creates a file inside the user-picked folder.
        // This requires the persisted URI permission obtained via ACTION_OPEN_DOCUMENT_TREE.
        val docUri = DocumentFile
            .fromTreeUri(context, treeFolderUri)
            ?.createFile("application/json", fileName)
            ?.uri
            ?: error("Could not create backup file in selected folder")

        return exportJson(docUri, fileName)
    }

    // ── JSON EXPORT ───────────────────────────────────────────────────────────

    private suspend fun exportJson(uri: Uri, fileName: String? = null): ExportResult {
        val accounts     = database.accountDao().getAllOnce()
        val categories   = database.categoryDao().getAllOnce()
        val budgets      = database.budgetDao().getAllOnce()
        val transactions = database.transactionDao().getAllOnce()
        // Collect first emission from Flow-based DAOs (all recurring + all templates)
        val recurring    = database.recurringTransactionDao().getAll().first()
        val templates    = database.transactionTemplateDao().observeAll().first()

        val settingsExport = settingsStore?.let { store ->
            SettingsExport(
                currencyCode = store.currencyCode.first(),
                numberFormat = store.numberFormat.first()
            )
        }

        val envelope = ExportEnvelope(
            meta = ExportMeta(
                app           = "TraceLedger",
                appVersion    = BuildConfig.VERSION_NAME,
                schemaVersion = database.openHelper.readableDatabase.version,
                exportedAtIso = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            ),
            accounts     = accounts.map     { it.toExport() },
            categories   = categories.map   { it.toExport() },
            budgets      = budgets.map      { it.toExport() },
            recurring    = recurring.map    { it.toExport() },
            templates    = templates.map    { it.toExport() },
            settings     = settingsExport,
            transactions = transactions.map { it.toExport() }
        )

        val jsonString = json.encodeToString(ExportEnvelope.serializer(), envelope)

        contentResolver.openOutputStream(uri)?.use { os ->
            OutputStreamWriter(os).use { writer ->
                writer.write(jsonString)
            }
        } ?: error("Unable to open output stream for JSON export")

        // Query file size via ContentResolver for accurate byte count
        val fileSize = runCatching {
            contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.statSize
            } ?: jsonString.toByteArray(Charsets.UTF_8).size.toLong()
        }.getOrDefault(jsonString.toByteArray(Charsets.UTF_8).size.toLong())

        val resolvedName = fileName
            ?: contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { c -> if (c.moveToFirst()) c.getString(0) else "backup.json" }
            ?: "backup.json"

        return ExportResult(fileSizeBytes = fileSize, fileName = resolvedName)
    }

    // ── CSV EXPORT ────────────────────────────────────────────────────────────

    /**
     * Human-readable CSV export of all transactions.
     *
     * Columns:
     *   date, type, amount, category, fromAccount, toAccount, note,
     *   categoryId, fromAccountId, toAccountId, createdAtEpoch, id
     *
     * createdAtEpoch is included so that CSV round-trips preserve original
     * transaction timestamps on import.
     */
    private suspend fun exportCsv(uri: Uri): ExportResult {
        val transactions = database.transactionDao().getAllOnce()
        val accountMap   = database.accountDao().getAllOnce().associate { it.id to it.name }
        val categoryMap  = database.categoryDao().getAllOnce().associate { it.id to it.name }

        var byteCount = 0L

        contentResolver.openOutputStream(uri)?.use { os ->
            OutputStreamWriter(os).use { w ->
                val header = "date,type,amount,category,fromAccount,toAccount,note," +
                        "categoryId,fromAccountId,toAccountId,createdAtEpoch,id\n"
                w.write(header)
                byteCount += header.toByteArray(Charsets.UTF_8).size

                transactions.forEach { t ->
                    val line = listOf(
                        t.date.toString(),
                        t.type,
                        t.amount.toPlainString(),
                        t.categoryId?.let { categoryMap[it] } ?: "",
                        t.fromAccountId?.let { accountMap[it] } ?: "",
                        t.toAccountId?.let { accountMap[it] } ?: "",
                        (t.note ?: "").replace("\n", " "),
                        t.categoryId    ?: "",
                        t.fromAccountId ?: "",
                        t.toAccountId   ?: "",
                        t.createdAt.epochSecond.toString(),   // ← added back
                        t.id
                    ).joinToString(",") { escapeCsv(it) } + "\n"
                    w.write(line)
                    byteCount += line.toByteArray(Charsets.UTF_8).size
                }
            }
        } ?: error("Unable to open output stream for CSV export")

        val resolvedName = contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { c -> if (c.moveToFirst()) c.getString(0) else "transactions.csv" } ?: "transactions.csv"

        return ExportResult(fileSizeBytes = byteCount, fileName = resolvedName)
    }

    // ── UNIFIED CSV EXPORT ────────────────────────────────────────────────────

    /**
     * Exports all transactions in the unified TraceLedger CSV format.
     * Account and category names are written as human-readable strings.
     * This file can be re-imported via the CSV import flow.
     */
    suspend fun exportUnifiedCsv(uri: Uri): ExportResult {
        val transactions = database.transactionDao().getAllOnce()
        val accountMap   = database.accountDao().getAllOnce().associate { it.id to it }
        val categoryMap  = database.categoryDao().getAllOnce().associate { it.id to it.name }

        var byteCount = 0L

        contentResolver.openOutputStream(uri)?.use { os ->
            OutputStreamWriter(os).use { w ->
                val header = UnifiedCsvSchema.HEADER + "\n"
                w.write(header)
                byteCount += header.toByteArray(Charsets.UTF_8).size

                transactions.forEach { t ->
                    val fromAccount = t.fromAccountId?.let { accountMap[it] }
                    val toAccount   = t.toAccountId?.let   { accountMap[it] }
                    val catName     = t.categoryId?.let    { categoryMap[it] } ?: ""

                    val line = listOf(
                        t.date.toString(),
                        t.type,
                        t.amount.toPlainString(),
                        fromAccount?.name ?: "",
                        fromAccount?.type ?: "BANK",
                        toAccount?.name ?: "",
                        toAccount?.type ?: "",
                        catName,
                        (t.note ?: "").replace("\n", " ")
                    ).joinToString(",") { escapeCsv(it) } + "\n"

                    w.write(line)
                    byteCount += line.toByteArray(Charsets.UTF_8).size
                }
            }
        } ?: error("Unable to open output stream for unified CSV export")

        val resolvedName = contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { c -> if (c.moveToFirst()) c.getString(0) else "traceledger-export.csv" }
            ?: "traceledger-export.csv"

        return ExportResult(fileSizeBytes = byteCount, fileName = resolvedName)
    }

    /**
     * Writes the CSV import template to [uri].
     * Template contains the header + commented instructions + example rows.
     * User fills it in and imports via the CSV import flow.
     */
    fun exportTemplate(uri: Uri): ExportResult {
        val bytes = UnifiedCsvSchema.TEMPLATE.toByteArray(Charsets.UTF_8)
        contentResolver.openOutputStream(uri)?.use { os ->
            os.write(bytes)
        } ?: error("Unable to open output stream for template")

        return ExportResult(
            fileSizeBytes = bytes.size.toLong(),
            fileName      = "traceledger-import-template.csv"
        )
    }

    // ── CSV field escaping ────────────────────────────────────────────────────

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }
}