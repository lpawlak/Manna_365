package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.ReadingProgressEntity
import com.example.data.entity.UserPlanStateEntity
import com.example.data.repository.ReadingPlanRepository
import com.example.model.BibleData
import com.example.model.BibleSource
import com.example.model.DayPlan
import com.example.model.PlanDefinition
import com.example.model.ReadingPassage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab {
    TODAY, PLANS, PROGRESS, SETTINGS
}

enum class AppThemeMode(val id: String, val label: String) {
    SYSTEM("system", "Zgodny z systemem"),
    LIGHT("light", "Jasny"),
    DARK("dark", "Ciemny");

    companion object {
        fun fromId(id: String?): AppThemeMode {
            return entries.firstOrNull { it.id == id } ?: SYSTEM
        }
    }
}

data class ReaderSession(
    val bookCode: String,
    val chapter: Int,
    val bookName: String,
    val dayNumber: Int,
    val isCompleted: Boolean,
    val hasNext: Boolean,
    val hasPrev: Boolean,
    val source: BibleSource = BibleSource.PISMOSWIETE,
    val passageLabel: String = "$bookName, rozdz. $chapter",
    val useNativeReader: Boolean = true
)

data class UiState(
    val currentTab: AppNavTab = AppNavTab.TODAY,
    val activePlan: PlanDefinition = BibleData.availablePlans[0],
    val userState: UserPlanStateEntity = UserPlanStateEntity(planId = "plan_bible_365"),
    val displayedDay: Int = 1,
    val dayPlan: DayPlan = BibleData.generateDayPlan("plan_bible_365", 1),
    val completedKeys: Set<String> = emptySet(),
    val totalPlanCompletedCount: Int = 0,
    val totalPlanPassagesCount: Int = 365 * 4,
    val planProgressRatio: Float = 0f,
    val readerSession: ReaderSession? = null,
    val allCompletedReadings: List<ReadingProgressEntity> = emptyList(),
    val selectedBibleSource: BibleSource = BibleSource.PISMOSWIETE,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReadingPlanRepository
    private val sharedPrefs = application.getSharedPreferences("reader_settings", Context.MODE_PRIVATE)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReadingPlanRepository(database.readingDao())
    }

    val availablePlans: List<PlanDefinition> = repository.availablePlans

    private val _currentTab = MutableStateFlow(AppNavTab.TODAY)
    private val _selectedDay = MutableStateFlow<Int?>(null)
    private val _readerSession = MutableStateFlow<ReaderSession?>(null)
    private val _selectedBibleSource = MutableStateFlow(
        BibleSource.fromId(sharedPrefs.getString("preferred_bible_source", BibleSource.PISMOSWIETE.id))
    )
    private val _themeMode = MutableStateFlow(
        AppThemeMode.fromId(sharedPrefs.getString("app_theme_mode", AppThemeMode.SYSTEM.id))
    )

    val activePlanState: StateFlow<UserPlanStateEntity> = repository.activePlanState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            UserPlanStateEntity(planId = "plan_bible_365")
        )

    private val completedForActivePlan = activePlanState.flatMapLatest { state ->
        repository.getCompletedPassagesForPlan(state.planId)
    }

    private data class PlanData(
        val userState: UserPlanStateEntity,
        val completedList: List<ReadingProgressEntity>,
        val allCompleted: List<ReadingProgressEntity>
    )

    private val planDataFlow = combine(
        activePlanState,
        completedForActivePlan,
        repository.getAllCompletedPassages()
    ) { userState, completedList, allCompleted ->
        PlanData(userState, completedList, allCompleted)
    }

    private data class ReaderPreferences(
        val bibleSource: BibleSource,
        val themeMode: AppThemeMode
    )

    private val readerPrefsFlow = combine(_selectedBibleSource, _themeMode) { source, mode ->
        ReaderPreferences(source, mode)
    }

    val uiState: StateFlow<UiState> = combine(
        _currentTab,
        _selectedDay,
        _readerSession,
        readerPrefsFlow,
        planDataFlow
    ) { tab, selectedDay, readerSession, readerPrefs, planData ->
        val bibleSource = readerPrefs.bibleSource
        val themeMode = readerPrefs.themeMode
        val userState = planData.userState
        val completedList = planData.completedList
        val allCompleted = planData.allCompleted

        val plan = availablePlans.firstOrNull { it.id == userState.planId } ?: availablePlans[0]
        val dayToShow = (selectedDay ?: userState.currentDay).coerceIn(1, plan.totalDays)
        val dayPlan = repository.getDayPlan(plan.id, dayToShow)

        val completedKeys = completedList.map { "${it.dayNumber}-${it.bookCode}-${it.chapter}" }.toSet()

        // Total passages estimate
        val sampleDay = repository.getDayPlan(plan.id, 1)
        val passagesPerDay = sampleDay.passages.size
        val totalExpected = plan.totalDays * passagesPerDay
        val progressRatio = if (totalExpected > 0) {
            (completedList.size.toFloat() / totalExpected).coerceIn(0f, 1f)
        } else 0f

        // If reader session is open, update its completion state and source
        val updatedReader = readerSession?.let { current ->
            val key = "${current.dayNumber}-${current.bookCode}-${current.chapter}"
            current.copy(
                isCompleted = completedKeys.contains(key),
                source = bibleSource
            )
        }

        UiState(
            currentTab = tab,
            activePlan = plan,
            userState = userState,
            displayedDay = dayToShow,
            dayPlan = dayPlan,
            completedKeys = completedKeys,
            totalPlanCompletedCount = completedList.size,
            totalPlanPassagesCount = totalExpected,
            planProgressRatio = progressRatio,
            readerSession = updatedReader,
            allCompletedReadings = allCompleted,
            selectedBibleSource = bibleSource,
            themeMode = themeMode
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UiState()
    )

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun setDisplayedDay(day: Int) {
        val maxDays = uiState.value.activePlan.totalDays
        _selectedDay.value = day.coerceIn(1, maxDays)
    }

    fun previousDay() {
        val current = uiState.value.displayedDay
        if (current > 1) {
            setDisplayedDay(current - 1)
        }
    }

    fun nextDay() {
        val current = uiState.value.displayedDay
        val maxDays = uiState.value.activePlan.totalDays
        if (current < maxDays) {
            setDisplayedDay(current + 1)
        }
    }

    fun jumpToToday() {
        _selectedDay.value = uiState.value.userState.currentDay
    }

    fun togglePassage(passage: ReadingPassage, dayNumber: Int) {
        viewModelScope.launch {
            val key = "$dayNumber-${passage.bookCode}-${passage.chapter}"
            val isCompleted = uiState.value.completedKeys.contains(key)
            val planId = uiState.value.activePlan.id
            repository.togglePassageCompletion(planId, dayNumber, passage, isCompleted)
        }
    }

    fun markDayCompleted() {
        viewModelScope.launch {
            val planId = uiState.value.activePlan.id
            val currentDayPlan = uiState.value.dayPlan
            repository.markDayCompleted(planId, currentDayPlan)
            // advance displayed day
            val next = (currentDayPlan.dayNumber + 1).coerceAtMost(uiState.value.activePlan.totalDays)
            _selectedDay.value = next
        }
    }

    fun openReader(passage: ReadingPassage, dayNumber: Int) {
        val dayPlan = repository.getDayPlan(uiState.value.activePlan.id, dayNumber)
        val index = dayPlan.passages.indexOfFirst { it.bookCode == passage.bookCode && it.chapter == passage.chapter }
        val key = "$dayNumber-${passage.bookCode}-${passage.chapter}"
        val useNative = sharedPrefs.getBoolean("pref_use_native_reader", true)

        _readerSession.value = ReaderSession(
            bookCode = passage.bookCode,
            chapter = passage.chapter,
            bookName = passage.bookName,
            dayNumber = dayNumber,
            isCompleted = uiState.value.completedKeys.contains(key),
            hasNext = index >= 0 && index < dayPlan.passages.size - 1,
            hasPrev = index > 0,
            source = _selectedBibleSource.value,
            passageLabel = passage.passageLabel,
            useNativeReader = useNative
        )
    }

    fun setReaderMode(native: Boolean) {
        sharedPrefs.edit().putBoolean("pref_use_native_reader", native).apply()
        _readerSession.value = _readerSession.value?.copy(useNativeReader = native)
    }

    fun setBibleSource(source: BibleSource) {
        _selectedBibleSource.value = source
        sharedPrefs.edit().putString("preferred_bible_source", source.id).apply()
        _readerSession.value = _readerSession.value?.copy(source = source)
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        sharedPrefs.edit().putString("app_theme_mode", mode.id).apply()
    }

    fun toggleThemeMode() {
        val next = when (_themeMode.value) {
            AppThemeMode.DARK -> AppThemeMode.LIGHT
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.SYSTEM -> AppThemeMode.DARK
        }
        setThemeMode(next)
    }

    fun closeReader() {
        _readerSession.value = null
    }

    fun nextChapterInReader() {
        val current = _readerSession.value ?: return
        val dayPlan = repository.getDayPlan(uiState.value.activePlan.id, current.dayNumber)
        val index = dayPlan.passages.indexOfFirst { it.bookCode == current.bookCode && it.chapter == current.chapter }
        if (index >= 0 && index < dayPlan.passages.size - 1) {
            val nextPassage = dayPlan.passages[index + 1]
            openReader(nextPassage, current.dayNumber)
        }
    }

    fun prevChapterInReader() {
        val current = _readerSession.value ?: return
        val dayPlan = repository.getDayPlan(uiState.value.activePlan.id, current.dayNumber)
        val index = dayPlan.passages.indexOfFirst { it.bookCode == current.bookCode && it.chapter == current.chapter }
        if (index > 0) {
            val prevPassage = dayPlan.passages[index - 1]
            openReader(prevPassage, current.dayNumber)
        }
    }

    fun toggleCurrentReaderPassage() {
        val current = _readerSession.value ?: return
        val passage = ReadingPassage(
            bookCode = current.bookCode,
            bookName = current.bookName,
            chapter = current.chapter
        )
        togglePassage(passage, current.dayNumber)
    }

    fun selectPlan(planId: String) {
        viewModelScope.launch {
            repository.selectPlan(planId)
            _selectedDay.value = 1
            _currentTab.value = AppNavTab.TODAY
        }
    }

    fun updateReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.updateReminderSettings(enabled, hour, minute)
        }
    }

    fun resetPlan() {
        viewModelScope.launch {
            val planId = uiState.value.activePlan.id
            repository.resetPlan(planId)
            _selectedDay.value = 1
        }
    }
}
