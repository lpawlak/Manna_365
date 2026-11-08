package com.example.model

import com.example.data.entity.ReadingProgressEntity
import com.example.data.entity.UserPlanStateEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

data class TimeSlotStat(
    val slotName: String,
    val timeRange: String,
    val count: Int,
    val percentage: Int,
    val iconEmoji: String
)

data class ReadingStats(
    val totalReadingSeconds: Long = 0L,
    val formattedReadingTime: String = "0 min",
    val currentStreakDays: Int = 0,
    val maxStreakDays: Int = 0,
    val streakStatusMessage: String = "Rozpocznij serię czytania",
    val isStreakActiveToday: Boolean = false,
    val favoriteTimeOfDay: String = "Brak danych",
    val favoriteTimeOfDayDetail: String = "Odznacz przeczytane fragmenty",
    val estimatedCompletionDate: String = "W trakcie obliczania",
    val estimatedDaysRemaining: Int = 0,
    val paceDescription: String = "Standardowe tempo (1 dzień/dobę)",
    val isPlanCompleted: Boolean = false,
    val timeSlotBreakdown: List<TimeSlotStat> = emptyList(),
    val averageSecondsPerReading: Long = 0L
)

object ReadingStatsCalculator {

    fun formatReadingTime(totalSeconds: Long): String {
        if (totalSeconds <= 0L) return "0 min"
        val minutes = totalSeconds / 60
        val hours = minutes / 60
        val remMinutes = minutes % 60
        return when {
            hours > 0 && remMinutes > 0 -> "$hours godz. $remMinutes min"
            hours > 0 -> "$hours godz."
            minutes > 0 -> "$minutes min"
            else -> "< 1 min"
        }
    }

    fun calculateStreak(
        lastReadMillis: Long,
        recordedStreak: Int,
        storedMaxStreak: Int
    ): Triple<Int, Int, String> {
        if (lastReadMillis == 0L || recordedStreak <= 0) {
            val maxS = storedMaxStreak.coerceAtLeast(0)
            val msg = if (maxS > 0) "Twój rekord to $maxS dni. Rozpocznij nową serię!" else "Przeczytaj dzisiejszy fragment, aby rozpocząć serię!"
            return Triple(0, maxS, msg)
        }

        val now = Calendar.getInstance()
        val last = Calendar.getInstance().apply { timeInMillis = lastReadMillis }

        val isSameDay = now.get(Calendar.YEAR) == last.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == last.get(Calendar.DAY_OF_YEAR)

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == last.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == last.get(Calendar.DAY_OF_YEAR)

        val currentStreak = when {
            isSameDay -> recordedStreak.coerceAtLeast(1)
            isYesterday -> recordedStreak.coerceAtLeast(1)
            else -> 0
        }

        val maxStreak = maxOf(currentStreak, storedMaxStreak)

        val message = when {
            isSameDay -> "Świetna passa! Dziś lektura już zaliczona 🔥"
            isYesterday -> "Przeczytaj dzisiejszy fragment, by utrzymać serię! ⏳"
            else -> "Najdłuższa seria do tej pory: $maxStreak dni. Czas na nowy rekord!"
        }

        return Triple(currentStreak, maxStreak, message)
    }

    fun calculateFavoriteTime(
        readings: List<ReadingProgressEntity>
    ): Triple<String, String, List<TimeSlotStat>> {
        if (readings.isEmpty()) {
            val emptySlots = listOf(
                TimeSlotStat("Poranek", "05:00 – 11:59", 0, 0, "🌅"),
                TimeSlotStat("Popołudnie", "12:00 – 17:59", 0, 0, "☀️"),
                TimeSlotStat("Wieczór", "18:00 – 22:59", 0, 0, "🌇"),
                TimeSlotStat("Noc", "23:00 – 04:59", 0, 0, "🌙")
            )
            return Triple("Brak danych", "Odznacz fragmenty po przeczytaniu", emptySlots)
        }

        val cal = Calendar.getInstance()
        var morning = 0
        var afternoon = 0
        var evening = 0
        var night = 0
        val hourCounts = IntArray(24)

        for (r in readings) {
            if (r.completedAt > 0L) {
                cal.timeInMillis = r.completedAt
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                hourCounts[hour]++
                when (hour) {
                    in 5..11 -> morning++
                    in 12..17 -> afternoon++
                    in 18..22 -> evening++
                    else -> night++
                }
            }
        }

        val total = (morning + afternoon + evening + night).coerceAtLeast(1)

        val slots = listOf(
            TimeSlotStat("Poranek", "05:00 – 11:59", morning, (morning * 100) / total, "🌅"),
            TimeSlotStat("Popołudnie", "12:00 – 17:59", afternoon, (afternoon * 100) / total, "☀️"),
            TimeSlotStat("Wieczór", "18:00 – 22:59", evening, (evening * 100) / total, "🌇"),
            TimeSlotStat("Noc", "23:00 – 04:59", night, (night * 100) / total, "🌙")
        )

        val best = slots.maxByOrNull { it.count }
        if (best == null || best.count == 0) {
            return Triple("Brak danych", "Odznacz fragmenty po przeczytaniu", slots)
        }

        val mostFrequentHour = hourCounts.indices.maxByOrNull { hourCounts[it] } ?: 8
        val hourStr = String.format(Locale.getDefault(), "%02d:00", mostFrequentHour)

        val detail = "Najczęściej ok. godz. $hourStr (${best.count} z $total czytań)"
        val title = "${best.iconEmoji} ${best.slotName} (${best.percentage}%)"

        return Triple(title, detail, slots)
    }

    fun calculateEstimatedCompletion(
        activePlan: PlanDefinition,
        userState: UserPlanStateEntity,
        readings: List<ReadingProgressEntity>
    ): Triple<String, Int, String> {
        val totalDays = activePlan.totalDays
        val currentDay = userState.currentDay.coerceIn(1, totalDays)
        val remainingPlanDays = (totalDays - currentDay).coerceAtLeast(0)

        if (remainingPlanDays == 0) {
            return Triple("Plan ukończony! 🎉", 0, "Wszystkie dni planu zostały przeczytane")
        }

        val planReadings = readings.filter { it.planId == activePlan.id }
        val distinctCompletedDays = planReadings.map { it.dayNumber }.distinct().size.coerceAtLeast(1)

        val now = System.currentTimeMillis()
        val elapsedDays = ((now - userState.startDate) / (1000 * 60 * 60 * 24L)).coerceAtLeast(1L)

        // Tempo czytania (dni planu / dzień kalendarzowy)
        val rawPace = distinctCompletedDays.toDouble() / elapsedDays.toDouble()
        val pace = rawPace.coerceIn(0.2, 4.0)

        val remainingCalendarDays = (remainingPlanDays / pace).roundToLong().coerceAtLeast(1L)

        val targetCal = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, remainingCalendarDays.toInt())
        }

        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("pl-PL"))
        val dateString = dateFormat.format(targetCal.time)

        val paceFormatted = String.format(Locale.US, "%.1f", pace)
        val subtitle = "za ok. $remainingCalendarDays dni (tempo: ~$paceFormatted dnia/dobę)"

        return Triple(dateString, remainingCalendarDays.toInt(), subtitle)
    }

    fun computeStats(
        totalReadingSeconds: Long,
        activePlan: PlanDefinition,
        userState: UserPlanStateEntity,
        readings: List<ReadingProgressEntity>,
        storedMaxStreak: Int
    ): ReadingStats {
        val (streak, maxStreak, streakMsg) = calculateStreak(
            userState.lastReadDateMillis,
            userState.streakDays,
            storedMaxStreak
        )

        val (favTitle, favDetail, timeSlots) = calculateFavoriteTime(readings)

        val (estDate, estDays, paceMsg) = calculateEstimatedCompletion(
            activePlan,
            userState,
            readings
        )

        val totalPassages = readings.size
        val avgSec = if (totalPassages > 0) totalReadingSeconds / totalPassages else 0L

        val isTodayActive = userState.lastReadDateMillis > 0L && run {
            val now = Calendar.getInstance()
            val last = Calendar.getInstance().apply { timeInMillis = userState.lastReadDateMillis }
            now.get(Calendar.YEAR) == last.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == last.get(Calendar.DAY_OF_YEAR)
        }

        return ReadingStats(
            totalReadingSeconds = totalReadingSeconds,
            formattedReadingTime = formatReadingTime(totalReadingSeconds),
            currentStreakDays = streak,
            maxStreakDays = maxStreak,
            streakStatusMessage = streakMsg,
            isStreakActiveToday = isTodayActive,
            favoriteTimeOfDay = favTitle,
            favoriteTimeOfDayDetail = favDetail,
            estimatedCompletionDate = estDate,
            estimatedDaysRemaining = estDays,
            paceDescription = paceMsg,
            isPlanCompleted = (activePlan.totalDays - userState.currentDay) <= 0,
            timeSlotBreakdown = timeSlots,
            averageSecondsPerReading = avgSec
        )
    }
}

data class FavoritePassage(
    val bookCode: String,
    val bookName: String,
    val chapter: Int,
    val passageLabel: String,
    val addedAt: Long = System.currentTimeMillis()
) {
    val key: String get() = "$bookCode-$chapter-$passageLabel"
}

object FavoritePassageStorage {
    fun serializeList(list: List<FavoritePassage>): String {
        val array = org.json.JSONArray()
        for (item in list) {
            val obj = org.json.JSONObject()
            obj.put("bookCode", item.bookCode)
            obj.put("bookName", item.bookName)
            obj.put("chapter", item.chapter)
            obj.put("passageLabel", item.passageLabel)
            obj.put("addedAt", item.addedAt)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeList(json: String?): List<FavoritePassage> {
        if (json.isNullOrBlank()) return emptyList()
        val result = mutableListOf<FavoritePassage>()
        return try {
            val array = org.json.JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    FavoritePassage(
                        bookCode = obj.optString("bookCode", ""),
                        bookName = obj.optString("bookName", ""),
                        chapter = obj.optInt("chapter", 1),
                        passageLabel = obj.optString("passageLabel", ""),
                        addedAt = obj.optLong("addedAt", System.currentTimeMillis())
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }
}

