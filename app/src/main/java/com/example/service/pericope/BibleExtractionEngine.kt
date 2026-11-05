package com.example.service.pericope

import android.content.Context
import com.example.model.BibleData
import com.example.model.BibleSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap

object BibleExtractionEngine {

    private val memoryCache = ConcurrentHashMap<String, String>()

    /**
     * Główna funkcja pobierająca i filtrująca wersety dla zadanego fragmentu.
     */
    suspend fun getPericopeReading(
        context: Context,
        bookCode: String,
        defaultChapter: Int,
        label: String,
        source: BibleSource
    ): PericopeReading = withContext(Dispatchers.IO) {
        val book = BibleData.getBook(bookCode)
        val bookName = book?.name ?: bookCode

        // Parsuj siglum na zakresy rozdziałów i wersetów
        val ranges = PericopeRangeParser.parse(bookCode, defaultChapter, label)

        // Równolegle pobierz i wyekstrahuj sekcje dla każdego wymaganego rozdziału
        val sections = coroutineScope {
            ranges.map { range ->
                async {
                    extractChapterSection(context, range, bookName, source)
                }
            }.awaitAll()
        }

        val sourceTitle = when (source) {
            BibleSource.PISMOSWIETE -> "Pismo Święte (Edycja Św. Pawła)"
            BibleSource.DEON -> "Biblia Tysiąclecia (Pallottinum)"
        }

        PericopeReading(
            bookCode = bookCode,
            bookName = bookName,
            passageLabel = label,
            sourceName = sourceTitle,
            sections = sections
        )
    }

    private suspend fun extractChapterSection(
        context: Context,
        range: ChapterRange,
        bookName: String,
        preferredSource: BibleSource
    ): PericopeSection {
        var rawHtml: String? = null
        var isDeon = false

        if (preferredSource == BibleSource.PISMOSWIETE) {
            val pismoJson = fetchChapterPismoswiete(context, range.bookCode, range.chapter)
            if (pismoJson != null && !pismoJson.contains("Treść dostępna tylko dla osób zalogowanych", ignoreCase = true)) {
                rawHtml = extractPismoswieteContent(pismoJson)
            }
        }

        // Jeśli wybrano DEON lub pismoswiete było niedostępne / zablokowane
        if (rawHtml == null) {
            rawHtml = fetchChapterDeon(context, range.bookCode, range.chapter)
            isDeon = true
        }

        if (rawHtml.isNullOrBlank()) {
            return PericopeSection(
                bookCode = range.bookCode,
                bookName = bookName,
                chapter = range.chapter,
                sectionTitle = "Rozdział ${range.chapter}",
                verses = listOf(ExtractedVerse(range.startVerse, "Nie udało się załadować tekstu. Sprawdź połączenie z internetem."))
            )
        }

        return if (isDeon) {
            parseDeonVerses(rawHtml, range, bookName)
        } else {
            parsePismoswieteVerses(rawHtml, range, bookName)
        }
    }

    // =========================================================================
    // PARSER BIBLIA.DEON.PL (Biblia Tysiąclecia)
    // =========================================================================

    private fun parseDeonVerses(html: String, range: ChapterRange, bookName: String): PericopeSection {
        val contentRegex = Regex("<div class=[\"'](?:tresc|the-content)[\"']>(.*?)</div>\\s*<!-- END the-content -->", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        val contentMatch = contentRegex.find(html)
        val content = contentMatch?.groupValues?.get(1) ?: html

        // Podział wersetów poznaczonych znacznikami <a name="W{numer}"></a>
        val parts = content.split(Regex("<a name=[\"']W(\\d+)[\"']></a>"))

        val verses = mutableListOf<ExtractedVerse>()
        var chapterTitle: String? = null

        // Perykopa / śródtytuł przed wersetem 1
        if (parts.isNotEmpty() && range.startVerse <= 1) {
            val mTitle = Regex("<div class=[\"']?miedzytytul\\d*[\"']?>(.*?)</div>", RegexOption.IGNORE_CASE).find(parts[0])
            if (mTitle != null) {
                chapterTitle = cleanHtml(mTitle.groupValues[1])
            }
        }

        // parts[0] = nagłówek, od 1 co 2 występuje numer wersetu (jeśli split z dopasowaniem)
        // W standardowym split numer wersetu musimy dopasować ze znaczników
        val verseMarkers = Regex("<a name=[\"']W(\\d+)[\"']></a>").findAll(content).toList()
        for (idx in verseMarkers.indices) {
            val vNum = verseMarkers[idx].groupValues[1].toIntOrNull() ?: (idx + 1)
            if (vNum in range.startVerse..range.endVerse) {
                val chunk = if (idx + 1 < parts.size) parts[idx + 1] else ""

                // Sprawdź śródtytuł wewnątrz wersetu
                val mTitle = Regex("<div class=[\"']?miedzytytul\\d*[\"']?>(.*?)</div>", RegexOption.IGNORE_CASE).find(chunk)
                if (mTitle != null && chapterTitle == null) {
                    chapterTitle = cleanHtml(mTitle.groupValues[1])
                }

                var cleanText = chunk
                cleanText = cleanText.replace(Regex("<div class=[\"']?miedzytytul\\d*[\"']?>.*?</div>", RegexOption.IGNORE_CASE), "")
                cleanText = cleanText.replace(Regex("<sup>.*?</sup>", RegexOption.IGNORE_CASE), "")
                cleanText = cleanText.replace(Regex("<span class=[\"']werset[\"']>.*?</span>", RegexOption.IGNORE_CASE), "")
                cleanText = cleanHtml(cleanText)

                // Usuń ewentualne strzałki nawigacyjne «« Rdz X »» na końcu rozdziału
                cleanText = cleanText.replace(Regex("[«»]+.*[«»]+"), "").trim()

                if (cleanText.isNotBlank()) {
                    verses.add(ExtractedVerse(vNum, cleanText))
                }
            }
        }

        return PericopeSection(
            bookCode = range.bookCode,
            bookName = bookName,
            chapter = range.chapter,
            sectionTitle = chapterTitle,
            verses = verses
        )
    }

    // =========================================================================
    // PARSER PISMOSWIETE.PL (Edycja Świętego Pawła)
    // =========================================================================

    private fun extractPismoswieteContent(jsonString: String): String? {
        return try {
            val root = JSONObject(jsonString)
            val data = root.optJSONObject("data") ?: return null
            val chapterObj = data.optJSONObject("chapter") ?: return null
            chapterObj.optString("content", null)
        } catch (e: Exception) {
            null
        }
    }

    private fun parsePismoswieteVerses(content: String, range: ChapterRange, bookName: String): PericopeSection {
        val markers = Regex("<span class=[\"']sigl sigl-start[\"'] data-sigl=[\"'](\\d+)[\"']></span>").findAll(content).toList()
        val parts = content.split(Regex("<span class=[\"']sigl sigl-start[\"'] data-sigl=[\"']\\d+[\"']></span>"))

        val verses = mutableListOf<ExtractedVerse>()
        var chapterTitle: String? = null

        // Tytuł perykopy przed wersetem 1
        if (parts.isNotEmpty() && range.startVerse <= 1) {
            val mTitle = Regex("<div class=[\"']perykopa[\"']>(.*?)</div>", RegexOption.IGNORE_CASE).find(parts[0])
            if (mTitle != null) {
                chapterTitle = cleanHtml(mTitle.groupValues[1])
            }
        }

        for (idx in markers.indices) {
            val vNum = markers[idx].groupValues[1].toIntOrNull() ?: (idx + 1)
            if (vNum in range.startVerse..range.endVerse) {
                val chunk = if (idx + 1 < parts.size) parts[idx + 1] else ""

                // Sprawdź śródtytuł
                val mTitle = Regex("<div class=[\"']perykopa[\"']>(.*?)</div>", RegexOption.IGNORE_CASE).find(chunk)
                if (mTitle != null && chapterTitle == null) {
                    chapterTitle = cleanHtml(mTitle.groupValues[1])
                }

                var cleanText = chunk
                cleanText = cleanText.replace(Regex("<div class=[\"']perykopa[\"']>.*?</div>", RegexOption.IGNORE_CASE), "")
                cleanText = cleanText.replace(Regex("<a class=[\"']link[^>]*>.*?</a>", RegexOption.IGNORE_CASE), "")
                cleanText = cleanText.replace(Regex("<sup class=[\"']werset-number[\"']>.*?</sup>", RegexOption.IGNORE_CASE), "")
                cleanText = cleanHtml(cleanText)

                // Usuń ewentualne cyfry wiodące
                cleanText = cleanText.replace(Regex("^\\d+\\s*"), "").trim()

                if (cleanText.isNotBlank()) {
                    verses.add(ExtractedVerse(vNum, cleanText))
                }
            }
        }

        return PericopeSection(
            bookCode = range.bookCode,
            bookName = bookName,
            chapter = range.chapter,
            sectionTitle = chapterTitle,
            verses = verses
        )
    }

    // =========================================================================
    // POBIERANIE SIECIOWE & CACHE DYSKOWY
    // =========================================================================

    private fun fetchChapterDeon(context: Context, bookCode: String, chapter: Int): String? {
        val cacheKey = "deon_${bookCode}_$chapter"
        memoryCache[cacheKey]?.let { return it }

        val diskFile = getCacheFile(context, cacheKey)
        if (diskFile.exists() && diskFile.length() > 200) {
            val cached = diskFile.readText(Charset.forName("ISO-8859-2"))
            memoryCache[cacheKey] = cached
            return cached
        }

        return try {
            val urlString = "https://biblia.deon.pl/otworz.php?skrot=$bookCode+$chapter"
            val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Manna 365)")
            }

            if (conn.responseCode == 200) {
                val text = InputStreamReader(conn.inputStream, Charset.forName("ISO-8859-2")).readText()
                if (text.length > 200) {
                    memoryCache[cacheKey] = text
                    diskFile.writeText(text, Charset.forName("ISO-8859-2"))
                    return text
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchChapterPismoswiete(context: Context, bookCode: String, chapter: Int): String? {
        val cacheKey = "ps_${bookCode}_$chapter"
        memoryCache[cacheKey]?.let { return it }

        val diskFile = getCacheFile(context, cacheKey)
        if (diskFile.exists() && diskFile.length() > 100) {
            val cached = diskFile.readText(Charsets.UTF_8)
            memoryCache[cacheKey] = cached
            return cached
        }

        return try {
            val urlString = "https://pismoswiete.pl/api/tome/$bookCode/chapter/$chapter"
            val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Manna 365)")
                setRequestProperty("Accept", "application/json")
            }

            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
                if (text.length > 100) {
                    memoryCache[cacheKey] = text
                    diskFile.writeText(text, Charsets.UTF_8)
                    return text
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun getCacheFile(context: Context, key: String): File {
        val dir = File(context.cacheDir, "bible_cache")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "$key.txt")
    }

    private fun cleanHtml(text: String): String {
        return text
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&quot;", "\"")
            .replace("&#8222;", "„")
            .replace("&#8221;", "”")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
