package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BibleBottomNavBar
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NativePericopeReader
import com.example.ui.screens.PlansScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MainViewModel

import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.viewmodel.AppThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BibleApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleApp(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (uiState.themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    MyApplicationTheme(darkTheme = isDarkTheme) {
        // Handle back button on sub-tabs
        if (uiState.readerSession == null && uiState.currentTab != AppNavTab.TODAY) {
            BackHandler {
                viewModel.selectTab(AppNavTab.TODAY)
            }
        }

        // Fullscreen in-app reader takes precedence when a chapter is open
        val currentReader = uiState.readerSession
        if (currentReader != null) {
            if (currentReader.useNativeReader) {
                NativePericopeReader(
                    session = currentReader,
                    onClose = { viewModel.closeReader() },
                    onToggleCompleted = { viewModel.toggleCurrentReaderPassage() },
                    onNextPassage = { viewModel.nextChapterInReader() },
                    onNextPassageAndMarkCompleted = { viewModel.markCurrentReaderPassageCompletedAndNext() },
                    onPrevPassage = { viewModel.prevChapterInReader() },
                    onSetBibleSource = { viewModel.setBibleSource(it) },
                    onSwitchToWebView = { viewModel.setReaderMode(native = false) },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleThemeMode() }
                )
            } else {
                ReaderScreen(
                    session = currentReader,
                    onClose = { viewModel.closeReader() },
                    onToggleCompleted = { viewModel.toggleCurrentReaderPassage() },
                    onNextChapter = { viewModel.nextChapterInReader() },
                    onPrevChapter = { viewModel.prevChapterInReader() },
                    onSetBibleSource = { viewModel.setBibleSource(it) },
                    onSwitchToNativeReader = { viewModel.setReaderMode(native = true) },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleThemeMode() }
                )
            }
        } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("bible_main_scaffold"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Manna 365",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Pismo Święte",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("main_top_app_bar")
                )
            },
            bottomBar = {
                BibleBottomNavBar(
                    selectedTab = uiState.currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = uiState.currentTab, label = "tab_transition") { tab ->
                    when (tab) {
                        AppNavTab.TODAY -> {
                            HomeScreen(
                                uiState = uiState,
                                onOpenReader = { passage, day -> viewModel.openReader(passage, day) },
                                onTogglePassage = { passage, day -> viewModel.togglePassage(passage, day) },
                                onNextDay = { viewModel.nextDay() },
                                onPrevDay = { viewModel.previousDay() },
                                onJumpToToday = { viewModel.jumpToToday() },
                                onMarkDayCompleted = { viewModel.markDayCompleted() },
                                onNavigateToPlans = { viewModel.selectTab(AppNavTab.PLANS) }
                            )
                        }

                        AppNavTab.PLANS -> {
                            PlansScreen(
                                uiState = uiState,
                                availablePlans = viewModel.availablePlans,
                                onSelectPlan = { planId -> viewModel.selectPlan(planId) }
                            )
                        }

                        AppNavTab.PROGRESS -> {
                            ProgressScreen(
                                uiState = uiState
                            )
                        }

                        AppNavTab.SETTINGS -> {
                            SettingsScreen(
                                uiState = uiState,
                                onUpdateReminder = { enabled, h, m -> viewModel.updateReminder(enabled, h, m) },
                                onResetPlan = { viewModel.resetPlan() },
                                onSetBibleSource = { viewModel.setBibleSource(it) },
                                onSetThemeMode = { viewModel.setThemeMode(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
}
