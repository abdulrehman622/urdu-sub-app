package com.example.data.repository

import com.example.data.local.ProjectDao
import com.example.data.local.ProjectEntity
import com.example.model.VideoProject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val projectDao: ProjectDao) {

    val allProjects: Flow<List<VideoProject>> = projectDao.getAllProjects().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getProjectById(id: String): VideoProject? {
        return projectDao.getProjectById(id)?.toDomain()
    }

    suspend fun saveProject(project: VideoProject) {
        val updated = project.copy(updatedAt = System.currentTimeMillis())
        projectDao.insertOrUpdate(ProjectEntity.fromDomain(updated))
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteProjectById(id)
    }
}
