package com.example.ui.components

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.CaptionSegment
import com.example.model.CaptionStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun VideoPlayerView(
    videoUri: Uri,
    isPlaying: Boolean,
    currentPositionMs: Long,
    captions: List<CaptionSegment>,
    style: CaptionStyle,
    onPositionUpdate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var surface by remember { mutableStateOf<Surface?>(null) }
    var isPrepared by remember { mutableStateOf(false) }

    // Synchronize play/pause
    LaunchedEffect(isPlaying, isPrepared) {
        if (isPrepared) {
            mediaPlayer?.let { mp ->
                if (isPlaying && !mp.isPlaying) {
                    mp.start()
                } else if (!isPlaying && mp.isPlaying) {
                    mp.pause()
                }
            }
        }
    }

    // Synchronize seeking if position deviates significantly from mediaPlayer's current time
    LaunchedEffect(currentPositionMs) {
        mediaPlayer?.let { mp ->
            if (isPrepared) {
                val mpPos = mp.currentPosition.toLong()
                if (Math.abs(mpPos - currentPositionMs) > 600L) {
                    mp.seekTo(currentPositionMs.toInt())
                }
            }
        }
    }

    // Polling position loop while playing
    LaunchedEffect(isPlaying, isPrepared) {
        if (isPlaying && isPrepared) {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        onPositionUpdate(mp.currentPosition.toLong())
                    }
                }
                delay(40)
            }
        }
    }

    // Find active caption
    val activeSegment = remember(captions, currentPositionMs) {
        captions.firstOrNull { it.containsTime(currentPositionMs) }
    }

    Box(
        modifier = modifier
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                TextureView(ctx).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                            surface = Surface(st)
                            mediaPlayer?.setSurface(surface)
                        }

                        override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {}

                        override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                            surface?.release()
                            surface = null
                            mediaPlayer?.setSurface(null)
                            return true
                        }

                        override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                    }
                }
            },
            update = {
                // If media player was recreated and surface is available, attach surface
                surface?.let { s -> mediaPlayer?.setSurface(s) }
            }
        )

        // Subtitle Overlay on top of video frames
        CaptionOverlay(
            activeSegment = activeSegment,
            currentPositionMs = currentPositionMs,
            style = style,
            modifier = Modifier.fillMaxSize()
        )
    }

    // Lifecycle of MediaPlayer
    DisposableEffect(videoUri) {
        val player = MediaPlayer().apply {
            try {
                setDataSource(context, videoUri)
                isLooping = true
                setOnPreparedListener { mp ->
                    isPrepared = true
                    if (isPlaying) {
                        mp.start()
                    }
                }
                prepareAsync()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaPlayer = player

        onDispose {
            player.stop()
            player.release()
            mediaPlayer = null
            isPrepared = false
        }
    }
}
