package com.greenicephoenix.traceledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.greenicephoenix.traceledger.core.navigation.Routes
import com.greenicephoenix.traceledger.core.navigation.TraceLedgerNavGraph
import com.greenicephoenix.traceledger.core.ui.components.BottomBar
import com.greenicephoenix.traceledger.core.ui.theme.TraceLedgerTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TraceLedgerTheme {

                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                // Screens that show top settings bar
                val showTopBar = currentRoute in listOf(
                    Routes.DASHBOARD,
                    Routes.ACCOUNTS,
                    Routes.TRANSACTIONS,
                    Routes.STATISTICS
                )

                // Screens that show bottom nav bar
                val showBottomBar = currentRoute in listOf(
                    Routes.DASHBOARD,
                    Routes.ACCOUNTS,
                    Routes.TRANSACTIONS,
                    Routes.STATISTICS
                )

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(Routes.DASHBOARD) {
                                            saveState = true
                                        }
                                    }
                                },
                                onAddTransaction = {
                                    navController.navigate(Routes.ADD_TRANSACTION)
                                }
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        TraceLedgerNavGraph(
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
