package com.greenicephoenix.traceledger.domain.model

sealed class AccountEditMode {
    object Add : AccountEditMode()
    data class Edit(val accountId: String) : AccountEditMode()
}
