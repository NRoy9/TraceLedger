package com.greenicephoenix.traceledger.feature.sms.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenicephoenix.traceledger.core.database.entity.SmsPendingTransactionEntity
import com.greenicephoenix.traceledger.domain.model.AccountUiModel
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import com.greenicephoenix.traceledger.feature.sms.viewmodel.SmsReviewViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── View mode enum ────────────────────────────────────────────────────────────

private enum class ReviewViewMode { CARD, TABLE, WIZARD }

// ── Root screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsReviewScreen(
    viewModel: SmsReviewViewModel,
    accounts: List<AccountUiModel>,
    categories: List<CategoryUiModel>,
    onNavigateBack: () -> Unit,
) {
    val isLoading            by viewModel.isLoading.collectAsState()
    val items                by viewModel.pendingItems.collectAsState()
    val lastSavedDescription by viewModel.lastSavedDescription.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(lastSavedDescription) {
        lastSavedDescription?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSavedMessage()
        }
    }

    var viewMode            by remember { mutableStateOf(ReviewViewMode.CARD) }
    var editingItem         by remember { mutableStateOf<SmsPendingTransactionEntity?>(null) }
    var showRejectAllDialog by remember { mutableStateOf(false) }

    // ── Reject-all dialog ─────────────────────────────────────────────────────
    if (showRejectAllDialog) {
        AlertDialog(
            onDismissRequest = { showRejectAllDialog = false },
            title  = { Text("Dismiss all?") },
            text   = { Text("Mark all ${items.size} pending transactions as rejected.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rejectAll()
                    showRejectAllDialog = false
                    onNavigateBack()
                }) { Text("Dismiss All", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showRejectAllDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ── Edit sheet (Card view) ────────────────────────────────────────────────
    editingItem?.let { item ->
        SmsEditSheet(
            item       = item,
            accounts   = accounts,
            categories = categories,
            onAccept   = { accountId, categoryId, note, dateMs ->
                viewModel.acceptTransaction(item, accountId, categoryId, note, dateMs)
                editingItem = null
            },
            onDismiss = { editingItem = null }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Text(
                            text  = "REVIEW TRANSACTIONS",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (items.isNotEmpty()) {
                            Text(
                                text          = "${items.size} PENDING",
                                style         = MaterialTheme.typography.labelSmall,
                                color         = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // View mode toggles — only shown when there are items
                    if (items.isNotEmpty()) {
                        // Card view
                        IconButton(onClick = { viewMode = ReviewViewMode.CARD }) {
                            Icon(
                                Icons.Default.ViewAgenda,
                                contentDescription = "Card view",
                                tint = if (viewMode == ReviewViewMode.CARD)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        // Table view
                        IconButton(onClick = { viewMode = ReviewViewMode.TABLE }) {
                            Icon(
                                Icons.Default.TableRows,
                                contentDescription = "Table view",
                                tint = if (viewMode == ReviewViewMode.TABLE)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        // Wizard view
                        IconButton(onClick = { viewMode = ReviewViewMode.WIZARD }) {
                            Icon(
                                Icons.Default.LinearScale,
                                contentDescription = "Wizard view",
                                tint = if (viewMode == ReviewViewMode.WIZARD)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        // Dismiss all
                        TextButton(onClick = { showRejectAllDialog = true }) {
                            Text(
                                "DISMISS",
                                color         = MaterialTheme.colorScheme.error,
                                style         = MaterialTheme.typography.labelMedium,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            when {
                isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                items.isEmpty() -> EmptyReviewState(onNavigateBack = onNavigateBack)

                else -> AnimatedContent(
                    targetState    = viewMode,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    },
                    label = "review-mode"
                ) { mode ->
                    when (mode) {
                        ReviewViewMode.CARD -> CardView(
                            items      = items,
                            accounts   = accounts,
                            categories = categories,
                            onAccept   = { item, accId, catId ->
                                viewModel.acceptTransaction(item, accId, catId)
                            },
                            onReject   = { viewModel.rejectTransaction(it) },
                            onEdit     = { editingItem = it }
                        )
                        ReviewViewMode.TABLE -> TableView(
                            items      = items,
                            accounts   = accounts,
                            categories = categories,
                            onAccept   = { item, accId, catId, note, dateMs ->
                                viewModel.acceptTransaction(item, accId, catId, note, dateMs)
                            },
                            onReject   = { viewModel.rejectTransaction(it) }
                        )
                        ReviewViewMode.WIZARD -> WizardView(
                            items      = items,
                            accounts   = accounts,
                            categories = categories,
                            onAccept   = { item, accId, catId, note, dateMs ->
                                viewModel.acceptTransaction(item, accId, catId, note, dateMs)
                            },
                            onReject   = { viewModel.rejectTransaction(it) },
                            onSkip     = { viewModel.skipTransaction(it) },
                            onDone     = onNavigateBack
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

// ── CARD VIEW ─────────────────────────────────────────────────────────────────

@Composable
private fun CardView(
    items:      List<SmsPendingTransactionEntity>,
    accounts:   List<AccountUiModel>,
    categories: List<CategoryUiModel>,
    onAccept:   (SmsPendingTransactionEntity, String, String?) -> Unit,
    onReject:   (SmsPendingTransactionEntity) -> Unit,
    onEdit:     (SmsPendingTransactionEntity) -> Unit,
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items = items, key = { it.id }) { item ->
            SmsReviewRow(
                item       = item,
                accounts   = accounts,
                categories = categories,
                onAccept   = { accId, catId -> onAccept(item, accId, catId) },
                onReject   = { onReject(item) },
                onEdit     = { onEdit(item) }
            )
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}

// ── TABLE VIEW ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableView(
    items:      List<SmsPendingTransactionEntity>,
    accounts:   List<AccountUiModel>,
    categories: List<CategoryUiModel>,
    onAccept:   (SmsPendingTransactionEntity, String, String?, String, Long) -> Unit,
    onReject:   (SmsPendingTransactionEntity) -> Unit,
) {
    // Per-row editable state — keyed by item.id
    // We keep this as a map so state survives recomposition as items are accepted/rejected
    val rowStates = remember(items) {
        items.associate { item ->
            item.id to TableRowState(
                accountId  = item.suggestedAccountId ?: "",
                categoryId = item.suggestedCategoryId,
                note       = "",                      // blank — user writes their own
                dateMs     = item.parsedDate
            )
        }.toMutableMap()
    }
    // Trigger recompose when a row state changes
    var stateVersion by remember { mutableIntStateOf(0) }

    // Column widths (fixed for horizontal scroll)
    val colDate    = 90.dp
    val colAccount = 110.dp
    val colCat     = 110.dp
    val colAmt     = 90.dp
    val colNote    = 140.dp
    val colActions = 80.dp

    // Single shared scroll state — header and all rows scroll together
    val sharedScrollState = rememberScrollState()

    val headerBg = MaterialTheme.colorScheme.surfaceVariant
    val divColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Sticky header ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .horizontalScroll(sharedScrollState)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("DATE",     colDate)
            TableHeaderCell("ACCOUNT",  colAccount)
            TableHeaderCell("CATEGORY", colCat)
            TableHeaderCell("AMOUNT",   colAmt)
            TableHeaderCell("NOTE",     colNote)
            TableHeaderCell("",         colActions)
        }
        HorizontalDivider(color = divColor)

        // ── Rows ──────────────────────────────────────────────────────────────
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(items = items, key = { it.id }) { item ->
                // Read current state for this row; stateVersion forces recompose on mutation
                @Suppress("UNUSED_EXPRESSION") stateVersion
                val state = rowStates[item.id] ?: TableRowState(
                    item.suggestedAccountId ?: "", item.suggestedCategoryId, "", item.parsedDate
                )

                TableRow(
                    item        = item,
                    state       = state,
                    accounts    = accounts,
                    categories  = categories,
                    scrollState = sharedScrollState,
                    colDate     = colDate,
                    colAccount  = colAccount,
                    colCat      = colCat,
                    colAmt      = colAmt,
                    colNote     = colNote,
                    colActions  = colActions,
                    divColor    = divColor,
                    onStateChange = { newState ->
                        rowStates[item.id] = newState
                        stateVersion++
                    },
                    onAccept = {
                        if (state.accountId.isNotBlank() && state.categoryId != null) {
                            onAccept(item, state.accountId, state.categoryId, state.note, state.dateMs)
                        }
                    },
                    onReject = { onReject(item) }
                )
            }
        }
    }
}

private data class TableRowState(
    val accountId:  String,
    val categoryId: String?,
    val note:       String,
    val dateMs:     Long,
)

@Composable
private fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text      = text,
        modifier  = Modifier.width(width).padding(horizontal = 4.dp),
        style     = MaterialTheme.typography.labelSmall,
        color     = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines  = 1,
        overflow  = TextOverflow.Clip,
        letterSpacing = 0.8.sp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableRow(
    item:          SmsPendingTransactionEntity,
    state:         TableRowState,
    accounts:      List<AccountUiModel>,
    categories:    List<CategoryUiModel>,
    scrollState:   androidx.compose.foundation.ScrollState,
    colDate:       androidx.compose.ui.unit.Dp,
    colAccount:    androidx.compose.ui.unit.Dp,
    colCat:        androidx.compose.ui.unit.Dp,
    colAmt:        androidx.compose.ui.unit.Dp,
    colNote:       androidx.compose.ui.unit.Dp,
    colActions:    androidx.compose.ui.unit.Dp,
    divColor:      Color,
    onStateChange: (TableRowState) -> Unit,
    onAccept:      () -> Unit,
    onReject:      () -> Unit,
) {
    val isExpense   = item.parsedType == "EXPENSE"
    val accentColor = if (isExpense) MaterialTheme.colorScheme.error else Color(0xFF27AE60)
    val isSaveable  = state.accountId.isNotBlank() && state.categoryId != null

    var showDatePicker    by remember { mutableStateOf(false) }
    var showAccountMenu   by remember { mutableStateOf(false) }
    var showCategoryMenu  by remember { mutableStateOf(false) }
    var smsExpanded       by remember { mutableStateOf(false) }

    val filteredCategories = categories.filter { it.type.name == item.parsedType }

    // Date picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dateMs
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onStateChange(state.copy(dateMs = it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Column {
        // ── Data row ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)   // shared — scrolls with header
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Spacer(Modifier.width(6.dp))

            // DATE cell — order 1
            Box(
                modifier = Modifier
                    .width(colDate)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Text(
                    text  = formatDate(state.dateMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ACCOUNT cell — order 2
            Box(modifier = Modifier.width(colAccount)) {
                val accName    = accounts.find { it.id == state.accountId }?.name
                val needsAccount = state.accountId.isBlank()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (needsAccount) MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                        .border(
                            width = if (needsAccount) 1.dp else 0.dp,
                            color = if (needsAccount) MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                            else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { showAccountMenu = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text     = accName ?: "Select…",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = if (needsAccount) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DropdownMenu(
                    expanded         = showAccountMenu,
                    onDismissRequest = { showAccountMenu = false }
                ) {
                    accounts.forEach { acc ->
                        DropdownMenuItem(
                            text    = { Text(acc.name, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                onStateChange(state.copy(accountId = acc.id))
                                showAccountMenu = false
                            },
                            leadingIcon = if (state.accountId == acc.id) ({
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            }) else null
                        )
                    }
                }
            }
            Spacer(Modifier.width(4.dp))

            // CATEGORY cell — order 3
            Box(modifier = Modifier.width(colCat)) {
                val catName  = categories.find { it.id == state.categoryId }?.name
                val needsCat = state.categoryId == null
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (needsCat) MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                        .border(
                            width = if (needsCat) 1.dp else 0.dp,
                            color = if (needsCat) MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                            else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { showCategoryMenu = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text     = catName ?: "Select…",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = if (needsCat) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DropdownMenu(
                    expanded         = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    filteredCategories.forEach { cat ->
                        DropdownMenuItem(
                            text    = { Text(cat.name, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                onStateChange(state.copy(categoryId = cat.id))
                                showCategoryMenu = false
                            },
                            leadingIcon = if (state.categoryId == cat.id) ({
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            }) else null
                        )
                    }
                }
            }
            Spacer(Modifier.width(4.dp))

            // AMOUNT cell — order 4
            Text(
                text       = "${if (isExpense) "−" else "+"}${formatAmount(item.parsedAmount)}",
                modifier   = Modifier.width(colAmt),
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color      = accentColor,
                textAlign  = TextAlign.End,
                maxLines   = 1
            )
            Spacer(Modifier.width(4.dp))

            // NOTE cell — order 5 (inline editable)
            BasicTextField(
                value         = state.note,
                onValueChange = { onStateChange(state.copy(note = it)) },
                modifier      = Modifier
                    .width(colNote)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                textStyle     = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine    = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                decorationBox = { inner ->
                    if (state.note.isEmpty()) {
                        Text(
                            "Add note…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }
                    inner()
                }
            )
            Spacer(Modifier.width(4.dp))

            // ACTION cells
            Row(
                modifier = Modifier.width(colActions),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save
                IconButton(
                    onClick  = onAccept,
                    enabled  = isSaveable,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Save",
                        modifier = Modifier.size(18.dp),
                        tint     = if (isSaveable) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    )
                }
                // Reject
                IconButton(
                    onClick  = onReject,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Reject",
                        modifier = Modifier.size(18.dp),
                        tint     = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
                // Expand SMS
                IconButton(
                    onClick  = { smsExpanded = !smsExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (smsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle SMS",
                        modifier = Modifier.size(16.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // ── Collapsible SMS body row ───────────────────────────────────────────
        AnimatedVisibility(
            visible = smsExpanded,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text  = item.smsBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }

        HorizontalDivider(color = divColor)
    }
}

// ── WIZARD VIEW ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WizardView(
    items:      List<SmsPendingTransactionEntity>,
    accounts:   List<AccountUiModel>,
    categories: List<CategoryUiModel>,
    onAccept:   (SmsPendingTransactionEntity, String, String?, String, Long) -> Unit,
    onReject:   (SmsPendingTransactionEntity) -> Unit,
    onSkip:     (SmsPendingTransactionEntity) -> Unit,
    onDone:     () -> Unit,
) {
    if (items.isEmpty()) {
        onDone()
        return
    }

    // Always show index 0 — the ViewModel reorders items when skip is called,
    // so index 0 is always the "current" transaction
    val item = items[0]
    val total = items.size

    val isExpense   = item.parsedType == "EXPENSE"
    val accentColor = if (isExpense) MaterialTheme.colorScheme.error else Color(0xFF27AE60)
    val filteredCategories = categories.filter { it.type.name == item.parsedType }

    // Per-transaction state — reset when item changes
    var selectedAccountId  by remember(item.id) {
        mutableStateOf(item.suggestedAccountId ?: accounts.firstOrNull()?.id ?: "")
    }
    var selectedCategoryId by remember(item.id) { mutableStateOf<String?>(item.suggestedCategoryId) }
    var note               by remember(item.id) { mutableStateOf("") }
    var dateMs             by remember(item.id) { mutableLongStateOf(item.parsedDate) }
    var smsExpanded        by remember(item.id) { mutableStateOf(false) }
    var showDatePicker     by remember(item.id) { mutableStateOf(false) }

    val isSaveable = selectedAccountId.isNotBlank() && selectedCategoryId != null

    // Date picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMs = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Progress bar ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    // Show how many are left, not an index (less cognitive load)
                    "$total remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Skip button
                TextButton(
                    onClick        = { onSkip(item) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        "SKIP",
                        style         = MaterialTheme.typography.labelSmall,
                        color         = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                // Progress = items done / original total
                // We don't track original total here so use inverse of remaining fraction
                progress = { 0f }, // placeholder — items shrinks as items are accepted/rejected
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(2.dp)),
                color    = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // ── Transaction card ──────────────────────────────────────────────────
        LazyColumn(
            modifier       = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Amount hero
            item(key = "amount") {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text  = item.parsedType,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "${if (isExpense) "−" else "+"}${formatAmount(item.parsedAmount)}",
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color      = accentColor
                    )
                    if (item.accountLastFour != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = "···${item.accountLastFour}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // SMS reference (collapsible)
            item(key = "sms") {
                Card(
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier  = Modifier
                        .fillMaxWidth()
                        .clickable { smsExpanded = !smsExpanded }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                "VIEW SMS",
                                style         = MaterialTheme.typography.labelSmall,
                                color         = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.8.sp
                            )
                            Icon(
                                if (smsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint     = MaterialTheme.colorScheme.primary
                            )
                        }
                        AnimatedVisibility(visible = smsExpanded) {
                            Text(
                                text     = item.smsBody,
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }
                }
            }

            // Date field
            item(key = "date") {
                WizardField(label = "DATE") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            formatDate(dateMs),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint     = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Note field
            item(key = "note") {
                WizardField(label = "NOTE (OPTIONAL)") {
                    BasicTextField(
                        value         = note,
                        onValueChange = { note = it },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        textStyle     = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { inner ->
                            if (note.isEmpty()) {
                                Text(
                                    "Add a note…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                )
                            }
                            inner()
                        }
                    )
                }
            }

            // Account selector
            item(key = "account") {
                WizardField(label = "ACCOUNT") {
                    WizardSelectorGrid(
                        items     = accounts,
                        selectedId = selectedAccountId,
                        label     = { it.name },
                        sublabel  = { it.type.name.replace("_", " ") },
                        onSelect  = { selectedAccountId = it.id },
                        isRequired = true
                    )
                }
            }

            // Category selector
            item(key = "category") {
                WizardField(label = "CATEGORY") {
                    WizardCategoryGrid(
                        categories = filteredCategories,
                        selectedId = selectedCategoryId,
                        onSelect   = { selectedCategoryId = if (selectedCategoryId == it.id) null else it.id }
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        // ── Bottom action bar ─────────────────────────────────────────────────
        Surface(
            tonalElevation = 4.dp,
            modifier       = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reject
                OutlinedButton(
                    onClick = {
                        onReject(item)
                        // If this was the last item, onDone is triggered by items becoming empty
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border   = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    )
                ) {
                    Text(
                        "REJECT",
                        style         = MaterialTheme.typography.labelLarge,
                        letterSpacing = 0.5.sp
                    )
                }

                // Save & next
                Button(
                    onClick  = {
                        if (isSaveable) {
                            onAccept(item, selectedAccountId, selectedCategoryId, note, dateMs)
                        }
                    },
                    modifier = Modifier.weight(2f).height(52.dp),
                    enabled  = isSaveable,
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        if (total == 1) "SAVE & DONE" else "SAVE & NEXT",
                        style         = MaterialTheme.typography.labelLarge,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// ── Wizard helper composables ─────────────────────────────────────────────────

@Composable
private fun WizardField(
    label:   String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text          = label,
            style         = MaterialTheme.typography.labelMedium,
            color         = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.8.sp
        )
        content()
    }
}

@Composable
private fun <T> WizardSelectorGrid(
    items:      List<T>,
    selectedId: String,
    label:      (T) -> String,
    sublabel:   (T) -> String,
    onSelect:   (T) -> Unit,
    isRequired: Boolean = false,
    idOf:       (T) -> String = { (it as AccountUiModel).id },
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            val isSelected = idOf(item) == selectedId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(item) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick  = { onSelect(item) },
                    colors   = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Column {
                    Text(label(item), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(sublabel(item), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WizardCategoryGrid(
    categories: List<CategoryUiModel>,
    selectedId: String?,
    onSelect:   (CategoryUiModel) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            val isSelected = cat.id == selectedId
            val catColor   = cat.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
            FilterChip(
                selected = isSelected,
                onClick  = { onSelect(cat) },
                label    = { Text(cat.name, style = MaterialTheme.typography.labelMedium) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = catColor.copy(alpha = 0.2f),
                    selectedLabelColor     = catColor
                ),
                border   = FilterChipDefaults.filterChipBorder(
                    enabled          = true,
                    selected         = isSelected,
                    selectedBorderColor = catColor.copy(alpha = 0.5f),
                    borderColor      = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }
    }
}

// ── CARD VIEW ROW (unchanged logic, note/date now in edit sheet) ──────────────

@Composable
private fun SmsReviewRow(
    item:       SmsPendingTransactionEntity,
    accounts:   List<AccountUiModel>,
    categories: List<CategoryUiModel>,
    onAccept:   (accountId: String, categoryId: String?) -> Unit,
    onReject:   () -> Unit,
    onEdit:     () -> Unit,
) {
    val isExpense   = item.parsedType == "EXPENSE"
    val accentColor = if (isExpense) MaterialTheme.colorScheme.error else Color(0xFF27AE60)

    val suggestedAccount  = accounts.find   { it.id == item.suggestedAccountId }
    val suggestedCategory = categories.find { it.id == item.suggestedCategoryId }
    var expanded          by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(
                        text  = formatDate(item.parsedDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text       = "${if (isExpense) "−" else "+"}${formatAmount(item.parsedAmount)}",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = accentColor
                    )
                }

                // Suggestion chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    item.accountLastFour?.let  { InfoChip("···$it") }
                    suggestedCategory?.let     { InfoChip(it.name) }
                    suggestedAccount?.let      { InfoChip(it.name) }
                }

                // SMS body (collapsible)
                Text(
                    text     = if (expanded) item.smsBody
                    else item.smsBody.take(72) + if (item.smsBody.length > 72) "…" else "",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.clickable { expanded = !expanded }
                )
                if (item.smsBody.length > 72) {
                    Text(
                        text     = if (expanded) "Show less" else "Show original",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                }

                // Actions
                val quickSaveAccountId = suggestedAccount?.id ?: accounts.firstOrNull()?.id
                val canQuickSave       = quickSaveAccountId != null && item.suggestedCategoryId != null

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick  = {
                            if (canQuickSave) onAccept(quickSaveAccountId!!, item.suggestedCategoryId)
                            else onEdit()
                        },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            when {
                                quickSaveAccountId == null        -> "SELECT ACCOUNT"
                                item.suggestedCategoryId == null  -> "SELECT CATEGORY"
                                else                              -> "SAVE"
                            },
                            style         = MaterialTheme.typography.labelMedium,
                            letterSpacing = 0.5.sp
                        )
                    }
                    OutlinedButton(
                        onClick  = onEdit,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(10.dp)
                    ) {
                        Text("EDIT", style = MaterialTheme.typography.labelMedium, letterSpacing = 0.5.sp)
                    }
                    IconButton(
                        onClick = onReject,
                        colors  = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Icon(Icons.Default.Close, "Reject", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ── EDIT SHEET (Card view — full editor) ──────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SmsEditSheet(
    item:      SmsPendingTransactionEntity,
    accounts:  List<AccountUiModel>,
    categories: List<CategoryUiModel>,
    onAccept:  (accountId: String, categoryId: String?, note: String, dateMs: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState          = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedAccountId   by remember { mutableStateOf(item.suggestedAccountId ?: accounts.firstOrNull()?.id ?: "") }
    var selectedCategoryId  by remember { mutableStateOf<String?>(item.suggestedCategoryId) }
    var note                by remember { mutableStateOf("") }          // blank — user writes own note
    var dateMs              by remember { mutableLongStateOf(item.parsedDate) }
    var showDatePicker      by remember { mutableStateOf(false) }

    val isExpense           = item.parsedType == "EXPENSE"
    val accentColor         = if (isExpense) MaterialTheme.colorScheme.error else Color(0xFF27AE60)
    val filteredCategories  = categories.filter { it.type.name == item.parsedType }
    val isSaveable          = selectedAccountId.isNotBlank() && selectedCategoryId != null

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMs = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "EDIT TRANSACTION",
                style         = MaterialTheme.typography.titleMedium,
                letterSpacing = 1.sp,
                fontWeight    = FontWeight.SemiBold
            )

            // Amount
            Text(
                "${if (isExpense) "−" else "+"}${formatAmount(item.parsedAmount)}",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = accentColor
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Date
            Text(
                "DATE",
                style         = MaterialTheme.typography.labelMedium,
                color         = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(formatDate(dateMs), style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            }

            // Note
            Text(
                "NOTE (OPTIONAL)",
                style         = MaterialTheme.typography.labelMedium,
                color         = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            BasicTextField(
                value         = note,
                onValueChange = { note = it },
                modifier      = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                textStyle     = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (note.isEmpty()) {
                        Text(
                            "Add a note…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }
                    inner()
                }
            )

            // SMS reference
            Text(
                "ORIGINAL SMS",
                style         = MaterialTheme.typography.labelMedium,
                color         = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                item.smsBody,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Account
            Text(
                "ACCOUNT",
                style         = MaterialTheme.typography.labelMedium,
                color         = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            accounts.forEach { account ->
                val isSelected = selectedAccountId == account.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                        .clickable { selectedAccountId = account.id }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick  = { selectedAccountId = account.id },
                        colors   = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Column {
                        Text(account.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(
                            account.type.name.replace("_", " "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Category
            Text(
                "CATEGORY",
                style         = MaterialTheme.typography.labelMedium,
                color         = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp)
            ) {
                filteredCategories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick  = {
                            selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id
                        },
                        label = { Text(cat.name, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Button(
                onClick  = { onAccept(selectedAccountId, selectedCategoryId, note, dateMs) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = isSaveable,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (isSaveable) "SAVE TRANSACTION" else "SELECT ACCOUNT & CATEGORY",
                    style         = MaterialTheme.typography.labelLarge,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ── EMPTY STATE ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyReviewState(onNavigateBack: () -> Unit) {
    Column(
        modifier                = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement     = Arrangement.Center,
        horizontalAlignment     = Alignment.CenterHorizontally
    ) {
        Text("✓", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("ALL CAUGHT UP", style = MaterialTheme.typography.titleLarge, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        Text("No transactions pending review.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        OutlinedButton(onClick = onNavigateBack, shape = RoundedCornerShape(12.dp)) {
            Text("BACK", letterSpacing = 1.sp)
        }
    }
}

// ── SHARED HELPERS ────────────────────────────────────────────────────────────

@Composable
private fun InfoChip(text: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp)) {
        Text(
            text     = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatAmount(amount: Double): String =
    NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(amount)

private fun formatDate(timestampMs: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestampMs))