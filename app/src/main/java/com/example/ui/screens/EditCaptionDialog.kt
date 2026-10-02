package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptionSegment
import com.example.util.TimeUtils
import com.example.util.UrduTextHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditCaptionBottomSheet(
    segment: CaptionSegment,
    currentPlayheadMs: Long,
    onSave: (CaptionSegment) -> Unit,
    onSplit: (Long) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var text by remember { mutableStateOf(segment.text) }
    var startMs by remember { mutableStateOf(segment.startMs) }
    var endMs by remember { mutableStateOf(segment.endMs) }

    val isUrdu = UrduTextHelper.isUrdu(text)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF131525),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Caption Line",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete cue",
                        tint = Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle Text Input with RTL support
            CompositionLocalProvider(
                LocalLayoutDirection provides if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Caption Text (اردو الفاظ)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF818CF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF818CF8),
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        textAlign = if (isUrdu) TextAlign.Right else TextAlign.Left
                    )
                )
            }

            // Quick Urdu Keyboard Helper Buttons
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Urdu Special Characters (فوری حروف)",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (UrduTextHelper.URDU_SPECIAL_LETTERS + UrduTextHelper.URDU_PUNCTUATION).forEach { char ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .clickable { text += char }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = char,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Timing Synchronization Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D36)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Time Synchronization (وقت کی ہم آہنگی)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Start Time Row
                    TimingAdjusterRow(
                        label = "Start Time",
                        timeMs = startMs,
                        onTimeChange = { newTime ->
                            startMs = newTime.coerceAtLeast(0L).coerceAtMost(endMs - 100L)
                        },
                        onSetToPlayhead = {
                            startMs = currentPlayheadMs.coerceAtLeast(0L).coerceAtMost(endMs - 100L)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // End Time Row
                    TimingAdjusterRow(
                        label = "End Time",
                        timeMs = endMs,
                        onTimeChange = { newTime ->
                            endMs = newTime.coerceAtLeast(startMs + 100L)
                        },
                        onSetToPlayhead = {
                            endMs = currentPlayheadMs.coerceAtLeast(startMs + 100L)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Split Action Row
            if (currentPlayheadMs in (startMs + 200L)..(endMs - 200L)) {
                OutlinedButton(
                    onClick = {
                        onSplit(currentPlayheadMs)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF59E0B)
                    )
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.CallSplit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Split Segment at Playhead (${TimeUtils.formatTimeMs(currentPlayheadMs)})")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Save and Cancel Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        val updated = segment.copy(
                            text = text.trim(),
                            startMs = startMs,
                            endMs = endMs,
                            words = UrduTextHelper.generateWordTimings(text.trim(), startMs, endMs)
                        )
                        onSave(updated)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Changes")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TimingAdjusterRow(
    label: String,
    timeMs: Long,
    onTimeChange: (Long) -> Unit,
    onSetToPlayhead: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFCBD5E1))
            )

            Text(
                text = TimeUtils.formatTimeMs(timeMs),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF59E0B)
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallTimingButton("-500ms") { onTimeChange(timeMs - 500L) }
            SmallTimingButton("-100ms") { onTimeChange(timeMs - 100L) }
            SmallTimingButton("+100ms") { onTimeChange(timeMs + 100L) }
            SmallTimingButton("+500ms") { onTimeChange(timeMs + 500L) }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onSetToPlayhead,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF818CF8)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Set Playhead", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun SmallTimingButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color(0xFF262A4E), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.White
        )
    }
}
