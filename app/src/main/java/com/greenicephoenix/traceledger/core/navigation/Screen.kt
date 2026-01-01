package com.greenicephoenix.traceledger.core.navigation

/**
 * Defines all navigation destinations in the app.
 * Each screen has a unique route.
 */
sealed class Screen(val route: String) {

    object Dashboard : Screen("dashboard")
    object Accounts : Screen("accounts")
    object History : Screen("history")
    object Statistics : Screen("statistics")
    object AddTransaction : Screen("add_transaction")
    object AddEditAccount : Screen("add_edit_account")
    object Settings : Screen("settings")
}
