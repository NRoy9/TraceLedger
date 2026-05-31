package com.greenicephoenix.traceledger.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.core.ui.theme.Dimens
import com.greenicephoenix.traceledger.core.ui.theme.TraceLedgerColors
import com.greenicephoenix.traceledger.domain.model.TransactionType

// =============================================================================
// TraceLedgerComponents.kt
//
// Shared Compose primitives for TraceLedger.
// Use these in ALL screens. Never build one-off scaffolds, top bars, or cards.
//
// Contents:
//   1. TLScreenScaffold       — standard screen wrapper with correct insets
//   2. TLTopBar               — read-only screen top bar (back arrow + title)
//   3. TLEditorTopBar         — add/edit screen top bar (close + title + save)
//   4. TLCard                 — standard surface card (20dp radius)
//   5. TLRowCard              — compact list row card (16dp radius)
//   6. TLSectionLabel         — muted uppercase section label
//   7. TLStatusPill           — colored status badge (e.g. "On Track")
//   8. TLAmountText           — financial amount with correct type color
//   9. TLEmptyState           — empty list placeholder
//  10. TLDivider              — standard 0.5dp divider
//  11. TLPrimaryButton        — full-width primary action button
//  12. TLDangerButton         — destructive action button (outlined, red)
//  13. TLTypeColor            — returns the correct Color for a TransactionType
// =============================================================================


// ─────────────────────────────────────────────────────────────────────────────
// 1. TLScreenScaffold
//
// Standard screen wrapper. Handles background color and snackbar host.
// Use this instead of bare Scaffold or Column in every screen.
//
// Usage:
//   TLScreenScaffold(snackbarHostState = snackbarHostState) {
//       ... screen content ...
//   }
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TLScreenScaffold(
    modifier:          Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    bottomBar:         @Composable () -> Unit = {},
    content:           @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier      = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost  = {
            if (snackbarHostState != null) {
                SnackbarHost(hostState = snackbarHostState)
            }
        },
        bottomBar     = bottomBar,
        content       = content
    )
}


// ─────────────────────────────────────────────────────────────────────────────
// 2. TLTopBar
//
// Read-only screen top bar — back arrow on the left, title centered.
// Use for: Accounts, Statistics detail screens, Settings sub-screens,
//          About, Changelog, Help, Budgets, Categories, etc.
//
// Usage:
//   TLTopBar(title = "ACCOUNTS", onBack = { navController.popBackStack() })
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TLTopBar(
    title:    String,
    onBack:   () -> Unit,
    actions:  @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.topBarHeight)
            .padding(horizontal = Dimens.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button — always 48dp touch target
        IconButton(
            onClick   = onBack,
            modifier  = Modifier.size(Dimens.touchTarget)
        ) {
            Icon(
                imageVector        = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                tint               = MaterialTheme.colorScheme.onBackground,
                modifier           = Modifier.size(Dimens.iconMd)
            )
        }

        // Title — centered between back and actions
        Text(
            text      = title,
            modifier  = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style     = MaterialTheme.typography.headlineSmall,
            color     = MaterialTheme.colorScheme.onBackground,
            maxLines  = 1
        )

        // Optional right-side actions (same width as back button for balance)
        Row(
            modifier              = Modifier.size(Dimens.touchTarget),
            horizontalArrangement = Arrangement.End,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            actions()
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 3. TLEditorTopBar
//
// Add/Edit screen top bar — close (X) on left, title centered, save (✓) right.
// The save icon is disabled and dimmed when canSave = false.
//
// Use for: AddTransaction, AddEditAccount, AddEditCategory,
//          AddEditBudget, AddEditRecurring, AddEditTemplate
//
// Usage:
//   TLEditorTopBar(
//       title   = "Add Transaction",
//       canSave = state.canSave,
//       onClose = onCancel,
//       onSave  = { onEvent(Save) }
//   )
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TLEditorTopBar(
    title:   String,
    canSave: Boolean,
    onClose: () -> Unit,
    onSave:  () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.topBarHeight)
            .padding(horizontal = Dimens.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick  = onClose,
            modifier = Modifier.size(Dimens.touchTarget)
        ) {
            Icon(
                imageVector        = Icons.Default.Close,
                contentDescription = "Close",
                tint               = MaterialTheme.colorScheme.onBackground,
                modifier           = Modifier.size(Dimens.iconMd)
            )
        }

        Text(
            text      = title,
            modifier  = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.onBackground,
            maxLines  = 1
        )

        IconButton(
            onClick  = onSave,
            enabled  = canSave,
            modifier = Modifier.size(Dimens.touchTarget)
        ) {
            Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = "Save",
                tint               = if (canSave)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier           = Modifier.size(Dimens.iconMd)
            )
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 4. TLCard
//
// Standard surface card — 20dp radius, surface color, optional border.
// Use for: dashboard summary cards, budget cards, account cards,
//          forecast cards, insight cards.
//
// Usage:
//   TLCard(modifier = Modifier.fillMaxWidth()) {
//       Column(modifier = Modifier.padding(Dimens.md)) { ... }
//   }
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TLCard(
    modifier:       Modifier = Modifier,
    onClick:        (() -> Unit)? = null,
    accentColor:    Color? = null,      // if set, draws a colored left border + subtle tint
    content:        @Composable ColumnScope.() -> Unit
) {
    val borderStroke = when {
        accentColor != null -> androidx.compose.foundation.BorderStroke(
            Dimens.borderWidth, accentColor.copy(alpha = 0.35f)
        )
        else -> null
    }

    if (onClick != null) {
        Card(
            onClick  = onClick,
            modifier = modifier,
            shape    = RoundedCornerShape(Dimens.cardRadius),
            colors   = CardDefaults.cardColors(
                containerColor = if (accentColor != null)
                    accentColor.copy(alpha = 0.04f).compositeOver(MaterialTheme.colorScheme.surface)
                else MaterialTheme.colorScheme.surface
            ),
            border   = borderStroke
        ) {
            Column(content = content)
        }
    } else {
        Card(
            modifier = modifier,
            shape    = RoundedCornerShape(Dimens.cardRadius),
            colors   = CardDefaults.cardColors(
                containerColor = if (accentColor != null)
                    accentColor.copy(alpha = 0.04f).compositeOver(MaterialTheme.colorScheme.surface)
                else MaterialTheme.colorScheme.surface
            ),
            border   = borderStroke
        ) {
            Column(content = content)
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 5. TLRowCard
//
// Compact list row card — 16dp radius, surface color.
// Use for: transaction rows, category list items, account list items.
//
// Usage:
//   TLRowCard(onClick = { ... }) {
//       Row(modifier = Modifier.padding(Dimens.md)) { ... }
//   }
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TLRowCard(
    modifier: Modifier = Modifier,
    onClick:  (() -> Unit)? = null,
    content:  @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick  = onClick,
            modifier = modifier,
            shape    = RoundedCornerShape(Dimens.rowCardRadius),
            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(content = content)
        }
    } else {
        Card(
            modifier = modifier,
            shape    = RoundedCornerShape(Dimens.rowCardRadius),
            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(content = content)
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 6. TLSectionLabel
//
// Muted uppercase section label — labelSmall, 50% opacity.
// Use for: card section headers, form field labels, list group labels.
//
// Usage:
//   TLSectionLabel("THIS MONTH")
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TLSectionLabel(
    text:     String,
    modifier: Modifier = Modifier,
    color:    Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
) {
    Text(
        text     = text.uppercase(),
        style    = MaterialTheme.typography.labelSmall,
        color    = color,
        modifier = modifier
    )
}


// ─────────────────────────────────────────────────────────────────────────────
// 7. TLStatusPill
//
// Colored status badge — filled pill with label.
// Use for: budget status, transaction type badges, forecast status.
//
// Usage:
//   TLStatusPill(label = "On Track", color = TraceLedgerColors.income)
//   TLStatusPill(label = "EXPENSE",  color = TraceLedgerColors.expense)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TLStatusPill(
    label:    String,
    color:    Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape    = RoundedCornerShape(50.dp),
        color    = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            modifier = Modifier.padding(horizontal = Dimens.sm, vertical = 3.dp)
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 8. TLAmountText
//
// Financial amount text with correct color per transaction type.
// Uses DotMatrix font (displaySmall) by default for large amounts,
// or pass a custom style for inline usage.
//
// Usage (large balance):
//   TLAmountText(amount = "₹1,234.00", type = TransactionType.EXPENSE)
//
// Usage (inline row):
//   TLAmountText(
//       amount = "₹500",
//       type   = TransactionType.INCOME,
//       style  = MaterialTheme.typography.titleMedium
//   )
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TLAmountText(
    amount:   String,
    type:     TransactionType,
    modifier: Modifier = Modifier,
    style:    androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium
) {
    Text(
        text     = amount,
        style    = style,
        color    = TLTypeColor(type),
        modifier = modifier
    )
}


// ─────────────────────────────────────────────────────────────────────────────
// 9. TLEmptyState
//
// Centered empty list placeholder — icon, title, optional subtitle.
// Use for: empty transaction list, no accounts, no categories, no budgets.
//
// Usage:
//   TLEmptyState(
//       icon     = Icons.Default.ReceiptLong,
//       title    = "No transactions",
//       subtitle = "Tap + to add your first transaction"
//   )
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TLEmptyState(
    icon:     ImageVector,
    title:    String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
            modifier           = Modifier.size(Dimens.iconXl)
        )
        Text(
            text      = title,
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Text(
                text      = subtitle,
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                textAlign = TextAlign.Center
            )
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 10. TLDivider
//
// Standard 0.5dp divider — matches Dimens.dividerWidth.
// Use instead of raw HorizontalDivider everywhere.
//
// Usage:
//   TLDivider()
//   TLDivider(modifier = Modifier.padding(horizontal = Dimens.md))
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TLDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier  = modifier,
        thickness = Dimens.dividerWidth,
        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )
}


// ─────────────────────────────────────────────────────────────────────────────
// 11. TLPrimaryButton
//
// Full-width primary action button — filled, uses primary color.
// Use for: Save, Confirm, Import, Export, Apply buttons.
//
// Usage:
//   TLPrimaryButton(
//       text    = "SAVE TRANSACTION",
//       enabled = state.canSave,
//       onClick = { onEvent(Save) }
//   )
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TLPrimaryButton(
    text:     String,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
    enabled:  Boolean = true,
    icon:     ImageVector? = null
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.touchTarget),
        shape    = RoundedCornerShape(Dimens.chipRadius)
    ) {
        if (icon != null) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                modifier           = Modifier.size(Dimens.iconMd)
            )
            Spacer(Modifier.width(Dimens.sm))
        }
        Text(
            text  = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 12. TLDangerButton
//
// Destructive action button — outlined with ErrorRed color.
// Use for: Delete, Remove, Clear buttons only.
// Never use for cancel/dismiss — use TextButton for that.
//
// Usage:
//   TLDangerButton(text = "DELETE", onClick = { onEvent(Delete) })
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TLDangerButton(
    text:     String,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
    enabled:  Boolean = true
) {
    val errorColor = MaterialTheme.colorScheme.error
    OutlinedButton(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.touchTarget),
        shape    = RoundedCornerShape(Dimens.chipRadius),
        border   = androidx.compose.foundation.BorderStroke(
            Dimens.borderWidth,
            if (enabled) errorColor.copy(alpha = 0.5f) else errorColor.copy(alpha = 0.2f)
        ),
        colors   = ButtonDefaults.outlinedButtonColors(
            contentColor         = errorColor,
            disabledContentColor = errorColor.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 13. TLTypeColor
//
// Returns the correct semantic Color for a given TransactionType.
// Single source of truth — replaces all inline when/Color() expressions.
//
// Usage:
//   val color = TLTypeColor(TransactionType.EXPENSE)
//   Text(text = amount, color = TLTypeColor(type))
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TLTypeColor(type: TransactionType): Color = when (type) {
    TransactionType.INCOME     -> TraceLedgerColors.income
    TransactionType.EXPENSE    -> TraceLedgerColors.expense
    TransactionType.INVESTMENT -> TraceLedgerColors.investment
    TransactionType.TRANSFER   -> TraceLedgerColors.transfer
}