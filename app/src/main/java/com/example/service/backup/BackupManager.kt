package com.example.service.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.entity.ReadingProgressEntity
import com.example.data.entity.UserPlanStateEntity
import com.example.model.BibleSource
import com.example.ui.viewmodel.AppThemeMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupRestoreResult(
    val success: Boolean,
    val importedProgressCount: Int = 0,
    val importedPlansCount: Int = 0,
    val restoredSettings: Map<String, Any> = emptyMap(),
    val states: List<UserPlanStateEntity> = emptyList(),
    val progress: List<ReadingProgressEntity> = emptyList(),
    val message: String
)

object BackupManager {

    private const val BACKUP_VERSION = 1
    private const val PREFS_NAME = "reader_settings"

    /**
     * Tworzy ciąg JSON z przekazanych danych (niezależny od Contextu Androida).
     */
    fun createBackupJsonFromData(
        planStates: List<UserPlanStateEntity>,
        progressList: List<ReadingProgressEntity>,
        activePlanId: String,
        settings: Map<String, Any>
    ): String {
        val root = JSONObject()
        root.put("version", BACKUP_VERSION)
        root.put("appName", "Manna 365")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("exportedAtFormatted", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        root.put("activePlanId", activePlanId)

        // 1. Ustawienia
        val settingsObj = JSONObject()
        for ((key, value) in settings) {
            settingsObj.put(key, value)
        }
        root.put("settings", settingsObj)

        // 2. Stany planów
        val planStatesArray = JSONArray()
        for (state in planStates) {
            val stateObj = JSONObject()
            stateObj.put("planId", state.planId)
            stateObj.put("isActive", state.isActive)
            stateObj.put("startDate", state.startDate)
            stateObj.put("currentDay", state.currentDay)
            stateObj.put("lastReadDateMillis", state.lastReadDateMillis)
            stateObj.put("streakDays", state.streakDays)
            stateObj.put("reminderHour", state.reminderHour)
            stateObj.put("reminderMinute", state.reminderMinute)
            stateObj.put("reminderEnabled", state.reminderEnabled)
            planStatesArray.put(stateObj)
        }
        root.put("planStates", planStatesArray)

        // 3. Postęp czytania
        val progressArray = JSONArray()
        for (p in progressList) {
            val pObj = JSONObject()
            pObj.put("planId", p.planId)
            pObj.put("dayNumber", p.dayNumber)
            pObj.put("bookCode", p.bookCode)
            pObj.put("chapter", p.chapter)
            pObj.put("isCompleted", p.isCompleted)
            pObj.put("completedAt", p.completedAt)
            progressArray.put(pObj)
        }
        root.put("readingProgress", progressArray)

        return root.toString(2)
    }

    /**
     * Tworzy pełną kopię zapasową w formacie JSON pobierając bieżące ustawienia z Context.
     */
    fun createBackupJson(
        context: Context,
        planStates: List<UserPlanStateEntity>,
        progressList: List<ReadingProgressEntity>,
        activePlanId: String
    ): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val settings = mapOf(
            "preferred_bible_source" to (prefs.getString("preferred_bible_source", BibleSource.PISMOSWIETE.id) ?: BibleSource.PISMOSWIETE.id),
            "app_theme_mode" to (prefs.getString("app_theme_mode", AppThemeMode.SYSTEM.id) ?: AppThemeMode.SYSTEM.id),
            "pref_use_native_reader" to prefs.getBoolean("pref_use_native_reader", true),
            "native_reader_font_size" to prefs.getInt("native_reader_font_size", 18),
            "font_zoom_percent" to prefs.getInt("font_zoom_percent", 130),
            "total_reading_time_seconds" to prefs.getLong("total_reading_time_seconds", 0L),
            "max_streak_days" to prefs.getInt("max_streak_days", 0),
            "favorite_passages_json" to (prefs.getString("favorite_passages_json", "[]") ?: "[]")
        )
        return createBackupJsonFromData(planStates, progressList, activePlanId, settings)
    }

    /**
     * Parsuje ciąg JSON do postaci BackupRestoreResult (niezależne od Contextu Androida).
     */
    fun parseBackupJsonData(jsonString: String): BackupRestoreResult {
        return try {
            val root = JSONObject(jsonString)

            if (!root.has("planStates") && !root.has("readingProgress") && !root.has("settings")) {
                return BackupRestoreResult(
                    success = false,
                    message = "Nieprawidłowy plik kopii: brak wymaganych sekcji (postęp/ustawienia)"
                )
            }

            // 1. Odczytaj ustawienia
            val restoredSettings = mutableMapOf<String, Any>()
            if (root.has("settings")) {
                val settingsObj = root.getJSONObject("settings")
                if (settingsObj.has("preferred_bible_source")) {
                    restoredSettings["preferred_bible_source"] = settingsObj.getString("preferred_bible_source")
                }
                if (settingsObj.has("app_theme_mode")) {
                    restoredSettings["app_theme_mode"] = settingsObj.getString("app_theme_mode")
                }
                if (settingsObj.has("pref_use_native_reader")) {
                    restoredSettings["pref_use_native_reader"] = settingsObj.getBoolean("pref_use_native_reader")
                }
                if (settingsObj.has("native_reader_font_size")) {
                    restoredSettings["native_reader_font_size"] = settingsObj.getInt("native_reader_font_size")
                }
                if (settingsObj.has("font_zoom_percent")) {
                    restoredSettings["font_zoom_percent"] = settingsObj.getInt("font_zoom_percent")
                }
                if (settingsObj.has("total_reading_time_seconds")) {
                    restoredSettings["total_reading_time_seconds"] = settingsObj.getLong("total_reading_time_seconds")
                }
                if (settingsObj.has("max_streak_days")) {
                    restoredSettings["max_streak_days"] = settingsObj.getInt("max_streak_days")
                }
                if (settingsObj.has("favorite_passages_json")) {
                    restoredSettings["favorite_passages_json"] = settingsObj.getString("favorite_passages_json")
                }
            }

            // 2. Odczytaj stany planów
            val planStates = mutableListOf<UserPlanStateEntity>()
            if (root.has("planStates")) {
                val arr = root.getJSONArray("planStates")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    planStates.add(
                        UserPlanStateEntity(
                            planId = obj.getString("planId"),
                            isActive = obj.optBoolean("isActive", false),
                            startDate = obj.optLong("startDate", System.currentTimeMillis()),
                            currentDay = obj.optInt("currentDay", 1),
                            lastReadDateMillis = obj.optLong("lastReadDateMillis", 0L),
                            streakDays = obj.optInt("streakDays", 0),
                            reminderHour = obj.optInt("reminderHour", 8),
                            reminderMinute = obj.optInt("reminderMinute", 0),
                            reminderEnabled = obj.optBoolean("reminderEnabled", false)
                        )
                    )
                }
            }

            // 3. Odczytaj postęp czytania
            val progressList = mutableListOf<ReadingProgressEntity>()
            if (root.has("readingProgress")) {
                val arr = root.getJSONArray("readingProgress")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    progressList.add(
                        ReadingProgressEntity(
                            planId = obj.getString("planId"),
                            dayNumber = obj.getInt("dayNumber"),
                            bookCode = obj.getString("bookCode"),
                            chapter = obj.getInt("chapter"),
                            isCompleted = obj.optBoolean("isCompleted", true),
                            completedAt = obj.optLong("completedAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            BackupRestoreResult(
                success = true,
                importedProgressCount = progressList.size,
                importedPlansCount = planStates.size,
                restoredSettings = restoredSettings,
                states = planStates,
                progress = progressList,
                message = "Pomyślnie wczytano ${progressList.size} przeczytanych fragmentów i ${planStates.size} planów."
            )
        } catch (e: Exception) {
            BackupRestoreResult(
                success = false,
                message = "Błąd parsowania pliku JSON: ${e.message}"
            )
        }
    }

    /**
     * Odczytuje i parsuje kopię zapasową z ciągu znaków JSON oraz zapisuje preferencje do SharedPreferences.
     */
    fun parseBackupJson(context: Context, jsonString: String): BackupRestoreResult {
        val result = parseBackupJsonData(jsonString)
        if (result.success && result.restoredSettings.isNotEmpty()) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            result.restoredSettings.forEach { (key, value) ->
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                }
            }
            editor.apply()
        }
        return result
    }

    /**
     * Zapisuje ciąg JSON do pliku wskazanego przez Uri (np. z systemowego wybieraka Storage Access Framework - Dysk Google / Pamięć).
     */
    fun writeJsonToUri(context: Context, uri: Uri, jsonString: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Odczytuje ciąg JSON z pliku wskazanego przez Uri (np. wybranego z Dysku Google lub pamięci urządzenia).
     */
    fun readJsonFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Zapisuje kopię do pamięci podręcznej i otwiera systemowe okno udostępniania (Android Sharesheet).
     * Użytkownik może bezpośrednio wybrać "Zapisz na Dysku Google" lub przesłać plik przez Gmail/komunikator.
     */
    fun shareBackupJson(context: Context, jsonString: String) {
        try {
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val filename = "manna365_kopia_$dateStr.json"
            val cacheFile = File(context.cacheDir, filename)
            cacheFile.writeText(jsonString, Charsets.UTF_8)

            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Manna 365 - Kopia zapasowa ($dateStr)")
                putExtra(Intent.EXTRA_TEXT, "Kopia zapasowa postępu czytania i ustawień aplikacji Manna 365.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(sendIntent, "Zapisz na Dysku Google lub udostępnij kopię")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback na zwykły tekst, jeśli udostępnienie pliku napotkało problem
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, jsonString)
                putExtra(Intent.EXTRA_SUBJECT, "Manna 365 - Kopia JSON")
            }
            val chooser = Intent.createChooser(sendIntent, "Zapisz lub wyślij kopię JSON")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }
}
