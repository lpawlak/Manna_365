package com.example.model

enum class FeedbackCategory(val title: String, val description: String) {
    BUG("Zgłoszenie błędu", "Coś nie działa poprawnie"),
    SUGGESTION("Propozycja funkcji", "Pomysł na ulepszenie aplikacji"),
    QUESTION("Pytanie do autora", "Pytanie dotyczące aplikacji lub tekstów"),
    OTHER("Inne", "Pozostałe wiadomości")
}

data class DeviceDiagnosticInfo(
    val osVersion: String = "",
    val sdkInt: Int = 0,
    val manufacturer: String = "",
    val model: String = "",
    val device: String = "",
    val appVersion: String = "",
    val appVersionCode: Int = 0,
    val networkType: String = "",
    val screenDensity: String = "",
    val preferredBibleSource: String = ""
)

data class FeedbackSubmission(
    val category: FeedbackCategory,
    val message: String,
    val contactEmail: String? = null,
    val deviceInfo: DeviceDiagnosticInfo
)
