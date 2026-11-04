# Słowo na Dzień – Codzienna Lektura Pisma Świętego

Aplikacja mobilna na system Android umożliwiająca codzienną, systematyczną lekturę Pisma Świętego według wybranego planu czytania (m.in. „Biblia w 365 dni”, „Nowy Testament w 90 dni”) z dedykowanym czytnikiem zintegrowanym z portalem pismoswiete.pl, automatycznym śledzeniem postępów, statystykami regularności oraz powiadomieniami przypominającymi.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> **Podsumowanie ustaleń z wywiadu:**
> - **Prezentacja tekstu:** Wbudowany czytnik internetowy (Android WebView zoptymalizowany pod kątem wygody lektury, z obsługą bezpośrednich odnośników do ksiąg i rozdziałów na `https://pismoswiete.pl/demo#/tome/{Tome}/chapter/{Chapter}/`, paskiem postępu oraz nawigacją do kolejnego fragmentu).
> - **Plany czytania:** Gotowe, zróżnicowane plany na start:
>   1. *Biblia w 365 dni* (zbalansowana lektura Starego i Nowego Testamentu).
>   2. *Nowy Testament w 90 dni* (dynamiczny plan skupiony na NT).
>   3. *Ewangelie i Psalmy w 60 dni* (idealny dla początkujących).
>   4. *Mądrość i Modlitwa w 30 dni* (Księga Przysłów, Mądrości, Psalmy).
> - **Kluczowe mechaniki:** Lokalna trwałość danych w bazie Room (offline-first dla struktury planów i postępów), śledzenie serii (streak), kalendarz przeczytanych dni, statystyki ukończenia oraz harmonogram przypomnień.

---

### 1. Overview & Core Concept

- **Cel:** Ułatwienie i zmotywowanie użytkownika do regularnego, codziennego kontaktu ze Słowem Bożym bez zagubienia w strukturze 73 ksiąg biblijnych.
- **Dla kogo:** Osoby pragnące przeczytać Pismo Święte w określonym czasie, czytelnicy szukający uporządkowanego harmonogramu lektury oraz użytkownicy ceniący oficjalny tekst Biblii Tysiąclecia / pismoswiete.pl.
- **Kluczowa wartość:** Jedno kliknięcie prowadzi z ekranu głównego prosto do dzisiejszego fragmentu w serwisie pismoswiete.pl, a po przeczytaniu fragment jest odznaczany, zliczając postęp i budując nawyk.

---

### 2. User Experience & Visual Design

#### Główne Ekrany i Przepływ Użytkownika

1. **Ekran Główny („Dziś”):**
   - Karta „Dzisiejsze czytanie” z numerem dnia, wyznaczonymi księgami i rozdziałami (np. *Rdz 1–2; Mt 1*).
   - Przycisk szybkiego startu: **„Rozpocznij lekturę”** – natychmiast otwiera czytnik na pierwszym nieprzeczytanym rozdziale.
   - Pasek postępu bieżącego planu (np. *Dzień 14 z 365 – 4%*) oraz licznik dni z rzędu (🔥 *Streak*).
   - Lista zadań na dany dzień z możliwością bezpośredniego odznaczenia lub podejrzenia.

2. **Ekran Czytnika (Zoptymalizowany In-App Reader):**
   - Wbudowany komponent webowy renderujący `pismoswiete.pl/demo#/tome/{tomeId}/chapter/{chapterId}/`.
   - Górny pasek z nazwą księgi, numerem rozdziału oraz przyciskiem powrotu.
   - Dolny pasek nawigacyjny: *Poprzedni rozdział*, przycisk akcji **„Oznacz jako przeczytany”** (z animacją sukcesu) oraz *Następny fragment*.
   - Wskaźnik ładowania strony i obsługa trybu offline / błędów połączenia z przyciskiem ponowienia.

3. **Katalog Planów Czytania („Plany”):**
   - Przegląd dostępnych harmonogramów wraz z opisem, czasem trwania i intensywnością (rozdziały na dzień).
   - Możliwość wyboru aktywnego planu lub zmiany na inny (z zachowaniem historii).
   - Podgląd pełnego harmonogramu dzień po dniu (możliwość czytania w przód lub nadrabiania zaległości).

4. **Statystyki i Historia („Postęp”):**
   - Kalendarz z zaznaczonymi dniami lektury (aktywność w bieżącym miesiącu).
   - Wskaźniki: suma przeczytanych rozdziałów, najdłuższa seria, aktualna seria, szacowana data ukończenia planu.

5. **Ustawienia & Przypomnienia:**
   - Wybór godziny codziennego przypomnienia o lekturze.
   - Opcje czytnika (np. automatyczne przechodzenie do kolejnego fragmentu).

#### Styl Wizualny i Motyw M3
- **Kierunek:** Ciepły, stonowany, sprzyjający skupieniu i wyciszeniu (Warm Editorial & Modern Minimal).
- **Kolorystyka:**
  - Barwy główne: Głęboki burgund / ciepły szlachetny granat jako akcenty duchowe i dostojne.
  - Tła: Ciepłe odcienie kości słoniowej i pergaminu w trybie jasnym (`#FBF9F5`), głębokie grafitowe tła w trybie ciemnym (`#141312`).
  - Akcent sukcesu: Szmaragdowa zieleń dla przeczytanych rozdziałów i passy dni.
- **Typografia:** Wyrazista czcionka nagłówków o charakterze wydawniczym i czytelny krój bezszeryfowy dla elementów interfejsu.

---

### 3. Key Product Decisions & Trade-Offs

- **Czytnik pismoswiete.pl w WebView vs Scraping/API:**
  - *Wybór:* Zoptymalizowany in-app WebView z bezpośrednim mapowaniem kodów ksiąg (np. `Rdz`, `Wj`, `Mt`, `J`) i rozdziałów do URL SPA `pismoswiete.pl/demo#/tome/{Tome}/chapter/{Chapter}/`.
  - *Uzasadnienie:* Najprostszy, najbardziej stabilny i legalny sposób prezentacji tekstu źródłowego bez ryzyka łamania zmian w nieoficjalnych wewnętrznych endpointach, zachowujący oryginalny układ i przypisy wydawnictwa.
- **Trwałość danych (Baza Room):**
  - *Wybór:* Lokalna baza SQLite za pośrednictwem Android Room + Flow.
  - *Uzasadnienie:* Zgodnie z wytycznymi – dane o postępach, harmonogram planów oraz historia lektury działają w 100% lokalnie i bez opóźnień, nie wymagając logowania ani zewnętrznego serwera.
- **Powiadomienia i przypomnienia:**
  - *Wybór:* Standardowy Android AlarmManager / WorkManager z dedykowanym kanałem powiadomień `daily_reading_channel`.

---

### 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                       Jetpack Compose UI                        │
│  ┌───────────────┐ ┌────────────────┐ ┌──────────────────────┐  │
│  │  HomeScreen   │ │  ReaderScreen  │ │  PlansCatalogScreen  │  │
│  │ (Today's read)│ │ (pismoswiete)  │ │ (Plan selection)     │  │
│  └───────┬───────┘ └────────┬───────┘ └──────────┬───────────┘  │
│          │                  │                    │              │
│          └──────────────┐   │   ┌────────────────┘              │
│                         ▼   ▼   ▼                               │
│                   ┌───────────────────┐                         │
│                   │   MainViewModel   │                         │
│                   └─────────┬─────────┘                         │
└─────────────────────────────┼───────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                      Repository Layer                           │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                   ReadingPlanRepository                    │ │
│  │   - Predefiniowane plany (365 dni, 90 dni, 60 dni itp.)    │ │
│  │   - Mapowanie ksiąg do kodów pismoswiete.pl (Rdz, Mt...)   │ │
│  └──────────────────────────┬─────────────────────────────────┘ │
└─────────────────────────────┼───────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                     Local Persistence (Room)                    │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ AppDatabase (UserPlanEntity, ReadingProgressEntity, Stats) │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

#### Mapowanie Ksiąg do pismoswiete.pl
Aplikacja zawiera pełny rejestr ksiąg biblijnych z ich oficjalnymi skrótami używanymi w strukturze adresów URL serwisu `pismoswiete.pl` (np. `Rdz` dla Księgi Rodzaju, `Wj` dla Księgi Wyjścia, `Mt` dla Ewangelii wg św. Mateusza, `Ap` dla Apokalipsy itd.), co pozwala generować bezpośrednie łącza do każdego rozdziału planu.

#### Gotowość do publikacji (Google Play & Web)
- Zgodność z polityką Google Play (brak zbędnych uprawnień, wyłącznie `INTERNET` do wyświetlania serwisu oraz `POST_NOTIFICATIONS` dla opcjonalnych przypomnień w Android 13+).
- Estetyczna ikona aplikacji i pełna konfiguracja `metadata.json` i `strings.xml`.
