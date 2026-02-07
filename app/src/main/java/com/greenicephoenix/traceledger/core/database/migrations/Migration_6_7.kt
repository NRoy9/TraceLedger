package com.greenicephoenix.traceledger.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("PRAGMA foreign_keys=OFF")

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
                FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE RESTRICT,
                FOREIGN KEY(fromAccountId) REFERENCES accounts(id) ON DELETE RESTRICT,
                FOREIGN KEY(toAccountId) REFERENCES accounts(id) ON DELETE RESTRICT
            )
        """)

        db.execSQL("""
            INSERT INTO transactions_new
            SELECT id, type, amount, date, fromAccountId, toAccountId, categoryId, note, createdAt
            FROM transactions
        """)

        db.execSQL("DROP TABLE transactions")
        db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

        // 🔑 RECREATE ALL INDICES
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

        db.execSQL("PRAGMA foreign_keys=ON")
    }
}