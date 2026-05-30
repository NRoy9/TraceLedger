package com.greenicephoenix.traceledger.feature.addtransaction

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.domain.model.TransactionType
import com.greenicephoenix.traceledger.domain.model.CategoryType
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.greenicephoenix.traceledger.domain.model.AccountUiModel
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import androidx.compose.runtime.*
import androidx.compose.ui.draw.rotate
import com.greenicephoenix.traceledger.feature.categories.CategoryIcons
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import java.time.LocalDate
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.TextStyle
import com.greenicephoenix.traceledger.core.currency.CurrencyFormatter
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import com.greenicephoenix.traceledger.core.ui.theme.SuccessGreen
import com.greenicephoenix.traceledger.feature.templates.domain.TransactionTemplateUiModel
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    state: AddTransactionState,
    accounts: List<AccountUiModel>,
    categories: List<CategoryUiModel>,
    templates: List<TransactionTemplateUiModel> = emptyList(),
    isEditMode: Boolean,
    onEvent: (AddTransactionEvent) -> Unit,
    onCancel: () -> Unit
) {

    var showDeleteConfirm by remember { mutableStateOf(false) }

    var showTemplatePicker     by remember { mutableStateOf(false) }
    var showSaveAsTemplateDialog by remember { mutableStateOf(false) }
    var templateNameInput      by remember { mutableStateOf("") }

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()   // shifts content up when keyboard appears
        ) {
            // ───────── HEADER ─────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onBackground)
                }

                Text(
                    text = if (isEditMode) "Edit Transaction" else "Add Transaction",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(
                    onClick = { onEvent(AddTransactionEvent.Save) },
                    enabled = state.canSave
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = if (state.canSave)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            )

            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp)
            ) {
                TransactionForm(
                    state         = state,
                    accounts      = accounts,
                    categories    = categories,
                    templates     = templates,        // ADD
                    isEditMode    = isEditMode,       // ADD (pass through)
                    onEvent       = onEvent,
                    onDeleteClick = { showDeleteConfirm = true },
                    onUseTemplate = { showTemplatePicker = true },          // ADD
                    onSaveAsTemplate = { showSaveAsTemplateDialog = true }  // ADD
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Delete transaction?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This action cannot be undone.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onEvent(AddTransactionEvent.Delete)
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = NothingRed
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false }
                ) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // ── Template Picker Sheet ─────────────────────────────────────────────────
    if (showTemplatePicker && templates.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showTemplatePicker = false }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text     = "USE TEMPLATE",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                templates.forEach { template ->
                    val typeColor = when (template.type) {
                        TransactionType.EXPENSE  -> NothingRed
                        TransactionType.INCOME   -> SuccessGreen
                        TransactionType.TRANSFER ->
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        TransactionType.INVESTMENT ->
                            Color(0xFFFFB300) // gold
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onEvent(AddTransactionEvent.ApplyTemplate(template))
                                showTemplatePicker = false
                            }
                            .padding(vertical = 14.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(typeColor, RoundedCornerShape(50))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = template.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            template.amount?.let {
                                val currency by CurrencyManager.currency.collectAsState()
                                Text(
                                    text  = CurrencyFormatter.format(it.toPlainString(), currency),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = typeColor
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                }
            }
        }
    }

// ── Save as Template Dialog ───────────────────────────────────────────────
    if (showSaveAsTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showSaveAsTemplateDialog = false; templateNameInput = "" },
            title = { Text("Save as Template", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                OutlinedTextField(
                    value         = templateNameInput,
                    onValueChange = { templateNameInput = it },
                    label         = { Text("Template Name") },
                    placeholder   = { Text("e.g. Monthly Rent") },
                    singleLine    = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick  = {
                        if (templateNameInput.isNotBlank()) {
                            onEvent(AddTransactionEvent.SaveAsTemplate(templateNameInput))
                            showSaveAsTemplateDialog = false
                            templateNameInput = ""
                        }
                    },
                    enabled = templateNameInput.isNotBlank()
                ) {
                    Text("Save", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSaveAsTemplateDialog = false
                    templateNameInput = ""
                }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun TransactionForm(
    state: AddTransactionState,
    accounts: List<AccountUiModel>,
    categories: List<CategoryUiModel>,
    templates: List<TransactionTemplateUiModel>,
    isEditMode: Boolean,
    onEvent: (AddTransactionEvent) -> Unit,
    onDeleteClick: () -> Unit,
    onUseTemplate: () -> Unit,
    onSaveAsTemplate: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // "Use Template" button — only shown in add mode when templates exist
        if (!isEditMode && templates.isNotEmpty()) {
            item {
                TextButton(
                    onClick  = onUseTemplate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector        = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp),
                        tint               = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = "Use Template",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        item {
            TransactionTypeSelector(
                selected = state.type,
                onSelected = { onEvent(AddTransactionEvent.ChangeType(it)) }
            )
        }

        item {
            FormSection {
                AmountInput(
                    rawValue     = state.amount,
                    transactionType = state.type,
                    onChange     = {
                        onEvent(AddTransactionEvent.ChangeAmount(it))
                    }
                )
            }
        }

        item {
            AnimatedContent(
                targetState = state.type,
                transitionSpec = {
                    fadeIn(animationSpec = tween(120)) togetherWith
                            fadeOut(animationSpec = tween(120))
                },
                label = "TransactionTypeContent"
            ) { type ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (type) {

                        TransactionType.EXPENSE -> {
                            FormSection {
                                AccountSelector(
                                    label = "From Account",
                                    accounts = accounts,
                                    selectedAccountId = state.fromAccountId,
                                    onSelect = {
                                        onEvent(AddTransactionEvent.SelectFromAccount(it))
                                    }
                                )
                            }

                            FormSection {
                                CategorySelector(
                                    categories = categories,
                                    type = CategoryType.EXPENSE,
                                    selectedCategoryId = state.categoryId,
                                    onSelect = {
                                        onEvent(AddTransactionEvent.SelectCategory(it))
                                    }
                                )
                            }
                        }

                        TransactionType.INCOME -> {
                            FormSection {
                                AccountSelector(
                                    label = "To Account",
                                    accounts = accounts,
                                    selectedAccountId = state.toAccountId,
                                    onSelect = {
                                        onEvent(AddTransactionEvent.SelectToAccount(it))
                                    }
                                )
                            }

                            FormSection {
                                CategorySelector(
                                    categories = categories,
                                    type = CategoryType.INCOME,
                                    selectedCategoryId = state.categoryId,
                                    onSelect = {
                                        onEvent(AddTransactionEvent.SelectCategory(it))
                                    }
                                )
                            }
                        }

                        TransactionType.INVESTMENT -> {
                            FormSection {
                                AccountSelector(
                                    label             = "From Account",
                                    accounts          = accounts,
                                    selectedAccountId = state.fromAccountId,
                                    onSelect          = {
                                        onEvent(AddTransactionEvent.SelectFromAccount(it))
                                    }
                                )
                            }
                            FormSection {
                                CategorySelector(
                                    categories         = categories,
                                    type               = CategoryType.INVESTMENT,
                                    selectedCategoryId = state.categoryId,
                                    onSelect           = {
                                        onEvent(AddTransactionEvent.SelectCategory(it))
                                    }
                                )
                            }
                        }

                        TransactionType.TRANSFER -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                FormSection {
                                    AccountSelector(
                                        label = "Source Account",
                                        accounts = accounts,
                                        selectedAccountId = state.fromAccountId,
                                        onSelect = {
                                            onEvent(AddTransactionEvent.SelectFromAccount(it))
                                        }
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp), // slightly more breathing room
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "↓",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                FormSection {
                                    AccountSelector(
                                        label = "Destination Account",
                                        accounts = accounts,
                                        selectedAccountId = state.toAccountId,
                                        onSelect = {
                                            onEvent(AddTransactionEvent.SelectToAccount(it))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            DateSelector(
                date = state.date,
                onSelect = { onEvent(AddTransactionEvent.ChangeDate(it)) }
            )
        }

        item {
            NotesField(
                value = state.notes,
                onChange = { onEvent(AddTransactionEvent.ChangeNotes(it)) }
            )
        }

        // Edit mode: DELETE + UPDATE side by side, equal weight
        item {
            if (state.isEditMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = onDeleteClick,
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = NothingRed),
                        border   = BorderStroke(1.dp, NothingRed.copy(alpha = 0.5f)),
                        shape    = RoundedCornerShape(14.dp)
                    ) {
                        Text("DELETE", style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
                    }
                    Button(
                        onClick  = { onEvent(AddTransactionEvent.Save) },
                        enabled  = state.canSave,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("UPDATE", style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // Add mode: Save as Template + SAVE
        if (!isEditMode) {
            item {
                TextButton(
                    onClick  = onSaveAsTemplate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.BookmarkBorder, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.width(6.dp))
                    Text("Save as Template", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick  = { onEvent(AddTransactionEvent.Save) },
                    enabled  = state.canSave,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("SAVE", style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TransactionTypeSelector(
    selected: TransactionType,
    onSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(22.dp)
            )
            .padding(4.dp)
    ) {
        TransactionType.entries.forEach { type ->
            val isSelected = type == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else Color.Transparent,
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { onSelected(type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (type) {
                        TransactionType.EXPENSE    -> "Expense"
                        TransactionType.INCOME     -> "Income"
                        TransactionType.TRANSFER   -> "Transfer"
                        TransactionType.INVESTMENT -> "Investment"
                    },
                    style    = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    color    = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun NotesField(value: String, onChange: (String) -> Unit) {
    // Collapsed if empty; expands when user taps "+ Add note"
    var expanded by remember { mutableStateOf(value.isNotEmpty()) }

    if (!expanded) {
        TextButton(
            onClick  = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector        = Icons.Default.Add,
                contentDescription = null,
                modifier           = Modifier.size(16.dp),
                tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text  = "Add note",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        }
    } else {
        OutlinedTextField(
            value         = value,
            onValueChange = onChange,
            label         = { Text("Notes") },
            modifier      = Modifier.fillMaxWidth(),
            trailingIcon  = if (value.isEmpty()) {{
                IconButton(onClick = { expanded = false }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Collapse notes",
                        modifier = Modifier.size(16.dp),
                        tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }} else null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelector(
    date: LocalDate,
    onSelect: (LocalDate) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.toEpochDay() * 86_400_000
    )
    val interactionSource = remember { MutableInteractionSource() }
    val today = LocalDate.now()
    val quickDates = listOf(
        "Today"     to today,
        "Yesterday" to today.minusDays(1),
        today.minusDays(2).format(java.time.format.DateTimeFormatter.ofPattern("EEE d")) to today.minusDays(2),
        today.minusDays(3).format(java.time.format.DateTimeFormatter.ofPattern("EEE d")) to today.minusDays(3),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            quickDates.forEach { (label, target) ->
                val isSelected = date == target
                FilterChip(
                    selected = isSelected,
                    onClick  = { onSelect(target) },
                    label    = {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedLabelColor     = MaterialTheme.colorScheme.primary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled             = true,
                        selected            = isSelected,
                        selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        borderColor         = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }
        }

        OutlinedTextField(
            value             = date.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")),
            onValueChange     = {},
            label             = { Text("Date") },
            readOnly          = true,
            interactionSource = interactionSource,
            modifier          = Modifier.fillMaxWidth(),
            trailingIcon      = {
                Icon(Icons.Default.DateRange, contentDescription = null)
            }
        )
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Release) {
                showPicker = true
            }
        }
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onSelect(
                                LocalDate.ofEpochDay(millis / 86_400_000)
                            )
                        }
                        showPicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelector(
    label: String,
    accounts: List<AccountUiModel>,
    selectedAccountId: String?,
    onSelect: (String) -> Unit
) {
    val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId }
    var showSheet by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val arrowRotation by animateFloatAsState(
        targetValue = if (showSheet) 180f else 0f,
        animationSpec = tween(
            durationMillis = 180,
            easing = FastOutSlowInEasing
        ),
        label = "arrowRotation"
    )


    OutlinedTextField(
        value = selectedAccount?.name ?: "",
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = selectedAccount?.let {
            {
                Icon(
                    imageVector = accountIcon(it.type),
                    contentDescription = null,
                    tint = Color(it.color),
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.rotate(arrowRotation),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    )

    // 🔑 THIS IS THE FIX
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Release) {
                showSheet = true
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                accounts.forEach { account ->
                    val isSelected = account.id == selectedAccountId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSelect(account.id)
                                showSheet = false
                            }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = accountIcon(account.type),
                            contentDescription = null,
                            tint = Color(account.color),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = account.name,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    categories: List<CategoryUiModel>,
    type: CategoryType,
    selectedCategoryId: String?,
    onSelect: (String) -> Unit
) {
    val filtered = remember(categories, type) {
        categories.filter { it.type == type }
    }

    val selectedCategory = filtered.firstOrNull { it.id == selectedCategoryId }
    var showSheet by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val arrowRotation by animateFloatAsState(
        targetValue = if (showSheet) 180f else 0f,
        animationSpec = tween(
            durationMillis = 180,
            easing = FastOutSlowInEasing
        ),
        label = "arrowRotation"
    )

    OutlinedTextField(
        value = selectedCategory?.name ?: "",
        onValueChange = {},
        label = { Text("Category") },
        readOnly = true,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = selectedCategory?.let {
            {
                Icon(
                    imageVector = CategoryIcons.iconFor(it.icon),
                    contentDescription = null,
                    tint = Color(it.color),
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.rotate(arrowRotation),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    )

    // 🔑 THIS IS THE FIX
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Release) {
                showSheet = true
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filtered.forEach { category ->
                    val isSelected = category.id == selectedCategoryId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSelect(category.id)
                                showSheet = false
                            }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = CategoryIcons.iconFor(category.icon),
                            contentDescription = null,
                            tint = Color(category.color),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = category.name,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                    }
                }
            }
        }
    }
}

private fun accountIcon(type: com.greenicephoenix.traceledger.domain.model.AccountType)
        : androidx.compose.ui.graphics.vector.ImageVector {

    return when (type) {
        com.greenicephoenix.traceledger.domain.model.AccountType.BANK ->
            Icons.Default.AccountBalance
        com.greenicephoenix.traceledger.domain.model.AccountType.CASH ->
            Icons.Default.Payments
        com.greenicephoenix.traceledger.domain.model.AccountType.CREDIT_CARD ->
            Icons.Default.CreditCard
        com.greenicephoenix.traceledger.domain.model.AccountType.WALLET ->
            Icons.Default.Wallet
    }
}

@Composable
private fun FormSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
private fun AmountInput(
    rawValue:        String,
    transactionType: TransactionType,
    onChange:        (String) -> Unit
) {
    val currency by CurrencyManager.currency.collectAsState()

    // Amount color matches transaction type
    val amountColor = when (transactionType) {
        TransactionType.EXPENSE    -> NothingRed
        TransactionType.INCOME     -> SuccessGreen
        TransactionType.TRANSFER   -> MaterialTheme.colorScheme.onSurface
        TransactionType.INVESTMENT -> Color(0xFFB8860B)
    }

    var textFieldValue by remember(rawValue) {
        mutableStateOf(
            androidx.compose.ui.text.input.TextFieldValue(
                text      = rawValue,
                selection = androidx.compose.ui.text.TextRange(rawValue.length)
            )
        )
    }

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value         = textFieldValue,
            onValueChange = { newValue ->
                val newText = newValue.text
                if (newText.isEmpty() || newText.matches(Regex("""\d+(\.\d{0,2})?"""))) {
                    textFieldValue = newValue.copy(
                        selection = androidx.compose.ui.text.TextRange(newText.length)
                    )
                    onChange(newText)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle       = TextStyle.Default.copy(color = Color.Transparent),
            cursorBrush     = SolidColor(amountColor),
            modifier        = Modifier.fillMaxWidth(),
            decorationBox   = {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isEmpty = textFieldValue.text.isEmpty()
                    Text(
                        text  = if (isEmpty) "0.00"
                        else CurrencyFormatter.format(textFieldValue.text, currency),
                        style = MaterialTheme.typography.displaySmall,
                        color = if (isEmpty) amountColor.copy(alpha = 0.3f) else amountColor
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(1.5.dp)
                            .background(amountColor.copy(alpha = if (isEmpty) 0.2f else 0.5f))
                    )
                }
            }
        )

        Spacer(Modifier.height(12.dp))

        // Quick-amount chips
        val quickAmounts = listOf("100", "500", "1000", "5000")
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickAmounts.forEach { amount ->
                val current = textFieldValue.text.toBigDecimalOrNull()
                val chip    = amount.toBigDecimal()
                SuggestionChip(
                    onClick = {
                        val newVal = (current?.add(chip) ?: chip).toPlainString()
                        textFieldValue = androidx.compose.ui.text.input.TextFieldValue(
                            text      = newVal,
                            selection = androidx.compose.ui.text.TextRange(newVal.length)
                        )
                        onChange(newVal)
                    },
                    label  = { Text("+$amount", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = amountColor.copy(alpha = 0.08f),
                        labelColor     = amountColor
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled         = true,
                        borderColor     = amountColor.copy(alpha = 0.2f),
                        borderWidth     = 1.dp
                    )
                )
            }
        }
    }
}