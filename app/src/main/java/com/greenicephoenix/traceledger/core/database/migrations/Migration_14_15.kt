package com.greenicephoenix.traceledger.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 14 → 15 — Investment transaction type support.
 *
 * No schema column changes needed — TransactionType and CategoryType are
 * stored as strings so INVESTMENT is valid without ALTER TABLE.
 *
 * Seeds 9 default investment categories. INSERT OR IGNORE is idempotent.
 */
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // id, name, color (signed Long), icon
        listOf(
            listOf("inv_stocks",       "Stocks",        4294551589L, "stocks"),
            listOf("inv_mutual_funds", "Mutual Funds",  4294278935L, "mutual_funds"),
            listOf("inv_fd",           "Fixed Deposit", 4293284096L, "fd"),
            listOf("inv_crypto",       "Crypto",        4285353025L, "crypto"),
            listOf("inv_gold",         "Gold",          4294947584L, "gold"),
            listOf("inv_ppf",          "PPF",           4283796271L, "ppf"),
            listOf("inv_nps",          "NPS",           4278223759L, "nps"),
            listOf("inv_real_estate",  "Real Estate",   4282722208L, "real_estate"),
            listOf("inv_other",        "Other",         4283723386L, "other"),
        ).forEach { cat ->
            db.execSQL(
                "INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES (?, ?, 'INVESTMENT', ?, ?)",
                cat.toTypedArray()
            )
        }
    }
}