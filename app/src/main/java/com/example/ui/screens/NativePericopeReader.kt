package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import com.example.service.auth.PismoswieteAuthManager
import com.example.service.tts.BibleTtsManager
import com.example.service.tts.TtsPlaybackState
import com.example.ui.components.PismoswieteLoginSheet
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontFamily
import com.example.ui.theme.LiterataFontFamily
import com.example.ui.theme.LoraFontFamily
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.model.BibleSource
import com.example.service.pericope.BibleExtractionEngine
import com.example.service.pericope.PericopeReading
import com.example.service.pericope.PericopeSection
import com.example.ui.viewmodel.ReaderSession

import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val READER_PREFS = "reader_settings"
private const val KEY_FONT_SIZE_SP = "native_reader_font_size"
private const val DEFAULT_FONT_SIZE = 18
private const val KEY_FONT_FAMILY = "native_reader_font_family"
private const val DEFAULT_FONT_FAMILY = "literata"

private val LightPaperColor = Color(0xFFFCFBF9)
private val DarkPaperColor = Color(0xFF161311)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NativePericopeReader(
    session: ReaderSession,
    onClose: () -> Unit,
    onToggleCompleted: () -> Unit,
    onNextPassage: () -> Unit,
    onPrevPassage: () -> Unit,
    onSetBibleSource: (BibleSource) -> Unit,
    onSwitchToWebView: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleFavorite: () -> Unit = {},
    onRecordReadingTime: (Long) -> Unit = {},
    onNextPassageAndMarkCompleted: () -> Unit = {
        if (!session.isCompleted) onToggleCompleted()
        onNextPassage()
    }
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(10_000L)
            onRecordReadingTime(10L)
        }
    }

    DisposableEffect(Unit) {
        val enterTime = System.currentTimeMillis()
        onDispose {
            val leftoverSeconds = ((System.currentTimeMillis() - enterTime) / 1000) % 10
            if (leftoverSeconds > 0) {
                onRecordReadingTime(leftoverSeconds)
            }
        }
    }

    val ttsManager = remember(context) {
        BibleTtsManager(context)
    }
    val ttsPlaybackState by ttsManager.playbackState.collectAsState()
    val ttsSpeechRate by ttsManager.speechRate.collectAsState()

    DisposableEffect(ttsManager) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    LaunchedEffect(session.bookCode, session.chapter, session.passageLabel) {
        ttsManager.stop()
    }

    BackHandler {
        ttsManager.stop()
        onClose()
    }

    val sharedPrefs = remember(context) {
        context.getSharedPreferences(READER_PREFS, Context.MODE_PRIVATE)
    }

    var fontSizeSp by remember {
        mutableIntStateOf(sharedPrefs.getInt(KEY_FONT_SIZE_SP, DEFAULT_FONT_SIZE))
    }
    var fontFamilyChoice by remember {
        mutableStateOf(sharedPrefs.getString(KEY_FONT_FAMILY, DEFAULT_FONT_FAMILY) ?: DEFAULT_FONT_FAMILY)
    }

    fun updateFontFamily(family: String) {
        fontFamilyChoice = family
        sharedPrefs.edit().putString(KEY_FONT_FAMILY, family).apply()
    }

    val currentFontFamily = when (fontFamilyChoice) {
        "lora" -> LoraFontFamily
        "system" -> FontFamily.Default
        else -> LiterataFontFamily
    }

    var showFontSizeSheet by remember { mutableStateOf(false) }
    var showSourceMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showPismoswieteLogin by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }

    val isPismoswieteLoggedIn = remember(context, reloadKey) {
        PismoswieteAuthManager.isLoggedIn(context)
    }

    var readingState by remember { mutableStateOf<PericopeReading?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    fun updateFontSize(newSize: Int) {
        val clamped = newSize.coerceIn(14, 28)
        fontSizeSp = clamped
        sharedPrefs.edit().putInt(KEY_FONT_SIZE_SP, clamped).apply()
    }

    // Załaduj i wyekstrahuj wersety dla zadanego fragmentu
    LaunchedEffect(session.bookCode, session.chapter, session.passageLabel, session.source, reloadKey) {
        isLoading = true
        hasError = false
        try {
            val result = BibleExtractionEngine.getPericopeReading(
                context = context,
                bookCode = session.bookCode,
                defaultChapter = session.chapter,
                label = session.passageLabel,
                source = session.source
            )
            readingState = result
            isLoading = false
        } catch (e: Exception) {
            hasError = true
            isLoading = false
        }
    }

    val paperColor = if (isDarkTheme) Color(0xFF141210) else LightPaperColor
    val textColor = if (isDarkTheme) Color(0xFFF5EBE6) else Color(0xFF212529)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(paperColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("native_pericope_reader")
    ) {
        // Górny pasek narzędziowy czytnika
        TopAppBar(
            title = {
                val shortBookTitle = remember(session.bookCode, session.chapter, session.passageLabel) {
                    if (session.passageLabel.startsWith(session.bookCode)) {
                        session.passageLabel
                    } else {
                        "${session.bookCode} ${session.chapter}"
                    }
                }
                Column(
                    modifier = Modifier.clickable { showSourceMenu = true }
                ) {
                    Text(
                        text = shortBookTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${session.source.shortLabel} ▾ • Dzień ${session.dayNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        ttsManager.stop()
                        onClose()
                    },
                    modifier = Modifier.testTag("reader_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Powrót"
                    )
                }
            },
            actions = {
                // Odtwarzanie lektora (wbudowany Android TTS)
                IconButton(
                    onClick = {
                        val reading = readingState
                        if (reading != null) {
                            ttsManager.togglePlayPause(reading)
                        }
                    },
                    enabled = readingState != null && !isLoading && !hasError,
                    modifier = Modifier.testTag("reader_tts_button")
                ) {
                    Icon(
                        imageVector = when (ttsPlaybackState) {
                            TtsPlaybackState.PLAYING -> Icons.Default.PauseCircle
                            TtsPlaybackState.PAUSED -> Icons.Default.PlayCircle
                            else -> Icons.AutoMirrored.Filled.VolumeUp
                        },
                        contentDescription = when (ttsPlaybackState) {
                            TtsPlaybackState.PLAYING -> "Wstrzymaj lektora"
                            TtsPlaybackState.PAUSED -> "Wznów lektora"
                            else -> "Odtwórz na głos (wbudowany lektor Android)"
                        },
                        tint = if (ttsPlaybackState == TtsPlaybackState.PLAYING) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }

                // Regulacja rozmiaru i kroju czcionki
                IconButton(
                    onClick = { showFontSizeSheet = true },
                    modifier = Modifier.testTag("reader_font_size_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = "Rozmiar tekstu ($fontSizeSp sp)",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Przełącznik trybu ciemnego/jasnego
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.testTag("reader_theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (isDarkTheme) "Włącz tryb jasny" else "Włącz tryb ciemny",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Menu Więcej (Overflow)
                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.testTag("reader_more_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Więcej opcji"
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Zmień źródło Biblii (${session.source.shortLabel})") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                showSourceMenu = true
                            },
                            modifier = Modifier.testTag("reader_switch_source_button")
                        )

                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = if (isPismoswieteLoggedIn) "Konto pismoswiete.pl" else "Zaloguj do pismoswiete.pl",
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = if (isPismoswieteLoggedIn) "Zalogowano • Kliknij, aby wylogować" else "Odblokuj wszystkie rozdziały",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isPismoswieteLoggedIn) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = if (isPismoswieteLoggedIn) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                if (isPismoswieteLoggedIn) {
                                    PismoswieteAuthManager.clearSession(context)
                                    BibleExtractionEngine.clearCache(context)
                                    reloadKey++
                                } else {
                                    showPismoswieteLogin = true
                                }
                            },
                            modifier = Modifier.testTag("reader_pismoswiete_auth_menu_item")
                        )

                        DropdownMenuItem(
                            text = { Text(if (session.isFavorite) "Usuń z ulubionych" else "Dodaj do ulubionych") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (session.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (session.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                onToggleFavorite()
                            },
                            modifier = Modifier.testTag("reader_favorite_button")
                        )

                        DropdownMenuItem(
                            text = { Text("Przełącz na widok WWW (WebView)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                ttsManager.stop()
                                onSwitchToWebView()
                            },
                            modifier = Modifier.testTag("reader_switch_webview_button")
                        )
                    }

                    // Menu wyboru źródła (pismoswiete.pl / deon.pl)
                    DropdownMenu(
                        expanded = showSourceMenu,
                        onDismissRequest = { showSourceMenu = false },
                        modifier = Modifier.testTag("reader_source_dropdown")
                    ) {
                        BibleSource.entries.forEach { source ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = source.title,
                                            fontWeight = if (source == session.source) FontWeight.Bold else FontWeight.Normal,
                                            color = if (source == session.source) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = source.translationInfo,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                leadingIcon = {
                                    if (source == session.source) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Wybrane źródło",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.size(24.dp))
                                    }
                                },
                                onClick = {
                                    showSourceMenu = false
                                    if (source != session.source) {
                                        onSetBibleSource(source)
                                    }
                                },
                                modifier = Modifier.testTag("reader_source_option_${source.id}")
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Pasek kontrolny odtwarzacza lektora TTS
        AnimatedVisibility(
            visible = ttsPlaybackState == TtsPlaybackState.PLAYING || ttsPlaybackState == TtsPlaybackState.PAUSED
        ) {
            Surface(
                color = if (isDarkTheme) Color(0xFF26201D) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                tonalElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reader_tts_player_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (ttsPlaybackState == TtsPlaybackState.PLAYING) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.Pause,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (ttsPlaybackState == TtsPlaybackState.PLAYING) "Odtwarzanie lektora" else "Lektor wstrzymany",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Przełącznik prędkości
                        TextButton(
                            onClick = { ttsManager.cycleSpeechRate() },
                            modifier = Modifier.testTag("reader_tts_speed_button")
                        ) {
                            Text(
                                text = "${ttsSpeechRate}x",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Pauza / Wznów
                        IconButton(
                            onClick = {
                                val reading = readingState
                                if (reading != null) {
                                    ttsManager.togglePlayPause(reading)
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("reader_tts_pause_resume_button")
                        ) {
                            Icon(
                                imageVector = if (ttsPlaybackState == TtsPlaybackState.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (ttsPlaybackState == TtsPlaybackState.PLAYING) "Pauza" else "Odtwórz",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Stop
                        IconButton(
                            onClick = { ttsManager.stop() },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("reader_tts_stop_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Zatrzymaj lektora",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // Główny obszar czytnika
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(paperColor)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Pobieranie wersetów...",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = session.passageLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                hasError || readingState == null -> {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                            .testTag("reader_error_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Nie udało się pobrać fragmentu",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sprawdź połączenie z internetem. Możesz spróbować ponownie lub otworzyć stronę w trybie WebView.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        isLoading = true
                                        hasError = false
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ponów")
                                }
                                Button(
                                    onClick = onSwitchToWebView,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Text("Otwórz WWW")
                                }
                            }
                        }
                    }
                }

                else -> {
                    val reading = readingState!!
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            // Nagłówek Księgi i Perykopy
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = reading.passageLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                            Text(
                                text = reading.bookName,
                                fontFamily = currentFontFamily,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDarkTheme) Color(0xFFFFE082) else MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = reading.sourceName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .widthIn(max = 240.dp)
                                    .padding(bottom = 14.dp)
                            )

                            // Przycisk odsłuchania na głos (wbudowany lektor Android)
                            AssistChip(
                                onClick = { ttsManager.togglePlayPause(reading) },
                                label = {
                                    Text(
                                        text = when (ttsPlaybackState) {
                                            TtsPlaybackState.PLAYING -> "Wstrzymaj lektora"
                                            TtsPlaybackState.PAUSED -> "Wznów lektora"
                                            else -> "Odsłuchaj fragment (Lektor)"
                                        },
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (ttsPlaybackState) {
                                            TtsPlaybackState.PLAYING -> Icons.Default.Pause
                                            TtsPlaybackState.PAUSED -> Icons.Default.PlayArrow
                                            else -> Icons.AutoMirrored.Filled.VolumeUp
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (ttsPlaybackState == TtsPlaybackState.PLAYING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (ttsPlaybackState == TtsPlaybackState.PLAYING) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                ),
                                modifier = Modifier
                                    .padding(bottom = 16.dp)
                                    .testTag("reader_header_tts_chip")
                            )
                        }

                        // Sekcje z rozdziałami i wyciętymi wersetami
                        items(reading.sections) { section ->
                            SectionContent(
                                section = section,
                                showChapterHeader = reading.sections.size > 1,
                                fontSizeSp = fontSizeSp,
                                fontFamily = currentFontFamily,
                                textColor = textColor,
                                isDarkTheme = isDarkTheme,
                                onOpenLogin = { showPismoswieteLogin = true }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(28.dp))

                            if (session.totalPassages > 1) {
                                if (session.hasNext) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        Text(
                                            text = "Koniec fragmentu ${session.passageIndex + 1} z ${session.totalPassages} na dziś",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                        )
                                    }

                                    val nextLabel = session.nextPassageLabel ?: "kolejnego fragmentu"
                                    FilledTonalButton(
                                        onClick = {
                                            ttsManager.stop()
                                            onNextPassageAndMarkCompleted()
                                        },
                                        modifier = Modifier
                                            .padding(bottom = 14.dp)
                                            .testTag("reader_next_fragment_button")
                                    ) {
                                        Text(
                                            text = "Kolejny fragment na dziś: $nextLabel",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(bottom = 14.dp)
                                    ) {
                                        Text(
                                            text = "Koniec wszystkich fragmentów na dziś (${session.totalPassages} z ${session.totalPassages})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(bottom = 14.dp)
                                ) {
                                    Text(
                                        text = "Koniec fragmentu na dziś",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            // Podlinkowana małą czcionką informacja o źródle tekstu
                            val passageUrl = remember(session.bookCode, session.chapter, session.source) {
                                com.example.model.ReadingPassage(session.bookCode, session.bookName, session.chapter).toUrl(session.source)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .padding(bottom = 36.dp)
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(passageUrl))
                                        context.startActivity(intent)
                                    }
                                    .testTag("reader_source_attribution_link")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tekst pochodzi ze strony ${session.source.title}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dolny pasek nawigacyjny czytnika
        Surface(
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Przycisk poprzedniej lektury
                FilledTonalIconButton(
                    onClick = {
                        ttsManager.stop()
                        onPrevPassage()
                    },
                    enabled = session.hasPrev,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("reader_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = "Poprzednia lektura"
                    )
                }

                // Przycisk oznaczania przeczytania
                Button(
                    onClick = onToggleCompleted,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("reader_toggle_completed_button"),
                    colors = if (session.isCompleted) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                ) {
                    Icon(
                        imageVector = if (session.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (session.isCompleted) "Przeczytane" else "Oznacz przeczytane",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Przycisk kolejnej lektury
                FilledTonalIconButton(
                    onClick = {
                        ttsManager.stop()
                        onNextPassage()
                    },
                    enabled = session.hasNext,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("reader_next_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = "Kolejna lektura"
                    )
                }
            }
        }
    }

    // Modalny arkusz zmiany rozmiaru czcionki
    if (showFontSizeSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showFontSizeSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.testTag("font_size_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rozmiar tekstu lektury",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dopasuj wielkość czcionki do swojej wygody czytania",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { updateFontSize(fontSizeSp - 2) },
                        enabled = fontSizeSp > 14,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("font_decrease_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zmniejsz czcionkę",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(width = 110.dp, height = 48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${fontSizeSp} sp",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    FilledIconButton(
                        onClick = { updateFontSize(fontSizeSp + 2) },
                        enabled = fontSizeSp < 28,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("font_increase_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zwiększ czcionkę",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Presety
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        14 to "Mała",
                        16 to "Standardowa",
                        18 to "Wygodna",
                        22 to "Duża",
                        26 to "Bardzo duża"
                    )
                    presets.forEach { (sz, label) ->
                        FilterChip(
                            selected = fontSizeSp == sz,
                            onClick = { updateFontSize(sz) },
                            label = {
                                Text(
                                    label,
                                    fontWeight = if (fontSizeSp == sz) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Krój czcionki lektury",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Wybierz czcionkę zoptymalizowaną do czytania Pisma Świętego",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val fontOptions = listOf(
                        "literata" to "Literata (Książkowa)",
                        "lora" to "Lora (Klasyczna)",
                        "system" to "Systemowa"
                    )
                    fontOptions.forEach { (key, name) ->
                        FilterChip(
                            selected = fontFamilyChoice == key,
                            onClick = { updateFontFamily(key) },
                            label = {
                                Text(
                                    text = name,
                                    fontFamily = when (key) {
                                        "lora" -> LoraFontFamily
                                        "system" -> FontFamily.Default
                                        else -> LiterataFontFamily
                                    },
                                    fontWeight = if (fontFamilyChoice == key) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("font_choice_$key")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Podgląd na żywo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF201B18) else Color(0xFFF9F7F4)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Podgląd tekstu lektury:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "¹ Na początku Bóg stworzył niebo i ziemię. ² Ziemia zaś była bezładem i pustką...",
                            fontFamily = currentFontFamily,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.7f).sp,
                            color = textColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showPismoswieteLogin) {
        PismoswieteLoginSheet(
            onDismiss = { showPismoswieteLogin = false },
            onLoginSuccess = { reloadKey++ }
        )
    }
}

@Composable
private fun SectionContent(
    section: PericopeSection,
    showChapterHeader: Boolean,
    fontSizeSp: Int,
    fontFamily: FontFamily,
    textColor: Color,
    isDarkTheme: Boolean,
    onOpenLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .padding(vertical = 8.dp)
    ) {
        // Informacja o pobraniu z deon.pl w razie braku logowania do pismoswiete.pl
        if (section.isDeonFallback) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("section_login_fallback_banner")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rozdział wymaga bezpłatnego logowania w pismoswiete.pl",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "W pismoswiete.pl treść po rozdziale 1 jest dostępna dla zalogowanych. Załadowano tekst z biblia.deon.pl (Biblia Tysiąclecia), aby czytanie nie zostało przerwane.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = onOpenLogin,
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("section_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Zaloguj się do pismoswiete.pl")
                    }
                }
            }
        }
        // Nagłówek rozdziału (np. "Rozdział 2"), gdy fragment obejmuje wiele rozdziałów
        if (showChapterHeader) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = "Rozdział ${section.chapter}",
                    fontFamily = fontFamily,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color(0xFFFFE082) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        // Śródtytuł / perykopa
        if (!section.sectionTitle.isNullOrBlank()) {
            Text(
                text = section.sectionTitle,
                fontFamily = fontFamily,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = if (isDarkTheme) Color(0xFFFFAB91) else Color(0xFF0D47A1),
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )
        }

        // Płynny tekst wersetów z indeksami górnymi
        val annotatedVerses = buildAnnotatedString {
            section.verses.forEach { verse ->
                // Numer wersetu w indeksie górnym z akcentem
                withStyle(
                    SpanStyle(
                        fontFamily = fontFamily,
                        fontSize = (fontSizeSp * 0.72f).sp,
                        baselineShift = BaselineShift.Superscript,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color(0xFFFFB74D) else Color(0xFF1565C0)
                    )
                ) {
                    append("${verse.verseNumber} ")
                }
                // Treść wersetu
                withStyle(
                    SpanStyle(
                        fontFamily = fontFamily,
                        fontSize = fontSizeSp.sp,
                        color = textColor
                    )
                ) {
                    append(verse.text)
                    append("  ")
                }
            }
        }

        Text(
            text = annotatedVerses,
            fontFamily = fontFamily,
            lineHeight = (fontSizeSp * 1.75f).sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
