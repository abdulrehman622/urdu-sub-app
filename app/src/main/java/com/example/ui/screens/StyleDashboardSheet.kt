package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptionAlignment
import com.example.model.CaptionAnimationStyle
import com.example.model.CaptionBgStyle
import com.example.model.CaptionStyle
import com.example.model.FontFamilyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleDashboardSheet(
    style: CaptionStyle,
    onStyleChange: (CaptionStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf("Presets", "Animation", "Font & Size", "Colors & Box", "Stroke & Shadow", "Position")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF101223),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Caption Styling Suite",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF171A31),
                contentColor = Color(0xFF818CF8),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color(0xFF818CF8) else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }

            // Tab Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    0 -> PresetsTab(style = style, onStyleChange = onStyleChange)
                    1 -> AnimationTab(style = style, onStyleChange = onStyleChange)
                    2 -> TypographyTab(style = style, onStyleChange = onStyleChange)
                    3 -> ColorsAndBgTab(style = style, onStyleChange = onStyleChange)
                    4 -> StrokeAndShadowTab(style = style, onStyleChange = onStyleChange)
                    5 -> PositionTab(style = style, onStyleChange = onStyleChange)
                }
            }
        }
    }
}

@Composable
fun PresetsTab(style: CaptionStyle, onStyleChange: (CaptionStyle) -> Unit) {
    Column {
        Text(
            text = "One-Tap Aesthetic Presets",
            style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PresetCard(
                title = "Poetry Gold",
                subtitle = "اردو غزل • سنہری",
                bgHex = "#1E1B4B",
                textHex = "#FEF08A",
                badgeHex = "#CC1E1B4B",
                isSelected = style == CaptionStyle.PRESET_POETRY_GOLD
            ) { onStyleChange(CaptionStyle.PRESET_POETRY_GOLD) }

            PresetCard(
                title = "Cyber Neon",
                subtitle = "نیون • شارٹس",
                bgHex = "#020617",
                textHex = "#38BDF8",
                badgeHex = "#B3030712",
                isSelected = style == CaptionStyle.PRESET_NEON_CYBER
            ) { onStyleChange(CaptionStyle.PRESET_NEON_CYBER) }

            PresetCard(
                title = "Emerald Folk",
                subtitle = "زمرد • صوفیانہ",
                bgHex = "#064E3B",
                textHex = "#A7F3D0",
                badgeHex = "#E6064E3B",
                isSelected = style == CaptionStyle.PRESET_EMERALD_ELEGANT
            ) { onStyleChange(CaptionStyle.PRESET_EMERALD_ELEGANT) }

            PresetCard(
                title = "Cinema Classic",
                subtitle = "سفید و سیاہ باکس",
                bgHex = "#000000",
                textHex = "#FFFFFF",
                badgeHex = "#D9000000",
                isSelected = style == CaptionStyle.PRESET_CLASSIC_CINEMA
            ) { onStyleChange(CaptionStyle.PRESET_CLASSIC_CINEMA) }
        }
    }
}

@Composable
fun PresetCard(
    title: String,
    subtitle: String,
    bgHex: String,
    textHex: String,
    badgeHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF312E81) else Color(0xFF1E213D)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF59E0B)) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(android.graphics.Color.parseColor(bgHex)), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(android.graphics.Color.parseColor(badgeHex)), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "سب ٹائٹل",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(textHex))
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            Text(subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun AnimationTab(style: CaptionStyle, onStyleChange: (CaptionStyle) -> Unit) {
    Column {
        Text("Dynamic Caption Animations", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(12.dp))

        CaptionAnimationStyle.values().forEach { anim ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onStyleChange(style.copy(animationStyle = anim)) },
                colors = CardDefaults.cardColors(
                    containerColor = if (style.animationStyle == anim) Color(0xFF2E266D) else Color(0xFF181B34)
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (style.animationStyle == anim) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF818CF8)) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(anim.displayName, fontWeight = FontWeight.SemiBold, color = Color.White)
                        val desc = when (anim) {
                            CaptionAnimationStyle.KARAOKE -> "Active singing/speaking words light up in sync"
                            CaptionAnimationStyle.POP_UP -> "Smooth scale bounce entrance on each phrase"
                            CaptionAnimationStyle.FADE -> "Cinematic smooth opacity fade in and out"
                            CaptionAnimationStyle.SLIDE_UP -> "Subtle upward glide motion"
                            CaptionAnimationStyle.CLASSIC -> "Steady professional television subtitle display"
                        }
                        Text(desc, fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                    if (style.animationStyle == anim) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFF59E0B))
                    }
                }
            }
        }
    }
}

@Composable
fun TypographyTab(style: CaptionStyle, onStyleChange: (CaptionStyle) -> Unit) {
    Column {
        Text("Font Family", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FontFamilyType.values().forEach { font ->
                FilterChip(
                    selected = style.fontFamilyType == font,
                    onClick = { onStyleChange(style.copy(fontFamilyType = font)) },
                    label = { Text(font.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6366F1),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Font Size", color = Color(0xFFCBD5E1))
            Text("${style.fontSizeSp.toInt()} sp", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
        }
        Slider(
            value = style.fontSizeSp,
            onValueChange = { onStyleChange(style.copy(fontSizeSp = it)) },
            valueRange = 16f..44f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFF59E0B),
                activeTrackColor = Color(0xFF6366F1)
            )
        )
    }
}

@Composable
fun ColorsAndBgTab(style: CaptionStyle, onStyleChange: (CaptionStyle) -> Unit) {
    val textColors = listOf("#FFFFFF", "#FEF08A", "#FBBF24", "#38BDF8", "#F472B6", "#4ADE80", "#FB923C")
    val highlightColors = listOf("#FBBF24", "#F59E0B", "#EF4444", "#10B981", "#06B6D4", "#EC4899", "#A855F7")

    Column {
        Text("Text Color", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            textColors.forEach { hex ->
                ColorCircle(hex = hex, isSelected = style.textColorHex.equals(hex, ignoreCase = true)) {
                    onStyleChange(style.copy(textColorHex = hex))
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text("Karaoke Word Highlight Color", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            highlightColors.forEach { hex ->
                ColorCircle(hex = hex, isSelected = style.karaokeHighlightColorHex.equals(hex, ignoreCase = true)) {
                    onStyleChange(style.copy(karaokeHighlightColorHex = hex))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Background Badge Style", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CaptionBgStyle.values().forEach { bg ->
                FilterChip(
                    selected = style.bgStyle == bg,
                    onClick = { onStyleChange(style.copy(bgStyle = bg)) },
                    label = { Text(bg.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6366F1),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun StrokeAndShadowTab(style: CaptionStyle, onStyleChange: (CaptionStyle) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Text Outline / Stroke", fontWeight = FontWeight.SemiBold, color = Color.White)
                Text("Increases text readability over bright video frames", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
            Switch(
                checked = style.strokeEnabled,
                onCheckedChange = { onStyleChange(style.copy(strokeEnabled = it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFF59E0B), checkedTrackColor = Color(0xFF4338CA))
            )
        }

        if (style.strokeEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Stroke Thickness", color = Color(0xFFCBD5E1))
                Text("${style.strokeWidthPx.toInt()} px", color = Color(0xFFF59E0B))
            }
            Slider(
                value = style.strokeWidthPx,
                onValueChange = { onStyleChange(style.copy(strokeWidthPx = it)) },
                valueRange = 1f..10f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFFF59E0B), activeTrackColor = Color(0xFF6366F1))
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Drop Shadow", fontWeight = FontWeight.SemiBold, color = Color.White)
                Text("Soft cinematic text depth", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
            Switch(
                checked = style.shadowEnabled,
                onCheckedChange = { onStyleChange(style.copy(shadowEnabled = it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFF59E0B), checkedTrackColor = Color(0xFF4338CA))
            )
        }
    }
}

@Composable
fun PositionTab(style: CaptionStyle, onStyleChange: (CaptionStyle) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Vertical Position", color = Color(0xFFCBD5E1))
            val posDesc = when {
                style.verticalPositionPercent < 0.25f -> "Top Banner"
                style.verticalPositionPercent in 0.40f..0.60f -> "Center Screen"
                style.verticalPositionPercent > 0.85f -> "Bottom Edge"
                else -> "Lower-Third (Recommended)"
            }
            Text(posDesc, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
        }

        Slider(
            value = style.verticalPositionPercent,
            onValueChange = { onStyleChange(style.copy(verticalPositionPercent = it)) },
            valueRange = 0.15f..0.90f,
            colors = SliderDefaults.colors(thumbColor = Color(0xFFF59E0B), activeTrackColor = Color(0xFF6366F1))
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Text Alignment", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CaptionAlignment.values().forEach { align ->
                FilterChip(
                    selected = style.alignment == align,
                    onClick = { onStyleChange(style.copy(alignment = align)) },
                    label = { Text(align.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6366F1),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun ColorCircle(hex: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.White }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color(0xFFF59E0B) else Color(0x66FFFFFF),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}
