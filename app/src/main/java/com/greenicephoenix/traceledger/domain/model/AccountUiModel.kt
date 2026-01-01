package com.greenicephoenix.traceledger.domain.model

/**
 * UI representation of an account
 * (Later this will map to Room entities)
 */
data class AccountUiModel(
    val id: String,
    val name: String,
    val balance: String,
    val type: AccountType,
    val includeInTotal: Boolean,

    // Optional notes
    val details: String? = null,

    // UI-only visual attributes
    val color: Long, // stored as ARGB
)


