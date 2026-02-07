package com.greenicephoenix.traceledger.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("PRAGMA foreign_keys=OFF")

        /* ---------- TRANSACTIONS ---------- */
        db.execSQL("""
            CREATE TABLE transactions_new (
                id TEXT NOT NULL PRIMARY KEY,
                type TEXT NOT NULL,
                amount TEXT NOT NULL,
                date TEXT NOT NULL,
                fromAccountId TEXT,
                toAccountId TEXT,
                categoryId TEXT,
                note TEXT,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE RESTRICT
            )
        """)

        db.execSQL("""
            INSERT INTO transactions_new
            SELECT id, type, amount, date, fromAccountId, toAccountId, categoryId, note, createdAt
            FROM transactions
        """)

        db.execSQL("DROP TABLE transactions")
        db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

        // 🔑 RECREATE ALL INDICES (THIS WAS MISSING)
        db.execSQL("""
            CREATE INDEX index_transactions_categoryId
            ON transactions(categoryId)
        """)

        db.execSQL("""
            CREATE INDEX index_transactions_fromAccountId
            ON transactions(fromAccountId)
        """)

        db.execSQL("""
            CREATE INDEX index_transactions_toAccountId
            ON transactions(toAccountId)
        """)

        /* ---------- BUDGETS ---------- */
        db.execSQL("""
            CREATE TABLE budgets_new (
                id TEXT NOT NULL PRIMARY KEY,
                categoryId TEXT NOT NULL,
                limitAmount TEXT NOT NULL,
                month TEXT NOT NULL,
                isActive INTEGER NOT NULL,
                FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE RESTRICT
            )
        """)

        db.execSQL("""
            INSERT INTO budgets_new
            SELECT id, categoryId, limitAmount, month, isActive
            FROM budgets
        """)

        db.execSQL("DROP TABLE budgets")
        db.execSQL("ALTER TABLE budgets_new RENAME TO budgets")

        // 🔑 RECREATE INDICES
        db.execSQL("""
            CREATE UNIQUE INDEX index_budgets_categoryId_month
            ON budgets(categoryId, month)
        """)

        db.execSQL("""
            CREATE INDEX index_budgets_categoryId
            ON budgets(categoryId)
        """)

        db.execSQL("PRAGMA foreign_keys=ON")
    }
}