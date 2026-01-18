package com.greenicephoenix.traceledger.feature.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.greenicephoenix.traceledger.TraceLedgerApp
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.feature.budgets.data.BudgetEntity
import java.time.YearMonth
import java.util.UUID
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.SolidColor
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.greenicephoenix.traceledger.feature.categories.CategoriesViewModel
import com.greenicephoenix.traceledger.domain.model.CategoryType
import com.greenicephoenix.traceledger.feature.addtransaction.CategorySelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetScreen(
    viewModel: BudgetsViewModel,
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

    // 🔑 reuse CategoriesViewModel (same as elsewhere)
    val categoriesViewModel: CategoriesViewModel = viewModel()
    val categories by categoriesViewModel.categories.collectAsState()
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
                .background(Color.Black)
        ) {

            /* ---------- HEADER ---------- */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color.White
                    )
                }

                Text(
                    text = if (budgetId == null) "Add Budget" else "Edit Budget",
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                if (budgetId != null) {
                    Text(
                        text = "Editing existing budget",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                IconButton(
                    enabled = isValid,
                    onClick = {

                        val isDuplicate = existingBudgets.any {
                            it.categoryId == selectedCategoryId &&
                                    it.month == month &&
                                    it.budgetId != budgetId
                        }

                        if (isDuplicate) {
                            duplicateError = true
                            return@IconButton
                        }

                        val budget = BudgetEntity(
                            id = budgetId ?: UUID.randomUUID().toString(),
                            categoryId = selectedCategoryId!!,
                            limitAmount = limit.toBigDecimal(),
                            month = selectedMonth,
                            isActive = true
                        )

                        scope.launch {
                            app.container.budgetRepository.upsertBudget(budget)
                            onBack()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = if (isValid) NothingRed else Color.Gray
                    )
                }
            }

            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = Color.White.copy(alpha = 0.1f)
            )

            /* ---------- CONTENT ---------- */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier.background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1A1A1A),
                                    Color(0xFF0F0F0F)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (!isEditMode) {
                                            selectedMonth = selectedMonth.minusMonths(1)
                                        }
                                    },
                                    enabled = !isEditMode
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Previous Month",
                                        tint = if (isEditMode) Color.Gray else Color.White
                                    )
                                }

                                Text(
                                    text = selectedMonth.month.name.lowercase()
                                        .replaceFirstChar { it.uppercase() } + " " + selectedMonth.year,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                if (isEditMode) {
                                    Text(
                                        text = "Month locked",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (!isEditMode) {
                                            selectedMonth = selectedMonth.plusMonths(1)
                                        }
                                    },
                                    enabled = !isEditMode
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Next Month",
                                        tint = if (isEditMode) Color.Gray else Color.White
                                    )
                                }
                            }

                            if (duplicateError) {
                                Text(
                                    text = "A budget for this category already exists for this month",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            /* ---------- CATEGORY (BOTTOM SHEET) ---------- */
                            CategorySelector(
                                categories = categories.filter { category ->
                                    budgetId != null || category.id !in usedCategoryIdsForMonth
                                },
                                type = CategoryType.EXPENSE,
                                selectedCategoryId = selectedCategoryId,
                                onSelect = {
                                    selectedCategoryId = it
                                    duplicateError = false
                                }
                            )

                            /* ---------- MONTHLY LIMIT ---------- */
                            OutlinedTextField(
                                value = limit,
                                onValueChange = { limit = it },
                                label = { Text("Monthly Limit") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            /* ---------- DELETE BUTTON ---------- */
                            if (budgetId != null) {

                                Spacer(modifier = Modifier.height(12.dp))

                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = Color.White.copy(alpha = 0.12f)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedButton(
                                    onClick = {
                                        showDeleteConfirm = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = NothingRed
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = SolidColor(NothingRed)
                                    )
                                ) {
                                    Text(
                                        text = "Delete budget",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
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
                    color = Color.White
                )

                Text(
                    text = "This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
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
