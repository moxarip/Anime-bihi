package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Deep AMOLED & Obsidian Backgrounds (Anime Witcher x Anime Slayer)
val AmoledBlack = Color(0xFF090A0F)
val RichObsidian = Color(0xFF12151F)
val ObsidianSurface = Color(0xFF181C2A)
val ObsidianElevated = Color(0xFF202639)

// Primary & Secondary Neon Accents
val ElectricCrimson = Color(0xFFFF2A54)
val CrimsonGlow = Color(0xFFFF5277)
val CyberPurple = Color(0xFF7C3AED)
val PurpleGlow = Color(0xFFA78BFA)

// Functional Status Colors
val CanonEmerald = Color(0xFF10B981)
val FillerAmber = Color(0xFFF59E0B)
val CyberCyan = Color(0xFF06B6D4)
val StarGold = Color(0xFFFBBF24)

// Text & Glassmorphism Borders
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val GlassBorder = Color(0x14FFFFFF) // rgba(255,255,255,0.08)
val GlassSurface = Color(0xCC12151F)
val GlassCard = Color(0xB3181C2A)

// Signature Gradients
val BihiPrimaryGradient = Brush.horizontalGradient(
    colors = listOf(ElectricCrimson, CyberPurple)
)

val BihiVerticalGradient = Brush.verticalGradient(
    colors = listOf(Color.Transparent, Color(0xCC090A0F), AmoledBlack)
)

val BihiCardGlowGradient = Brush.linearGradient(
    colors = listOf(
        ElectricCrimson.copy(alpha = 0.18f),
        CyberPurple.copy(alpha = 0.18f)
    )
)
