package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.audio.AudioExtractor
import com.example.data.export.SubtitleExporter
import com.example.data.export.VideoBurnerEngine
import com.example.data.gemini.GeminiTranscriptionService
import com.example.data.gemini.TranscriptionResult
import com.example.data.local.AppDatabase
import com.example.data.repository.ProjectRepository
import com.example.data.sample.SampleVideoData
import com.example.model.CaptionSegment
import com.example.model.CaptionStyle
import com.example.model.TimedWord
import com.example.model.VideoProject
import com.example.util.MediaValidationResult
import com.example.util.MediaValidator
import com.example.util.UrduTextHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class AppScreen {
    HOME,
    TRANSCRIBING,
    EDITOR,
    EXPORT_PREVIEW
}

class VideoCaptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(AppDatabase.getDatabase(application).projectDao())
    private val geminiService = GeminiTranscriptionService(application)
    private val burnerEngine = VideoBurnerEngine(application)

    // Room DB projects
    val savedProjects: StateFlow<List<VideoProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Current Project
    private val _currentProject = MutableStateFlow<VideoProject?>(null)
    val currentProject: StateFlow<VideoProject?> = _currentProject.asStateFlow()

    // Media validation error
    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    // Video Playback
    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Transcription Status
    private val _transcriptionStatus = MutableStateFlow("Preparing audio...")
    val transcriptionStatus: StateFlow<String> = _transcriptionStatus.asStateFlow()

    private val _transcriptionError = MutableStateFlow<String?>(null)
    val transcriptionError: StateFlow<String?> = _transcriptionError.asStateFlow()

    // Export state
    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportedVideoUri = MutableStateFlow<Uri?>(null)
    val exportedVideoUri: StateFlow<Uri?> = _exportedVideoUri.asStateFlow()

    private val _exportedVideoFile = MutableStateFlow<File?>(null)
    val exportedVideoFile: StateFlow<File?> = _exportedVideoFile.asStateFlow()

    // API Key setting
    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Active Segment being edited in dialog
    private val _editingSegment = MutableStateFlow<CaptionSegment?>(null)
    val editingSegment: StateFlow<CaptionSegment?> = _editingSegment.asStateFlow()

    private var playbackJob: Job? = null

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
    }

    fun clearValidationError() {
        _validationError.value = null
    }

    fun setPlaybackPosition(posMs: Long) {
        _playbackPositionMs.value = posMs
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun selectVideoUri(uri: Uri) {
        val context = getApplication<Application>()
        when (val validation = MediaValidator.validateVideo(context, uri)) {
            is MediaValidationResult.Valid -> {
                _validationError.value = null
                val project = VideoProject(
                    id = UUID.randomUUID().toString(),
                    title = "Video Clip (${validation.formattedDuration})",
                    videoUriString = uri.toString(),
                    durationMs = validation.durationMs,
                    width = validation.width,
                    height = validation.height,
                    captions = emptyList(),
                    style = CaptionStyle()
                )
                _currentProject.value = project
                startAiTranscription(uri, "Urdu")
            }
            is MediaValidationResult.ExceedsDurationLimit -> {
                _validationError.value = "Selected video is ${validation.formattedActual} long. UrduSub supports clips up to 2 minutes (${validation.formattedMax}). Please select a shorter video."
            }
            is MediaValidationResult.Error -> {
                _validationError.value = validation.message
            }
        }
    }

    fun loadSampleTrack(track: SampleVideoData.SampleTrack) {
        viewModelScope.launch {
            _transcriptionStatus.value = "Preparing ${track.title} sample..."
            _currentScreen.value = AppScreen.TRANSCRIBING
            delay(400)

            val context = getApplication<Application>()
            val sampleFile = SampleVideoData.createSyntheticSampleVideo(context, track)
            val sampleUri = Uri.fromFile(sampleFile)

            val project = VideoProject(
                id = UUID.randomUUID().toString(),
                title = track.title,
                videoUriString = sampleUri.toString(),
                durationMs = track.durationMs,
                width = 720,
                height = 1280,
                captions = track.captions,
                style = when (track.id) {
                    "faiz_ghazal" -> CaptionStyle.PRESET_POETRY_GOLD
                    "tu_jhoom" -> CaptionStyle.PRESET_EMERALD_ELEGANT
                    else -> CaptionStyle.PRESET_NEON_CYBER
                }
            )

            _currentProject.value = project
            _playbackPositionMs.value = 0L
            _isPlaying.value = false

            // Auto-save project in Room DB
            repository.saveProject(project)

            delay(600)
            _currentScreen.value = AppScreen.EDITOR
        }
    }

    fun loadExistingProject(project: VideoProject) {
        _currentProject.value = project
        _playbackPositionMs.value = 0L
        _isPlaying.value = false
        _currentScreen.value = AppScreen.EDITOR
    }

    fun startAiTranscription(videoUri: Uri, languagePreference: String) {
        viewModelScope.launch {
            _currentScreen.value = AppScreen.TRANSCRIBING
            _transcriptionError.value = null
            _transcriptionStatus.value = "Extracting video audio track..."

            val context = getApplication<Application>()
            val audioFile = AudioExtractor.extractAudioTrack(context, videoUri)

            _transcriptionStatus.value = "Analyzing Urdu vocals and song lyrics with Gemini AI..."

            val result = geminiService.transcribeAudio(
                audioFile = audioFile,
                customApiKey = _customApiKey.value,
                languagePreference = languagePreference
            )

            when (result) {
                is TranscriptionResult.Success -> {
                    _transcriptionStatus.value = "Urdu transcription generated successfully!"
                    val current = _currentProject.value ?: VideoProject(
                        title = "Urdu Subtitled Video",
                        videoUriString = videoUri.toString(),
                        durationMs = 30_000L
                    )
                    val updated = current.copy(
                        captions = result.segments,
                        updatedAt = System.currentTimeMillis()
                    )
                    _currentProject.value = updated
                    repository.saveProject(updated)
                    delay(500)
                    _currentScreen.value = AppScreen.EDITOR
                }
                is TranscriptionResult.Error -> {
                    _transcriptionError.value = result.message
                    // If AI transcription failed (e.g. no API key or network), provide fallback draft cues so user can still proceed!
                    val fallbackCues = generateFallbackSegments(_currentProject.value?.durationMs ?: 20_000L)
                    val current = _currentProject.value ?: VideoProject(
                        title = "Urdu Subtitled Video",
                        videoUriString = videoUri.toString(),
                        durationMs = 20_000L
                    )
                    val updated = current.copy(
                        captions = fallbackCues,
                        updatedAt = System.currentTimeMillis()
                    )
                    _currentProject.value = updated
                    repository.saveProject(updated)
                }
            }
        }
    }

    fun continueWithManualCues() {
        _currentScreen.value = AppScreen.EDITOR
    }

    private fun generateFallbackSegments(durationMs: Long): List<CaptionSegment> {
        val list = mutableListOf<CaptionSegment>()
        val segmentDuration = 4500L
        var curStart = 500L
        var count = 1
        while (curStart + 1000L < durationMs && count <= 5) {
            val curEnd = (curStart + segmentDuration).coerceAtMost(durationMs - 200L)
            val placeholderText = when (count) {
                1 -> "اردو کیپشن شامل کرنے کے لیے یہاں ٹیپ کریں"
                2 -> "دل سے جو بات نکلتی ہے اثر رکھتی ہے"
                3 -> "سُر اور سنگیت کے خوبصورت بول"
                else -> "اپنے الفاظ یہاں ٹائپ کریں"
            }
            list.add(
                CaptionSegment(
                    id = UUID.randomUUID().toString(),
                    startMs = curStart,
                    endMs = curEnd,
                    text = placeholderText,
                    words = UrduTextHelper.generateWordTimings(placeholderText, curStart, curEnd)
                )
            )
            curStart = curEnd + 400L
            count++
        }
        return list
    }

    // --- Caption Editing Studio Actions ---

    fun openEditSegment(segment: CaptionSegment) {
        _editingSegment.value = segment
    }

    fun closeEditSegment() {
        _editingSegment.value = null
    }

    fun saveEditedSegment(updated: CaptionSegment) {
        val project = _currentProject.value ?: return
        val updatedCaptions = project.captions.map {
            if (it.id == updated.id) updated else it
        }.sortedBy { it.startMs }
        val newProj = project.copy(captions = updatedCaptions, updatedAt = System.currentTimeMillis())
        _currentProject.value = newProj
        _editingSegment.value = null
        viewModelScope.launch { repository.saveProject(newProj) }
    }

    fun addCaptionAtCurrentPlayhead() {
        val project = _currentProject.value ?: return
        val currentPlayhead = _playbackPositionMs.value
        val newEnd = (currentPlayhead + 3000L).coerceAtMost(project.durationMs)
        val defaultText = "نئی اردو لائن"
        val newSegment = CaptionSegment(
            id = UUID.randomUUID().toString(),
            startMs = currentPlayhead,
            endMs = newEnd,
            text = defaultText,
            words = UrduTextHelper.generateWordTimings(defaultText, currentPlayhead, newEnd)
        )
        val updatedList = (project.captions + newSegment).sortedBy { it.startMs }
        val newProj = project.copy(captions = updatedList, updatedAt = System.currentTimeMillis())
        _currentProject.value = newProj
        viewModelScope.launch { repository.saveProject(newProj) }
        openEditSegment(newSegment)
    }

    fun deleteCaption(id: String) {
        val project = _currentProject.value ?: return
        val updatedList = project.captions.filterNot { it.id == id }
        val newProj = project.copy(captions = updatedList, updatedAt = System.currentTimeMillis())
        _currentProject.value = newProj
        if (_editingSegment.value?.id == id) {
            _editingSegment.value = null
        }
        viewModelScope.launch { repository.saveProject(newProj) }
    }

    fun splitCaption(id: String, splitTimeMs: Long) {
        val project = _currentProject.value ?: return
        val target = project.captions.firstOrNull { it.id == id } ?: return
        val pair = target.splitAt(splitTimeMs) ?: return
        val updatedList = project.captions.flatMap {
            if (it.id == id) listOf(pair.first, pair.second) else listOf(it)
        }.sortedBy { it.startMs }
        val newProj = project.copy(captions = updatedList, updatedAt = System.currentTimeMillis())
        _currentProject.value = newProj
        viewModelScope.launch { repository.saveProject(newProj) }
    }

    fun updateStyle(newStyle: CaptionStyle) {
        val project = _currentProject.value ?: return
        val newProj = project.copy(style = newStyle, updatedAt = System.currentTimeMillis())
        _currentProject.value = newProj
        viewModelScope.launch { repository.saveProject(newProj) }
    }

    fun deleteSavedProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
            if (_currentProject.value?.id == id) {
                _currentProject.value = null
            }
        }
    }

    // --- Export Video Actions ---

    fun startVideoExport() {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            _isExporting.value = true
            _exportProgress.value = 0f
            _currentScreen.value = AppScreen.EXPORT_PREVIEW

            try {
                val videoUri = Uri.parse(project.videoUriString)
                val (file, galleryUri) = burnerEngine.burnCaptionsAndExport(
                    videoUri = videoUri,
                    captions = project.captions,
                    style = project.style,
                    onProgress = { p ->
                        _exportProgress.value = p
                    }
                )
                _exportedVideoFile.value = file
                _exportedVideoUri.value = galleryUri ?: Uri.fromFile(file)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun getSrtContent(): String {
        val project = _currentProject.value ?: return ""
        return SubtitleExporter.generateSrtContent(project.captions)
    }

    fun getVttContent(): String {
        val project = _currentProject.value ?: return ""
        return SubtitleExporter.generateVttContent(project.captions)
    }
}
