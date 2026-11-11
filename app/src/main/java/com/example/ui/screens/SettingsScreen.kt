package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.UiState

import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.Circle
import com.example.model.BibleSource
import com.example.service.auth.PismoswieteAuthManager
import com.example.service.pericope.BibleExtractionEngine
import com.example.ui.components.FeedbackDialog
import com.example.ui.components.PismoswieteLoginSheet
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.service.backup.BackupManager
import com.example.service.backup.BackupRestoreResult
import com.example.ui.viewmodel.AppThemeMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: UiState,
    onUpdateReminder: (Boolean, Int, Int) -> Unit,
    onResetPlan: () -> Unit,
    onSetBibleSource: (BibleSource) -> Unit = {},
    onSetThemeMode: (AppThemeMode) -> Unit = {},
    onExportBackup: suspend () -> String = { "" },
    onImportBackup: suspend (String) -> BackupRestoreResult = { BackupRestoreResult(false, message = "") },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = remember(context) {
        context.getSharedPreferences("reader_settings", Context.MODE_PRIVATE)
    }
    var readerFontZoom by remember {
        mutableIntStateOf(sharedPrefs.getInt("font_zoom_percent", 130))
    }

    var showResetDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var showPismoswieteLogin by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var loginStateTrigger by remember { mutableIntStateOf(0) }

    var backupStatusMessage by remember { mutableStateOf<String?>(null) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var pendingRestoreResult by remember { mutableStateOf<BackupRestoreResult?>(null) }
    var isOperatingBackup by remember { mutableStateOf(false) }

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isOperatingBackup = true
                val json = onExportBackup()
                val ok = BackupManager.writeJsonToUri(context, uri, json)
                isOperatingBackup = false
                backupStatusMessage = if (ok) {
                    "Kopia zapasowa została pomyślnie zapisana (Dysk Google / pamięć urządzenia)!"
                } else {
                    "Nie udało się zapisać pliku kopii zapasowej."
                }
            }
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isOperatingBackup = true
                val json = BackupManager.readJsonFromUri(context, uri)
                isOperatingBackup = false
                if (json.isNullOrBlank()) {
                    backupStatusMessage = "Nie udało się odczytać wybranego pliku lub plik jest pusty."
                } else {
                    val preview = BackupManager.parseBackupJson(context, json)
                    if (preview.success) {
                        pendingRestoreJson = json
                        pendingRestoreResult = preview
                    } else {
                        backupStatusMessage = preview.message
                    }
                }
            }
        }
    }

    val isPismoswieteLoggedIn = remember(context, loginStateTrigger) {
        PismoswieteAuthManager.isLoggedIn(context)
    }
    val pismoswieteUserEmail = remember(context, loginStateTrigger) {
        PismoswieteAuthManager.getUserEmail(context)
    }

    var selectedHour by remember(uiState.userState.reminderHour) {
        mutableIntStateOf(uiState.userState.reminderHour)
    }
    var selectedMinute by remember(uiState.userState.reminderMinute) {
        mutableIntStateOf(uiState.userState.reminderMinute)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onUpdateReminder(true, selectedHour, selectedMinute)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Ustawienia",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Personalizacja przypomnień i informacje o źródle tekstu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Reminders Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminder_settings_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Codzienne przypomnienie",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Powiadomienie o lekturze na dany dzień",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = uiState.userState.reminderEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onUpdateReminder(enabled, selectedHour, selectedMinute)
                                }
                            },
                            modifier = Modifier.testTag("reminder_switch")
                        )
                    }

                    if (uiState.userState.reminderEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Godzina przypomnienia",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = String.format("%02d:%02d", uiState.userState.reminderHour, uiState.userState.reminderMinute),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            OutlinedButton(
                                onClick = { showTimeDialog = true },
                                modifier = Modifier.testTag("change_time_button")
                            ) {
                                Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Zmień")
                            }
                        }
                    }
                }
            }
        }

        // App Theme Mode Card (System / Light / Dark)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_theme_settings_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Motyw aplikacji",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Wybierz tryb jasny, nocny (ciemny) lub automatyczny",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val themeOptions = listOf(
                            Triple(
                                AppThemeMode.SYSTEM,
                                "Zgodny z systemem",
                                "Dostosowuje się automatycznie do ustawień urządzenia"
                            ),
                            Triple(
                                AppThemeMode.LIGHT,
                                "Jasny (Dzień)",
                                "Jasne tło i wysoki kontrast w ciągu dnia"
                            ),
                            Triple(
                                AppThemeMode.DARK,
                                "Ciemny (Noc)",
                                "Ciemne, ciepłe tło łagodne dla oczu w nocy"
                            )
                        )

                        themeOptions.forEach { (mode, title, desc) ->
                            val isSelected = uiState.themeMode == mode
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                onClick = { onSetThemeMode(mode) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (mode) {
                                            AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                            AppThemeMode.LIGHT -> Icons.Default.LightMode
                                            AppThemeMode.DARK -> Icons.Default.DarkMode
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                        contentDescription = if (isSelected) "Wybrany motyw" else null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bible Source selection card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bible_source_settings_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Źródło tekstu Pisma Świętego",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Wybierz portal, z którego wyświetlany jest tekst lektury",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        BibleSource.entries.forEach { source ->
                            val isSelected = uiState.selectedBibleSource == source
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                onClick = { onSetBibleSource(source) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                        contentDescription = if (isSelected) "Zaznaczone" else "Niezaznaczone",
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = source.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = source.translationInfo,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = if (source == BibleSource.PISMOSWIETE) {
                                                "Współczesny, dynamiczny przekład Towarzystwa Świętego Pawła"
                                            } else {
                                                "Oficjalny tekst liturgiczny Kościoła w Polsce (Pallottinum)"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reader typography / font zoom card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reader_font_settings_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Rozmiar tekstu lektury",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Dopasowanie wielkości czcionki na ekranie ($readerFontZoom%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            100 to "100% Mała",
                            115 to "115% Standard",
                            130 to "130% Wygodna",
                            150 to "150% Duża",
                            175 to "175% Bardzo duża"
                        )
                        presets.forEach { (pct, label) ->
                            FilterChip(
                                selected = readerFontZoom == pct,
                                onClick = {
                                    readerFontZoom = pct
                                    sharedPrefs.edit().putInt("font_zoom_percent", pct).apply()
                                },
                                label = {
                                    Text(
                                        label,
                                        fontWeight = if (readerFontZoom == pct) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Podgląd wielkości tekstu:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Na początku Bóg stworzył niebo i ziemię. A ziemia była bezładną pustką...",
                                fontSize = (15f * readerFontZoom / 100f).sp,
                                lineHeight = (22f * readerFontZoom / 100f).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // About Bible Sources & credits
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_source_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "O źródłach tekstu Pisma Świętego",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Aplikacja umożliwia czytanie fragmentów z dwóch zaufanych polskich portali biblijnych w zoptymalizowanym, czytelnym trybie lektury:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• biblia.deon.pl — Biblia Tysiąclecia (Wydawnictwo Pallottinum), podstawowy przekład liturgiczny Kościoła katolickiego w Polsce.\n• pismoswiete.pl — Edycja Świętego Pawła (Towarzystwo Świętego Pawła), ceniony współczesny przekład z bogatym komentarzem.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://biblia.deon.pl/rozdzial.php?id=1"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("visit_deon_button")
                        ) {
                            Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deon.pl")
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pismoswiete.pl/demo#/tome/Rdz/chapter/1/"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("visit_pismoswiete_button")
                        ) {
                            Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pismoswiete.pl")
                        }
                    }
                }
            }
        }

        // Karta konta pismoswiete.pl
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pismoswiete_account_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = if (isPismoswieteLoggedIn) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Konto pismoswiete.pl",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isPismoswieteLoggedIn) {
                                    if (!pismoswieteUserEmail.isNullOrBlank()) {
                                        "Zalogowano ($pismoswieteUserEmail) — pełny dostęp do wszystkich rozdziałów Edycji Św. Pawła"
                                    } else {
                                        "Zalogowano — pełny dostęp do wszystkich rozdziałów Edycji Św. Pawła"
                                    }
                                } else {
                                    "Niezalogowany — bezpłatna rejestracja/logowanie w pismoswiete.pl odblokowuje wszystkie rozdziały Edycji Św. Pawła (domyślnie używane jest biblia.deon.pl)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isPismoswieteLoggedIn) {
                        OutlinedButton(
                            onClick = {
                                PismoswieteAuthManager.clearSession(context)
                                BibleExtractionEngine.clearCache(context)
                                loginStateTrigger++
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("logout_pismoswiete_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wyloguj z pismoswiete.pl")
                        }
                    } else {
                        Button(
                            onClick = { showPismoswieteLogin = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_pismoswiete_button")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Zaloguj lub zarejestruj w pismoswiete.pl")
                        }
                    }
                }
            }
        }

        // Backup & Cloud (Google Drive / JSON) section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backup_settings_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Kopia zapasowa i chmura",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dysk Google / Plik JSON",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Możesz bezpiecznie wyeksportować swój postęp czytania ze wszystkich planów oraz ustawienia bezpośrednio na bezpłatny Dysk Google lub do pliku JSON w pamięci urządzenia. Pozwala to zachować historię czytania lub przenieść ją na inny telefon.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // 1. Zapisz kopię (SAF - wybór Dysku Google lub pamięci urządzenia)
                        Button(
                            onClick = {
                                val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault()).format(java.util.Date())
                                exportFileLauncher.launch("manna365_kopia_$dateStr.json")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("export_backup_button"),
                            enabled = !isOperatingBackup
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Zapisz kopię (Dysk Google / Plik JSON)")
                        }

                        // 2. Udostępnij do chmury (Sharesheet - Dysk Google, Gmail itp.)
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    isOperatingBackup = true
                                    val json = onExportBackup()
                                    isOperatingBackup = false
                                    BackupManager.shareBackupJson(context, json)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("share_backup_button"),
                            enabled = !isOperatingBackup
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Udostępnij / Prześlij na Dysk Google")
                        }

                        // 3. Przywróć z Dysku Google lub pliku JSON
                        OutlinedButton(
                            onClick = {
                                importFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("import_backup_button"),
                            enabled = !isOperatingBackup
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Przywróć z Dysku Google / Pliku JSON")
                        }
                    }
                }
            }
        }

        // Contact and Feedback section (Firebase database submission)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_feedback_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Kontakt z autorem i pomoc",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Zgłoś błąd, zaproponuj funkcję lub zadaj pytanie",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Masz sugestię nowej funkcji, zauważyłeś błąd w tekście lub chcesz skontaktować się z autorem? Wypełnij prosty formularz — zgłoszenie zostanie bezpiecznie zapisane w bazie danych wraz z parametrami technicznymi urządzenia ułatwiającymi diagnozę.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showFeedbackDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_feedback_button")
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Otwórz formularz kontaktowy")
                    }
                }
            }
        }

        // Reset plan section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("danger_zone_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Zarządzanie postępem",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Możesz zresetować postęp bieżącego planu (${uiState.activePlan.title}) i zacząć od Dnia 1.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("reset_plan_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Zresetuj postęp planu")
                    }
                }
            }
        }

        // App info footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Manna 365 • Wersja ${com.example.BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Gotowość do publikacji w Google Play",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Zresetować postęp planu?") },
            text = {
                Text("Wszystkie odznaczone przeczytane rozdziały dla planu „${uiState.activePlan.title}” zostaną usunięte, a plan powróci do Dnia 1.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetPlan()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("Tak, zresetuj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // Material 3 Time Picker Dialog pozwalający na dowolny wybór godziny i minuty
    if (showTimeDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = true
        )
        var isKeyboardMode by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pora przypomnienia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { isKeyboardMode = !isKeyboardMode },
                        modifier = Modifier.testTag("toggle_time_input_mode_button")
                    ) {
                        Icon(
                            imageVector = if (isKeyboardMode) Icons.Default.Schedule else Icons.Default.Keyboard,
                            contentDescription = if (isKeyboardMode) "Tryb tarczy zegara" else "Tryb klawiatury",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isKeyboardMode) {
                        TimeInput(
                            state = timePickerState,
                            modifier = Modifier.testTag("reminder_time_input")
                        )
                    } else {
                        TimePicker(
                            state = timePickerState,
                            modifier = Modifier.testTag("reminder_time_picker")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Szybki wybór:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickTimes = listOf(
                            Pair(6, 30) to "06:30",
                            Pair(7, 0) to "07:00",
                            Pair(8, 0) to "08:00",
                            Pair(12, 0) to "12:00",
                            Pair(19, 0) to "19:00",
                            Pair(21, 0) to "21:00"
                        )
                        quickTimes.forEach { (time, label) ->
                            val isSelected = timePickerState.hour == time.first && timePickerState.minute == time.second
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedHour = time.first
                                    selectedMinute = time.second
                                    onUpdateReminder(true, time.first, time.second)
                                    showTimeDialog = false
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val h = timePickerState.hour
                        val m = timePickerState.minute
                        selectedHour = h
                        selectedMinute = m
                        onUpdateReminder(true, h, m)
                        showTimeDialog = false
                    },
                    modifier = Modifier.testTag("reminder_time_confirm_button")
                ) {
                    Text("Zapisz")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimeDialog = false },
                    modifier = Modifier.testTag("reminder_time_cancel_button")
                ) {
                    Text("Anuluj")
                }
            }
        )
    }

    if (showPismoswieteLogin) {
        PismoswieteLoginSheet(
            onDismiss = { showPismoswieteLogin = false },
            onLoginSuccess = { loginStateTrigger++ }
        )
    }

    // Confirmation Dialog for Restore
    pendingRestoreResult?.let { preview ->
        AlertDialog(
            onDismissRequest = {
                pendingRestoreResult = null
                pendingRestoreJson = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Przywrócić kopię zapasową?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Znaleziono poprawne dane w pliku kopii zapasowej:")
                    Text("• Przeczytane fragmenty: ${preview.importedProgressCount}", fontWeight = FontWeight.SemiBold)
                    Text("• Plany czytania: ${preview.importedPlansCount}", fontWeight = FontWeight.SemiBold)
                    if (preview.restoredSettings.isNotEmpty()) {
                        Text("• Przywrócone ustawienia (motyw, czcionka, źródło)")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Czy chcesz zastąpić bieżący postęp danymi z tej kopii?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val json = pendingRestoreJson
                        pendingRestoreResult = null
                        pendingRestoreJson = null
                        if (json != null) {
                            coroutineScope.launch {
                                isOperatingBackup = true
                                val res = onImportBackup(json)
                                isOperatingBackup = false
                                backupStatusMessage = if (res.success) {
                                    "Pomyślnie przywrócono postęp czytania (${res.importedProgressCount} fragmentów) i ustawienia!"
                                } else {
                                    "Błąd przywracania: ${res.message}"
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_restore_button")
                ) {
                    Text("Tak, przywróć dane")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingRestoreResult = null
                    pendingRestoreJson = null
                }) {
                    Text("Anuluj")
                }
            }
        )
    }

    if (showFeedbackDialog) {
        FeedbackDialog(
            preferredBibleSourceTitle = uiState.selectedBibleSource.title,
            onDismiss = { showFeedbackDialog = false }
        )
    }

    // Status / Feedback Alert Dialog
    backupStatusMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { backupStatusMessage = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Kopia zapasowa") },
            text = { Text(msg) },
            confirmButton = {
                Button(onClick = { backupStatusMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

