package com.greenicephoenix.traceledger.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
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
        mutableStateOf(
            existingCategory?.type
                ?: CategoryType.EXPENSE
        )
    }

    var selectedIcon by remember {
        mutableStateOf(
            existingCategory?.icon
                ?: CategoryIconIds.OTHER
        )
    }

    val initialColor = remember(existingCategory?.id) {
        if (existingCategory != null) {
            Color(existingCategory.color)
        } else {
            CategoryColors.colorsFor(type).first()
        }
    }

    var selectedColor by remember(existingCategory?.id) {
        mutableStateOf(initialColor)
    }

    val isValid by remember {
        derivedStateOf {
            name.trim().isNotBlank()
        }
    }

    val isLightTheme =
        MaterialTheme.colorScheme.background.luminance() > 0.5f

    val containerBrush =
        if (isLightTheme) {
            Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surface
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xFF1A1A1A),
                    Color(0xFF0F0F0F)
                )
            )
        }

    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    start = padding.calculateStartPadding(layoutDirection),
                    end = padding.calculateEndPadding(layoutDirection),
                    bottom = padding.calculateBottomPadding()
                )
        ) {

            // ───────────────── HEADER ─────────────────

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text =
                        if (isEditMode)
                            "Edit Category"
                        else
                            "Add Category",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    enabled = isValid,
                    onClick = {

                        val category = CategoryUiModel(
                            id =
                                existingCategory?.id
                                    ?: java.util.UUID.randomUUID().toString(),
                            name = name.trim(),
                            type = type,
                            color = selectedColor.toArgb().toLong(),
                            icon = selectedIcon
                        )

                        onSave(category)
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint =
                            if (isValid)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            )

            // ───────────────── CONTENT ─────────────────

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 32.dp)
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
                ) {

                    Box(
                        modifier = Modifier.background(
                            brush = containerBrush,
                            shape = RoundedCornerShape(22.dp)
                        )
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            // ───────────────── NAME ─────────────────

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = {
                                    Text("Category Name")
                                },
                                shape = RoundedCornerShape(16.dp)
                            )

                            // ───────────────── TYPE ─────────────────

                            CategoryTypeSelector(
                                selected = type,
                                enabled = !isEditMode,
                                onSelected = {

                                    type = it

                                    if (
                                        selectedColor !in CategoryColors.colorsFor(it)
                                    ) {
                                        selectedColor =
                                            CategoryColors.colorsFor(it).first()
                                    }
                                }
                            )

                            // ───────────────── COLORS ─────────────────

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                SectionLabel("COLOR")

                                ColorPicker(
                                    colors = CategoryColors.colorsFor(type),
                                    selectedColor = selectedColor,
                                    onColorSelect = {
                                        selectedColor = it
                                    }
                                )
                            }

                            // ───────────────── ICONS ─────────────────

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                SectionLabel("ICON")

                                IconPicker(
                                    selectedIcon = selectedIcon,
                                    onIconSelect = {
                                        selectedIcon = it
                                    }
                                )
                            }
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
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(22.dp)
            )
            .padding(4.dp)
    ) {

        CategoryType.entries.forEach { type ->

            val isSelected = selected == type

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                        else
                            Color.Transparent
                    )
                    .clickable(enabled = enabled) {
                        onSelected(type)
                    },
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = type.name,
                    style = MaterialTheme.typography.labelLarge,
                    color =
                        if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                )
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

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 34.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        items(colors) { color ->

            val selected = selectedColor == color

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width =
                            if (selected) 2.dp
                            else 0.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
                    .clickable {
                        onColorSelect(color)
                    }
            )
        }
    }
}

@Composable
private fun IconPicker(
    selectedIcon: String,
    onIconSelect: (String) -> Unit
) {

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 42.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        items(CategoryIcons.ids) { iconId ->

            val selected = selectedIcon == iconId

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(
                        if (selected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable {
                        onIconSelect(iconId)
                    },
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = CategoryIcons.iconFor(iconId),
                    contentDescription = null,
                    tint =
                        if (selected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(
    text: String
) {

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    )
}