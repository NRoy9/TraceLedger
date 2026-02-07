package com.greenicephoenix.traceledger.core.export

import android.content.ContentResolver
import android.net.Uri
import com.greenicephoenix.traceledger.BuildConfig
import com.greenicephoenix.traceledger.core.database.TraceLedgerDatabase
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.time.Instant
import java.time.format.DateTimeFormatter

enum class ExportFormat { JSON, CSV }

class ExportService(
    private val database: TraceLedgerDatabase,
    private val contentResolver: ContentResolver
) {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = false
    }

    suspend fun export(format: ExportFormat, uri: Uri) {
        when (format) {
            ExportFormat.JSON -> exportJson(uri)
            ExportFormat.CSV -> exportCsv(uri)
        }
    }

    private suspend fun exportJson(uri: Uri) {
        val accounts = database.accountDao().getAllOnce()
        val categories = database.categoryDao().getAllOnce()
        val budgets = database.budgetDao().getAllOnce()
        val transactions = database.transactionDao().getAllOnce()

        val envelope = ExportEnvelope(
            meta = ExportMeta(
                app = "TraceLedger",
                appVersion = BuildConfig.VERSION_NAME,
                schemaVersion = database.openHelper.readableDatabase.version,
                exportedAtIso = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            ),
            accounts = accounts.map { it.toExport() },
            categories = categories.map { it.toExport() },
            budgets = budgets.map { it.toExport() },
            transactions = transactions.map { it.toExport() }
        )

        contentResolver.openOutputStream(uri)?.use { os ->
            OutputStreamWriter(os).use { writer ->
                writer.write(
                    json.encodeToString(
                        ExportEnvelope.serializer(),
                        envelope
                    )
                )
            }
        } ?: error("Unable to open output stream")
    }

    private suspend fun exportCsv(uri: Uri) {
        val transactions = database.transactionDao().getAllOnce()

        contentResolver.openOutputStream(uri)?.use { os ->
            OutputStreamWriter(os).use { w ->
                // header
                w.appendLine(
                    "id,type,amount,date,fromAccountId,toAccountId,categoryId,note,createdAt"
                )

                transactions.forEach { t ->
                    w.appendLine(
                        listOf(
                            t.id,
                            t.type,
                            t.amount.toPlainString(),
                            t.date.toString(),
                            t.fromAccountId ?: "",
                            t.toAccountId ?: "",
                            t.categoryId ?: "",
                            (t.note ?: "").replace("\n", " "),
                            t.createdAt.epochSecond.toString()
                        ).joinToString(",") { escapeCsv(it) }
                    )
                }
            }
        } ?: error("Unable to open output stream")
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"")
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }
}