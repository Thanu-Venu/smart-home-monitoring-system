package com.thanu.smarthome.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * SMART HOME COLOR PALETTE
 *
 * A hand-built Material 3 tonal palette (replaces the default
 * Android Studio template purple). Three roles map onto the app's
 * own domain instead of just "brand decoration":
 *
 *   - Blue (primary)    — the app's own chrome: app bar, buttons,
 *                          the launcher icon.
 *   - Amber (secondary) — warmth/energy accent, and reused as the
 *                          DISCONNECTED device-status color.
 *   - Green (tertiary)  — reused as the ON device-status color.
 *
 * Reusing tertiary/secondary for device status (instead of inventing
 * separate hardcoded status colors) keeps status colors automatically
 * correct in both light and dark mode, and keeps every color coming
 * from one MaterialTheme.colorScheme instead of two sources of truth.
 *
 * Each "on___" color is chosen to sit at (at least) WCAG AA contrast
 * against its paired surface, both in light and dark mode.
 */

// ---------------------------------------------------------------
// LIGHT
// ---------------------------------------------------------------

val LightPrimary = Color(0xFF0058CC)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD8E2FF)
val LightOnPrimaryContainer = Color(0xFF001A41)

val LightSecondary = Color(0xFFB76E00)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFFDDB3)
val LightOnSecondaryContainer = Color(0xFF3A2500)

val LightTertiary = Color(0xFF146C2E)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFA6F5AD)
val LightOnTertiaryContainer = Color(0xFF002106)

val LightBackground = Color(0xFFFBFCFF)
val LightOnBackground = Color(0xFF1A1C1E)
val LightSurface = Color(0xFFFBFCFF)
val LightOnSurface = Color(0xFF1A1C1E)
val LightSurfaceVariant = Color(0xFFE1E2EC)
val LightOnSurfaceVariant = Color(0xFF44474E)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

val LightOutline = Color(0xFF74777F)
val LightOutlineVariant = Color(0xFFC4C6D0)


// ---------------------------------------------------------------
// DARK
// ---------------------------------------------------------------

val DarkPrimary = Color(0xFFAAC7FF)
val DarkOnPrimary = Color(0xFF002E6B)
val DarkPrimaryContainer = Color(0xFF00458F)
val DarkOnPrimaryContainer = Color(0xFFD8E2FF)

val DarkSecondary = Color(0xFFFFB868)
val DarkOnSecondary = Color(0xFF5F3D00)
val DarkSecondaryContainer = Color(0xFF875800)
val DarkOnSecondaryContainer = Color(0xFFFFDDB3)

val DarkTertiary = Color(0xFF7DDA92)
val DarkOnTertiary = Color(0xFF00390F)
val DarkTertiaryContainer = Color(0xFF00531A)
val DarkOnTertiaryContainer = Color(0xFFA6F5AD)

val DarkBackground = Color(0xFF121316)
val DarkOnBackground = Color(0xFFE3E2E6)
val DarkSurface = Color(0xFF121316)
val DarkOnSurface = Color(0xFFE3E2E6)
val DarkSurfaceVariant = Color(0xFF44474E)
val DarkOnSurfaceVariant = Color(0xFFC4C6D0)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

val DarkOutline = Color(0xFF8E9099)
val DarkOutlineVariant = Color(0xFF44474E)
