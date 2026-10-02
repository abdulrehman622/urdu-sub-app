package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.TranscribingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.VideoCaptionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF090A14)
                ) {
                    UrduSubApp()
                }
            }
        }
    }
}

@Composable
fun UrduSubApp(
    viewModel: VideoCaptionViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()
    val savedProjects by viewModel.savedProjects.collectAsStateWithLifecycle()
    val validationError by viewModel.validationError.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.playbackPositionMs.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val transcriptionStatus by viewModel.transcriptionStatus.collectAsStateWithLifecycle()
    val transcriptionError by viewModel.transcriptionError.collectAsStateWithLifecycle()
    val editingSegment by viewModel.editingSegment.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    val exportedVideoUri by viewModel.exportedVideoUri.collectAsStateWithLifecycle()
    val exportedVideoFile by viewModel.exportedVideoFile.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                savedProjects = savedProjects,
                validationError = validationError,
                customApiKey = customApiKey,
                onVideoSelected = { uri ->
                    viewModel.selectVideoUri(uri)
                },
                onLoadSample = { track ->
                    viewModel.loadSampleTrack(track)
                },
                onOpenProject = { proj ->
                    viewModel.loadExistingProject(proj)
                },
                onDeleteProject = { id ->
                    viewModel.deleteSavedProject(id)
                },
                onDismissError = {
                    viewModel.clearValidationError()
                },
                onSetCustomApiKey = { key ->
                    viewModel.setCustomApiKey(key)
                }
            )
        }

        AppScreen.TRANSCRIBING -> {
            TranscribingScreen(
                statusText = transcriptionStatus,
                errorMessage = transcriptionError,
                onContinueManual = {
                    viewModel.continueWithManualCues()
                },
                onCancel = {
                    viewModel.navigateTo(AppScreen.HOME)
                }
            )
        }

        AppScreen.EDITOR -> {
            currentProject?.let { proj ->
                EditorScreen(
                    project = proj,
                    currentPositionMs = currentPositionMs,
                    isPlaying = isPlaying,
                    editingSegment = editingSegment,
                    onPositionUpdate = { pos -> viewModel.setPlaybackPosition(pos) },
                    onTogglePlay = { viewModel.setPlaying(!isPlaying) },
                    onSeek = { pos -> viewModel.setPlaybackPosition(pos) },
                    onAddCaption = { viewModel.addCaptionAtCurrentPlayhead() },
                    onOpenEditSegment = { seg -> viewModel.openEditSegment(seg) },
                    onCloseEditSegment = { viewModel.closeEditSegment() },
                    onSaveSegment = { seg -> viewModel.saveEditedSegment(seg) },
                    onSplitSegment = { id, splitTime -> viewModel.splitCaption(id, splitTime) },
                    onDeleteSegment = { id -> viewModel.deleteCaption(id) },
                    onUpdateStyle = { newStyle -> viewModel.updateStyle(newStyle) },
                    onExportVideo = { viewModel.startVideoExport() },
                    onBack = { viewModel.navigateTo(AppScreen.HOME) }
                )
            } ?: run {
                viewModel.navigateTo(AppScreen.HOME)
            }
        }

        AppScreen.EXPORT_PREVIEW -> {
            currentProject?.let { proj ->
                ExportScreen(
                    project = proj,
                    isExporting = isExporting,
                    exportProgress = exportProgress,
                    exportedVideoUri = exportedVideoUri,
                    exportedVideoFile = exportedVideoFile,
                    onBackToEditor = { viewModel.navigateTo(AppScreen.EDITOR) },
                    onBackToHome = { viewModel.navigateTo(AppScreen.HOME) }
                )
            } ?: run {
                viewModel.navigateTo(AppScreen.HOME)
            }
        }
    }
}
