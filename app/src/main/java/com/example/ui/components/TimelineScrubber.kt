package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptionSegment
import com.example.util.TimeUtils

@Composable
fun TimelineScrubber(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    captions: List<CaptionSegment>,
    onSeek: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDuration = durationMs.coerceAtLeast(1000L)
    val progress = (currentPositionMs.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Visual Multi-Cue Segment Timeline Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .pointerInput(totalDuration) {
                    detectTapGestures { offset ->
                        val tapRatio = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((tapRatio * totalDuration).toLong())
                    }
                }
                .pointerInput(totalDuration) {
                    detectDragGestures { change, _ ->
                        val dragRatio = (change.position.x / size.width).coerceIn(0f, 1f)
                        onSeek((dragRatio * totalDuration).toLong())
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val trackHeight = 16f
            val trackY = (canvasHeight - trackHeight) / 2f

            // 1. Background base track
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(0f, trackY),
                size = Size(canvasWidth, trackHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // 2. Subtitle Cue segments as colored blocks
            for (cue in captions) {
                val startX = (cue.startMs.toFloat() / totalDuration.toFloat()) * canvasWidth
                val endX = (cue.endMs.toFloat() / totalDuration.toFloat()) * canvasWidth
                val segWidth = (endX - startX).coerceAtLeast(6f)

                val isCueActive = currentPositionMs in cue.startMs..cue.endMs
                val cueColor = if (isCueActive) Color(0xFFF59E0B) else Color(0xFF6366F1)

                drawRoundRect(
                    color = cueColor,
                    topLeft = Offset(startX, trackY),
                    size = Size(segWidth, trackHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            // 3. Playhead progress needle
            val playheadX = progress * canvasWidth
            drawLine(
                color = Color.White,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, canvasHeight),
                strokeWidth = 5f
            )
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = 8f,
                center = Offset(playheadX, canvasHeight / 2f)
            )
        }

        // Time display row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = TimeUtils.formatTimeMs(currentPositionMs),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFF59E0B)
                )
            )
            Text(
                text = TimeUtils.formatDurationMmSs(totalDuration),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF94A3B8)
                )
            )
        }

        // Playback Control Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onSeek((currentPositionMs - 5000L).coerceAtLeast(0L)) }
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Rewind 5s",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            FilledIconButton(
                onClick = onTogglePlay,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White
                ),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = { onSeek((currentPositionMs + 5000L).coerceAtMost(totalDuration)) }
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Forward 5s",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = { onSeek(0L) }
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Replay from start",
                    tint = Color(0xFF94A3B8)
                )
            }
        }
    }
}
