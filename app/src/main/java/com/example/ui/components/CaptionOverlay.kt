package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptionAlignment
import com.example.model.CaptionAnimationStyle
import com.example.model.CaptionBgStyle
import com.example.model.CaptionSegment
import com.example.model.CaptionStyle
import com.example.model.FontFamilyType

@Composable
fun CaptionOverlay(
    activeSegment: CaptionSegment?,
    currentPositionMs: Long,
    style: CaptionStyle,
    modifier: Modifier = Modifier
) {
    if (activeSegment == null) return

    val isUrdu = activeSegment.isUrduText()
    val layoutDirection = when (style.alignment) {
        CaptionAlignment.AUTO_RTL -> if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr
        CaptionAlignment.RIGHT -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

    // Colors
    val textColor = try { Color(android.graphics.Color.parseColor(style.textColorHex)) } catch (_: Exception) { Color.White }
    val highlightColor = try { Color(android.graphics.Color.parseColor(style.karaokeHighlightColorHex)) } catch (_: Exception) { Color(0xFFF59E0B) }
    val strokeColor = try { Color(android.graphics.Color.parseColor(style.strokeColorHex)) } catch (_: Exception) { Color.Black }
    val bgColor = try { Color(android.graphics.Color.parseColor(style.bgColorHex)) } catch (_: Exception) { Color(0xB3000000) }

    // Font Family
    val composeFontFamily = when (style.fontFamilyType) {
        FontFamilyType.URDU_NASTALIQ -> FontFamily.Serif
        FontFamilyType.MODERN_SANS -> FontFamily.SansSerif
        FontFamilyType.BOLD_IMPACT -> FontFamily.Default
        FontFamilyType.ELEGANT_SERIF -> FontFamily.Serif
        FontFamilyType.NEON_SCRIPT -> FontFamily.Monospace
    }

    // Animation computation
    val elapsedMs = (currentPositionMs - activeSegment.startMs).coerceAtLeast(0L)
    val remainingMs = (activeSegment.endMs - currentPositionMs).coerceAtLeast(0L)

    val scale by animateFloatAsState(
        targetValue = when (style.animationStyle) {
            CaptionAnimationStyle.POP_UP -> if (elapsedMs < 250L) 1.15f else 1.0f
            CaptionAnimationStyle.SLIDE_UP -> 1.0f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "caption_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = when (style.animationStyle) {
            CaptionAnimationStyle.FADE -> {
                if (elapsedMs < 250L) (elapsedMs / 250f).coerceIn(0f, 1f)
                else if (remainingMs < 250L) (remainingMs / 250f).coerceIn(0f, 1f)
                else 1.0f
            }
            else -> 1.0f
        },
        label = "caption_alpha"
    )

    val textShadow = if (style.shadowEnabled) {
        val sColor = try { Color(android.graphics.Color.parseColor(style.shadowColorHex)) } catch (_: Exception) { Color(0x99000000) }
        Shadow(color = sColor, offset = Offset(2f, 3f), blurRadius = style.shadowRadiusPx)
    } else null

    // Vertical placement box
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            // Container with vertical alignment offset
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = when {
                    style.verticalPositionPercent < 0.35f -> Alignment.TopCenter
                    style.verticalPositionPercent > 0.70f -> Alignment.BottomCenter
                    else -> Alignment.Center
                }
            ) {
                // Background badge Modifier
                val bgModifier = when (style.bgStyle) {
                    CaptionBgStyle.SOLID_BOX -> Modifier.background(bgColor, RoundedCornerShape(4.dp))
                    CaptionBgStyle.ROUNDED_PILL -> Modifier.background(bgColor, RoundedCornerShape(24.dp))
                    CaptionBgStyle.TRANSLUCENT_GLASS -> Modifier
                        .background(Color(0xB30F172A), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x4D818CF8), RoundedCornerShape(16.dp))
                    CaptionBgStyle.GRADIENT_PILL -> Modifier.background(
                        Brush.horizontalGradient(
                            listOf(bgColor, Color(0xFF4338CA))
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    CaptionBgStyle.NONE -> Modifier
                }

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .alpha(alpha)
                        .then(bgModifier)
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    if (style.animationStyle == CaptionAnimationStyle.KARAOKE && activeSegment.words.isNotEmpty()) {
                        // Word by word Karaoke Highlight
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            activeSegment.words.forEachIndexed { idx, wordObj ->
                                val isCurrentWord = currentPositionMs in wordObj.startMs..wordObj.endMs
                                val wordColor = if (isCurrentWord) highlightColor else textColor
                                val wordWeight = if (isCurrentWord) FontWeight.Black else FontWeight.Bold
                                val wordScale = if (isCurrentWord) 1.08f else 1.0f

                                Text(
                                    text = wordObj.word + if (idx < activeSegment.words.size - 1) " " else "",
                                    style = TextStyle(
                                        fontSize = (style.fontSizeSp * wordScale).sp,
                                        fontFamily = composeFontFamily,
                                        fontWeight = wordWeight,
                                        color = wordColor,
                                        shadow = textShadow,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    } else {
                        // Static / PopUp / Fade / Slide text
                        Text(
                            text = activeSegment.text,
                            style = TextStyle(
                                fontSize = style.fontSizeSp.sp,
                                fontFamily = composeFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                shadow = textShadow,
                                textAlign = when (style.alignment) {
                                    CaptionAlignment.CENTER -> TextAlign.Center
                                    CaptionAlignment.RIGHT, CaptionAlignment.AUTO_RTL -> TextAlign.Right
                                    CaptionAlignment.LEFT -> TextAlign.Left
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}
