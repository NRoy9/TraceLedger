package com.greenicephoenix.traceledger.core.importer

/**
 * Unified CSV schema for TraceLedger import/export.
 *
 * A single transaction-centric file. Accounts and categories are
 * derived from the transaction rows — no separate declaration needed.
 *
 * Column spec:
 *   date             — yyyy-MM-dd (required)
 *   type             — EXPENSE / INCOME / TRANSFER / INVESTMENT (case-insensitive, required)
 *   amount           — positive decimal, e.g. 500 or 1250.50 (required)
 *   from_account     — account name (required)
 *   from_account_type— BANK / WALLET / CASH / CREDIT_CARD (blank = BANK)
 *   to_account       — destination account name (required for TRANSFER, blank otherwise)
 *   to_account_type  — account type for to_account (blank = BANK)
 *   category         — category name (blank allowed for TRANSFER)
 *   note             — free text (optional)
 */
object UnifiedCsvSchema {

    val HEADER = "date,type,amount,from_account,from_account_type,to_account,to_account_type,category,note"

    // Column indices
    const val COL_DATE              = 0
    const val COL_TYPE              = 1
    const val COL_AMOUNT            = 2
    const val COL_FROM_ACCOUNT      = 3
    const val COL_FROM_ACCOUNT_TYPE = 4
    const val COL_TO_ACCOUNT        = 5
    const val COL_TO_ACCOUNT_TYPE   = 6
    const val COL_CATEGORY          = 7
    const val COL_NOTE              = 8

    const val MIN_COLUMNS = 9

    /**
     * Template CSV with header + commented instructions + example rows.
     * Downloaded by the user and filled in manually.
     *
     * Note: CSV does not support comments — example rows are clearly marked
     * with a EXAMPLE_ prefix in the date column so the parser can skip them.
     * Users delete example rows before importing.
     */
    val TEMPLATE = buildString {
        appendLine("# TraceLedger Import Template")
        appendLine("# Instructions:")
        appendLine("#   date            : yyyy-MM-dd format  e.g. 2026-05-01")
        appendLine("#   type            : EXPENSE / INCOME / TRANSFER / INVESTMENT")
        appendLine("#   amount          : positive number     e.g. 500 or 1250.50")
        appendLine("#   from_account    : your account name   e.g. HDFC Savings")
        appendLine("#   from_account_type: BANK / WALLET / CASH / CREDIT_CARD  (blank = BANK)")
        appendLine("#   to_account      : only for TRANSFER   e.g. Paytm")
        appendLine("#   to_account_type : only for TRANSFER   e.g. WALLET")
        appendLine("#   category        : expense/income category name")
        appendLine("#   note            : optional note")
        appendLine("#")
        appendLine("# Delete these comment lines and example rows before importing.")
        appendLine("# Accounts and categories are created automatically from your data.")
        appendLine("# Balances for new accounts are calculated from transactions —")
        appendLine("# please verify balances in the Accounts screen after import.")
        appendLine("#")
        appendLine(HEADER)
        // Example rows
        appendLine("2026-05-01,EXPENSE,500,HDFC Savings,BANK,,Food,Lunch")
        appendLine("2026-05-31,INCOME,50000,HDFC Savings,BANK,,Salary,May salary")
        appendLine("2026-05-01,TRANSFER,1000,HDFC Savings,BANK,Paytm Wallet,WALLET,,Transfer to wallet")
        appendLine("2026-05-03,EXPENSE,700,HDFC Savings,BANK,,Transport,Uber ride")
        appendLine("2026-05-05,EXPENSE,500,ICICI Credit,CREDIT_CARD,,Shopping,Amazon")
        append("2026-05-10,INVESTMENT,5000,HDFC Savings,BANK,,Stocks,Zerodha SIP")
    }
}