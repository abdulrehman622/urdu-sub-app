package com.example.model

import java.util.UUID

data class VideoProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val videoUriString: String,
    val durationMs: Long,
    val width: Int = 1080,
    val height: Int = 1920,
    val captions: List<CaptionSegment> = emptyList(),
    val style: CaptionStyle = CaptionStyle(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
