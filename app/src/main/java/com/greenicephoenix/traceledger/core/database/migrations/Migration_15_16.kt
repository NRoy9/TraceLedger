package com.greenicephoenix.traceledger.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1.5.0 migration: adds lastFourDigits column to accounts table.
 * NULL default means existing accounts are unaffected.
 */
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE accounts ADD COLUMN lastFourDigits TEXT DEFAULT NULL"
        )
    }
}