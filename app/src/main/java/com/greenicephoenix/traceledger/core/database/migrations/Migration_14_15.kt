package com.greenicephoenix.traceledger.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 14 → 15 — Investment transaction type + category overhaul.
 *
 * No schema column changes — TransactionType and CategoryType are stored as
 * strings, so INVESTMENT is valid without ALTER TABLE.
 *
 * What this migration does:
 *
 * 1. INSERT OR IGNORE new EXPENSE categories that did not exist in v1.3.0:
 *    exp_groceries, exp_fuel, exp_rent, exp_insurance, exp_subscription,
 *    exp_travel, exp_education, exp_gifts, exp_personal, exp_pets, exp_other
 *
 * 2. INSERT OR IGNORE new INCOME categories that did not exist in v1.3.0:
 *    inc_gift, inc_refund, inc_other
 *
 * 3. UPDATE existing categories whose colors changed in v1.4.0:
 *    exp_entertainment: 0xFF1E88E5 → 0xFF039BE5
 *    Note: inc_account_credit icon ("account_credit") is preserved as-is —
 *    the key is retained in CategoryIcons.kt as a legacy entry.
 *
 * 4. INSERT OR IGNORE all INVESTMENT categories.
 *    Uses the same IDs as CategorySeed.kt — inv_retirement replaces inv_ppf/inv_nps
 *    from the original Migration_14_15 draft.
 *
 * All INSERT OR IGNORE statements are idempotent — safe to run more than once.
 * UPDATE statements only touch rows that already exist.
 */
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // ── 1. NEW EXPENSE CATEGORIES ─────────────────────────────────────────
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('exp_groceries',    'Groceries',     'EXPENSE', ${0xFFD32F2F.toLong()}, 'groceries')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('exp_fuel',         'Fuel',          'EXPENSE', ${0xFF6D4C41.toLong()}, 'fuel')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('exp_rent',         'Rent',          'EXPENSE', ${0xFF7E57C2.toLong()}, 'rent')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('exp_insurance',    'Insurance',     'EXPENSE', ${0xFF1E88E5.toLong()}, 'insurance')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('exp_subscription', 'Subscriptions', 'EXPENSE', ${0xFF00897B.toLong()}, 'subscription')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('exp_travel',       'Travel',        'EXPENSE', ${0xFF00897B.toLong()}, 'travel')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('exp_education',    'Education',     'EXPENSE', ${0xFF6D4C41.toLong()}, 'education')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('exp_gifts',        'Gifts',         'EXPENSE', ${0xFFEC407A.toLong()}, 'gift')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('exp_personal',     'Personal Care', 'EXPENSE', ${0xFF8D6E63.toLong()}, 'personal_care')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('exp_pets',         'Pets',          'EXPENSE', ${0xFFFF7043.toLong()}, 'pets')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('exp_other',        'Other',         'EXPENSE', ${0xFF546E7A.toLong()}, 'other')")

        // ── 2. NEW INCOME CATEGORIES ──────────────────────────────────────────
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('inc_gift',   'Gift',   'INCOME', ${0xFF8E24AA.toLong()}, 'gift')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('inc_refund', 'Refund', 'INCOME', ${0xFF039BE5.toLong()}, 'refund')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('inc_other',  'Other',  'INCOME', ${0xFF546E7A.toLong()}, 'other')")

        // ── 3. UPDATE EXISTING CATEGORY COLORS ───────────────────────────────
        // exp_entertainment color changed from 0xFF1E88E5 → 0xFF039BE5
        db.execSQL("UPDATE categories SET color = ${0xFF039BE5.toLong()} WHERE id = 'exp_entertainment'")

        // ── 4. NEW INVESTMENT CATEGORIES ─────────────────────────────────────
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('inv_stocks',       'Stocks',        'INVESTMENT', ${0xFFF9A825.toLong()}, 'stocks')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('inv_mutual_funds', 'Mutual Funds',  'INVESTMENT', ${0xFFF57F17.toLong()}, 'mutual_funds')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('inv_fd',           'Fixed Deposit', 'INVESTMENT', ${0xFFE65100.toLong()}, 'fd')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('inv_gold',         'Gold',          'INVESTMENT', ${0xFFFFB300.toLong()}, 'gold')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('inv_crypto',       'Crypto',        'INVESTMENT', ${0xFF6D4C41.toLong()}, 'crypto')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('inv_real_estate',  'Real Estate',   'INVESTMENT', ${0xFF4527A0.toLong()}, 'real_estate')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('inv_retirement',   'Retirement',    'INVESTMENT', ${0xFF558B2F.toLong()}, 'retirement')")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, color, icon) VALUES ('inv_other',        'Other',         'INVESTMENT', ${0xFF546E7A.toLong()}, 'other')")
    }
}