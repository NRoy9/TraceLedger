package com.greenicephoenix.traceledger.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.TraceLedgerApp
import com.greenicephoenix.traceledger.core.importer.CsvImportMapping
import com.greenicephoenix.traceledger.core.importer.ParsedImportData
import kotlinx.coroutines.launch

private val IconGreen  = Color(0xFF2ECC71)
private val IconAmber  = Color(0xFFF59E0B)
private val IconRed    = Color(0xFFE53935)
private val BgGreen    = IconGreen.copy(alpha = 0.12f)
private val BgAmber    = IconAmber.copy(alpha = 0.12f)

// Sentinel value — means "create a new account/category"
private const val CREATE_NEW = "__CREATE_NEW__"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportMappingScreen(
    parsedData: ParsedImportData,
    onBack:     () -> Unit,
    onConfirm:  (CsvImportMapping) -> Unit
) {
    val context        = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarState  = remember { SnackbarHostState() }

    // Load existing accounts and categories once
    val container = remember { (context.applicationContext as TraceLedgerApp).container }
    val existingAccounts   by container.accountRepository.observeAccounts()
        .collectAsState(initial = emptyList())
    val existingCategories by container.categoryRepository.observeCategories()
        .collectAsState(initial = emptyList())

    // ── Mapping state ─────────────────────────────────────────────────────────
    // accountKey → selected existing ID, or CREATE_NEW, or null (unresolved)
    val accountMappings  = remember {
        mutableStateMapOf<String, String?>().also { map ->
            parsedData.derivedAccounts.forEach { da ->
                // Auto-map if exact name+type match found
                map[da.key] = null  // will be resolved once existingAccounts loads
            }
        }
    }
    val categoryMappings = remember {
        mutableStateMapOf<String, String?>().also { map ->
            parsedData.derivedCategories.forEach { dc ->
                map[dc.key] = null
            }
        }
    }

    // Auto-resolve mappings when existing data loads — runs in composable scope, not coroutine
    if (existingAccounts.isNotEmpty()) {
        val nameMap = existingAccounts.associate { it.name.trim().lowercase() to it.id }
        parsedData.derivedAccounts.forEach { da ->
            if (accountMappings[da.key] == null) {
                val matchedId = nameMap[da.name.trim().lowercase()]
                accountMappings[da.key] = matchedId ?: CREATE_NEW
            }
        }
    }

    if (existingCategories.isNotEmpty()) {
        val nameTypeMap = existingCategories.associate { cat ->
            val typeStr = cat.type
            "${cat.name.trim().lowercase()}|${typeStr}" to cat.id
        }
        parsedData.derivedCategories.forEach { dc ->
            if (categoryMappings[dc.key] == null) {
                val matchedId = nameTypeMap[dc.key]
                categoryMappings[dc.key] = matchedId ?: CREATE_NEW
            }
        }
    }

    // Sheet state for pickers
    var pickingAccountKey  by remember { mutableStateOf<String?>(null) }
    var pickingCategoryKey by remember { mutableStateOf<String?>(null) }

    // Validation — all mappings must be resolved
    val allResolved = accountMappings.values.all { it != null } &&
            categoryMappings.values.all { it != null }

    val newAccountCount  = accountMappings.values.count  { it == CREATE_NEW }
    val newCategoryCount = categoryMappings.values.count { it == CREATE_NEW }

    // ── UI ────────────────────────────────────────────────────────────────────

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {

            // Top bar
            item {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("MAP YOUR DATA",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground)
                        Text("${parsedData.transactions.size} transactions · ${parsedData.derivedAccounts.size} accounts · ${parsedData.derivedCategories.size} categories",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                }
            }

            // Balance warning
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = CardDefaults.cardColors(IconAmber.copy(alpha = 0.10f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier          = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Outlined.Warning, null,
                            tint     = IconAmber,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp))
                        Text(
                            "New accounts will be created with a balance calculated from your transactions. Please verify balances in the Accounts screen after import.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // Parse errors (if any)
            if (parsedData.parseErrors.isNotEmpty()) {
                item {
                    Card(
                        modifier  = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape     = RoundedCornerShape(12.dp),
                        colors    = CardDefaults.cardColors(IconRed.copy(alpha = 0.08f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${parsedData.parseErrors.size} row(s) skipped",
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color      = IconRed)
                            parsedData.parseErrors.take(5).forEach { err ->
                                Text(err,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            if (parsedData.parseErrors.size > 5) {
                                Text("… and ${parsedData.parseErrors.size - 5} more",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }

            // ── ACCOUNTS section ──────────────────────────────────────────────
            item {
                MappingSectionHeader(
                    title    = "ACCOUNTS",
                    subtitle = "${parsedData.derivedAccounts.size} found in file"
                )
            }

            items(parsedData.derivedAccounts) { derived ->
                val selectedId = accountMappings[derived.key]
                val selectedAccount = existingAccounts.firstOrNull { it.id == selectedId }
                val isCreateNew = selectedId == CREATE_NEW

                MappingRow(
                    fromLabel   = derived.name,
                    fromSub     = derived.normalisedType,
                    toLabel     = when {
                        isCreateNew        -> "+ Create New"
                        selectedAccount != null -> selectedAccount.name
                        else               -> "Resolving…"
                    },
                    isCreateNew = isCreateNew,
                    isMatched   = selectedAccount != null,
                    onClick     = { pickingAccountKey = derived.key }
                )
                Spacer(Modifier.height(6.dp))
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── CATEGORIES section ────────────────────────────────────────────
            item {
                MappingSectionHeader(
                    title    = "CATEGORIES",
                    subtitle = "${parsedData.derivedCategories.size} found in file"
                )
            }

            items(parsedData.derivedCategories) { derived ->
                val selectedId = categoryMappings[derived.key]
                val selectedCategory = existingCategories.firstOrNull { it.id == selectedId }
                val isCreateNew = selectedId == CREATE_NEW

                MappingRow(
                    fromLabel   = derived.name,
                    fromSub     = derived.forType,
                    toLabel     = when {
                        isCreateNew             -> "+ Create New"
                        selectedCategory != null -> selectedCategory.name
                        else                    -> "Resolving…"
                    },
                    isCreateNew = isCreateNew,
                    isMatched   = selectedCategory != null,
                    onClick     = { pickingCategoryKey = derived.key }
                )
                Spacer(Modifier.height(6.dp))
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── Summary ───────────────────────────────────────────────────────
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("IMPORT SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                        SummaryRow("Transactions", parsedData.transactions.size.toString())
                        if (newAccountCount  > 0) SummaryRow("New accounts",   "$newAccountCount will be created")
                        if (newCategoryCount > 0) SummaryRow("New categories", "$newCategoryCount will be created")
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        // Fixed bottom bar with Cancel + Import
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onBack,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp)
                ) { Text("Cancel") }

                Button(
                    onClick  = {
                        if (!allResolved) {
                            coroutineScope.launch {
                                snackbarState.showSnackbar("Please resolve all mappings before importing")
                            }
                            return@Button
                        }
                        val finalAccountMap  = accountMappings.mapValues  { (_, v) -> if (v == CREATE_NEW) null else v }
                        val finalCategoryMap = categoryMappings.mapValues { (_, v) -> if (v == CREATE_NEW) null else v }
                        onConfirm(CsvImportMapping(finalAccountMap, finalCategoryMap))
                    },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    enabled  = allResolved
                //) { Text("Import ${parsedData.transactions.size} Transactions") }
                ) { Text("Import") }
            }
        }

        SnackbarHost(
            hostState = snackbarState,
            modifier  = Modifier.align(Alignment.BottomCenter).padding(bottom = 70.dp)
        )
    }

    // ── Account picker sheet ──────────────────────────────────────────────────
    pickingAccountKey?.let { key ->
        val derived = parsedData.derivedAccounts.firstOrNull { it.key == key }
        ModalBottomSheet(onDismissRequest = { pickingAccountKey = null }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Map \"${derived?.name}\"",
                    style    = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp))

                // Create new option
                PickerOption(
                    label      = "+ Create New Account",
                    subtitle   = "A new account will be created with calculated balance",
                    isSelected = accountMappings[key] == CREATE_NEW,
                    color      = IconGreen,
                    onClick    = {
                        accountMappings[key!!] = CREATE_NEW
                        pickingAccountKey = null
                    }
                )

                Spacer(Modifier.height(8.dp))
                Text("OR MAP TO EXISTING",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 6.dp))

                existingAccounts.forEach { acct ->
                    PickerOption(
                        label      = acct.name,
                        subtitle   = acct.type.toString(),
                        isSelected = accountMappings[key] == acct.id,
                        color      = MaterialTheme.colorScheme.primary,
                        onClick    = {
                            accountMappings[key!!] = acct.id
                            pickingAccountKey = null
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ── Category picker sheet ─────────────────────────────────────────────────
    pickingCategoryKey?.let { key ->
        val derived = parsedData.derivedCategories.firstOrNull { it.key == key }
        val filteredCats = existingCategories.filter { it.type == derived?.forType }

        ModalBottomSheet(onDismissRequest = { pickingCategoryKey = null }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Map \"${derived?.name}\"",
                    style    = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp))

                PickerOption(
                    label      = "+ Create New Category",
                    subtitle   = "A new ${derived?.forType?.lowercase()} category will be created",
                    isSelected = categoryMappings[key] == CREATE_NEW,
                    color      = IconGreen,
                    onClick    = {
                        categoryMappings[key!!] = CREATE_NEW
                        pickingCategoryKey = null
                    }
                )

                Spacer(Modifier.height(8.dp))
                Text("OR MAP TO EXISTING ${derived?.forType?.uppercase()} CATEGORIES",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 6.dp))

                if (filteredCats.isEmpty()) {
                    Text("No existing ${derived?.forType?.lowercase()} categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(8.dp))
                }

                filteredCats.forEach { cat ->
                    PickerOption(
                        label      = cat.name,
                        subtitle   = cat.type,
                        isSelected = categoryMappings[key] == cat.id,
                        color      = Color(cat.color),
                        onClick    = {
                            categoryMappings[key!!] = cat.id
                            pickingCategoryKey = null
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Private components ────────────────────────────────────────────────────────

@Composable
private fun MappingSectionHeader(title: String, subtitle: String) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
        Text(subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
    }
}

@Composable
private fun MappingRow(
    fromLabel:   String,
    fromSub:     String,
    toLabel:     String,
    isCreateNew: Boolean,
    isMatched:   Boolean,
    onClick:     () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // FROM (CSV)
            Column(modifier = Modifier.weight(1f)) {
                Text(fromLabel,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurface)
                Text(fromSub,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
            }

            Icon(
                Icons.AutoMirrored.Outlined.ArrowForward, null,
                tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp))

            // TO (existing or create new)
            Row(
                modifier          = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isCreateNew -> IconGreen.copy(alpha = 0.10f)
                            isMatched   -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else        -> IconAmber.copy(alpha = 0.10f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = toLabel,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        isCreateNew -> IconGreen
                        isMatched   -> MaterialTheme.colorScheme.primary
                        else        -> IconAmber
                    }
                )
                Icon(Icons.Outlined.ExpandMore, null,
                    tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun PickerOption(
    label:      String,
    subtitle:   String,
    isSelected: Boolean,
    color:      Color,
    onClick:    () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) color.copy(alpha = 0.10f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface)
            Text(subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
        }
        if (isSelected) {
            Icon(Icons.Outlined.Check, null,
                tint     = color,
                modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface)
    }
}