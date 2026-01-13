package com.greenicephoenix.traceledger.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.greenicephoenix.traceledger.feature.accounts.AccountsScreen
import com.greenicephoenix.traceledger.feature.accounts.AddEditAccountScreen
import com.greenicephoenix.traceledger.feature.dashboard.DashboardScreen
import com.greenicephoenix.traceledger.feature.statistics.StatisticsScreen
import com.greenicephoenix.traceledger.feature.addtransaction.AddTransactionScreen
import com.greenicephoenix.traceledger.feature.transactions.HistoryScreen
import com.greenicephoenix.traceledger.feature.accounts.AccountsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.greenicephoenix.traceledger.feature.categories.CategoriesScreen
import com.greenicephoenix.traceledger.feature.categories.CategoriesViewModel
import com.greenicephoenix.traceledger.feature.settings.SettingsScreen
import com.greenicephoenix.traceledger.feature.categories.AddEditCategoryScreen
import com.greenicephoenix.traceledger.feature.addtransaction.AddTransactionViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.greenicephoenix.traceledger.TraceLedgerApp
import com.greenicephoenix.traceledger.feature.addtransaction.AddTransactionViewModelFactory
import com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModel
import com.greenicephoenix.traceledger.feature.transactions.TransactionsViewModel
import com.greenicephoenix.traceledger.feature.transactions.TransactionsViewModelFactory


/**
 * Central navigation graph for TraceLedger
 * This file owns ALL navigation decisions.
 */
@Composable
fun TraceLedgerNavGraph(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    val categoriesViewModel: CategoriesViewModel = viewModel()
    val categories by categoriesViewModel.categories.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {

        /* ---------------- DASHBOARD ---------------- */
        composable(Routes.DASHBOARD) {

            val context = LocalContext.current
            val app = context.applicationContext as TraceLedgerApp

            val statisticsViewModel =
                viewModel<com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModel>(
                    factory = app.container.statisticsViewModelFactory
                )

            val accountsViewModel: AccountsViewModel = viewModel()
            val accounts by accountsViewModel.accounts.collectAsState()

            DashboardScreen(
                accounts = accounts,
                statisticsViewModel = statisticsViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onAddAccount = { navController.navigate(Routes.ADD_ACCOUNT) },
                onAccountClick = { account ->
                    navController.navigate(
                        Routes.EDIT_ACCOUNT.replace("{accountId}", account.id)
                    )
                }
            )
        }

        /* ---------------- ACCOUNTS ---------------- */
        composable(Routes.ACCOUNTS) {

            val accountsViewModel: AccountsViewModel = viewModel()
            val accounts by accountsViewModel.accounts.collectAsState()

            AccountsScreen(
                accounts = accounts,
                onBack = { navController.popBackStack() },
                onAddAccount = { navController.navigate(Routes.ADD_ACCOUNT) },
                onAccountClick = { account ->
                    navController.navigate(
                        Routes.EDIT_ACCOUNT.replace("{accountId}", account.id)
                    )
                },
                onAccountLongPress = { account ->
                    accountsViewModel.deleteAccount(account.id)
                    true
                }
            )
        }

        /* ---------------- ADD ACCOUNT ---------------- */
        composable(Routes.ADD_ACCOUNT) {

            val accountsViewModel: AccountsViewModel = viewModel()

            AddEditAccountScreen(
                existingAccount = null,
                onCancel = { navController.popBackStack() },
                onSave = { newAccount ->
                    accountsViewModel.addAccount(newAccount)
                    navController.popBackStack()
                }
            )
        }

        /* ---------------- EDIT ACCOUNT ---------------- */
        composable(
            route = Routes.EDIT_ACCOUNT,
            arguments = listOf(
                androidx.navigation.navArgument("accountId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->

            val accountsViewModel: AccountsViewModel = viewModel()
            val accounts by accountsViewModel.accounts.collectAsState()

            val accountId = backStackEntry.arguments?.getString("accountId")
            val accountToEdit = accounts.firstOrNull { it.id == accountId }

            AddEditAccountScreen(
                existingAccount = accountToEdit,
                onCancel = { navController.popBackStack() },
                onSave = { updatedAccount ->
                    accountsViewModel.updateAccount(updatedAccount)
                    navController.popBackStack()
                }
            )
        }

        /* ---------------- TRANSACTIONS LIST ---------------- */
        composable(Routes.TRANSACTIONS) {

            val context = LocalContext.current
            val app = context.applicationContext as TraceLedgerApp

            val transactionsViewModel: TransactionsViewModel =
                viewModel(
                    factory = TransactionsViewModelFactory(
                        transactionRepository = app.container.transactionRepository
                    )
                )

            val accountsViewModel: AccountsViewModel = viewModel()
            val categoriesViewModel: CategoriesViewModel = viewModel()

            HistoryScreen(
                viewModel = transactionsViewModel,
                accounts = accountsViewModel.accounts.collectAsState().value,
                categories = categoriesViewModel.categories.collectAsState().value,
                onBack = { navController.popBackStack() },
                onEditTransaction = { transactionId ->
                    navController.navigate(
                        Routes.EDIT_TRANSACTION.replace("{transactionId}", transactionId)
                    )
                }
            )

        }

        /* ---------------- ADD TRANSACTION ---------------- */
        composable(Routes.ADD_TRANSACTION) {

            val scope = rememberCoroutineScope()

            val context = LocalContext.current
            val app = context.applicationContext as TraceLedgerApp

            // ✅ Create ViewModel FIRST
            val addTransactionViewModel: AddTransactionViewModel =
                viewModel(
                    factory = AddTransactionViewModelFactory(
                        transactionRepository = app.container.transactionRepository
                    )
                )

            // ✅ Collect state
            val state by addTransactionViewModel.state.collectAsState()

            // ✅ Accounts are NOT in scope here — fetch them explicitly
            val accountsViewModel: AccountsViewModel = viewModel()
            val accounts by accountsViewModel.accounts.collectAsState()

            // ✅ UI
            AddTransactionScreen(
                state = state,
                accounts = accounts,
                categories = categories,
                isEditMode = false,
                onEvent = addTransactionViewModel::onEvent,
                onCancel = { navController.popBackStack() }
            )

            // ✅ Save-complete side effect
            LaunchedEffect(state.saveCompleted) {
                if (state.saveCompleted) {
                    navController.popBackStack()

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Transaction added"
                        )
                    }

                    addTransactionViewModel.consumeSaveCompleted()
                }
            }
        }

        /* ---------------- EDIT TRANSACTION ---------------- */
        composable(
            route = Routes.EDIT_TRANSACTION,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val transactionId =
                backStackEntry.arguments?.getString("transactionId") ?: return@composable

            val context = LocalContext.current
            val app = context.applicationContext as TraceLedgerApp

            val viewModel: AddTransactionViewModel =
                viewModel(
                    factory = AddTransactionViewModelFactory(
                        transactionRepository = app.container.transactionRepository
                    )
                )

            LaunchedEffect(transactionId) {
                viewModel.initEdit(transactionId)
            }

            val state by viewModel.state.collectAsState()

            val accountsViewModel: AccountsViewModel = viewModel()
            val accounts by accountsViewModel.accounts.collectAsState()

            AddTransactionScreen(
                state = state,
                accounts = accounts,
                categories = categories,
                isEditMode = true,
                onEvent = viewModel::onEvent,
                onCancel = { navController.popBackStack() }
            )

            // 🔑 THIS WAS MISSING
            val scope = rememberCoroutineScope()

            LaunchedEffect(state.saveCompleted) {
                if (state.saveCompleted) {

                    navController.popBackStack()

                    scope.launch {
                        snackbarHostState.showSnackbar("Transaction updated")
                    }

                    viewModel.consumeSaveCompleted()
                }
            }
        }

        /* ---------------- STATISTICS ---------------- */
        composable(Routes.STATISTICS) {
            StatisticsScreen()
        }


        /* ---------------- SETTINGS ---------------- */
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        /* ---------------- CATEGORIES ---------------- */
        composable(Routes.CATEGORIES) {

            CategoriesScreen(
                categories = categories,
                onBack = { navController.popBackStack() },
                onAddCategory = {
                    navController.navigate(Routes.ADD_CATEGORY)
                },
                onCategoryClick = { category ->
                    navController.navigate(
                        Routes.EDIT_CATEGORY.replace("{categoryId}", category.id)
                    )
                }
            )
        }

        composable(Routes.ADD_CATEGORY) {
            AddEditCategoryScreen(
                existingCategory = null,
                onCancel = { navController.popBackStack() },
                onSave = { newCategory ->
                    categoriesViewModel.addCategory(newCategory)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.EDIT_CATEGORY,
            arguments = listOf(
                androidx.navigation.navArgument("categoryId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->

            val categoryId = backStackEntry.arguments?.getString("categoryId")
            val categoryToEdit = categories.firstOrNull { it.id == categoryId }

            AddEditCategoryScreen(
                existingCategory = categoryToEdit,
                onCancel = { navController.popBackStack() },
                onSave = { updatedCategory ->
                    categoriesViewModel.updateCategory(updatedCategory)
                    navController.popBackStack()
                }
            )
        }

    }
}
