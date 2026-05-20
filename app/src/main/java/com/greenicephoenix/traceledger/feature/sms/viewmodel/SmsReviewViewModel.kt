package com.greenicephoenix.traceledger.feature.sms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenicephoenix.traceledger.core.database.entity.SmsPendingTransactionEntity
import com.greenicephoenix.traceledger.feature.sms.repository.SmsQueueRepository
import com.greenicephoenix.traceledger.feature.sms.store.SmsLearningStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SmsReviewViewModel(
    private val repository: SmsQueueRepository,
    private val learningStore: SmsLearningStore,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ── Pending items from DB ─────────────────────────────────────────────────
    // We keep a separate in-memory skip queue so "skip" reorders without a DB write.
    private val _dbItems: StateFlow<List<SmsPendingTransactionEntity>> =
        repository.observePending()
            .onEach { _isLoading.value = false }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // IDs that the user has skipped this session — appended to end of list
    private val _skippedIds = MutableStateFlow<List<Long>>(emptyList())

    // Final ordered list: non-skipped first, then skipped items at the end
    val pendingItems: StateFlow<List<SmsPendingTransactionEntity>> =
        combine(_dbItems, _skippedIds) { items, skipped ->
            val skippedSet = skipped.toSet()
            val normal  = items.filter { it.id !in skippedSet }
            val skippedItems = skipped.mapNotNull { id -> items.find { it.id == id } }
            normal + skippedItems
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val lastSavedDescription: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Accept a pending SMS transaction.
     * @param note       User-written note. Blank string = no note stored.
     * @param dateOverride Unix ms to override parsed date. 0L = use parsedDate as-is.
     */
    fun acceptTransaction(
        item:         SmsPendingTransactionEntity,
        accountId:    String,
        categoryId:   String?,
        note:         String = "",
        dateOverride: Long   = 0L,
    ) {
        if (accountId.isBlank()) return
        if (categoryId == null) return

        viewModelScope.launch {
            // Learn from corrections for future auto-suggestions
            if (accountId != item.suggestedAccountId) {
                learningStore.learnAccountForSender(item.sender, accountId)
            }
            if (categoryId != item.suggestedCategoryId) {
                learningStore.learnCategoryForDescription(item.parsedDescription, categoryId)
            }

            // Remove from skip queue if it was skipped
            _skippedIds.value = _skippedIds.value.filter { it != item.id }

            repository.acceptTransaction(
                pendingId   = item.id,
                accountId   = accountId,
                categoryId  = categoryId,
                amount      = item.parsedAmount,
                // Use user's note if provided, otherwise empty (NOT parsedDescription)
                description = note.trim(),
                dateMsEpoch = if (dateOverride > 0L) dateOverride else item.parsedDate,
                type        = item.parsedType,
            )
            _snackbarMessage.value = "Transaction saved"
        }
    }

    fun rejectTransaction(item: SmsPendingTransactionEntity) {
        _skippedIds.value = _skippedIds.value.filter { it != item.id }
        viewModelScope.launch { repository.rejectTransaction(item.id) }
    }

    fun rejectAll() {
        _skippedIds.value = emptyList()
        viewModelScope.launch { repository.rejectAll() }
    }

    /**
     * Push item to the end of the review queue for this session.
     * No DB write — purely in-memory reorder.
     */
    fun skipTransaction(item: SmsPendingTransactionEntity) {
        val current = _skippedIds.value
        if (item.id !in current) {
            _skippedIds.value = current + item.id
        }
    }

    fun clearSavedMessage() {
        _snackbarMessage.value = null
    }
}