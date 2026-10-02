package com.example.data.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.model.CaptionAnimationStyle
import com.example.model.CaptionBgStyle
import com.example.model.CaptionSegment
import com.example.model.CaptionStyle
import com.example.model.FontFamilyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer

class VideoBurnerEngine(private val context: Context) {

    suspend fun burnCaptionsAndExport(
        videoUri: Uri,
        captions: List<CaptionSegment>,
        style: CaptionStyle,
        onProgress: (Float) -> Unit
    ): Pair<File, Uri?> = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)

        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val totalDurationMs = durationStr?.toLongOrNull() ?: 10_000L

        val origWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 720
        val origHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 1280
        val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0

        // Handle rotation swap
        val targetWidth = if (rotation == 90 || rotation == 270) origHeight else origWidth
        val targetHeight = if (rotation == 90 || rotation == 270) origWidth else origHeight

        // Normalize dimensions to multiples of 16 for encoder compatibility
        val outWidth = (targetWidth / 16) * 16
        val outHeight = (targetHeight / 16) * 16

        val fps = 24
        val frameIntervalMs = 1000L / fps
        val totalFrames = ((totalDurationMs / 1000f) * fps).toInt().coerceAtLeast(1)

        val tempOutputFile = File(context.cacheDir, "burned_video_${System.currentTimeMillis()}.mp4")

        // 1. Audio Track Setup via MediaExtractor
        var audioExtractor: MediaExtractor? = null
        var audioTrackIndexInExtractor = -1
        var audioFormat: MediaFormat? = null

        try {
            audioExtractor = MediaExtractor()
            audioExtractor.setDataSource(context, videoUri, null)
            for (i in 0 until audioExtractor.trackCount) {
                val format = audioExtractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndexInExtractor = i
                    audioFormat = format
                    audioExtractor.selectTrack(i)
                    break
                }
            }
        } catch (_: Exception) {}

        // 2. Video Encoder & Muxer Setup
        val videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, outWidth, outHeight).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, 3_500_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        encoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = encoder.createInputSurface()
        encoder.start()

        val muxer = MediaMuxer(tempOutputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var muxerVideoTrack = -1
        var muxerAudioTrack = -1
        var muxerStarted = false

        if (audioFormat != null) {
            muxerAudioTrack = muxer.addTrack(audioFormat)
        }

        val bufferInfo = MediaCodec.BufferInfo()

        // Prepare Paints for Subtitle rendering
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
        }
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
        }

        val typeface = when (style.fontFamilyType) {
            FontFamilyType.URDU_NASTALIQ -> Typeface.create("serif", Typeface.BOLD)
            FontFamilyType.MODERN_SANS -> Typeface.SANS_SERIF
            FontFamilyType.BOLD_IMPACT -> Typeface.DEFAULT_BOLD
            FontFamilyType.ELEGANT_SERIF -> Typeface.SERIF
            FontFamilyType.NEON_SCRIPT -> Typeface.MONOSPACE
        }
        textPaint.typeface = typeface
        strokePaint.typeface = typeface
        highlightPaint.typeface = typeface

        // Base text size scaled to video output resolution
        val scaleFactor = outHeight / 1280f
        val computedTextSize = style.fontSizeSp * 2.2f * scaleFactor
        textPaint.textSize = computedTextSize
        strokePaint.textSize = computedTextSize
        highlightPaint.textSize = computedTextSize

        val textColor = try { Color.parseColor(style.textColorHex) } catch (_: Exception) { Color.WHITE }
        val strokeColor = try { Color.parseColor(style.strokeColorHex) } catch (_: Exception) { Color.BLACK }
        val highlightColor = try { Color.parseColor(style.karaokeHighlightColorHex) } catch (_: Exception) { Color.parseColor("#F59E0B") }
        val bgColor = try { Color.parseColor(style.bgColorHex) } catch (_: Exception) { Color.parseColor("#B3000000") }

        textPaint.color = textColor
        strokePaint.color = strokeColor
        strokePaint.strokeWidth = style.strokeWidthPx * scaleFactor * 2f
        highlightPaint.color = highlightColor

        try {
            // Loop through each video frame
            for (frame in 0 until totalFrames) {
                val frameTimeMs = (frame * frameIntervalMs).coerceAtMost(totalDurationMs)
                val frameTimeUs = frameTimeMs * 1000L

                // Extract source video bitmap
                val sourceBitmap = try {
                    retriever.getFrameAtTime(frameTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                } catch (_: Exception) { null }

                val canvas = inputSurface.lockCanvas(null)
                try {
                    // 1. Draw source video frame
                    if (sourceBitmap != null) {
                        val srcRect = Rect(0, 0, sourceBitmap.width, sourceBitmap.height)
                        val dstRect = Rect(0, 0, outWidth, outHeight)
                        canvas.drawBitmap(sourceBitmap, srcRect, dstRect, null)
                    } else {
                        canvas.drawColor(Color.BLACK)
                    }

                    // 2. Find active subtitle for this timestamp
                    val activeCue = captions.firstOrNull { it.containsTime(frameTimeMs) }
                    if (activeCue != null) {
                        val centerY = outHeight * style.verticalPositionPercent
                        val centerX = outWidth / 2f

                        // Animation modifiers
                        var animScale = 1.0f
                        var animAlpha = 255
                        val cueElapsed = frameTimeMs - activeCue.startMs
                        val cueRemain = activeCue.endMs - frameTimeMs

                        when (style.animationStyle) {
                            CaptionAnimationStyle.POP_UP -> {
                                if (cueElapsed < 250L) {
                                    val t = cueElapsed / 250f
                                    animScale = 0.7f + (0.3f * t)
                                }
                            }
                            CaptionAnimationStyle.FADE -> {
                                if (cueElapsed < 300L) {
                                    animAlpha = ((cueElapsed / 300f) * 255).toInt().coerceIn(0, 255)
                                } else if (cueRemain < 300L) {
                                    animAlpha = ((cueRemain / 300f) * 255).toInt().coerceIn(0, 255)
                                }
                            }
                            CaptionAnimationStyle.SLIDE_UP -> {
                                if (cueElapsed < 250L) {
                                    val t = cueElapsed / 250f
                                    animScale = 0.9f + (0.1f * t)
                                }
                            }
                            else -> {}
                        }

                        canvas.save()
                        canvas.scale(animScale, animScale, centerX, centerY)

                        val text = activeCue.text.trim()
                        val textWidth = textPaint.measureText(text)
                        val textHeight = computedTextSize

                        val padX = 32f * scaleFactor
                        val padY = 18f * scaleFactor
                        val badgeRect = RectF(
                            centerX - (textWidth / 2f) - padX,
                            centerY - (textHeight / 2f) - padY,
                            centerX + (textWidth / 2f) + padX,
                            centerY + (textHeight / 2f) + padY
                        )

                        // Draw background box
                        bgPaint.alpha = animAlpha
                        when (style.bgStyle) {
                            CaptionBgStyle.SOLID_BOX -> {
                                bgPaint.color = bgColor
                                canvas.drawRect(badgeRect, bgPaint)
                            }
                            CaptionBgStyle.ROUNDED_PILL -> {
                                bgPaint.color = bgColor
                                canvas.drawRoundRect(badgeRect, badgeRect.height() / 2f, badgeRect.height() / 2f, bgPaint)
                            }
                            CaptionBgStyle.TRANSLUCENT_GLASS -> {
                                bgPaint.color = Color.parseColor("#B30F172A")
                                canvas.drawRoundRect(badgeRect, 20f, 20f, bgPaint)
                            }
                            CaptionBgStyle.GRADIENT_PILL -> {
                                val shader = LinearGradient(
                                    badgeRect.left, badgeRect.top, badgeRect.right, badgeRect.bottom,
                                    bgColor, Color.parseColor("#4338CA"), Shader.TileMode.CLAMP
                                )
                                bgPaint.shader = shader
                                canvas.drawRoundRect(badgeRect, badgeRect.height() / 2f, badgeRect.height() / 2f, bgPaint)
                                bgPaint.shader = null
                            }
                            CaptionBgStyle.NONE -> {}
                        }

                        // Baseline Y
                        val baselineY = centerY + (textHeight * 0.35f)

                        // Render text
                        if (style.animationStyle == CaptionAnimationStyle.KARAOKE && activeCue.words.isNotEmpty()) {
                            // Word by word rendering for Karaoke
                            val words = activeCue.words
                            val totalWordsWidth = words.sumOf { textPaint.measureText(it.word).toDouble() }.toFloat()
                            val spaceWidth = textPaint.measureText(" ")
                            val totalWidthWithSpaces = totalWordsWidth + (spaceWidth * (words.size - 1))
                            var curX = centerX - (totalWidthWithSpaces / 2f)

                            for (w in words) {
                                val wWidth = textPaint.measureText(w.word)
                                val wCenterX = curX + (wWidth / 2f)
                                val isActiveWord = frameTimeMs in w.startMs..w.endMs

                                val curPaint = if (isActiveWord) highlightPaint else textPaint

                                if (style.strokeEnabled) {
                                    canvas.drawText(w.word, wCenterX, baselineY, strokePaint)
                                }
                                canvas.drawText(w.word, wCenterX, baselineY, curPaint)

                                curX += wWidth + spaceWidth
                            }
                        } else {
                            // Standard line rendering
                            if (style.strokeEnabled) {
                                canvas.drawText(text, centerX, baselineY, strokePaint)
                            }
                            textPaint.alpha = animAlpha
                            canvas.drawText(text, centerX, baselineY, textPaint)
                        }

                        canvas.restore()
                    }

                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }

                // Drain video encoder
                while (true) {
                    val status = encoder.dequeueOutputBuffer(bufferInfo, 0)
                    if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break
                    } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (!muxerStarted) {
                            muxerVideoTrack = muxer.addTrack(encoder.outputFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                    } else if (status >= 0) {
                        val encodedData = encoder.getOutputBuffer(status)
                        if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            bufferInfo.presentationTimeUs = (frame * 1_000_000L) / fps
                            muxer.writeSampleData(muxerVideoTrack, encodedData, bufferInfo)
                        }
                        encoder.releaseOutputBuffer(status, false)
                    }
                }

                onProgress((frame + 1).toFloat() / totalFrames.toFloat())
            }

            encoder.signalEndOfInputStream()

            // Drain remaining frames from encoder
            var isEos = false
            while (!isEos) {
                val status = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
                if (status >= 0) {
                    val encodedData = encoder.getOutputBuffer(status)
                    if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(muxerVideoTrack, encodedData, bufferInfo)
                    }
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isEos = true
                    }
                    encoder.releaseOutputBuffer(status, false)
                } else if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break
                }
            }

            // 3. Audio pass-through into muxer
            if (audioExtractor != null && audioTrackIndexInExtractor != -1 && muxerStarted && muxerAudioTrack != -1) {
                val audioBuffer = ByteBuffer.allocate(128 * 1024)
                val audioBufferInfo = MediaCodec.BufferInfo()
                audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

                while (true) {
                    val sampleSize = audioExtractor.readSampleData(audioBuffer, 0)
                    if (sampleSize < 0) break
                    audioBufferInfo.offset = 0
                    audioBufferInfo.size = sampleSize
                    audioBufferInfo.presentationTimeUs = audioExtractor.sampleTime
                    audioBufferInfo.flags = audioExtractor.sampleFlags

                    if (audioBufferInfo.presentationTimeUs <= totalDurationMs * 1000L) {
                        muxer.writeSampleData(muxerAudioTrack, audioBuffer, audioBufferInfo)
                    }
                    audioExtractor.advance()
                }
            }

            if (muxerStarted) {
                muxer.stop()
            }

        } finally {
            try { retriever.release() } catch (_: Exception) {}
            try { audioExtractor?.release() } catch (_: Exception) {}
            try {
                encoder.stop()
                encoder.release()
            } catch (_: Exception) {}
            try { muxer.release() } catch (_: Exception) {}
        }

        // 4. Save to Android MediaStore Gallery
        val galleryUri = saveVideoToGallery(tempOutputFile)

        Pair(tempOutputFile, galleryUri)
    }

    private fun saveVideoToGallery(videoFile: File): Uri? {
        val filename = "UrduSub_${System.currentTimeMillis()}.mp4"
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, filename)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/UrduSub")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return null

        try {
            resolver.openOutputStream(uri)?.use { out ->
                FileInputStream(videoFile).use { input ->
                    input.copyTo(out)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            return uri
        } catch (e: Exception) {
            e.printStackTrace()
            return uri
        }
    }
}
