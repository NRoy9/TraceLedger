package com.greenicephoenix.traceledger.core.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.greenicephoenix.traceledger.core.database.TraceLedgerDatabase
import com.greenicephoenix.traceledger.core.datastore.SettingsDataStore
import com.greenicephoenix.traceledger.core.export.ExportService
import com.greenicephoenix.traceledger.core.importer.ImportService
import com.greenicephoenix.traceledger.core.backup.AutoBackupScheduler
import com.greenicephoenix.traceledger.core.backup.BackupFrequency
import kotlinx.coroutines.flow.first
import com.greenicephoenix.traceledger.core.recurring.RecurringTransactionGenerator
import com.greenicephoenix.traceledger.core.repository.AccountRepository
import com.greenicephoenix.traceledger.core.repository.CategoryRepository
import com.greenicephoenix.traceledger.core.repository.RecurringTransactionRepository
import com.greenicephoenix.traceledger.core.repository.TransactionRepository
import com.greenicephoenix.traceledger.feature.accounts.AccountsViewModelFactory
import com.greenicephoenix.traceledger.feature.budgets.data.BudgetRepository
import com.greenicephoenix.traceledger.feature.categories.CategoriesViewModelFactory
import com.greenicephoenix.traceledger.feature.dashboard.DashboardViewModelFactory
import com.greenicephoenix.traceledger.feature.recurring.AddEditRecurringViewModelFactory
import com.greenicephoenix.traceledger.feature.recurring.RecurringTransactionsViewModelFactory
import com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModelFactory
import com.greenicephoenix.traceledger.feature.templates.data.TemplateRepository
import com.greenicephoenix.traceledger.feature.templates.TemplatesViewModelFactory
// v1.3.0 imports
import com.greenicephoenix.traceledger.feature.accountimport.repository.StatementImportRepository
import com.greenicephoenix.traceledger.feature.accountimport.viewmodel.StatementImportViewModelFactory
import com.greenicephoenix.traceledger.feature.sms.repository.SmsQueueRepository
import com.greenicephoenix.traceledger.feature.sms.repository.SmsRuleRepository
import com.greenicephoenix.traceledger.feature.sms.store.SmsLearningStore
import com.greenicephoenix.traceledger.feature.sms.viewmodel.SmsReviewViewModel
import com.greenicephoenix.traceledger.feature.sms.viewmodel.SmsSettingsViewModel
import com.greenicephoenix.traceledger.feature.sms.viewmodel.CustomRulesViewModel
import com.greenicephoenix.traceledger.feature.sms.viewmodel.AddEditRuleViewModel

class AppContainer(private val context: Context) {

    private val database = TraceLedgerDatabase.getInstance(context)

    val settingsDataStore: SettingsDataStore = SettingsDataStore(context)

    val accountRepository: AccountRepository =
        AccountRepository(database.accountDao())

    val transactionRepository: TransactionRepository =
        TransactionRepository(
            database       = database,
            transactionDao = database.transactionDao(),
            accountDao     = database.accountDao()
        )

    val categoryRepository: CategoryRepository =
        CategoryRepository(database.categoryDao())

    val budgetRepository: BudgetRepository by lazy {
        BudgetRepository(database.budgetDao())
    }

    val recurringTransactionRepository: RecurringTransactionRepository by lazy {
        RecurringTransactionRepository(
            recurringDao   = database.recurringTransactionDao(),
            transactionDao = database.transactionDao()
        )
    }

    val templateRepository: TemplateRepository by lazy {
        TemplateRepository(database.transactionTemplateDao())
    }

    // ── v1.3.0: Statement Import ──────────────────────────────────────────────
    // StatementImportRepository handles all DB writes for the import feature:
    // bulk inserts, duplicate checks, and balance strategy execution.
    val statementImportRepository: StatementImportRepository by lazy {
        StatementImportRepository(
            database       = database,
            transactionDao = database.transactionDao(),
            accountDao     = database.accountDao()
        )
    }

    val exportService by lazy {
        ExportService(
            database        = database,
            contentResolver = context.contentResolver,
            settingsStore   = settingsDataStore
        )
    }

    val importService by lazy {
        ImportService(
            database        = database,
            contentResolver = context.contentResolver,
            settingsStore   = settingsDataStore
        )
    }

    /**
     * Called from TraceLedgerApp.onCreate() inside a coroutine.
     * Re-registers the periodic backup work on every app start so the schedule
     * survives app updates (WorkManager clears jobs on APK update on some devices).
     * If backup is disabled or no folder is set, this is a no-op.
     */
    suspend fun scheduleBackupIfEnabled() {
        val enabled   = settingsDataStore.autoBackupEnabled.first()
        val freqName  = settingsDataStore.autoBackupFrequency.first()
        val folderUri = settingsDataStore.autoBackupFolderUri.first()
        if (enabled && folderUri != null) {
            AutoBackupScheduler.schedule(context, BackupFrequency.fromName(freqName))
        }
    }

    val recurringGenerator: RecurringTransactionGenerator by lazy {
        RecurringTransactionGenerator(
            recurringRepository   = recurringTransactionRepository,
            transactionRepository = transactionRepository
        )
    }

    // ── ViewModel factories ───────────────────────────────────────────────────

    val accountsViewModelFactory   = AccountsViewModelFactory(accountRepository)
    val categoriesViewModelFactory = CategoriesViewModelFactory(categoryRepository)
    val statisticsViewModelFactory by lazy {
        StatisticsViewModelFactory(
            transactionRepository = transactionRepository,
            budgetRepository      = budgetRepository,
            accountRepository     = accountRepository,
            recurringRepository   = recurringTransactionRepository,
            categoryRepository    = categoryRepository               // ADD
        )
    }

    val dashboardViewModelFactory by lazy {
        DashboardViewModelFactory(
            transactionRepository = transactionRepository,
            recurringRepository   = recurringTransactionRepository,
            smsQueueRepository    = smsQueueRepository   // ← ADDED
        )
    }

    val recurringViewModelFactory  = RecurringTransactionsViewModelFactory(recurringTransactionRepository)
    val addEditRecurringFactory    = AddEditRecurringViewModelFactory(recurringTransactionRepository)

    val templatesViewModelFactory by lazy {
        TemplatesViewModelFactory(templateRepository)
    }

    // ── v1.3.0: Statement import ViewModel factory ────────────────────────────
    // Returns a new factory each time — the factory is lightweight.
    // The ViewModel itself is scoped to the NavBackStackEntry (in NavGraph),
    // so it lives exactly as long as the review screen.
    val statementImportViewModelFactory: StatementImportViewModelFactory
        get() = StatementImportViewModelFactory(
            appContext       = context.applicationContext,
            importRepository = statementImportRepository
        )

    // --- SMS Repositories ---
    val smsQueueRepository: SmsQueueRepository by lazy {
        SmsQueueRepository(
            smsPendingDao    = database.smsPendingTransactionDao(),
            smsCustomRuleDao = database.smsCustomRuleDao(),
            transactionDao   = database.transactionDao(),
            accountDao       = database.accountDao(),
            categoryDao      = database.categoryDao(),
            context          = context,
            learningStore    = smsLearningStore,   // ← ADD
        )
    }

    val smsRuleRepository: SmsRuleRepository by lazy {
        SmsRuleRepository(dao = database.smsCustomRuleDao())
    }

    val smsLearningStore: SmsLearningStore by lazy {
        SmsLearningStore(context)
    }

    // --- SMS ViewModel Factories ---
    val smsSettingsViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SmsSettingsViewModel(
                application = context.applicationContext as Application,
                smsQueueRepository = smsQueueRepository,
                settingsDataStore = settingsDataStore,
            ) as T
    }

    val smsReviewViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SmsReviewViewModel(
                repository    = smsQueueRepository,
                learningStore = smsLearningStore,   // ← ADD
            ) as T
    }

    val customRulesViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CustomRulesViewModel(smsRuleRepository) as T
    }

    val addEditRuleViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AddEditRuleViewModel(smsRuleRepository) as T
    }
}