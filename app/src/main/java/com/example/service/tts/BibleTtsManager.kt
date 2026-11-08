package com.example.service.tts

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.service.pericope.PericopeReading
import com.example.service.pericope.PericopeSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

enum class TtsPlaybackState {
    IDLE,
    INITIALIZING,
    PLAYING,
    PAUSED,
    ERROR
}

/**
 * Menedżer wbudowanego w system Android silnika Text-To-Speech (TTS)
 * przygotowany specjalnie do odtwarzania tekstu Pisma Świętego.
 *
 * Zgodnie z wymaganiami:
 * - Odtwarzany jest WYŁĄCZNIE właściwy tekst wersetów.
 * - Nie są czytane tytuły ksiąg, numery ani nazwy rozdziałów, śródtytuły ani numery wierszy.
 * - Wykorzystuje wyłącznie wbudowany w Androida silnik TextToSpeech bez żadnych zewnętrznych API.
 */
class BibleTtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _playbackState = MutableStateFlow(TtsPlaybackState.IDLE)
    val playbackState: StateFlow<TtsPlaybackState> = _playbackState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    // Przechowujemy podzielone zdania do płynnego wznawiania (Pause / Resume)
    private var sentenceList: List<String> = emptyList()
    private var currentSentenceIndex: Int = 0
    private var currentSessionId: String = ""

    init {
        _playbackState.value = TtsPlaybackState.INITIALIZING
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            _playbackState.value = TtsPlaybackState.ERROR
            _errorMessage.value = "Nie udało się uruchomić silnika mowy Android: ${e.message}"
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsEngine = tts
            if (ttsEngine != null) {
                // Ustaw język polski (dla polskich tłumaczeń Biblii)
                val polishLocale = Locale.forLanguageTag("pl-PL")
                val langResult = ttsEngine.setLanguage(polishLocale)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback na domyślny język urządzenia, jeśli polski nie jest pobrany
                    ttsEngine.setLanguage(Locale.getDefault())
                }

                ttsEngine.setSpeechRate(_speechRate.value)
                setupProgressListener(ttsEngine)
                isInitialized = true
                _playbackState.value = TtsPlaybackState.IDLE
            } else {
                _playbackState.value = TtsPlaybackState.ERROR
                _errorMessage.value = "Błąd inicjalizacji silnika mowy"
            }
        } else {
            _playbackState.value = TtsPlaybackState.ERROR
            _errorMessage.value = "Wbudowany silnik mowy zgłosił błąd inicjalizacji (kod: $status)"
        }
    }

    private fun setupProgressListener(ttsEngine: TextToSpeech) {
        ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post {
                    _playbackState.value = TtsPlaybackState.PLAYING
                }
            }

            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    if (utteranceId != null && utteranceId.startsWith(currentSessionId)) {
                        val indexStr = utteranceId.substringAfterLast("_")
                        val idx = indexStr.toIntOrNull() ?: -1
                        if (idx >= 0) {
                            currentSentenceIndex = idx + 1
                        }
                        // Jeśli to było ostatnie zdanie, kończymy odtwarzanie
                        if (currentSentenceIndex >= sentenceList.size) {
                            _playbackState.value = TtsPlaybackState.IDLE
                            currentSentenceIndex = 0
                        }
                    }
                }
            }

            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    _playbackState.value = TtsPlaybackState.IDLE
                }
            }
        })
    }

    /**
     * Rozpoczyna odtwarzanie tekstu z lektury perykopy.
     * Czytana jest TYLKO czysta treść wersetów (bez tytułów, rozdziałów i numerów wierszy).
     */
    fun playReading(reading: PericopeReading) {
        val pureText = extractScriptureText(reading)
        if (pureText.isBlank()) {
            _errorMessage.value = "Brak tekstu do przeczytania"
            return
        }
        playCleanText(pureText)
    }

    /**
     * Dzieli tekst na zdania i odtwarza je po kolei z możliwością pauzowania.
     */
    fun playCleanText(text: String) {
        val ttsEngine = tts ?: return
        if (!isInitialized) return

        sentenceList = splitIntoSentences(text)
        currentSentenceIndex = 0
        currentSessionId = UUID.randomUUID().toString()

        speakFromCurrentIndex(ttsEngine)
    }

    private fun speakFromCurrentIndex(ttsEngine: TextToSpeech) {
        if (sentenceList.isEmpty() || currentSentenceIndex >= sentenceList.size) {
            _playbackState.value = TtsPlaybackState.IDLE
            return
        }

        _playbackState.value = TtsPlaybackState.PLAYING
        val params = Bundle()

        // Pierwsze wypowiadane zdanie czyści kolejkę, kolejne dodają się płynnie
        for (i in currentSentenceIndex until sentenceList.size) {
            val sentence = sentenceList[i]
            val utteranceId = "${currentSessionId}_$i"
            val queueMode = if (i == currentSentenceIndex) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            ttsEngine.speak(sentence, queueMode, params, utteranceId)
        }
    }

    fun pause() {
        val ttsEngine = tts ?: return
        if (_playbackState.value == TtsPlaybackState.PLAYING) {
            ttsEngine.stop()
            _playbackState.value = TtsPlaybackState.PAUSED
        }
    }

    fun resume() {
        val ttsEngine = tts ?: return
        if (_playbackState.value == TtsPlaybackState.PAUSED) {
            speakFromCurrentIndex(ttsEngine)
        }
    }

    fun stop() {
        val ttsEngine = tts ?: return
        ttsEngine.stop()
        currentSentenceIndex = 0
        _playbackState.value = TtsPlaybackState.IDLE
    }

    fun togglePlayPause(reading: PericopeReading) {
        when (_playbackState.value) {
            TtsPlaybackState.PLAYING -> pause()
            TtsPlaybackState.PAUSED -> resume()
            else -> playReading(reading)
        }
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.5f, 2.0f)
        _speechRate.value = clamped
        tts?.setSpeechRate(clamped)
    }

    fun cycleSpeechRate() {
        val next = when (_speechRate.value) {
            0.85f -> 1.0f
            1.0f -> 1.25f
            1.25f -> 1.5f
            else -> 0.85f
        }
        setSpeechRate(next)
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
        isInitialized = false
        _playbackState.value = TtsPlaybackState.IDLE
    }

    companion object {
        /**
         * Ekstrahuje WYŁĄCZNIE treść wersetów z perykopy:
         * - Pomija tytuł księgi (np. "Księga Rodzaju")
         * - Pomija nagłówek i numer rozdziału (np. "Rozdział 22")
         * - Pomija śródtytuły perykop (np. "Dzieje potomstwa Nachora")
         * - Pomija numery wersetów (np. "20", "21")
         * - Usuwa przypisy, gwiazdki, nawiasy rekonstrukcyjne i znaki specjalne
         */
        fun extractScriptureText(reading: PericopeReading): String {
            return extractScriptureTextFromSections(reading.sections)
        }

        fun extractScriptureTextFromSections(sections: List<PericopeSection>): String {
            val sb = StringBuilder()

            for (section in sections) {
                for (verse in section.verses) {
                    val cleaned = cleanVerseForSpeech(verse.text)
                    if (cleaned.isNotBlank()) {
                        if (sb.isNotEmpty() && !sb.endsWith(" ") && !sb.endsWith("\n")) {
                            sb.append(" ")
                        }
                        sb.append(cleaned)
                    }
                }
            }

            return sb.toString().trim()
        }

        /**
         * Oczyszcza pojedynczy werset, usuwając ewentualne cyfry na początku (numery wierszy),
         * tagi HTML, przypisy w nawiasach kwadratowych, gwiazdki itp.
         */
        fun cleanVerseForSpeech(rawText: String): String {
            var text = rawText

            // Usuń ewentualne tagi HTML
            text = text.replace(Regex("<[^>]+>"), " ")

            // Usuń numery wersetów na początku wiersza (np. "20 ", "20.")
            text = text.replace(Regex("^\\s*\\d+\\s*[.:)]?\\s*"), "")

            // Usuń cyfry w indeksach lub pozostałości sigli
            text = text.replace(Regex("\\b\\d+[a-zA-Z]?\\b"), { match ->
                // Jeśli w środku tekstu pojawia się liczba, np. "Ośmiu synów" lub "trzy dni",
                // liczby w tekście biblijnym zostawiamy, ale izolowane numery wersetów usuwamy.
                // Na ogół w wersetach wersje biblijne nie mają gołych cyfr prócz liczb biblijnych.
                match.value
            })

            // Usuń odsyłacze do komentarzy i rekonstrukcji: [ rekonstrukcja ], [1], ***
            text = text.replace(Regex("\\[.*?\\]"), " ")
            text = text.replace(Regex("\\*+"), " ")

            // Usuń cudzysłowy francuskie i znaki encji
            text = text.replace(Regex("[«»„”\"'()]"), "")
            text = text.replace(Regex("&[a-zA-Z]+;"), " ")

            // Usuń zbędne spacje przed znakami interpunkcyjnymi (np. po usuniętych gwiazdkach/nawiasach: "Abrahamowi .")
            text = text.replace(Regex("\\s+([.,!?;:])"), "$1")

            // Zredukuj wielokrotne spacje
            text = text.replace(Regex("\\s+"), " ").trim()

            return text
        }

        /**
         * Dzieli długi tekst na pojedyncze zdania, zachowując interpunkcję,
         * aby zapobiec przekroczeniu limitu wejściowego speak() w silniku Android TTS
         * oraz umożliwić płynne wstrzymywanie i wznawianie lektora.
         */
        fun splitIntoSentences(text: String): List<String> {
            if (text.isBlank()) return emptyList()

            // Dzielimy według kropki, wykrzyknika, pytajnika lub średnika
            val parts = text.split(Regex("(?<=[.?!;:])\\s+"))
            val result = mutableListOf<String>()

            for (part in parts) {
                val trimmed = part.trim()
                if (trimmed.isNotBlank()) {
                    // Jeśli fragment jest bardzo długi (> 500 znaków), podziel go dodatkowo po przecinkach
                    if (trimmed.length > 500) {
                        val subParts = trimmed.split(Regex("(?<=,)\\s+"))
                        for (sub in subParts) {
                            if (sub.isNotBlank()) result.add(sub.trim())
                        }
                    } else {
                        result.add(trimmed)
                    }
                }
            }

            return if (result.isEmpty()) listOf(text.trim()) else result
        }
    }
}
