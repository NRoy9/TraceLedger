package com.greenicephoenix.traceledger.feature.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.greenicephoenix.traceledger.TraceLedgerApp
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.feature.budgets.data.BudgetEntity
import java.time.YearMonth
import java.util.UUID
import androidx.compose.runtime.rememberCoroutineScope
import com.greenicephoenix.traceledger.core.ui.components.TLEditorTopBar
import com.greenicephoenix.traceledger.core.ui.components.TLDangerButton
import com.greenicephoenix.traceledger.core.ui.components.TLDivider
import com.greenicephoenix.traceledger.core.ui.theme.Dimens
import kotlinx.coroutines.launch
import com.greenicephoenix.traceledger.domain.model.CategoryType
import com.greenicephoenix.traceledger.feature.addtransaction.CategorySelector
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetScreen(
    viewModel: BudgetsViewModel,
    categories: List<CategoryUiModel>,
    budgetId: String?,
    month: YearMonth,
    onBack: () -> Unit
) {
    val isEditMode = budgetId != null
    val context = LocalContext.current
    val app = context.applicationContext as TraceLedgerApp
    var selectedMonth by remember { mutableStateOf(month) }
    val existingBudgets by viewModel.budgetStatuses.collectAsState()
    val budgetBeingEdited = remember(existingBudgets, budgetId) {
        if (budgetId == null) null
        else existingBudgets.firstOrNull { it.budgetId == budgetId }
    }
    val usedCategoryIdsForMonth = remember(existingBudgets, month) {
        existingBudgets
            .filter { it.month == month }
            .map { it.categoryId }
            .toSet()
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var limit by remember { mutableStateOf("") }
    LaunchedEffect(budgetBeingEdited) {
        if (budgetBeingEdited != null) {
            selectedCategoryId = budgetBeingEdited.categoryId
            limit = budgetBeingEdited.limit.toPlainString()
        }
    }
    var duplicateError by remember { mutableStateOf(false) }

    val isValid =
        selectedCategoryId != null && limit.toBigDecimalOrNull() != null

    val scope = rememberCoroutineScope()

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                //.statusBarsPadding()
                //.padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {

            /* ---------- HEADER ---------- */
            TLEditorTopBar(
                title   = if (budgetId == null) "Add Budget" else "Edit Budget",
                canSave = isValid,
                onClose = onBack,
                onSave  = {
                    val isDuplicate = existingBudgets.any {
                        it.categoryId == selectedCategoryId &&
                                it.month == month &&
                                it.budgetId != budgetId
                    }
                    if (isDuplicate) { duplicateError = true; return@TLEditorTopBar }
                    val budget = BudgetEntity(
                        id          = budgetId ?: UUID.randomUUID().toString(),
                        categoryId  = selectedCategoryId!!,
                        limitAmount = limit.toBigDecimal(),
                        month       = selectedMonth,
                        isActive    = true
                    )
                    scope.launch {
                        app.container.budgetRepository.upsertBudget(budget)
                        onBack()
                    }
                }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))

            /* ---------- CONTENT ---------- */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .padding(horizontal = Dimens.md)
                        .fillMaxWidth(),
                    shape  = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier            = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Month selector row (unchanged content)
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (!isEditMode) selectedMonth = selectedMonth.minusMonths(1) },
                                enabled = !isEditMode
                            ) {
                                Icon(
                                    Icons.Default.ChevronLeft, "Previous Month",
                                    tint = if (isEditMode) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text  = selectedMonth.month.name.lowercase().replaceFirstChar { it.uppercase() } +
                                        " " + selectedMonth.year,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (isEditMode) {
                                Text(
                                    text  = "Month locked",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            IconButton(
                                onClick = { if (!isEditMode) selectedMonth = selectedMonth.plusMonths(1) },
                                enabled = !isEditMode
                            ) {
                                Icon(
                                    Icons.Default.ChevronRight, "Next Month",
                                    tint = if (isEditMode) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (duplicateError) {
                            Text(
                                text  = "A budget for this category already exists for this month",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        CategorySelector(
                            categories         = categories.filter { category ->
                                budgetId != null || category.id !in usedCategoryIdsForMonth
                            },
                            type               = CategoryType.EXPENSE,
                            selectedCategoryId = selectedCategoryId,
                            onSelect           = { selectedCategoryId = it; duplicateError = false }
                        )

                        OutlinedTextField(
                            value         = limit,
                            onValueChange = { limit = it },
                            label         = { Text("Monthly Limit") },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true
                        )

                        if (budgetId != null) {
                            TLDivider()
                            TLDangerButton(
                                text    = "DELETE BUDGET",
                                onClick = { showDeleteConfirm = true }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteConfirm = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Delete budget?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {

                    TextButton(
                        onClick = { showDeleteConfirm = false }
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            viewModel.deleteBudget(budgetId!!)
                            showDeleteConfirm = false
                            onBack()
                        }
                    ) {
                        Text(
                            text = "Delete",
                            color = NothingRed
                        )
                    }
                }
            }
        }
    }
}
