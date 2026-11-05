package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.ReadingPassage
import com.example.ui.viewmodel.ReaderSession
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.example.model.BibleSource

private const val READER_PREFS = "reader_settings"
private const val KEY_FONT_ZOOM = "font_zoom_percent"
private const val DEFAULT_FONT_ZOOM = 130
private val LightPaperColor = Color(0xFFFCFBF9)
private val DarkPaperColor = Color(0xFF161311)

private fun injectSafeReaderStyles(webView: WebView?, isDark: Boolean = false) {
    val bgColor = if (isDark) "#161311" else "#fcfbf9"
    val textColor = if (isDark) "#ECE0DB" else "#212529"
    val primaryColor = if (isDark) "#F3BD80" else "#1565c0"
    val titleColor = if (isDark) "#D36868" else "#0d47a1"
    val subtitleColor = if (isDark) "#9E8E89" else "#5f6368"

    val js = """
        (function() {
            try {
                // Ensure viewport meta tag exists
                var meta = document.querySelector('meta[name="viewport"]');
                if (!meta) {
                    meta = document.createElement('meta');
                    meta.name = 'viewport';
                    meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes';
                    if (document.head) document.head.appendChild(meta);
                }

                // Add or update reader CSS without breaking layout
                var styleId = 'biblia_reader_clean_style';
                var style = document.getElementById(styleId);
                if (!style) {
                    style = document.createElement('style');
                    style.id = styleId;
                    style.type = 'text/css';
                    if (document.head) document.head.appendChild(style);
                }
                style.innerHTML = `
                    html, body {
                        background-color: $bgColor !important;
                        background-image: none !important;
                        color: $textColor !important;
                        -webkit-text-size-adjust: 100% !important;
                        margin: 0 !important;
                        padding: 0 !important;
                    }

                    /* ========================================================
                       1. PISMOSWIETE.PL RULES
                       ======================================================== */
                    md-toolbar.toolbar,
                    .toolbar,
                    #toastContainer,
                    .search-box,
                    .search-area,
                    .search-select-container,
                    .bookmark-chapter,
                    .preview-bottom-toolbar,
                    [class*="bottom-toolbar"],
                    .next-prev-button,
                    #btn-prev-chapter-bottom,
                    #btn-next-chapter-bottom,
                    #btn-prev-chapter-top,
                    #btn-next-chapter-top,
                    [aria-label*="Poprzedni rozdział"],
                    [aria-label*="Następny rozdział"],
                    [aria-label*="poprzedni"],
                    [aria-label*="następny"],
                    md-tooltip,
                    .md-tooltip,
                    .md-tooltip-container,
                    .md-tooltip-grow,
                    [role="tooltip"],
                    md-backdrop,
                    .md-scroll-mask,
                    md-sidenav,
                    .preview .link,
                    .preview .link.comment,
                    .preview .link.definition,
                    .preview .link.note,
                    .preview .link.dictionary,
                    .preview .link.bookmark,
                    .preview .comment,
                    .preview .comment-perykopa,
                    .preview .comment-tome,
                    .preview a.link,
                    a.link.comment,
                    a.link.definition,
                    .links,
                    .fa-comments,
                    .fa.fa-comments {
                        display: none !important;
                        height: 0 !important;
                        min-height: 0 !important;
                        max-height: 0 !important;
                        visibility: hidden !important;
                        opacity: 0 !important;
                        pointer-events: none !important;
                        overflow: hidden !important;
                        position: absolute !important;
                        left: -9999px !important;
                        top: -9999px !important;
                        margin: 0 !important;
                        padding: 0 !important;
                    }
                    #main-div, .chapter-view-content-anim {
                        padding-top: 0 !important;
                        margin-top: 0 !important;
                        background-color: $bgColor !important;
                    }
                    .md-content {
                        padding-top: 4px !important;
                        background-color: $bgColor !important;
                    }
                    .preview.preview-with-left-margin {
                        margin-left: 0 !important;
                    }
                    .preview {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif !important;
                        font-size: 1.12rem !important;
                        line-height: 1.8 !important;
                        color: $textColor !important;
                        background-color: $bgColor !important;
                        max-width: 780px !important;
                        margin: 0 auto !important;
                        padding: 12px 16px 80px 16px !important;
                        box-sizing: border-box !important;
                    }
                    @media (max-width: 600px) {
                        .preview {
                            padding: 8px 12px 60px 12px !important;
                            font-size: 1.08rem !important;
                            line-height: 1.75 !important;
                        }
                    }
                    .preview .chapter-number {
                        font-size: 2em !important;
                        font-weight: bold !important;
                        color: $primaryColor !important;
                        float: left !important;
                        margin: 2px 6px 0 0 !important;
                    }
                    .preview .werset-number, .preview sup {
                        color: $primaryColor !important;
                        font-weight: bold !important;
                        font-size: 0.8em !important;
                    }
                    .preview .perykopa {
                        font-size: 1.15em !important;
                        font-weight: bold !important;
                        color: $titleColor !important;
                        margin: 16px 0 6px !important;
                    }

                    /* ========================================================
                       2. BIBLIA.DEON.PL RULES
                       ======================================================== */
                    .header,
                    .footer,
                    .sculpture,
                    .border-top,
                    .border-bottom,
                    .nav,
                    .mobile-nav-wrapper,
                    .mobile-nav,
                    .chapter-nav,
                    .border-nav,
                    .ad-content,
                    .bottom-navi,
                    [class*="bottom-navi"],
                    div#chapter-buttons,
                    div.title,
                    .footer-nav,
                    #switch-chapter-nav,
                    #right-button,
                    #left-button,
                    a[title="następny"],
                    a[title="poprzedni"],
                    .blue-dot-line,
                    #img-pallottinum,
                    #pismo-swiete,
                    #starego-i-nowego,
                    a.przypis,
                    sup a.przypis,
                    .footnotes,
                    .footnotes-content,
                    div.menu,
                    div.submenu,
                    #top_search,
                    .search,
                    .reklama,
                    .social-share,
                    .fb-like,
                    #cookie-law,
                    .cookie-info,
                    .cookie {
                        display: none !important;
                        height: 0 !important;
                        min-height: 0 !important;
                        max-height: 0 !important;
                        visibility: hidden !important;
                        opacity: 0 !important;
                        pointer-events: none !important;
                        overflow: hidden !important;
                        position: absolute !important;
                        left: -9999px !important;
                        top: -9999px !important;
                        margin: 0 !important;
                        padding: 0 !important;
                    }

                    .wrap, .the-content, .tresc, #content, .main {
                        background-color: $bgColor !important;
                        background-image: none !important;
                        max-width: 780px !important;
                        margin: 0 auto !important;
                        padding: 12px 16px 80px 16px !important;
                        box-sizing: border-box !important;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif !important;
                        font-size: 1.12rem !important;
                        line-height: 1.8 !important;
                        color: $textColor !important;
                        width: 100% !important;
                    }

                    @media (max-width: 600px) {
                        .wrap, .the-content, .tresc, #content {
                            padding: 8px 14px 60px 14px !important;
                            font-size: 1.08rem !important;
                            line-height: 1.75 !important;
                        }
                    }

                    .testament-label {
                        font-size: 0.85rem !important;
                        text-transform: uppercase !important;
                        color: $subtitleColor !important;
                        letter-spacing: 1.5px !important;
                        text-align: center !important;
                        margin: 8px 0 4px !important;
                        display: block !important;
                    }

                    .book-label {
                        font-size: 1.45rem !important;
                        font-weight: bold !important;
                        color: $titleColor !important;
                        text-align: center !important;
                        margin: 4px 0 16px !important;
                        display: block !important;
                    }

                    .tytul1, .miedzytytul1, .tytul2 {
                        font-size: 1.18rem !important;
                        font-weight: bold !important;
                        color: $titleColor !important;
                        margin: 20px 0 8px !important;
                        display: block !important;
                    }

                    .initial-letter {
                        font-size: 2.2rem !important;
                        font-weight: bold !important;
                        color: $primaryColor !important;
                        float: left !important;
                        line-height: 1 !important;
                        margin: 2px 8px 0 0 !important;
                    }

                    .werset, span.werset {
                        color: $primaryColor !important;
                        font-weight: bold !important;
                        font-size: 0.85em !important;
                        margin-right: 4px !important;
                        display: inline !important;
                    }

                    sup {
                        color: $primaryColor !important;
                        font-size: 0.75em !important;
                    }
                `;

                // Apply direct inline styles to body
                document.body.style.backgroundColor = '$bgColor';
                document.body.style.color = '$textColor';

                // Function to permanently purge unwanted elements from the DOM
                function purgeOffendingElements() {
                    var selectors = [
                        'md-tooltip', '.md-tooltip', '.md-tooltip-container', '.md-tooltip-grow', '[role="tooltip"]',
                        '#toastContainer', '.preview-bottom-toolbar', '[class*="bottom-toolbar"]', '.next-prev-button',
                        '#btn-prev-chapter-bottom', '#btn-next-chapter-bottom',
                        '#btn-prev-chapter-top', '#btn-next-chapter-top',
                        'md-toolbar', '.toolbar', 'md-sidenav', '.md-scroll-mask',
                        '.preview .link', '.preview .comment', 'a.link', '.fa-comments', '.links',
                        '.header', '.footer', '.sculpture', '.nav', '.mobile-nav-wrapper', '.mobile-nav',
                        '.chapter-nav', '.ad-content', '.bottom-navi', '[class*="bottom-navi"]', 'div#chapter-buttons',
                        '#right-button', '#left-button', 'a[title="następny"]', 'a[title="poprzedni"]',
                        '.footer-nav', '#switch-chapter-nav', '.blue-dot-line', '#img-pallottinum',
                        '#pismo-swiete', '#starego-i-nowego', 'a.przypis', 'sup a.przypis',
                        '.footnotes', '.footnotes-content', '.reklama', '.cookie', '#cookie-law'
                    ];
                    for (var s = 0; s < selectors.length; s++) {
                        var els = document.querySelectorAll(selectors[s]);
                        for (var i = 0; i < els.length; i++) {
                            if (els[i] && els[i].parentNode) {
                                els[i].parentNode.removeChild(els[i]);
                            }
                        }
                    }
                }

                purgeOffendingElements();

                // Install observer once to continually purge asynchronous SPA additions
                if (!window._mannaObserverInstalled) {
                    window._mannaObserverInstalled = true;
                    try {
                        var obs = new MutationObserver(function() {
                            purgeOffendingElements();
                        });
                        obs.observe(document.documentElement || document.body, { childList: true, subtree: true });
                        setInterval(purgeOffendingElements, 500);
                    } catch (e) {
                        console.error("Observer init error: " + e);
                    }
                }
            } catch (e) {
                console.error("Reader styling error: " + e);
            }
        })();
    """.trimIndent()
    webView?.evaluateJavascript(js, null)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReaderScreen(
    session: ReaderSession,
    onClose: () -> Unit,
    onToggleCompleted: () -> Unit,
    onNextChapter: () -> Unit,
    onPrevChapter: () -> Unit,
    onSetBibleSource: (BibleSource) -> Unit = {},
    onSwitchToNativeReader: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    BackHandler { onClose() }

    val sharedPrefs = remember(context) {
        context.getSharedPreferences(READER_PREFS, Context.MODE_PRIVATE)
    }

    var fontZoom by remember {
        mutableIntStateOf(sharedPrefs.getInt(KEY_FONT_ZOOM, DEFAULT_FONT_ZOOM))
    }
    var showFontSizeSheet by remember { mutableStateOf(false) }
    var showSourceMenu by remember { mutableStateOf(false) }

    val currentUrl = remember(session.bookCode, session.chapter, session.source) {
        val passage = ReadingPassage(session.bookCode, session.bookName, session.chapter)
        passage.toUrl(session.source)
    }

    var lastLoadedUrl by remember { mutableStateOf("") }
    var webProgress by remember { mutableFloatStateOf(0.1f) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    fun applyFontZoom(zoom: Int) {
        val clamped = zoom.coerceIn(90, 220)
        fontZoom = clamped
        sharedPrefs.edit().putInt(KEY_FONT_ZOOM, clamped).apply()
        webViewInstance?.settings?.textZoom = clamped
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("reader_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "${session.bookName} ${session.chapter}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${session.source.shortLabel} • Dzień ${session.dayNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("reader_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Powrót"
                    )
                }
            },
            actions = {
                // Source Switcher Menu
                Box {
                    IconButton(
                        onClick = { showSourceMenu = true },
                        modifier = Modifier.testTag("reader_switch_source_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Zmień źródło Biblii (${session.source.shortLabel})",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

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

                // Theme mode toggle button (Day / Night)
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.testTag("reader_theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (isDarkTheme) "Włącz tryb jasny" else "Włącz tryb nocny",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Switch to Native Pericope Reader
                IconButton(
                    onClick = onSwitchToNativeReader,
                    modifier = Modifier.testTag("reader_switch_native_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "Przełącz na natywny czytnik perykop",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Font scaling button
                IconButton(
                    onClick = { showFontSizeSheet = true },
                    modifier = Modifier.testTag("reader_font_size_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = "Rozmiar tekstu ($fontZoom%)",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = {
                        hasError = false
                        isLoading = true
                        webViewInstance?.reload()
                    },
                    modifier = Modifier.testTag("reader_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Odśwież"
                    )
                }
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.testTag("reader_open_browser_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "Otwórz w przeglądarce"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Loading bar
        AnimatedVisibility(visible = isLoading) {
            LinearProgressIndicator(
                progress = { webProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reader_progress_bar"),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        val currentPaperColor = if (isDarkTheme) DarkPaperColor else LightPaperColor
        val webBgHex = if (isDarkTheme) "#161311" else "#FCFBF9"

        // Main Reader Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(currentPaperColor)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        // Paper background matches current theme (dark/light)
                        setBackgroundColor(android.graphics.Color.parseColor(webBgHex))

                        // Enable cookies
                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT

                            // Auto-fit responsive layout
                            useWideViewPort = true
                            loadWithOverviewMode = false

                            // Scalable text sizing
                            textZoom = fontZoom
                            defaultFontSize = 20
                            minimumFontSize = 14
                            minimumLogicalFontSize = 14
                            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

                            builtInZoomControls = true
                            displayZoomControls = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                webProgress = newProgress / 100f
                                if (newProgress >= 70) {
                                    injectSafeReaderStyles(view, isDarkTheme)
                                }
                                if (newProgress >= 100) {
                                    isLoading = false
                                }
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                hasError = false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                injectSafeReaderStyles(view, isDarkTheme)
                                view?.postDelayed({ injectSafeReaderStyles(view, isDarkTheme) }, 300)
                                view?.postDelayed({ injectSafeReaderStyles(view, isDarkTheme) }, 800)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    hasError = true
                                    isLoading = false
                                }
                            }
                        }

                        lastLoadedUrl = currentUrl
                        loadUrl(currentUrl)
                        webViewInstance = this
                    }
                },
                update = { webView ->
                    // Apply zoom changes without reloading
                    if (webView.settings.textZoom != fontZoom) {
                        webView.settings.textZoom = fontZoom
                    }

                    // Dynamically update background color when theme switches
                    webView.setBackgroundColor(android.graphics.Color.parseColor(webBgHex))

                    // Only loadUrl if the requested passage URL actually changed (prevents reload loop)
                    if (currentUrl != lastLoadedUrl) {
                        lastLoadedUrl = currentUrl
                        isLoading = true
                        hasError = false
                        webView.loadUrl(currentUrl)
                    } else {
                        injectSafeReaderStyles(webView, isDarkTheme)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Graceful loading overlay inside the reading box
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(currentPaperColor.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Ładowanie: ${session.bookName} ${session.chapter}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Error display card
            if (hasError) {
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
                            text = "Nie udało się załadować tekstu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sprawdź połączenie z internetem i spróbuj ponownie.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                hasError = false
                                isLoading = true
                                lastLoadedUrl = currentUrl
                                webViewInstance?.loadUrl(currentUrl)
                            },
                            modifier = Modifier.testTag("reader_retry_button")
                        ) {
                            Text("Spróbuj ponownie")
                        }
                    }
                }
            }
        }

        // Bottom Reader Navigation Bar
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
                // Previous Chapter Button (always fixed 48x48dp, solid tonal style)
                FilledTonalIconButton(
                    onClick = onPrevChapter,
                    enabled = session.hasPrev,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("reader_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = "Poprzedni rozdział"
                    )
                }

                // Toggle Completed Action Button (expands to fill middle space cleanly)
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

                // Next Chapter Button (always fixed 48x48dp, solid tonal style)
                FilledTonalIconButton(
                    onClick = onNextChapter,
                    enabled = session.hasNext,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("reader_next_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = "Kolejny rozdział"
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for adjusting reader typography / font size
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
                    text = "Dopasuj czcionkę do swojego ekranu i wygody czytania",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stepper Row: [-] [ 130% ] [+]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { applyFontZoom(fontZoom - 15) },
                        enabled = fontZoom > 90,
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
                                text = "$fontZoom%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    FilledIconButton(
                        onClick = { applyFontZoom(fontZoom + 15) },
                        enabled = fontZoom < 210,
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

                // Preset Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
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
                            selected = fontZoom == pct,
                            onClick = { applyFontZoom(pct) },
                            label = {
                                Text(
                                    label,
                                    fontWeight = if (fontZoom == pct) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive live preview box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Podgląd tekstu:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Na początku Bóg stworzył niebo i ziemię. A ziemia była bezładną pustką...",
                            fontSize = (16f * fontZoom / 100f).sp,
                            lineHeight = (24f * fontZoom / 100f).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { applyFontZoom(DEFAULT_FONT_ZOOM) },
                        modifier = Modifier.testTag("font_reset_button")
                    ) {
                        Text("Resetuj (130%)")
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                showFontSizeSheet = false
                            }
                        },
                        modifier = Modifier.testTag("font_done_button")
                    ) {
                        Text("Zastosuj")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.stopLoading()
        }
    }
}
