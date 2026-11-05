# Inteligentny Silnik Ekstrakcji Perykop i Natywny Czytnik Pisma Świętego

Precyzyjne pobieranie, filtrowanie i prezentowanie pociętych fragmentów biblijnych (np. Rdz 1,1 – 2,4a lub Rdz 12 – 14) w dedykowanym, natywnym czytniku Jetpack Compose jako jeden spójny, ciągły tekst z nagłówkami rozdziałów.

### User Review & Critical Decisions

> [!IMPORTANT] 
> Użytkownik potwierdził preferencję natywnego czytnika Compose wyświetlającego wyłącznie zadany zakres wersetów (zamiast całej strony z rozdziałem) oraz łączenia wielorozdziałowych fragmentów w jeden ciągły tekst z nagłówkami rozdziałów.

- **Potwierdzona decyzja 1 (Format prezentacji):** Natywny czytnik Jetpack Compose filtrujący i wyświetlający wyłącznie zadane wersety (od wersetu początkowego do końcowego), eliminujący szum informacyjny i zbędne fragmenty spoza planu dnia.
- **Potwierdzona decyzja 2 (Wielorozdziałowe fragmenty):** Połączony, jeden płynny ciąg tekstu z estetycznymi separatorami i nagłówkami kolejnych rozdziałów (np. Rdz 12, Rdz 13, Rdz 14 w jednym widoku).
- **Rekomendowane źródło ekstrakcji (Pismoswiete.pl REST API):** Bezpośrednie odpytywanie oficjalnego endpointu JSON (`https://pismoswiete.pl/api/tome/{tome}/chapter/{chapter}`), co gwarantuje 10-krotnie szybsze ładowanie niż renderowanie WebView, zerowe ryzyko problemów z CSS/JS strony i pełną kontrolę nad typografią.
- **Dostępność trybu WebView:** Zachowanie przycisku "Otwórz pełny rozdział w WebView / przeglądarce" dla użytkowników chcących w dowolnej chwili podejrzeć cały kontekst rozdziału.

---

### 1. Overview & Core Concept

Dotychczasowy czytnik oparty na komponencie `WebView` otwierał wyłącznie pojedyncze, całe rozdziały (np. cały Rozdział 1 Księgi Rodzaju). W planach takich jak Plan Jacka Święckiego (Mateusz.pl), gdzie czytania są precyzyjnie pocięte według perykop liturgiczno-historycznych (np. Dzień 1: `Rdz 1,1 – 2,4a`; Dzień 6: `Rdz 23:; 25,1–28; 26,1–33`), użytkownik musiał ręcznie domyślać się, gdzie skończyć czytanie oraz przełączać się do kolejnego rozdziału.

Nowy silnik **PericopeEngine**:
1. Rozpoznaje i parsuje dowolną notację siglów perykop:
   - Zakresy wersetowe w jednym rozdziale (np. `Rdz 4,1 – 6,2`).
   - Zakresy przekraczające granicę rozdziału (np. `Rdz 1,1 – 2,4a` -> łączy Rdz 1:1-31 oraz Rdz 2:1-4).
   - Całe serie rozdziałów (np. `Rdz 12: – 14:` -> pobiera i łączy rozdziały 12, 13 i 14).
   - Fragmenty nieciągłe i łączone średnikami (np. `Rdz 22,20–24; 24:`).
2. Pobiera dane wprost z API `pismoswiete.pl`, odrzuca przypisy i komentarze, po czym wycina wersety o numerach ściśle odpowiadających zadanemu fragmentowi.
3. Prezentuje tekst w natywnym komponencie Compose z regulacją czcionki, obsługą motywu nocnego/dziennego oraz zapisem stanu przeczytania.

---

### 2. User Experience & Visual Design

#### Ścieżka Użytkownika (User Flow)
1. **Ekran Główny / Lista Planu:** Użytkownik klika na lekturę dnia, np. `Rdz 1,1 – 2,4a` (lub `Rdz 12: – 14:`).
2. **Otwarcie Natywnego Czytnika:**
   - Natychmiast pojawia się elegancki widok lektury z tytułem perykopy i siglami na pasku górnym.
   - Płynny wskaźnik ładowania pobiera niezbędne rozdziały równolegle przez coroutines.
3. **Układ Treści:**
   - Nagłówek Księgi i Tytuł Perykopy (np. *„Pieśń o Bogu Stwórcy”*).
   - Tekst wersetów z numeracją w indeksie górnym (np. `¹ Na początku Bóg stworzył niebo i ziemię...`).
   - Płynne przejście do drugiego rozdziału: estetyczny, stonowany nagłówek *„Rozdział 2”* i wersety od 1 do 4. Tekst kończy się dokładnie na wersecie 4 – bez dalszych wersetów rozdziału 2!
4. **Dolny Pasek:**
   - Przycisk `Poprzednia lektura` (`<`).
   - Przycisk `Oznacz przeczytane` (z ikoną i wyrazistą zmianą barwy na tertiary po zaznaczeniu).
   - Przycisk `Następna lektura` (`>`).
5. **Narzędzia Górnego Paska:**
   - Przełącznik rozmiaru czcionki (A- / A+ z suwakiem i podglądem na żywo).
   - Przełącznik motywu ciemny / jasny.
   - Opcjonalne przełączenie na klasyczny widok WebView (dla chcących zobaczyć oryginalną stronę).

#### Typografia i Kolorystyka
- **Tło lektury:** Ciepły pergamin w trybie jasnym (`#FCFBF9`) oraz głęboki, matowy odcień w trybie ciemnym (`#161311`), eliminujący zmęczenie wzroku.
- **Typografia:** Szeryfowa/czytelna rodzina krojów dla tekstu biblijnego, wysoka interlinia (line-height: 1.7), optymalna szerokość kolumny do 680 dp na tabletach.
- **Wyróżniki:** Numery wersetów barwione subtelnym akcentem podstawowym (`primary`), perykopy i śródtytuły pogrubione z akcentem barwnym.

---

### 3. Key Product Decisions & Trade-Offs

- **Decyzja 1: Pobieranie przez REST API zamiast parsowania WebView**
  - *Wybrana metoda:* Wywołanie `https://pismoswiete.pl/api/tome/{sigl}/chapter/{ch}` w tle za pomocą Ktor/HttpURLConnection z pamięcią podręczną na urządzeniu.
  - *Dlaczego:* Jest to o rząd wielkości szybsze niż WebView, działa bez lagów i bez problemów z JavaScriptem, a HTML w odpowiedzi ma jednoznaczne atrybuty `data-sigl="1"`, `data-sigl="2"`, co pozwala na matematycznie precyzyjne wycięcie wersetów.
  - *Alternatywa odrzucona:* Wstrzykiwanie skomplikowanego kodu JavaScript do WebView w celu ukrywania elementów DOM – WebView często migało, spowalniało na starszych urządzeniach i nie potrafiło płynnie skleić dwóch rozdziałów w jeden widok.

- **Decyzja 2: Parser Siglów Biblijnych (PericopeRangeParser)**
  - *Wybrana metoda:* Wbudowany dedykowany parser siglów rozbijający tekst (np. `Rdz 1,1 - 2,4a`, `Rdz 22,20-24; 24:`, `Rdz 12: - 14:`) na listę zadań ekstrakcji: `ChapterVerseRange(tome, chapter, startVerse, endVerse)`.
  - *Dlaczego:* Uniwersalność – obsłuży zarówno Plan Mateusza, jak i każdy inny plan dodany w przyszłości.

- **Decyzja 3: Caching Rozdziałów (Offline Resilience)**
  - *Wybrana metoda:* Prosty dyskowy/pamięciowy cache pobranych rozdziałów JSON w katalogu aplikacji.
  - *Dlaczego:* Raz pobrany rozdział ładuje się natychmiastowo offline przy kolejnym otwarciu, oszczędzając transfer i czas użytkownika.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

#### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                              UI LAYER                                  │
│                                                                        │
│   ┌─────────────────────┐               ┌──────────────────────────┐   │
│   │     HomeScreen      │ ────────────> │   NativePericopeReader   │   │
│   │ (Wybór dnia planu)  │   Nawigacja   │ (Ciągły tekst wersetów)  │   │
│   └─────────────────────┘               └─────────────┬────────────┘   │
└───────────────────────────────────────────────────────┼────────────────┘
                                                        │ Obserwuje stan
┌───────────────────────────────────────────────────────▼────────────────┐
│                           VIEWMODEL LAYER                              │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                       ReaderViewModel                          │   │
│   │  - parsePericope(passageLabel) -> List<ChapterVerseRange>       │   │
│   │  - loadPericopeContent() -> Flow<PericopeUiState>               │   │
│   │  - toggleCompleted()                                           │   │
│   └───────────────────────────────┬────────────────────────────────┘   │
└───────────────────────────────────┼────────────────────────────────────┘
                                    │ Żądanie danych
┌───────────────────────────────────▼────────────────────────────────────┐
│                          REPOSITORY & ENGINE                           │
│                                                                        │
│   ┌──────────────────────────┐          ┌──────────────────────────┐   │
│   │  PericopeRangeParser     │          │  BibleExtractionService  │   │
│   │ (Analiza Rdz 1,1 - 2,4a) │          │ (Pobiera JSON z API)     │   │
│   └─────────────┬────────────┘          └─────────────┬────────────┘   │
│                 │                                     │                │
│                 └───────────────┬─────────────────────┘                │
│                                 ▼                                      │
│                 ┌──────────────────────────────┐                       │
│                 │     PericopeHtmlFilter       │                       │
│                 │ (Wycięcie wersetów data-sigl │                       │
│                 │  i połączenie rozdziałów)    │                       │
│                 └───────────────┬──────────────┘                       │
└─────────────────────────────────┼──────────────────────────────────────┘
                                  │ Zapytanie HTTP / Cache
┌─────────────────────────────────▼──────────────────────────────────────┐
│                    REMOTE API & LOCAL CACHE                            │
│                                                                        │
│   ┌──────────────────────────────────┐   ┌─────────────────────────┐   │
│   │ https://pismoswiete.pl/api/tome/ │   │   Dyskowa pamięć cache  │   │
│   │      {tome}/chapter/{ch}         │   │   (app_cache/bible_api) │   │
│   └──────────────────────────────────┘   └─────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

#### Kluczowe Modele Danych

```kotlin
// Reprezentacja wycinka wersetów z danego rozdziału
data class ChapterVerseRange(
    val bookCode: String,
    val chapter: Int,
    val startVerse: Int = 1,
    val endVerse: Int = Int.MAX_VALUE, // Int.MAX_VALUE oznacza do końca rozdziału
    val endSubVerse: String? = null    // np. "a" w 4a
)

// Wyekstrahowany i oczyszczony fragment gotowy do wyświetlenia
data class ExtractedVerse(
    val verseNumber: Int,
    val text: String
)

data class ExtractedChapterSection(
    val bookName: String,
    val chapterNumber: Int,
    val perykopaTitle: String?,
    val verses: List<ExtractedVerse>
)

data class PericopeReadingContent(
    val label: String,                // np. "Rdz 1,1 – 2,4a"
    val sections: List<ExtractedChapterSection>
)
```

#### Integracja z Istniejącym Kodem
1. **`BibleData.kt` & `MateuszPlanData.kt`**: Istniejące definicje perykop (np. `Rdz 1,1 - 2,4a`, `Rdz 12: - 14:`) przekazują `passageLabel` bezpośrednio do `ReaderViewModel`.
2. **`NativePericopeReader.kt`**: Nowy, dedykowany ekran czytnika zastępujący lub uzupełniający dotychczasowy WebView. W razie potrzeby użytkownik ma w menu ikonę "Otwórz w tradycyjnym WebView".
3. **Zarządzanie stanem i postępem**: Przyciski ukończenia lektury oraz przejścia do następnego dnia działają bez zmian w `UserPreferencesRepository`.
