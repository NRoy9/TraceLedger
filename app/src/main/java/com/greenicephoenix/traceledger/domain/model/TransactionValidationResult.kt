package com.greenicephoenix.traceledger.domain.model

/**
 * Result of validating a TransactionUiModel.
 */
sealed class TransactionValidationResult {
    object Valid : TransactionValidationResult()

    data class Invalid(
        val reason: Reason
    ) : TransactionValidationResult()

    enum class Reason {
        AMOUNT_NOT_POSITIVE,
        DATE_MISSING,
        FROM_ACCOUNT_MISSING,
        TO_ACCOUNT_MISSING,
        SAME_ACCOUNT_TRANSFER,
        CATEGORY_MISSING,
        CATEGORY_NOT_ALLOWED_FOR_TRANSFER,
        INVALID_ACCOUNT_COMBINATION
    }
}
