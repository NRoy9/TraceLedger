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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.navigation.Routes
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed

@Composable
fun BottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAddTransaction: () -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF141414)
    ) {

        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = NothingRed,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = NothingRed.copy(alpha = 0.15f)
        )

        // 1️⃣ DASHBOARD
        NavigationBarItem(
            selected = currentRoute == Routes.DASHBOARD,
            onClick = { onNavigate(Routes.DASHBOARD) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            colors = itemColors
        )

        // 2️⃣ TRANSACTIONS
        NavigationBarItem(
            selected = currentRoute == Routes.TRANSACTIONS,
            onClick = { onNavigate(Routes.TRANSACTIONS) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Transactions") },
            colors = itemColors
        )

        // 3️⃣ CENTER ADD TRANSACTION BUTTON (RED)
        Box(
            modifier = Modifier
                .size(56.dp)
                //.offset(y = (-8 ).dp) // lift above bar
                .shadow(8.dp, CircleShape)
                .background(NothingRed, CircleShape)
                .clickable { onAddTransaction() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Transaction",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // 4️⃣ STATISTICS
        NavigationBarItem(
            //selected = currentRoute?.startsWith(Routes.STATISTICS) == true,
            selected = currentRoute?.startsWith(Routes.STATISTICS) == true,
            onClick = { onNavigate(Routes.STATISTICS) },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Statistics") },
            colors = itemColors
        )

        // 5️⃣ SETTINGS
        NavigationBarItem(
            selected = currentRoute == Routes.SETTINGS,
            onClick = { onNavigate(Routes.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            colors = itemColors
        )
    }
}
