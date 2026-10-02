package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CaptionSegment
import com.example.model.CaptionStyle
import com.example.model.VideoProject

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val videoUriString: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val captions: List<CaptionSegment>,
    val style: CaptionStyle,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): VideoProject = VideoProject(
        id = id,
        title = title,
        videoUriString = videoUriString,
        durationMs = durationMs,
        width = width,
        height = height,
        captions = captions,
        style = style,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(project: VideoProject): ProjectEntity = ProjectEntity(
            id = project.id,
            title = project.title,
            videoUriString = project.videoUriString,
            durationMs = project.durationMs,
            width = project.width,
            height = project.height,
            captions = project.captions,
            style = project.style,
            createdAt = project.createdAt,
            updatedAt = project.updatedAt
        )
    }
}
