package com.greenicephoenix.traceledger.core.ui.theme

import androidx.compose.ui.unit.dp

/**
 * TraceLedger layout token system.
 *
 * SPACING — use xs/sm/md/lg/xl for all padding/gap values.
 * LAYOUT  — use named tokens for structural dimensions.
 * RADIUS  — use named tokens for corner radii.
 * Never use raw dp values in screens — always reference a token here.
 */
object Dimens {

    // ── Spacing ───────────────────────────────────────────────
    val xs  =  4.dp
    val sm  =  8.dp
    val md  = 16.dp
    val lg  = 24.dp
    val xl  = 32.dp
    val xxl = 48.dp

    // ── Layout ────────────────────────────────────────────────
    // Screen horizontal padding (all content inside screens)
    val screenPadding   = 16.dp

    // Top bar height (all screens)
    val topBarHeight    = 56.dp

    // Minimum touch target size (WCAG / Material)
    val touchTarget     = 48.dp

    // Bottom nav height
    val bottomNavHeight = 64.dp

    // FAB size
    val fabSize         = 56.dp

    // ── Card radii ────────────────────────────────────────────
    // Standard card (dashboard cards, account cards, budget cards)
    val cardRadius      = 20.dp

    // Compact row card (transaction rows, list items)
    val rowCardRadius   = 16.dp

    // Chip / badge radius
    val chipRadius      = 8.dp

    // Dialog / bottom sheet radius
    val sheetRadius     = 28.dp

    // ── Icon sizes ────────────────────────────────────────────
    val iconSm  = 16.dp
    val iconMd  = 24.dp
    val iconLg  = 32.dp
    val iconXl  = 48.dp

    // ── Elevation / Border ────────────────────────────────────
    val borderWidth     = 1.dp
    val dividerWidth    = 0.5.dp
}