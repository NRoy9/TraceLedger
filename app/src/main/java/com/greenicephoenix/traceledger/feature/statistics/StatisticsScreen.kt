package com.greenicephoenix.traceledger.feature.statistics

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.currency.CurrencyFormatter
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import com.greenicephoenix.traceledger.core.navigation.Routes
import com.greenicephoenix.traceledger.core.ui.components.MonthSelector
import com.greenicephoenix.traceledger.core.ui.theme.ErrorRed
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.core.ui.theme.SuccessGreen
import com.greenicephoenix.traceledger.core.ui.theme.WarningAmber
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import com.greenicephoenix.traceledger.feature.statistics.components.BudgetRing
import com.greenicephoenix.traceledger.feature.statistics.components.SparklineChart
import com.greenicephoenix.traceledger.feature.statistics.components.SpendingStreakCard
import java.math.BigDecimal

// ─────────────────────────────────────────────────────────────────────────────
// Route manifest — every route appears exactly once in this file.
//
// BREAKDOWNS : BREAKDOWN · INCOME · CASHFLOW · WATERFALL · AREA
//              TRENDS · CAT_COMPARE · TREEMAP · SANKEY
// PATTERNS   : SPENDING_PATTERNS · WEEKDAY · HEATMAP · VELOCITY
//              SAVINGS_RATE · TOP_DAYS · ROLLING
// FORECASTING: FORECASTING · HEALTH
// ACCOUNTS   : ACCOUNT_INSIGHTS
// RECURRING  : RECURRING
// ANALYSIS   : INCOME_STABILITY
// ─────────────────────────────────────────────────────────────────────────────

private data class ChartTile(
    val emoji:    String,
    val title:    String,
    val subtitle: String,
    val route:    String,
    val accentColor: Color = Color.Unspecified
)

@Composable
fun StatisticsScreen(
    viewModel:   StatisticsViewModel,
    categoryMap: Map<String, CategoryUiModel>,
    onNavigate:  (String) -> Unit
) {
    val currency       by CurrencyManager.currency.collectAsState()
    val selectedMonth  by viewModel.selectedMonth.collectAsState()
    val totalIncome    by viewModel.totalIncome.collectAsState()
    val totalExpense   by viewModel.totalExpense.collectAsState()
    val netAmount      by viewModel.netAmount.collectAsState()
    val prevIncome     by viewModel.prevMonthIncome.collectAsState()
    val prevExpense    by viewModel.prevMonthExpense.collectAsState()
    val dailyExpPoints by viewModel.dailyExpensePoints.collectAsState()
    val dailyIncPoints by viewModel.dailyIncomePoints.collectAsState()
    val budgetRings    by viewModel.budgetRings.collectAsState()
    val streak         by viewModel.spendingStreak.collectAsState()
    val forecast       by viewModel.forecastData.collectAsState()

    val savingsRate: Int? = remember(totalIncome, netAmount) {
        if (totalIncome > BigDecimal.ZERO)
            netAmount.multiply(BigDecimal(100))
                .divide(totalIncome, 0, java.math.RoundingMode.HALF_UP)
                .toInt()
        else null
    }

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {

        // ── HEADER ────────────────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
            ) {
                Text(
                    text  = "STATISTICS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))
                MonthSelector(selectedMonth, viewModel::previousMonth, viewModel::nextMonth)
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        // ── INCOME + EXPENSE SPARK CARDS ──────────────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SparkStatCard(
                    modifier    = Modifier.weight(1f).fillMaxHeight(),
                    label       = "INCOME",
                    value       = CurrencyFormatter.format(totalIncome.toPlainString(), currency),
                    valueColor  = SuccessGreen,
                    previous    = prevIncome,
                    current     = totalIncome,
                    invertDelta = false,
                    sparkPoints = dailyIncPoints,
                    sparkColor  = SuccessGreen
                )
                SparkStatCard(
                    modifier    = Modifier.weight(1f).fillMaxHeight(),
                    label       = "EXPENSE",
                    value       = CurrencyFormatter.format(totalExpense.toPlainString(), currency),
                    valueColor  = NothingRed,
                    previous    = prevExpense,
                    current     = totalExpense,
                    invertDelta = true,
                    sparkPoints = dailyExpPoints,
                    sparkColor  = NothingRed
                )
            }
        }

        item { Spacer(Modifier.height(10.dp)) }

        // ── NET + SAVINGS RATE CARD ───────────────────────────────────────────
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text  = "NET THIS MONTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text  = CurrencyFormatter.format(netAmount.toPlainString(), currency),
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (netAmount.signum() >= 0) SuccessGreen else NothingRed
                        )
                    }
                    if (savingsRate != null) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text  = "SAVINGS RATE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text  = "$savingsRate%",
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (savingsRate >= 0) SuccessGreen else NothingRed
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(10.dp)) }

        // ── SAFE TO SPEND — only when budget is active ────────────────────────
        if (forecast.dailySafeToSpend > 0.0) {
            item {
                val trackColor = if (forecast.isOnTrack) SuccessGreen else WarningAmber
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = trackColor.copy(alpha = 0.10f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text  = "SAFE TO SPEND TODAY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                            Text(
                                text  = CurrencyFormatter.format(
                                    forecast.dailySafeToSpend.toString(), currency
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                color = trackColor
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text       = if (forecast.isOnTrack) "ON TRACK" else "OVER PACE",
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color      = trackColor
                            )
                            Text(
                                text  = "${forecast.daysRemaining} days left",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(10.dp)) }
        }

        // ── BUDGET RINGS — only when budgets exist ────────────────────────────
        if (budgetRings.isNotEmpty()) {
            item {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text  = "BUDGETS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding        = PaddingValues(end = 8.dp)
                    ) {
                        items(budgetRings) { ring -> BudgetRing(data = ring) }
                    }
                }
            }
            item { Spacer(Modifier.height(10.dp)) }
        }

        // ── SPENDING STREAK ───────────────────────────────────────────────────
        item {
            SpendingStreakCard(
                streak   = streak,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        // ═════════════════════════════════════════════════════════════════════
        // BREAKDOWNS
        // Routes: BREAKDOWN, INCOME, CASHFLOW, WATERFALL, AREA,
        //         TRENDS, CAT_COMPARE, TREEMAP, SANKEY
        // ═════════════════════════════════════════════════════════════════════
        item { Spacer(Modifier.height(28.dp)) }
        item {
            DashboardSectionHeader(
                title       = "BREAKDOWNS",
                subtitle    = "Where your money is going",
                accentColor = MaterialTheme.colorScheme.primary
            )
        }
        item { Spacer(Modifier.height(14.dp)) }

        item { NavCard("Expenses",  "Category breakdown")    { onNavigate(Routes.STATISTICS_BREAKDOWN)   } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Income",  "Income sources")    { onNavigate(Routes.STATISTICS_INCOME)   } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Cashflow",  "Daily in vs out")    { onNavigate(Routes.STATISTICS_CASHFLOW)   } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Money Flow",  "Sankey diagram")    { onNavigate(Routes.STATISTICS_SANKEY)   } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Cashflow Waterfall",  "How income flows into expenses")    { onNavigate(Routes.STATISTICS_WATERFALL)   } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Income vs Expense",   "12-month overlapping area chart")   { onNavigate(Routes.STATISTICS_AREA)        } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Spending Trends",     "Category spend over time")          { onNavigate(Routes.STATISTICS_TRENDS)      } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Month vs Last Month", "Top 5 category comparison")         { onNavigate(Routes.STATISTICS_CAT_COMPARE) } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Spending Map",        "Proportional treemap by category")  { onNavigate(Routes.STATISTICS_TREEMAP)     } }

        // ═════════════════════════════════════════════════════════════════════
        // SPENDING PATTERNS
        // Routes: SPENDING_PATTERNS, WEEKDAY, HEATMAP, VELOCITY,
        //         SAVINGS_RATE, TOP_DAYS, ROLLING
        // ═════════════════════════════════════════════════════════════════════
        item { Spacer(Modifier.height(28.dp)) }
        item {
            DashboardSectionHeader(
                title       = "SPENDING PATTERNS",
                subtitle    = "When and how you spend",
                accentColor = WarningAmber
            )
        }
        item { Spacer(Modifier.height(14.dp)) }

        item { NavCard("Behavior",   "Daily trends & cycles?") { onNavigate(Routes.STATISTICS_SPENDING_PATTERNS) } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Day of Week",   "Weekly pattern") { onNavigate(Routes.STATISTICS_WEEKDAY) } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Heatmap",   "Calendar spend density") { onNavigate(Routes.STATISTICS_HEATMAP) } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Velocity",   "Spending pace vs avg") { onNavigate(Routes.STATISTICS_VELOCITY) } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Savings Rate Trend",   "Are you saving more each month?") { onNavigate(Routes.STATISTICS_SAVINGS_RATE) } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Top Spending Days",    "Your 10 biggest spend days ever") { onNavigate(Routes.STATISTICS_TOP_DAYS)     } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("30/60/90 Day Summary", "Rolling expense windows")         { onNavigate(Routes.STATISTICS_ROLLING)      } }

        // ═════════════════════════════════════════════════════════════════════
        // FORECASTING
        // Routes: FORECASTING, HEALTH
        // ═════════════════════════════════════════════════════════════════════
        item { Spacer(Modifier.height(28.dp)) }
        item {
            DashboardSectionHeader(
                title       = "FORECASTING",
                subtitle    = "Predictions and financial health",
                accentColor = SuccessGreen
            )
        }
        item { Spacer(Modifier.height(14.dp)) }

        item { NavCard("Forecasting",    "Month-end prediction & burn rate") { onNavigate(Routes.STATISTICS_FORECASTING)     } }
        item { Spacer(Modifier.height(8.dp)) }
        item { NavCard("Health Score", "Savings, budget & consistency") { onNavigate(Routes.STATISTICS_HEALTH)      } }

        // ═════════════════════════════════════════════════════════════════════
        // ACCOUNTS
        // Routes: ACCOUNT_INSIGHTS
        // ═════════════════════════════════════════════════════════════════════
        item { Spacer(Modifier.height(28.dp)) }
        item {
            DashboardSectionHeader(
                title       = "ACCOUNTS",
                subtitle    = "Balance and cashflow by account",
                accentColor = MaterialTheme.colorScheme.primary
            )
        }
        item { Spacer(Modifier.height(14.dp)) }
        item {
            NavCard(
                title    = "Account Insights",
                subtitle = "Balance distribution, per-account cashflow and running balance"
            ) { onNavigate(Routes.STATISTICS_ACCOUNT_INSIGHTS) }
        }

        // ═════════════════════════════════════════════════════════════════════
        // RECURRING
        // Routes: RECURRING
        // ═════════════════════════════════════════════════════════════════════
        item { Spacer(Modifier.height(28.dp)) }
        item {
            DashboardSectionHeader(
                title       = "RECURRING",
                subtitle    = "Subscriptions and fixed commitments",
                accentColor = WarningAmber
            )
        }
        item { Spacer(Modifier.height(14.dp)) }
        item {
            NavCard(
                title    = "Recurring Analytics",
                subtitle = "Monthly commitment, upcoming payments and burden analysis"
            ) { onNavigate(Routes.STATISTICS_RECURRING) }
        }

        // ═════════════════════════════════════════════════════════════════════
        // DEEP ANALYSIS
        // Routes: INCOME_STABILITY
        // ═════════════════════════════════════════════════════════════════════
        item { Spacer(Modifier.height(28.dp)) }
        item {
            DashboardSectionHeader(
                title       = "DEEP ANALYSIS",
                subtitle    = "Advanced metrics and insights",
                accentColor = MaterialTheme.colorScheme.primary
            )
        }
        item { Spacer(Modifier.height(14.dp)) }
        item {
            NavCard(
                title    = "Income Stability",
                subtitle = "Variance analysis — how consistent is your income over 12 months?"
            ) { onNavigate(Routes.STATISTICS_INCOME_STABILITY) }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Private composables
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Section header with left-accent color bar, bold title and muted subtitle.
 * Each section uses a different accent color for visual rhythm.
 */
@Composable
private fun DashboardSectionHeader(
    title:       String,
    subtitle:    String,
    accentColor: Color
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text  = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.50f)
            )
        }
    }
}

/**
 * Standard navigation list card — title + subtitle + right chevron.
 * Consistent padding and spacing throughout.
 */
@Composable
private fun NavCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text     = title,
                    style    = MaterialTheme.typography.titleSmall,
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text     = subtitle,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector        = Icons.Default.ChevronRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f),
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Income/Expense spark card with month-over-month comparison arrow.
 */
@Composable
private fun SparkStatCard(
    modifier:    Modifier,
    label:       String,
    value:       String,
    valueColor:  Color,
    previous:    BigDecimal,
    current:     BigDecimal,
    invertDelta: Boolean,
    sparkPoints: List<Float>,
    sparkColor:  Color
) {
    val delta       = current.subtract(previous)
    val isFlat      = delta.compareTo(BigDecimal.ZERO) == 0 || previous == BigDecimal.ZERO
    val isUp        = delta > BigDecimal.ZERO
    val positiveDir = if (invertDelta) !isUp else isUp

    val arrowIcon: ImageVector = when {
        isFlat -> Icons.Default.Remove
        isUp   -> Icons.Default.ArrowDropUp
        else   -> Icons.AutoMirrored.Filled.KeyboardArrowRight
    }
    val arrowColor = when {
        isFlat      -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        positiveDir -> SuccessGreen
        else        -> NothingRed
    }

    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
            Text(
                text  = value,
                style = MaterialTheme.typography.titleMedium,
                color = valueColor
            )
            if (sparkPoints.any { it > 0f }) {
                SparklineChart(
                    points   = sparkPoints,
                    color    = sparkColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .padding(top = 4.dp),
                    showArea = true
                )
            }
            if (!isFlat) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = arrowIcon,
                        contentDescription = null,
                        tint               = arrowColor,
                        modifier           = Modifier.size(16.dp)
                    )
                    Text(
                        text  = "vs last month",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}