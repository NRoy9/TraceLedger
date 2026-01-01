package com.greenicephoenix.traceledger.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.greenicephoenix.traceledger.core.navigation.Routes
import com.greenicephoenix.traceledger.feature.accounts.AccountsScreen
import com.greenicephoenix.traceledger.feature.accounts.AddEditAccountScreen
import com.greenicephoenix.traceledger.feature.dashboard.DashboardScreen
import com.greenicephoenix.traceledger.feature.statistics.StatisticsScreen
import com.greenicephoenix.traceledger.feature.addtransaction.AddTransactionScreen
import com.greenicephoenix.traceledger.feature.transactions.HistoryScreen
import com.greenicephoenix.traceledger.domain.model.AccountUiModel
import com.greenicephoenix.traceledger.domain.model.AccountType
import com.greenicephoenix.traceledger.feature.accounts.AccountsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel



/**
 * Central navigation graph for TraceLedger
 * This file owns ALL navigation decisions.
 */
@Composable
fun TraceLedgerNavGraph(
    navController: NavHostController
) {
    val accountsViewModel: AccountsViewModel = viewModel()
    val accounts by accountsViewModel.accounts.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {

        /* ---------------- DASHBOARD ---------------- */
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                accounts = accounts,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onAddAccount = {
                    navController.navigate(Routes.ADD_ACCOUNT)
                }
            )
        }

        /* ---------------- ACCOUNTS ---------------- */
        composable(Routes.ACCOUNTS) {
            AccountsScreen(
                accounts = accounts,
                onBack = { navController.popBackStack() },
                onAddAccount = { navController.navigate(Routes.ADD_ACCOUNT) },
                onAccountClick = { account ->
                    navController.navigate("${Routes.ADD_ACCOUNT}/${account.id}")
                }
            )
        }

        /* ---------------- ADD ACCOUNT ---------------- */
        composable(Routes.ADD_ACCOUNT) {
            AddEditAccountScreen(
                onCancel = {
                    navController.popBackStack()
                },
                onSave = { newAccount ->

                    // ✅ ADD ACCOUNT TO VIEWMODEL
                    accountsViewModel.addAccount(newAccount)

                    // ✅ GO BACK
                    navController.popBackStack()
                }
            )
        }

        /* ---------------- TRANSACTIONS LIST ---------------- */
        composable(Routes.TRANSACTIONS) {
            HistoryScreen()
        }

        /* ---------------- ADD TRANSACTION ---------------- */
        composable(Routes.ADD_TRANSACTION) {
            // IMPORTANT:
            // This matches your EXISTING screen signature
            AddTransactionScreen()
        }

        /* ---------------- STATISTICS ---------------- */
        composable(Routes.STATISTICS) {
            StatisticsScreen()
        }
    }
}
