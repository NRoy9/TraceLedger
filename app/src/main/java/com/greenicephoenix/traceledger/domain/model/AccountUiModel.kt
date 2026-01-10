package com.greenicephoenix.traceledger.domain.model

import java.math.BigDecimal

data class AccountUiModel(
    val id: String,
    val name: String,
    val balance: BigDecimal,
    val type: AccountType,
    val includeInTotal: Boolean,
    val details: String? = null,
    val color: Long
)