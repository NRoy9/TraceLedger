package com.greenicephoenix.traceledger.feature.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.greenicephoenix.traceledger.core.util.ChangeType
import com.greenicephoenix.traceledger.core.util.VersionEntry

/**
 * WhatsNewDialog — shown once per version on first launch after an update.
 *
 * TRIGGER LOGIC (in MainActivity):
 *   initialLastSeenVersion != BuildConfig.VERSION_NAME → show dialog
 *   on dismiss → settingsStore.setLastSeenVersion(BuildConfig.VERSION_NAME)
 *
 * DESIGN:
 * - Full-width dialog (usePlatformDefaultWidth = false)
 * - Scrollable change list capped at 320dp
 * - Dot + type label + description per row (no pills)
 * - Single "GOT IT" CTA
 */
@Composable
fun WhatsNewDialog(
    entry:     VersionEntry,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier       = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape          = RoundedCornerShape(20.dp),
            color          = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // ── Header ────────────────────────────────────────────────────
                Text(
                    text  = "WHAT'S NEW",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 3.sp,
                        fontSize      = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text  = "Version ${entry.version}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text  = entry.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )

                Spacer(Modifier.height(20.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )

                Spacer(Modifier.height(16.dp))

                // ── Scrollable change list ────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    entry.changes.forEach { change ->
                        ChangeRow(type = change.type, description = change.description)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── CTA ───────────────────────────────────────────────────────
                Button(
                    onClick  = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text  = "GOT IT",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight    = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Single changelog row — coloured dot + type label + description.
 *
 * Dot colours:
 *   NEW      → primary (Sovereign Violet)
 *   IMPROVED → green
 *   FIXED    → amber
 *   SECURITY → red
 */
@Composable
private fun ChangeRow(type: ChangeType, description: String) {
    Row(
        verticalAlignment     = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Coloured dot
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(7.dp)
                .background(dotColor(type), CircleShape)
        )

        Column {
            Text(
                text  = type.label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.8.sp,
                    fontSize      = 9.sp,
                    fontWeight    = FontWeight.Bold
                ),
                color = dotColor(type).copy(alpha = 0.85f)
            )
            Text(
                text       = description,
                style      = MaterialTheme.typography.bodySmall,
                color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun dotColor(type: ChangeType): Color = when (type) {
    ChangeType.NEW      -> MaterialTheme.colorScheme.primary  // Sovereign Violet
    ChangeType.IMPROVED -> Color(0xFF4CAF50)                  // green
    ChangeType.FIXED    -> Color(0xFFFF9800)                  // amber
    ChangeType.SECURITY -> Color(0xFFE53935)                  // red
}