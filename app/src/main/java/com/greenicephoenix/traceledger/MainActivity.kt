package com.greenicephoenix.traceledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.greenicephoenix.traceledger.core.navigation.Routes
import com.greenicephoenix.traceledger.core.navigation.TraceLedgerNavGraph
import com.greenicephoenix.traceledger.core.ui.components.BottomBar
import com.greenicephoenix.traceledger.core.ui.theme.TraceLedgerTheme
import com.greenicephoenix.traceledger.core.currency.CurrencyManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔒 We handle system insets manually via Compose
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LaunchedEffect(Unit) {
                CurrencyManager.init(applicationContext)
            }

            TraceLedgerTheme {

                val navController = rememberNavController()
                // DO NOT read back stack before NavHost is attached
                var currentRoute by remember { mutableStateOf<String?>(null) }


                // Screens that show top settings bar
                val showTopBar = currentRoute in listOf(
                    Routes.DASHBOARD,
                    Routes.TRANSACTIONS,
                    Routes.STATISTICS,
                    Routes.SETTINGS
                )

                // Screens that show bottom nav bar
                val showBottomBar =
                    currentRoute?.let { route ->
                        route == Routes.DASHBOARD ||
                                route == Routes.TRANSACTIONS ||
                                route.startsWith(Routes.STATISTICS) ||
                                route == Routes.SETTINGS
                    } ?: false

                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
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
                    },
                    contentWindowInsets = WindowInsets.systemBars
                ) { paddingValues ->
                    Box(
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        TraceLedgerNavGraph(
                            navController = navController,
                            snackbarHostState = snackbarHostState
                        )
                        // SAFELY observe route AFTER graph is attached
                        val backStackEntry by navController.currentBackStackEntryAsState()
                        currentRoute = backStackEntry?.destination?.route
                    }
                }
            }
        }
    }
}
