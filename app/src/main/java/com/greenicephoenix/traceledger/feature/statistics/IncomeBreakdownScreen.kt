package com.greenicephoenix.traceledger.feature.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import com.greenicephoenix.traceledger.feature.statistics.components.BackHeader
import com.greenicephoenix.traceledger.feature.statistics.components.IncomeDonutChart
import com.greenicephoenix.traceledger.feature.statistics.components.IncomeLegend

@Composable
fun IncomeBreakdownScreen(
    viewModel: StatisticsViewModel,
    categoryMap: Map<String, CategoryUiModel>,
    onBack: () -> Unit
) {
    val slices by viewModel.incomeCategorySlices.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        item {
            BackHeader(
                title = "Income Breakdown",
                onBack = onBack
            )
        }

        item {
            IncomeDonutChart(
                slices = slices,
                categoryMap = categoryMap,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            IncomeLegend(
                slices = slices,
                categoryMap = categoryMap
            )
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}