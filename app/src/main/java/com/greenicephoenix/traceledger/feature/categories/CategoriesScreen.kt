package com.greenicephoenix.traceledger.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.domain.model.CategoryType
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed

@Composable
fun CategoriesScreen(
    categories: List<CategoryUiModel>,
    onBack: () -> Unit,
    onAddCategory: () -> Unit,
    onCategoryClick: (CategoryUiModel) -> Unit
) {
    var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }

    val filteredCategories = remember(categories, selectedType) {
        categories.filter { it.type == selectedType }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // ================= HEADER =================
        CategoriesHeader(
            selectedType = selectedType,
            onTypeChange = { selectedType = it },
            onBack = onBack
        )

        // ================= GRID =================
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ADD CATEGORY CARD
            item {
                AddCategoryCard(onClick = onAddCategory)
            }

            // CATEGORY CARDS
            items(filteredCategories) { category ->
                CategoryCard(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }

            // bottom spacer (FAB safety / future)
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

@Composable
private fun CategoriesHeader(
    selectedType: CategoryType,
    onTypeChange: (CategoryType) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "CATEGORIES",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        CategoryTypeSelector(
            selectedType = selectedType,
            onTypeChange = onTypeChange
        )
    }
}

@Composable
private fun CategoryTypeSelector(
    selectedType: CategoryType,
    onTypeChange: (CategoryType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFF1C1C1C), RoundedCornerShape(22.dp))
            .padding(4.dp)
    ) {
        CategoryType.entries.forEach { type ->
            val selected = type == selectedType

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (selected) NothingRed.copy(alpha = 0.25f)
                        else Color.Transparent,
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { onTypeChange(type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.name,
                    color = if (selected) NothingRed else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun AddCategoryCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141414)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A), // top highlight
                            Color(0xFF0F0F0F)  // base
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                ),

            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Category",
                tint = NothingRed,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: CategoryUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A), // top highlight
                            Color(0xFF0F0F0F)  // base
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // ── ICON + NAME ───────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = CategoryIcons.all[category.icon]
                            ?: Icons.Default.Category,
                        contentDescription = null,
                        tint = Color(category.color),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = category.name,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // ── SUBTLE DIVIDER ────────────────────────────
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .fillMaxWidth(0.8f)
                        .background(
                            Color(category.color).copy(alpha = 0.35f),
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}