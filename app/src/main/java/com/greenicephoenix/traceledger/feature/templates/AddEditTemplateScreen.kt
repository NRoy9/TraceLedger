package com.greenicephoenix.traceledger.feature.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.ui.components.TLEditorTopBar
import com.greenicephoenix.traceledger.domain.model.AccountUiModel
import com.greenicephoenix.traceledger.domain.model.CategoryType
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import com.greenicephoenix.traceledger.domain.model.TransactionType
import com.greenicephoenix.traceledger.feature.addtransaction.AccountSelector
import com.greenicephoenix.traceledger.feature.addtransaction.CategorySelector
import com.greenicephoenix.traceledger.feature.addtransaction.TransactionTypeSelector
import com.greenicephoenix.traceledger.feature.templates.domain.TransactionTemplateUiModel
import java.util.UUID

/**
 * Add / Edit Template screen.
 *
 * Visual style matches AddEditRecurringScreen:
 *  - TLEditorTopBar (close + title + save checkmark)
 *  - Floating dark card with scrollable form content
 *
 * Functional differences from AddTransactionScreen:
 *  - Template Name field (required)
 *  - Amount is optional
 *  - No date field
 *  - No delete button (deletion handled from TemplatesScreen)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTemplateScreen(
    existingTemplate: TransactionTemplateUiModel?,
    accounts: List<AccountUiModel>,
    categories: List<CategoryUiModel>,
    onSave: (TransactionTemplateUiModel) -> Unit,
    onCancel: () -> Unit
) {
    val isEdit = existingTemplate != null

    // ── Form state ────────────────────────────────────────────────────────────
    var name          by remember { mutableStateOf(existingTemplate?.name ?: "") }
    var type          by remember { mutableStateOf(existingTemplate?.type ?: TransactionType.EXPENSE) }
    var amount        by remember { mutableStateOf(existingTemplate?.amount?.toPlainString() ?: "") }
    var fromAccountId by remember { mutableStateOf(existingTemplate?.fromAccountId) }
    var toAccountId   by remember { mutableStateOf(existingTemplate?.toAccountId) }
    var categoryId    by remember { mutableStateOf(existingTemplate?.categoryId) }
    var notes         by remember { mutableStateOf(existingTemplate?.notes ?: "") }
    var nameError     by remember { mutableStateOf(false) }

    val canSave = name.isNotBlank()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // ── Header ────────────────────────────────────────────────────────────
        TLEditorTopBar(
            title   = if (isEdit) "Edit Template" else "New Template",
            canSave = canSave,
            onClose = onCancel,
            onSave  = {
                if (name.isBlank()) { nameError = true; return@TLEditorTopBar }
                onSave(
                    TransactionTemplateUiModel(
                        id            = existingTemplate?.id ?: UUID.randomUUID().toString(),
                        name          = name.trim(),
                        type          = type,
                        amount        = amount.toBigDecimalOrNull(),
                        fromAccountId = fromAccountId,
                        toAccountId   = toAccountId,
                        categoryId    = categoryId,
                        notes         = notes.takeIf { it.isNotBlank() }
                    )
                )
            }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        )

        // ── Form card — matches AddEditRecurringScreen floating card style ────
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape  = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier            = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Template name — required
                OutlinedTextField(
                    value          = name,
                    onValueChange  = { name = it; nameError = false },
                    label          = { Text("Template Name *") },
                    placeholder    = { Text("e.g. Monthly Rent") },
                    isError        = nameError,
                    supportingText = if (nameError) {{ Text("Name is required") }} else null,
                    modifier       = Modifier.fillMaxWidth(),
                    singleLine     = true
                )

                // Transaction type selector — reuses AddTransactionScreen component
                TransactionTypeSelector(
                    selected   = type,
                    onSelected = { newType ->
                        type = newType
                        // Clear fields that don't apply to the new type
                        when (newType) {
                            TransactionType.EXPENSE    -> toAccountId = null
                            TransactionType.INCOME     -> fromAccountId = null
                            TransactionType.TRANSFER   -> categoryId = null
                            TransactionType.INVESTMENT -> toAccountId = null
                        }
                    }
                )

                // Amount — optional for templates
                OutlinedTextField(
                    value         = amount,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("""\d+(\.\d{0,2})?""")))
                            amount = it
                    },
                    label         = { Text("Amount (optional)") },
                    placeholder   = { Text("Leave blank to enter each time") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Category — shown for EXPENSE, INCOME, INVESTMENT
                if (type != TransactionType.TRANSFER) {
                    CategorySelector(
                        categories         = categories,
                        type               = when (type) {
                            TransactionType.EXPENSE    -> CategoryType.EXPENSE
                            TransactionType.INVESTMENT -> CategoryType.INVESTMENT
                            else                       -> CategoryType.INCOME
                        },
                        selectedCategoryId = categoryId,
                        onSelect           = { categoryId = it }
                    )
                }

                // From account — EXPENSE and TRANSFER
                if (type == TransactionType.EXPENSE || type == TransactionType.TRANSFER ||
                    type == TransactionType.INVESTMENT) {
                    AccountSelector(
                        label             = if (type == TransactionType.TRANSFER) "From Account" else "Account",
                        accounts          = accounts,
                        selectedAccountId = fromAccountId,
                        onSelect          = { fromAccountId = it }
                    )
                }

                // To account — INCOME and TRANSFER
                if (type == TransactionType.INCOME || type == TransactionType.TRANSFER) {
                    AccountSelector(
                        label             = if (type == TransactionType.TRANSFER) "To Account" else "Account",
                        accounts          = accounts,
                        selectedAccountId = toAccountId,
                        onSelect          = { toAccountId = it }
                    )
                }

                // Notes
                OutlinedTextField(
                    value         = notes,
                    onValueChange = { notes = it },
                    label         = { Text("Notes (optional)") },
                    modifier      = Modifier.fillMaxWidth(),
                    minLines      = 2,
                    maxLines      = 4
                )
            }
        }
    }
}