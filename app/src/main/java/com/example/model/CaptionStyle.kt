package com.example.model

enum class FontFamilyType(val displayName: String) {
    URDU_NASTALIQ("Urdu Nastaliq"),
    MODERN_SANS("Modern Sans"),
    BOLD_IMPACT("Bold Impact"),
    ELEGANT_SERIF("Elegant Serif"),
    NEON_SCRIPT("Neon Glow")
}

enum class CaptionBgStyle(val displayName: String) {
    NONE("None"),
    SOLID_BOX("Solid Box"),
    ROUNDED_PILL("Rounded Pill"),
    TRANSLUCENT_GLASS("Glassmorphism"),
    GRADIENT_PILL("Vibrant Gradient")
}

enum class CaptionAnimationStyle(val displayName: String) {
    KARAOKE("Karaoke (Word by Word)"),
    POP_UP("Pop Bounce"),
    FADE("Smooth Fade"),
    SLIDE_UP("Slide Up"),
    CLASSIC("Classic Subtitle")
}

enum class CaptionAlignment(val displayName: String) {
    AUTO_RTL("Auto (RTL for Urdu)"),
    CENTER("Center"),
    RIGHT("Right (RTL)"),
    LEFT("Left")
}

data class CaptionStyle(
    val fontFamilyType: FontFamilyType = FontFamilyType.URDU_NASTALIQ,
    val fontSizeSp: Float = 26f,
    val textColorHex: String = "#FFFFFF",
    val karaokeHighlightColorHex: String = "#FBBF24", // Warm gold
    val strokeEnabled: Boolean = true,
    val strokeColorHex: String = "#000000",
    val strokeWidthPx: Float = 5f,
    val bgStyle: CaptionBgStyle = CaptionBgStyle.ROUNDED_PILL,
    val bgColorHex: String = "#B3000000", // 70% black
    val shadowEnabled: Boolean = true,
    val shadowColorHex: String = "#99000000",
    val shadowRadiusPx: Float = 8f,
    val animationStyle: CaptionAnimationStyle = CaptionAnimationStyle.KARAOKE,
    val verticalPositionPercent: Float = 0.82f, // 82% from top (lower third)
    val alignment: CaptionAlignment = CaptionAlignment.AUTO_RTL,
    val letterSpacingSp: Float = 0.5f,
    val lineHeightMultiplier: Float = 1.3f
) {
    companion object {
        val PRESET_POETRY_GOLD = CaptionStyle(
            fontFamilyType = FontFamilyType.URDU_NASTALIQ,
            fontSizeSp = 28f,
            textColorHex = "#FEF08A", // Soft gold
            karaokeHighlightColorHex = "#F59E0B", // Bright amber
            strokeEnabled = true,
            strokeColorHex = "#0F172A",
            strokeWidthPx = 6f,
            bgStyle = CaptionBgStyle.ROUNDED_PILL,
            bgColorHex = "#CC1E1B4B", // Translucent deep indigo
            animationStyle = CaptionAnimationStyle.KARAOKE,
            verticalPositionPercent = 0.82f
        )

        val PRESET_NEON_CYBER = CaptionStyle(
            fontFamilyType = FontFamilyType.MODERN_SANS,
            fontSizeSp = 26f,
            textColorHex = "#38BDF8", // Neon Cyan
            karaokeHighlightColorHex = "#EC4899", // Neon Pink
            strokeEnabled = true,
            strokeColorHex = "#020617",
            strokeWidthPx = 6f,
            bgStyle = CaptionBgStyle.TRANSLUCENT_GLASS,
            bgColorHex = "#B3030712",
            animationStyle = CaptionAnimationStyle.POP_UP,
            verticalPositionPercent = 0.80f
        )

        val PRESET_CLASSIC_CINEMA = CaptionStyle(
            fontFamilyType = FontFamilyType.BOLD_IMPACT,
            fontSizeSp = 24f,
            textColorHex = "#FFFFFF",
            karaokeHighlightColorHex = "#FBBF24",
            strokeEnabled = true,
            strokeColorHex = "#000000",
            strokeWidthPx = 4f,
            bgStyle = CaptionBgStyle.SOLID_BOX,
            bgColorHex = "#D9000000",
            animationStyle = CaptionAnimationStyle.CLASSIC,
            verticalPositionPercent = 0.85f
        )

        val PRESET_EMERALD_ELEGANT = CaptionStyle(
            fontFamilyType = FontFamilyType.URDU_NASTALIQ,
            fontSizeSp = 28f,
            textColorHex = "#A7F3D0", // Emerald mint
            karaokeHighlightColorHex = "#34D399",
            strokeEnabled = true,
            strokeColorHex = "#064E3B",
            strokeWidthPx = 5f,
            bgStyle = CaptionBgStyle.GRADIENT_PILL,
            bgColorHex = "#E6064E3B",
            animationStyle = CaptionAnimationStyle.FADE,
            verticalPositionPercent = 0.82f
        )
    }
}
