package com.greenicephoenix.traceledger.feature.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.TraceLedgerApp
import com.greenicephoenix.traceledger.core.backup.AutoBackupScheduler
import com.greenicephoenix.traceledger.core.backup.BackupFrequency
import com.greenicephoenix.traceledger.core.datastore.SettingsDataStore
import com.greenicephoenix.traceledger.core.export.ExportFormat
import com.greenicephoenix.traceledger.core.importer.ParsedImportData
import com.greenicephoenix.traceledger.core.importer.UnifiedCsvParser
import kotlinx.coroutines.launch

// Accent colours — consistent with SettingsScreen
private val IconGreen  = Color(0xFF2ECC71)
private val IconBlue   = Color(0xFF2196F3)
private val IconAmber  = Color(0xFFF59E0B)
private val IconPurple = Color(0xFF9575CD)
private val IconRed    = Color(0xFFE53935)
private val BgGreen    = IconGreen.copy(alpha = 0.12f)
private val BgBlue     = IconBlue.copy(alpha = 0.12f)
private val BgAmber    = IconAmber.copy(alpha = 0.12f)
private val BgPurple   = IconPurple.copy(alpha = 0.12f)
private val BgRed      = IconRed.copy(alpha = 0.12f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    onBack:            () -> Unit,
    onNavigateToCsvMapping: (ParsedImportData) -> Unit,
    onJsonImportPreviewRequested: suspend (Uri) -> com.greenicephoenix.traceledger.core.importer.ImportPreview,
    onJsonImportConfirmed:        (Uri, (Int?) -> Unit) -> Unit,
    onImportError:     (String) -> Unit
) {
    val context        = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsStore  = remember { SettingsDataStore(context) }
    val snackbarState  = remember { SnackbarHostState() }

    // Auto backup state
    val autoBackupEnabled   by settingsStore.autoBackupEnabled.collectAsState(initial = false)
    val autoBackupFreqName  by settingsStore.autoBackupFrequency.collectAsState(initial = "WEEKLY")
    val autoBackupFolderUri by settingsStore.autoBackupFolderUri.collectAsState(initial = null)
    val autoBackupFrequency  = BackupFrequency.fromName(autoBackupFreqName)

    // Sheet visibility
    var showBackupFreqSheet by remember { mutableStateOf(false) }
    var showJsonPreview     by remember { mutableStateOf(false) }
    var pendingJsonUri      by remember { mutableStateOf<Uri?>(null) }
    var jsonPreview         by remember {
        mutableStateOf<com.greenicephoenix.traceledger.core.importer.ImportPreview?>(null)
    }
    var importProgress      by remember { mutableStateOf<Int?>(null) }

    // ── File launchers ────────────────────────────────────────────────────────

    val exportJsonLauncher = rememberLauncherForActivityResult(CreateDocument("application/json")) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val result = (context.applicationContext as TraceLedgerApp)
                        .container.exportService.export(ExportFormat.JSON, uri)
                    val kb = result.fileSizeBytes / 1024
                    snackbarState.showSnackbar("Backup saved — ${result.fileName} (${kb} KB)")
                } catch (e: Exception) {
                    snackbarState.showSnackbar("Export failed: ${e.message}")
                }
            }
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val result = (context.applicationContext as TraceLedgerApp)
                        .container.exportService.exportUnifiedCsv(uri)
                    val kb = result.fileSizeBytes / 1024
                    snackbarState.showSnackbar("CSV saved — ${result.fileName} (${kb} KB)")
                } catch (e: Exception) {
                    snackbarState.showSnackbar("Export failed: ${e.message}")
                }
            }
        }
    }

    val templateLauncher = rememberLauncherForActivityResult(CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    (context.applicationContext as TraceLedgerApp)
                        .container.exportService.exportTemplate(uri)
                    snackbarState.showSnackbar("Template downloaded — fill it in and import via CSV")
                } catch (e: Exception) {
                    snackbarState.showSnackbar("Download failed: ${e.message}")
                }
            }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    jsonPreview    = onJsonImportPreviewRequested(uri)
                    pendingJsonUri = uri
                    showJsonPreview = true
                } catch (e: Exception) {
                    onImportError(e.message ?: "Invalid JSON file")
                }
            }
        }
    }

    val importCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    // Parse on IO thread
                    val parsed = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        UnifiedCsvParser(context.contentResolver).parse(uri)
                    }
                    if (parsed.transactions.isEmpty() && parsed.derivedAccounts.isEmpty()) {
                        snackbarState.showSnackbar("No valid rows found in CSV")
                    } else {
                        onNavigateToCsvMapping(parsed)
                    }
                } catch (e: Exception) {
                    onImportError(e.message ?: "Invalid CSV file")
                }
            }
        }
    }

    val backupFolderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            coroutineScope.launch {
                settingsStore.setAutoBackupFolderUri(uri.toString())
                // Always enable backup after folder is picked —
                // user picked a folder specifically to enable backup
                settingsStore.setAutoBackupEnabled(true)
                AutoBackupScheduler.schedule(context, autoBackupFrequency)
                snackbarState.showSnackbar("Auto backup enabled — ${autoBackupFrequency.label}")
            }
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 80.dp)
        ) {

            // Top bar
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "IMPORT / EXPORT",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // ── EXPORT ───────────────────────────────────────────────────────
            IESectionLabel("Export")

            IECard(
                icon     = Icons.Outlined.Backup,
                iconTint = IconBlue,
                iconBg   = BgBlue,
                title    = "JSON Backup",
                subtitle = "Full backup — accounts, categories, budgets, transactions",
                badge    = "RECOMMENDED",
                badgeColor = IconBlue,
                onClick  = { exportJsonLauncher.launch("TraceLedger-backup.json") }
            )
            Spacer(Modifier.height(8.dp))
            IECard(
                icon     = Icons.Outlined.TableChart,
                iconTint = IconGreen,
                iconBg   = BgGreen,
                title    = "CSV Export",
                subtitle = "All transactions in unified format — re-importable",
                onClick  = { exportCsvLauncher.launch("TraceLedger-export.csv") }
            )
            Spacer(Modifier.height(8.dp))
            IECard(
                icon     = Icons.Outlined.FileDownload,
                iconTint = IconAmber,
                iconBg   = BgAmber,
                title    = "Download CSV Template",
                subtitle = "Fill in manually and import — includes instructions",
                onClick  = { templateLauncher.launch("TraceLedger-import-template.csv") }
            )

            Spacer(Modifier.height(24.dp))

            // ── IMPORT ───────────────────────────────────────────────────────
            IESectionLabel("Import")

            // Balance warning
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = IconAmber.copy(alpha = 0.10f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier          = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint     = IconAmber,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Text(
                        text  = "Account balances are calculated from imported transactions and may be inaccurate. Please verify in the Accounts screen after import.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            IECard(
                icon     = Icons.Outlined.RestorePage,
                iconTint = IconBlue,
                iconBg   = BgBlue,
                title    = "JSON Restore",
                subtitle = "Merges backup — skips duplicate accounts, categories, transactions",
                badge    = "RECOMMENDED",
                badgeColor = IconBlue,
                onClick  = { importJsonLauncher.launch(arrayOf("application/json", "*/*")) }
            )
            Spacer(Modifier.height(8.dp))
            IECard(
                icon     = Icons.Outlined.Upload,
                iconTint = IconGreen,
                iconBg   = BgGreen,
                title    = "CSV Import",
                subtitle = "Map accounts & categories then import transactions",
                onClick  = {
                    importCsvLauncher.launch(arrayOf(
                        "text/csv",
                        "text/comma-separated-values",
                        "application/csv",
                        "application/octet-stream",
                        "*/*"
                    ))
                }
            )

            Spacer(Modifier.height(24.dp))

            // ── AUTO BACKUP ──────────────────────────────────────────────────
            IESectionLabel("Auto Backup")

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier              = Modifier.weight(1f)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(BgPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.CloudUpload, null,
                            tint = IconPurple, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("Periodic Backup",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text  = if (autoBackupEnabled)
                                "${autoBackupFrequency.label} · ${if (autoBackupFolderUri != null) "Folder set" else "No folder"}"
                            else
                                "Automatically save JSON backup",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Switch(
                    checked = autoBackupEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (autoBackupFolderUri == null) {
                                // Launch folder picker directly — not inside a coroutine
                                backupFolderLauncher.launch(null)
                            } else {
                                coroutineScope.launch {
                                    settingsStore.setAutoBackupEnabled(true)
                                    AutoBackupScheduler.schedule(context, autoBackupFrequency)
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                settingsStore.setAutoBackupEnabled(false)
                                AutoBackupScheduler.cancel(context)
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            if (autoBackupEnabled) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IEChip(
                        modifier = Modifier.weight(1f),
                        label    = "FOLDER",
                        value    = if (autoBackupFolderUri != null) "Set" else "Not set",
                        color    = if (autoBackupFolderUri != null) IconGreen else IconRed,
                        onClick  = { backupFolderLauncher.launch(null) }
                    )
                    IEChip(
                        modifier = Modifier.weight(1f),
                        label    = "FREQUENCY",
                        value    = autoBackupFrequency.label,
                        color    = IconPurple,
                        onClick  = { showBackupFreqSheet = true }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        SnackbarHost(
            hostState = snackbarState,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )
    }

    // ── JSON preview sheet ────────────────────────────────────────────────────
    if (showJsonPreview && jsonPreview != null) {
        ModalBottomSheet(onDismissRequest = {
            showJsonPreview = false
            jsonPreview     = null
            pendingJsonUri  = null
        }) {
            val preview   = jsonPreview!!
            val canImport = preview.transactions > 0 || preview.accounts > 0

            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("JSON Restore Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)

                if (preview.accounts > 0)     PreviewRow("Accounts",              preview.accounts)
                if (preview.categories > 0)   PreviewRow("Categories",            preview.categories)
                if (preview.budgets > 0)       PreviewRow("Budgets",               preview.budgets)
                if (preview.recurring > 0)     PreviewRow("Recurring rules",       preview.recurring)
                if (preview.templates > 0)     PreviewRow("Templates",             preview.templates)
                if (preview.transactions > 0)  PreviewRow("Transactions",          preview.transactions)

                // Balance warning
                Text(
                    text  = "Existing accounts and transactions with matching names or IDs will not be duplicated. Account balances are restored from the backup — verify after import.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        showJsonPreview = false
                        jsonPreview     = null
                        pendingJsonUri  = null
                    }) { Text("Cancel") }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        enabled  = canImport,
                        onClick  = {
                            val uri = pendingJsonUri ?: return@Button
                            showJsonPreview = false
                            onJsonImportConfirmed(uri) { p -> importProgress = p }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Restore") }
                }
            }
        }
    }

    // Import progress overlay
    importProgress?.let { progress ->
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier            = Modifier.padding(40.dp)
            ) {
                Text("Importing…", style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("$progress%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }

    LaunchedEffect(importProgress) {
        if (importProgress != null && importProgress!! >= 100) {
            kotlinx.coroutines.delay(400)
            importProgress = null
            pendingJsonUri = null
            snackbarState.showSnackbar("Restore completed")
        }
    }

    // Backup frequency sheet
    if (showBackupFreqSheet) {
        ModalBottomSheet(onDismissRequest = { showBackupFreqSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Backup Frequency",
                    style    = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp))
                BackupFrequency.entries.forEach { freq ->
                    val selected = freq == autoBackupFrequency
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable {
                                showBackupFreqSheet = false
                                coroutineScope.launch {
                                    settingsStore.setAutoBackupFrequency(freq.name)
                                    if (autoBackupEnabled) AutoBackupScheduler.schedule(context, freq)
                                    snackbarState.showSnackbar("Frequency set to ${freq.label}")
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(freq.label,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium)
                        if (selected) Icon(Icons.Outlined.Check, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Private components ────────────────────────────────────────────────────────

@Composable
private fun IESectionLabel(text: String) {
    Text(
        text     = text.uppercase(),
        style    = MaterialTheme.typography.labelSmall,
        color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun IECard(
    icon:        ImageVector,
    iconTint:    Color,
    iconBg:      Color,
    title:       String,
    subtitle:    String,
    badge:       String?   = null,
    badgeColor:  Color     = Color.Unspecified,
    onClick:     () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(title,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.onSurface)
                    if (badge != null) {
                        Text(
                            text  = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            modifier = Modifier
                                .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            }
            Icon(Icons.Outlined.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun IEChip(
    modifier: Modifier,
    label:    String,
    value:    String,
    color:    Color,
    onClick:  () -> Unit
) {
    Card(
        modifier  = modifier.clickable { onClick() },
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Text(value,
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color      = color)
        }
    }
}

@Composable
private fun PreviewRow(label: String, count: Int) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface)
        Text(count.toString(),
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.primary)
    }
}