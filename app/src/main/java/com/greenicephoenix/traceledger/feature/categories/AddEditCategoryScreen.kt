package com.greenicephoenix.traceledger.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.ui.theme.AccountColors
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.domain.model.CategoryType
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryScreen(
    existingCategory: CategoryUiModel? = null,
    onCancel: () -> Unit,
    onSave: (CategoryUiModel) -> Unit
) {
    val isEditMode = existingCategory != null

    var name by remember {
        mutableStateOf(existingCategory?.name ?: "")
    }

    var type by remember {
        mutableStateOf(existingCategory?.type ?: CategoryType.EXPENSE)
    }

    val initialColor = remember(existingCategory?.id) {
        if (existingCategory != null) {
            Color(existingCategory.color)
        } else {
            CategoryColors.expenseColors.first()
        }
    }

    var selectedColor by remember(existingCategory?.id) {
        mutableStateOf(initialColor)
    }


    var selectedIcon by remember {
        mutableStateOf(existingCategory?.icon ?: "category")
    }

    val isValid by remember {
        derivedStateOf { name.isNotBlank() }
    }

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
        ) {

            // ================= HEADER =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color.White
                    )
                }

                Text(
                    text = if (isEditMode) "Edit Category" else "Add Category",
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(
                    onClick = {
                        if (!isValid) return@IconButton

                        val category = CategoryUiModel(
                            id = existingCategory?.id
                                ?: System.currentTimeMillis().toString(),
                            name = name.trim(),
                            type = type,
                            color = selectedColor.toArgb().toLong(),
                            icon = selectedIcon
                        )

                        onSave(category)
                    },
                    enabled = isValid
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

            // ================= CONTENT =================
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
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
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
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {

                            // NAME
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Category Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            // TYPE
                            CategoryTypeSelector(
                                selected = type,
                                enabled = !isEditMode,
                                onSelected = { type = it }
                            )

                            // ICON
                            SectionLabel("ICON")
                            IconPicker(
                                selectedIcon = selectedIcon,
                                onIconSelect = { selectedIcon = it }
                            )

                            // COLOR
                            SectionLabel("COLOR")
                            ColorPicker(
                                colors = if (type == CategoryType.EXPENSE)
                                    CategoryColors.expenseColors
                                else
                                    CategoryColors.incomeColors,
                                selectedColor = selectedColor,
                                onColorSelect = { selectedColor = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTypeSelector(
    selected: CategoryType,
    enabled: Boolean,
    onSelected: (CategoryType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFF1C1C1C), RoundedCornerShape(22.dp))
            .padding(4.dp)
    ) {
        CategoryType.entries.forEach { type ->
            val isSelected = type == selected

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) NothingRed.copy(alpha = 0.25f)
                        else Color.Transparent,
                        RoundedCornerShape(18.dp)
                    )
                    .clickable(enabled) { onSelected(type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.name,
                    color = if (isSelected) NothingRed else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun IconPicker(
    selectedIcon: String,
    onIconSelect: (String) -> Unit
) {
    val icons = CategoryIcons.ids.chunked(7)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        icons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                row.forEach { iconId ->
                    val selected = iconId == selectedIcon

                    Box(
                        modifier = Modifier
                            //.padding(end = 12.dp) // ✅ spacing moved here
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected)
                                    NothingRed.copy(alpha = 0.25f)
                                else
                                    Color(0xFF2A2A2A)
                            )
                            .clickable { onIconSelect(iconId) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryIcons.all[iconId]!!,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ColorPicker(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelect: (Color) -> Unit
) {
    colors.chunked(8).forEach { row ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            row.forEach { color ->
                val selected = color == selectedColor

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) Color.White else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelect(color) }
                )
            }
        }
        //Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.Gray
    )
}
