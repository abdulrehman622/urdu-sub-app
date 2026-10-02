package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptionSegment
import com.example.model.CaptionStyle
import com.example.model.VideoProject
import com.example.ui.components.TimelineScrubber
import com.example.ui.components.VideoPlayerView
import com.example.util.TimeUtils
import com.example.util.UrduTextHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    project: VideoProject,
    currentPositionMs: Long,
    isPlaying: Boolean,
    editingSegment: CaptionSegment?,
    onPositionUpdate: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onAddCaption: () -> Unit,
    onOpenEditSegment: (CaptionSegment) -> Unit,
    onCloseEditSegment: () -> Unit,
    onSaveSegment: (CaptionSegment) -> Unit,
    onSplitSegment: (String, Long) -> Unit,
    onDeleteSegment: (String) -> Unit,
    onUpdateStyle: (CaptionStyle) -> Unit,
    onExportVideo: () -> Unit,
    onBack: () -> Unit
) {
    var showStyleDashboard by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Handle back press
    BackHandler {
        onBack()
    }

    // Auto-scroll list to active segment
    val activeIndex = remember(project.captions, currentPositionMs) {
        project.captions.indexOfFirst { it.containsTime(currentPositionMs) }
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex != -1 && !listState.isScrollInProgress) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    Scaffold(
        containerColor = Color(0xFF090A14),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "${project.captions.size} Urdu Cues • ${project.style.animationStyle.displayName}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFBBF24))
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Style Customizer Button
                    IconButton(onClick = { showStyleDashboard = true }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Style Suite",
                            tint = Color(0xFFF59E0B)
                        )
                    }

                    // Export Button
                    Button(
                        onClick = onExportVideo,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF101223))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCaption,
                containerColor = Color(0xFFF59E0B),
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Caption at Playhead")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Video Player Container (Aspect ratio adjusted for mobile reels/horizontal)
            val isPortrait = project.height >= project.width
            val aspectRatio = if (isPortrait) 9f / 12f else 16f / 9f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                VideoPlayerView(
                    videoUri = Uri.parse(project.videoUriString),
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    captions = project.captions,
                    style = project.style,
                    onPositionUpdate = onPositionUpdate,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Timeline Scrubber & Playback Controls
            TimelineScrubber(
                currentPositionMs = currentPositionMs,
                durationMs = project.durationMs,
                isPlaying = isPlaying,
                captions = project.captions,
                onSeek = onSeek,
                onTogglePlay = onTogglePlay
            )

            // Subtitle Segments List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subtitle Segments (${project.captions.size})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Text(
                    text = "Tap cue to seek / edit",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                )
            }

            // Subtitle Segments List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(project.captions, key = { it.id }) { cue ->
                    val isActive = currentPositionMs in cue.startMs..cue.endMs
                    val isUrdu = cue.isUrduText()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSeek(cue.startMs) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) Color(0xFF262058) else Color(0xFF131528)
                        ),
                        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)) else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Time row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isActive) Color(0xFFF59E0B) else Color(0xFF222647),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "${TimeUtils.formatTimeMs(cue.startMs)} → ${TimeUtils.formatTimeMs(cue.endMs)}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) Color.Black else Color(0xFF818CF8)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = "${(cue.durationMs / 1000f).let { String.format("%.1fs", it) }}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { onOpenEditSegment(cue) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit text",
                                            tint = Color(0xFF818CF8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteSegment(cue.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete cue",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Urdu Subtitle Text (with RTL support)
                            CompositionLocalProvider(
                                LocalLayoutDirection provides if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr
                            ) {
                                Text(
                                    text = cue.text,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = if (isActive) Color(0xFFFEF08A) else Color.White,
                                        textAlign = if (isUrdu) TextAlign.Right else TextAlign.Left
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    // Edit Caption Bottom Sheet
    editingSegment?.let { seg ->
        EditCaptionBottomSheet(
            segment = seg,
            currentPlayheadMs = currentPositionMs,
            onSave = onSaveSegment,
            onSplit = { splitTime ->
                onSplitSegment(seg.id, splitTime)
                onCloseEditSegment()
            },
            onDelete = {
                onDeleteSegment(seg.id)
                onCloseEditSegment()
            },
            onDismiss = onCloseEditSegment
        )
    }

    // Style Dashboard Bottom Sheet
    if (showStyleDashboard) {
        StyleDashboardSheet(
            style = project.style,
            onStyleChange = onUpdateStyle,
            onDismiss = { showStyleDashboard = false }
        )
    }
}
