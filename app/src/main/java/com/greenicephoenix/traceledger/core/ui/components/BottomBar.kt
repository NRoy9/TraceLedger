package com.greenicephoenix.traceledger.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.navigation.Routes

@Composable
fun BottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAddTransaction: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {

        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        )

        // DASHBOARD
        NavigationBarItem(
            selected = currentRoute == Routes.DASHBOARD,
            onClick = { onNavigate(Routes.DASHBOARD) },
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Dashboard"
                )
            },
            colors = itemColors
        )

        // TRANSACTIONS
        NavigationBarItem(
            selected = currentRoute == Routes.TRANSACTIONS,
            onClick = { onNavigate(Routes.TRANSACTIONS) },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = "Transactions"
                )
            },
            colors = itemColors
        )

        // CENTER FAB STYLE ADD BUTTON
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(6.dp, CircleShape)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .clickable { onAddTransaction() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Transaction",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        // STATISTICS
        NavigationBarItem(
            selected = currentRoute?.startsWith(Routes.STATISTICS) == true,
            onClick = { onNavigate(Routes.STATISTICS) },
            icon = {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "Statistics"
                )
            },
            colors = itemColors
        )

        // SETTINGS
        NavigationBarItem(
            selected = currentRoute == Routes.SETTINGS,
            onClick = { onNavigate(Routes.SETTINGS) },
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            },
            colors = itemColors
        )
    }
}
