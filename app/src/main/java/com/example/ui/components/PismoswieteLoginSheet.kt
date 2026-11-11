package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.service.auth.PismoswieteAuthManager
import com.example.service.pericope.BibleExtractionEngine
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PismoswieteLoginSheet(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isPageLoading by remember { mutableStateOf(true) }
    var pageProgress by remember { mutableFloatStateOf(0.1f) }
    var currentUrl by remember { mutableStateOf("https://pismoswiete.pl/login") }
    var canGoBackState by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Obsługa fizycznego/gestowego przycisku cofania w Androidzie
    BackHandler {
        val wv = webViewRef
        if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxHeight(0.95f)
            .testTag("pismoswiete_login_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Górny pasek nawigacyjny z przyciskiem cofania
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = {
                            val wv = webViewRef
                            if (wv != null && wv.canGoBack()) {
                                wv.goBack()
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("login_sheet_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (canGoBackState) "Wstecz" else "Zamknij"
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Logowanie do pismoswiete.pl",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (currentUrl.contains("google.com")) "Logowanie przez konto Google" else "Edycja Św. Pawła (bezpłatne konto)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { webViewRef?.reload() },
                        modifier = Modifier.testTag("reload_pismoswiete_login_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież stronę")
                    }

                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Nie można otworzyć przeglądarki", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("open_external_browser_login_button")
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Otwórz w przeglądarce")
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_pismoswiete_login_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij")
                    }
                }
            }

            // Pasek wskazówki dla użytkownika
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Możesz zalogować się e-mailem/hasłem lub przyciskiem Google. Aplikacja zapamięta sesję.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Wskaźnik ładowania strony
            if (isPageLoading) {
                LinearProgressIndicator(
                    progress = { pageProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                HorizontalDivider()
            }

            // Widok przeglądarki z formularzem logowania
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            // Czysty User-Agent Chrome Mobile bez custom tokenów,
                            // aby Google OAuth nie odrzucało logowania z błędem 403 disallowed_useragent
                            val standardChromeUa = "Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36"
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                userAgentString = standardChromeUa
                                javaScriptCanOpenWindowsAutomatically = true
                                setSupportMultipleWindows(true)
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                cacheMode = WebSettings.LOAD_DEFAULT
                                useWideViewPort = true
                                loadWithOverviewMode = true
                            }

                            val cm = CookieManager.getInstance()
                            cm.setAcceptCookie(true)
                            cm.setAcceptThirdPartyCookies(this, true)

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    pageProgress = newProgress / 100f
                                    if (newProgress >= 100) {
                                        isPageLoading = false
                                    }
                                }

                                override fun onCreateWindow(
                                    view: WebView?,
                                    isDialog: Boolean,
                                    isUserGesture: Boolean,
                                    resultMsg: Message?
                                ): Boolean {
                                    // Obsługa logowania przez Google/Apple/Facebook - przekieruj w tym samym oknie
                                    val transport = resultMsg?.obj as? WebView.WebViewTransport
                                    transport?.webView = view
                                    resultMsg?.sendToTarget()
                                    return true
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isPageLoading = true
                                    currentUrl = url ?: ""
                                    canGoBackState = view?.canGoBack() == true
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    canGoBackState = view?.canGoBack() == true
                                    return false // Obsługuj wszystkie przekierowania w WebView
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isPageLoading = false
                                    currentUrl = url ?: ""
                                    canGoBackState = view?.canGoBack() == true

                                    // Sprawdzamy stan zalogowania w DOM strony pismoswiete.pl
                                    if (url != null && (url.contains("pismoswiete.pl") || url.contains("/sso/google/callback"))) {
                                        val checkScript = """
                                            (function() {
                                                try {
                                                    var hasLogout = document.querySelector('a[href*="/logout"]') !== null;
                                                    var hasLoginForm = document.querySelector('form[action*="/login_check"]') !== null;
                                                    var hasRegister = document.querySelector('form[action*="/register"]') !== null;
                                                    var userElement = document.querySelector('.user-name, .user-email, .account, [href*="/logout"], [href*="/profil"]');
                                                    var email = userElement ? userElement.innerText.trim() : null;
                                                    return JSON.stringify({
                                                        hasLogout: hasLogout,
                                                        hasLoginForm: hasLoginForm,
                                                        hasRegister: hasRegister,
                                                        email: email,
                                                        url: window.location.href
                                                    });
                                                } catch(e) {
                                                    return JSON.stringify({ error: e.toString() });
                                                }
                                            })()
                                        """.trimIndent()

                                        view?.evaluateJavascript(checkScript) { resultJson ->
                                            try {
                                                val cleanJson = if (resultJson.startsWith("\"") && resultJson.endsWith("\"")) {
                                                    org.json.JSONTokener(resultJson).nextValue().toString()
                                                } else {
                                                    resultJson
                                                }
                                                val json = JSONObject(cleanJson)
                                                val hasLogout = json.optBoolean("hasLogout", false)
                                                val hasLoginForm = json.optBoolean("hasLoginForm", false)
                                                val email = json.optString("email", "").ifBlank { null }
                                                val pageUrl = json.optString("url", url)

                                                val cookies = cm.getCookie("https://pismoswiete.pl")
                                                val hasRememberMe = cookies?.contains("REMEMBERME") == true

                                                // Logowanie jest potwierdzone gdy:
                                                // 1. Strona zawiera przycisk /logout
                                                // 2. LUB ciasteczka zawierają REMEMBERME
                                                // 3. LUB przeszliśmy na inną podstronę serwisu i nie ma formularza logowania
                                                val isVerifiedLoggedIn = hasLogout || hasRememberMe ||
                                                        (!hasLoginForm && !pageUrl.contains("/login") && !pageUrl.contains("/register") && !pageUrl.contains("/resetting"))

                                                if (isVerifiedLoggedIn && !cookies.isNullOrBlank()) {
                                                    PismoswieteAuthManager.saveCookies(ctx, cookies, email)
                                                    BibleExtractionEngine.clearCache(ctx)
                                                    Toast.makeText(ctx, "Zalogowano pomyślnie do pismoswiete.pl!", Toast.LENGTH_SHORT).show()
                                                    onLoginSuccess()
                                                    onDismiss()
                                                }
                                            } catch (ignored: Exception) {}
                                        }
                                    }
                                }
                            }

                            webViewRef = this
                            loadUrl("https://pismoswiete.pl/login")
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("pismoswiete_login_webview")
                )

                if (isPageLoading && pageProgress < 0.3f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }
            }

            // Dolny pasek potwierdzenia zalogowania
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_login_button")
                    ) {
                        Text("Anuluj")
                    }

                    Button(
                        onClick = {
                            val wv = webViewRef
                            val cm = CookieManager.getInstance()
                            val cookies = cm.getCookie("https://pismoswiete.pl")

                            if (wv != null) {
                                val verifyScript = """
                                    (function() {
                                        var hasLogout = document.querySelector('a[href*="/logout"]') !== null;
                                        var hasLoginForm = document.querySelector('form[action*="/login_check"]') !== null;
                                        return JSON.stringify({
                                            hasLogout: hasLogout,
                                            hasLoginForm: hasLoginForm,
                                            url: window.location.href
                                        });
                                    })()
                                """.trimIndent()

                                wv.evaluateJavascript(verifyScript) { result ->
                                    var isTrulyLogged = false
                                    try {
                                        val clean = if (result.startsWith("\"") && result.endsWith("\"")) {
                                            org.json.JSONTokener(result).nextValue().toString()
                                        } else result
                                        val obj = JSONObject(clean)
                                        val hasLogout = obj.optBoolean("hasLogout", false)
                                        val hasLoginForm = obj.optBoolean("hasLoginForm", false)
                                        val current = obj.optString("url", "")
                                        val hasRememberMe = cookies?.contains("REMEMBERME") == true

                                        isTrulyLogged = hasLogout || hasRememberMe ||
                                                (!hasLoginForm && !current.contains("/login") && !current.contains("/register"))
                                    } catch (e: Exception) {
                                        isTrulyLogged = cookies?.contains("REMEMBERME") == true
                                    }

                                    if (isTrulyLogged && !cookies.isNullOrBlank()) {
                                        PismoswieteAuthManager.saveCookies(context, cookies)
                                        BibleExtractionEngine.clearCache(context)
                                        Toast.makeText(context, "Zapisano sesję logowania do pismoswiete.pl!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Wypełnij formularz powyżej i kliknij 'Zaloguj się' (lub wybierz Google).",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Zaloguj się najpierw w formularzu powyżej.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("confirm_pismoswiete_login_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Zatwierdź zalogowanie")
                    }
                }
            }
        }
    }
}
