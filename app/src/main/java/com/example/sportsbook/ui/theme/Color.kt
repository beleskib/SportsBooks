package com.example.sportsbook.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════
// SportsBooks v3 Dark Theme Palette
// Design: #121212 background, #1E1E1E surfaces, #4CAF50 green
// accent, #2196F3 blue secondary. Matches mockup spec.
// ══════════════════════════════════════════════════════════════

// ── v3 Dark backgrounds & surfaces ──
val DarkBg = Color(0xFF121212)           // main background
val DarkSurface = Color(0xFF1E1E1E)      // cards, sheets, nav bar
val DarkSurfaceLight = Color(0xFF252525) // elevated cards, inputs
val DarkBorder = Color(0xFF2A2A2A)       // borders, dividers
val DarkNavBar = Color(0xFF1A1A1A)       // bottom nav bar

// ── v3 Text on dark ──
val DarkTextPrimary = Color(0xFFFFFFFF)   // primary text
val DarkTextSecondary = Color(0xFF888888) // secondary / muted text
val DarkTextTertiary = Color(0xFF555555)  // disabled / hint text
val DarkTextLink = Color(0xFF2196F3)      // links on dark bg

// ── v3 Accent colors ──
val GreenAccent = Color(0xFF4CAF50)       // primary accent — active tabs, CTAs
val GreenDark = Color(0xFF2E7D32)         // darker green for gradients
val BlueAccent = Color(0xFF2196F3)        // secondary accent
val BlueDarkAccent = Color(0xFF1565C0)    // darker blue for gradients
val OrangeAccent = Color(0xFFFF9800)      // warning, pending states
val PurpleAccentV3 = Color(0xFFAB47BC)    // gamification, special

// ── Legacy Light Theme Palette (kept for backward compat) ──

// ── Primary grays (backgrounds & text) ──
val LightBg = Color(0xFFF9FAFB)          // gray-50 — page background
val CardWhite = Color(0xFFFFFFFF)         // white — cards, inputs
val DividerGray = Color(0xFFF3F4F6)      // gray-100 — card dividers
val BorderGray = Color(0xFFE5E7EB)       // gray-200 — borders, outlines
val TextTertiary = Color(0xFF9CA3AF)     // gray-400 — hint text
val TextSecondary = Color(0xFF6B7280)    // gray-500 — secondary text
val TextSubtle = Color(0xFF374151)       // gray-700 — body text
val TextPrimary = Color(0xFF111827)      // gray-900 — primary text, nav bar bg

// ── Accent / brand ──
val GoldAccent = Color(0xFFFDE047)       // yellow-300 — primary accent
val GoldMid = Color(0xFFFACC15)          // yellow-400
val GoldDark = Color(0xFFEAB308)         // yellow-500

// ── Semantic colors ──
val SportGreen = Color(0xFF10B981)       // emerald-500 — success/active
val EmeraldLight = Color(0xFFD1FAE5)     // emerald-100
val EmeraldDark = Color(0xFF047857)      // emerald-700

val ErrorRed = Color(0xFFEF4444)         // red-500 — errors
val RoseLight = Color(0xFFFECDD3)        // rose-200
val RoseText = Color(0xFFBE123C)         // rose-700

val InfoBlue = Color(0xFF3B82F6)         // blue-500 — info
val BlueLight = Color(0xFFDBEAFE)        // blue-100
val BlueDark = Color(0xFF1D4ED8)         // blue-700

val PurpleAccent = Color(0xFF7C3AED)     // purple-600
val PurpleLight = Color(0xFFF3E8FF)      // purple-100
val PurpleDark = Color(0xFF6D28D9)       // purple-700

// ── Nav bar (dark bar on light pages) ──
val NavBarBg = Color(0xFF111827)         // gray-900
val NavBarText = Color(0xFFFDE047)       // yellow-300

// ── Legacy aliases (kept for backward compat during migration) ──
@Deprecated("Use TextPrimary or NavBarBg", ReplaceWith("NavBarBg"))
val Navy900 = Color(0xFF0A1628)
@Deprecated("Use CardWhite", ReplaceWith("CardWhite"))
val Navy800 = Color(0xFF0F1E36)
@Deprecated("Use LightBg", ReplaceWith("LightBg"))
val Navy700 = Color(0xFF132A44)
@Deprecated("Use BorderGray", ReplaceWith("BorderGray"))
val Navy600 = Color(0xFF1A3658)
val Navy500 = Color(0xFF1E4472)
val Navy400 = Color(0xFF2D5F8A)
val Navy300 = Color(0xFF4A7FA8)

@Deprecated("Use GoldAccent", ReplaceWith("GoldAccent"))
val USOpenGold = Color(0xFFE8B931)
@Deprecated("Use GoldDark", ReplaceWith("GoldDark"))
val GoldLegacyDark = Color(0xFFD4A020)
val GoldLight = Color(0xFFF5D060)

@Deprecated("Use TextPrimary or CardWhite", ReplaceWith("CardWhite"))
val WarmWhite = Color(0xFFF0EBE0)
@Deprecated("Use TextSecondary", ReplaceWith("TextSecondary"))
val CoolGray = Color(0xFF8A9BB5)

val CoralRed = Color(0xFFE74C3C)
val CoralRedDark = Color(0xFFCF3B2B)

// ── Sport category accent colors ──
val SportBasketball = Color(0xFFFF6B35)
val SportFootball = Color(0xFF2D6A4F)
val SportTennis = Color(0xFF95D55D)
val SportPaddle = Color(0xFF4361EE)
val SportVolleyball = Color(0xFF2EC4B6)
val SportSwimming = Color(0xFF0096C7)
val SportBoxing = Color(0xFFE63946)
val SportMMA = Color(0xFF9B2335)
val SportYoga = Color(0xFF7B2D8E)
val SportPilates = Color(0xFFFF6B9D)
val SportCrossfit = Color(0xFFFF4500)
val SportRunning = Color(0xFF48CAE4)
val SportCycling = Color(0xFF52B788)
val SportGolf = Color(0xFF1B4332)
val SportBadminton = Color(0xFF80ED99)
val SportTableTennis = Color(0xFFFF5400)
val SportHandball = Color(0xFF264653)
val SportBaseball = Color(0xFFBC6C25)
val SportCricket = Color(0xFF606C38)
