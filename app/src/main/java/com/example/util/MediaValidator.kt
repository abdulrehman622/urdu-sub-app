package com.example.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

sealed class MediaValidationResult {
    data class Valid(
        val durationMs: Long,
        val width: Int,
        val height: Int,
        val isPortrait: Boolean,
        val formattedDuration: String
    ) : MediaValidationResult()

    data class ExceedsDurationLimit(
        val actualDurationMs: Long,
        val maxDurationMs: Long = 120_000L,
        val formattedActual: String,
        val formattedMax: String = "02:00"
    ) : MediaValidationResult()

    data class Error(val message: String) : MediaValidationResult()
}

object MediaValidator {
    const val MAX_ALLOWED_DURATION_MS = 120_000L // 2 minutes

    fun validateVideo(context: Context, videoUri: Uri): MediaValidationResult {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, videoUri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L

            if (durationMs <= 0L) {
                return MediaValidationResult.Error("Could not determine video duration or file is unreadable.")
            }

            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)

            var width = widthStr?.toIntOrNull() ?: 1080
            var height = heightStr?.toIntOrNull() ?: 1920
            val rotation = rotationStr?.toIntOrNull() ?: 0

            // If rotated 90 or 270 degrees, swap width and height
            if (rotation == 90 || rotation == 270) {
                val temp = width
                width = height
                height = temp
            }

            val isPortrait = height >= width

            if (durationMs > MAX_ALLOWED_DURATION_MS) {
                return MediaValidationResult.ExceedsDurationLimit(
                    actualDurationMs = durationMs,
                    maxDurationMs = MAX_ALLOWED_DURATION_MS,
                    formattedActual = TimeUtils.formatDurationMmSs(durationMs)
                )
            }

            MediaValidationResult.Valid(
                durationMs = durationMs,
                width = width,
                height = height,
                isPortrait = isPortrait,
                formattedDuration = TimeUtils.formatDurationMmSs(durationMs)
            )
        } catch (e: Exception) {
            MediaValidationResult.Error("Error inspecting video: ${e.localizedMessage ?: "Unknown error"}")
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }
}
