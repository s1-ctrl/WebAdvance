package com.cylonid.nativealpha.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CodeOff
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DoNotTouch
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.ui.theme.BgDeep
import com.cylonid.nativealpha.ui.theme.BgDark
import com.cylonid.nativealpha.ui.theme.CardBorder
import com.cylonid.nativealpha.ui.theme.CardSurface
import com.cylonid.nativealpha.ui.theme.CyanPrimary
import com.cylonid.nativealpha.ui.theme.ErrorRed
import com.cylonid.nativealpha.ui.theme.GradCyanEnd
import com.cylonid.nativealpha.ui.theme.GradCyanStart
import com.cylonid.nativealpha.ui.theme.GradVioletEnd
import com.cylonid.nativealpha.ui.theme.GradVioletStart
import com.cylonid.nativealpha.ui.theme.StatusActive
import com.cylonid.nativealpha.ui.theme.TextMuted
import com.cylonid.nativealpha.ui.theme.TextPrimary
import com.cylonid.nativealpha.ui.theme.TextSecondary
import com.cylonid.nativealpha.ui.theme.VioletSecondary
import com.cylonid.nativealpha.util.ScreenshotUtil
import com.cylonid.nativealpha.viewmodel.ConsoleMessageData
import com.cylonid.nativealpha.viewmodel.WebViewViewModel
import com.cylonid.nativealpha.ui.DownloadHistoryActivity
import com.cylonid.nativealpha.waos.util.WaosConstants
import com.cylonid.nativealpha.webview.SessionManager
import com.cylonid.nativealpha.webview.SessionRestoreData
import com.cylonid.nativealpha.webview.WebViewClientWithDownload
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebViewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val webAppId = intent.getLongExtra("webAppId", 0L)

        // WAOS Session Isolation: apply separate WebView data directory BEFORE
        // setContent() so it takes effect before any WebView is instantiated.
        // This ensures cookies, localStorage, IndexedDB, cache are fully isolated
        // per app — no sharing across apps.
        SessionManager.applyIsolation(webAppId)

        setContent {
            WebViewScreen(
                webAppId = webAppId,
                onBackPressed = { finish() }
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CREDENTIALS && resultCode == RESULT_OK && data != null) {
            val username = data.getStringExtra("CREDENTIAL_USERNAME")
            val password = data.getStringExtra("CREDENTIAL_PASSWORD")
            if (username != null && password != null) {
                pendingAutoFill = Pair(username, password)
            }
        }
    }

    companion object {
        const val REQUEST_CODE_CREDENTIALS = 4231
        var pendingAutoFill: Pair<String, String>? = null
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    webAppId: Long,
    onBackPressed: () -> Unit,
    viewModel: WebViewViewModel = viewModel()
) {
    val context = LocalContext.current
    val webViewState by viewModel.webViewState.collectAsState()
    val consoleMessages by viewModel.consoleMessages.collectAsState()
    val showConsole by viewModel.showConsole.collectAsState()
    val isDesktopMode by viewModel.isDesktopMode.collectAsState()
    val isAdblockEnabled by viewModel.isAdblockEnabled.collectAsState()
    val isAutoScrollEnabled by viewModel.isAutoScrollEnabled.collectAsState()
    val isAutoClickEnabled by viewModel.isAutoClickEnabled.collectAsState()
    val webApp by viewModel.webApp.collectAsState()

    var showFindBar by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var findResultCount by remember { mutableStateOf(0) }
    val webViewRef = remember { mutableStateOf<android.webkit.WebView?>(null) }
    val webAppRef = remember { mutableStateOf<WebApp?>(null) }
    var initialUrlLoaded by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    var pinUnlocked by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val settingsPrefs = remember { context.getSharedPreferences("waos_settings", Context.MODE_PRIVATE) }
    val developerModeEnabled = remember { settingsPrefs.getBoolean("developer_mode", false) }
    val floatingWindowsEnabled = remember { settingsPrefs.getBoolean("floating_windows", true) }
    val autoScrollSpeed = remember { settingsPrefs.getInt("auto_scroll_speed", 3) }

    LaunchedEffect(webAppId) {
        viewModel.loadWebApp(webAppId)
    }

    LaunchedEffect(webApp?.url) {
        val url = webApp?.lastUrl ?: webApp?.url
        if (!initialUrlLoaded && !url.isNullOrBlank() && (webApp?.isLocked == false || pinUnlocked)) {
            webViewRef.value?.loadUrl(url)
            initialUrlLoaded = true
        }
    }

    LaunchedEffect(pinUnlocked) {
        if (pinUnlocked) {
            val url = webApp?.lastUrl ?: webApp?.url
            if (!url.isNullOrBlank()) {
                webViewRef.value?.loadUrl(url)
                initialUrlLoaded = true
            }
        }
    }

    LaunchedEffect(webApp) {
        webApp?.let { app ->
            if (app.isLocked && !pinUnlocked) {
                showPinDialog = true
            }
            val window = (context as? ComponentActivity)?.window
            if (app.isKeepAwake) {
                window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            // Load last session if available
            val sessionManager = SessionManager(context, app.id, app.name)
            val lastSession = sessionManager.loadLastSessionSnapshot()
            if (lastSession != null) {
                val restoreData = sessionManager.applySessionSnapshot(lastSession)
                viewModel.updateWebViewState(webViewState.copy(
                    shouldImportSession = restoreData
                ))
            }
        }
    }

    LaunchedEffect(Unit) {
        val autoFill = WebViewActivity.pendingAutoFill
        if (autoFill != null) {
            viewModel.autoFillCredentials(autoFill.first, autoFill.second)
            WebViewActivity.pendingAutoFill = null
        }
    }

    // Show toast when session export finishes
    LaunchedEffect(webViewState.lastSessionExportPath) {
        val path = webViewState.lastSessionExportPath
        if (path != null) {
            android.widget.Toast.makeText(
                context,
                "Session exported: ${path.substringAfterLast('/')}",
                android.widget.Toast.LENGTH_LONG
            ).show()
            viewModel.clearSessionExportPath()
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false; onBackPressed() },
            containerColor = CardSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.radialGradient(listOf(VioletSecondary.copy(0.3f), Color.Transparent)),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = VioletSecondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("App Locked", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Enter PIN to unlock ${webApp?.name ?: "this app"}", color = TextSecondary, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it; pinError = false },
                        label = { Text("PIN", color = TextMuted) },
                        isError = pinError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletSecondary,
                            unfocusedBorderColor = CardBorder,
                            cursorColor = VioletSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    if (pinError) {
                        Spacer(Modifier.height(6.dp))
                        Text("Incorrect PIN", color = ErrorRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput == webApp?.pin) {
                            pinUnlocked = true
                            showPinDialog = false
                            pinInput = ""
                        } else {
                            pinError = true
                        }
                    },
                    modifier = Modifier.background(
                        Brush.horizontalGradient(listOf(GradVioletStart, GradVioletEnd)),
                        RoundedCornerShape(8.dp)
                    )
                ) { Text("Unlock") }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false; onBackPressed() }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    webViewState.shouldShowImageLongPressDialog?.let { imageUrl ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissImageLongPressDialog() },
            containerColor = CardSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.radialGradient(listOf(VioletSecondary.copy(0.3f), Color.Transparent)),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null, tint = VioletSecondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("Image Options", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("What would you like to do with this image?", color = TextSecondary, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.handleDownload(imageUrl, webApp)
                        viewModel.dismissImageLongPressDialog()
                    },
                    modifier = Modifier.background(
                        Brush.horizontalGradient(listOf(GradVioletStart, GradVioletEnd)),
                        RoundedCornerShape(8.dp)
                    )
                ) { Text("Download") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        // Share image URL
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, imageUrl)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Image URL"))
                        viewModel.dismissImageLongPressDialog()
                    }) {
                        Text("Share URL", color = TextMuted)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.dismissImageLongPressDialog() }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            }
        )
    }

    webViewState.shouldShowLinkLongPressDialog?.let { linkUrl ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissLinkLongPressDialog() },
            containerColor = CardSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.radialGradient(listOf(CyanPrimary.copy(0.3f), Color.Transparent)),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("Link Options", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Link:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(linkUrl, color = CyanPrimary, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Link", linkUrl))
                        // Also save to app clipboard
                        webApp?.let { app ->
                            val item = com.cylonid.nativealpha.waos.model.ClipboardItem(
                                text = linkUrl,
                                appId = app.id.toInt(),
                                timestamp = System.currentTimeMillis(),
                                type = "link"
                            )
                            com.cylonid.nativealpha.waos.model.ClipboardRepository.saveClipboardItem(
                                context, item, app.clipboardMaxItems
                            )
                        }
                        android.widget.Toast.makeText(context, "Link copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                        viewModel.dismissLinkLongPressDialog()
                    },
                    modifier = Modifier.background(
                        Brush.horizontalGradient(listOf(GradCyanStart, GradCyanEnd)),
                        RoundedCornerShape(8.dp)
                    )
                ) { Text("Copy Link") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl))
                        context.startActivity(intent)
                        viewModel.dismissLinkLongPressDialog()
                    }) { Text("Open in Browser", color = TextMuted) }
                    TextButton(onClick = { viewModel.dismissLinkLongPressDialog() }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    webViewState.selectedText?.let { selectedText ->
        androidx.compose.animation.AnimatedVisibility(
            visible = true,
            enter = androidx.compose.animation.slideInVertically { it } + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically { it } + androidx.compose.animation.fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardSurface)
                        .border(1.dp, CyanPrimary.copy(0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ContentCopy, null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        selectedText.take(60) + if (selectedText.length > 60) "…" else "",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Text", selectedText))
                            // Also save to app clipboard
                            webApp?.let { app ->
                                val item = com.cylonid.nativealpha.waos.model.ClipboardItem(
                                    text = selectedText,
                                    appId = app.id.toInt(),
                                    timestamp = System.currentTimeMillis(),
                                    type = "text"
                                )
                                com.cylonid.nativealpha.waos.model.ClipboardRepository.saveClipboardItem(
                                    context, item, app.clipboardMaxItems
                                )
                            }
                            android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                            viewModel.clearSelectedText()
                        },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = CyanPrimary)
                    ) { Text("Copy", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    IconButton(onClick = { viewModel.clearSelectedText() }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }

    LaunchedEffect(webViewState.shouldOpenCredentialKeeper) {
        if (webViewState.shouldOpenCredentialKeeper) {
            val intent = Intent(context, CredentialVaultActivity::class.java).apply {
                putExtra(WaosConstants.EXTRA_WAOS_APP_ID, webAppId)
            }
            (context as? ComponentActivity)?.startActivityForResult(
                intent,
                WebViewActivity.REQUEST_CODE_CREDENTIALS
            )
            viewModel.clearActionFlags()
        }
    }

    LaunchedEffect(webViewState.shouldOpenClipboardManager) {
        if (webViewState.shouldOpenClipboardManager) {
            val intent = Intent(context, ClipboardManagerActivity::class.java).apply {
                putExtra(WaosConstants.EXTRA_CLIPBOARD_APP_ID, webAppId)
            }
            context.startActivity(intent)
            viewModel.clearActionFlags()
        }
    }

    LaunchedEffect(webViewState.shouldOpenDownloadHistory) {
        if (webViewState.shouldOpenDownloadHistory) {
            val intent = Intent(context, DownloadHistoryActivity::class.java).apply {
                putExtra(WaosConstants.EXTRA_DOWNLOAD_APP_ID, webAppId)
                putExtra("APP_DISPLAY_NAME", webApp?.name ?: "App")
            }
            context.startActivity(intent)
            viewModel.clearActionFlags()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .statusBarsPadding()
    ) {
        WaosTopBar(
            title = webApp?.name ?: "Web App",
            url = webViewState.currentUrl,
            isLoading = webViewState.isLoading,
            progress = webViewState.progress,
            onBack = onBackPressed
        )

        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { ctx ->
                    val webView = WebView(ctx)
                    webViewRef.value = webView
                    webView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webView.settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        builtInZoomControls = true
                        displayZoomControls = false
                        setSupportZoom(true)
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        mediaPlaybackRequiresUserGesture = false
                    }
                    webView.webViewClient = WebViewClientWithDownload(
                        context = ctx,
                        onPageStarted = { url -> viewModel.onPageStarted(url) },
                        onPageFinished = { url ->
                            viewModel.onPageFinished(url)
                            webAppRef.value?.let { app ->
                                if (app.isDarkModeEnabled) injectDarkMode(webView)
                            }
                            // Inject auto scroll functionality
                            webViewRef.value?.evaluateJavascript("""
                                javascript:(function() {
                                    window.waosAutoScroll = {
                                        interval: null,
                                        start: function(speed) {
                                            this.stop();
                                            this.interval = setInterval(function() {
                                                    window.scrollBy(0, speed);
                                                }, 50);
                                            },
                                            stop: function() {
                                                if (this.interval) {
                                                    clearInterval(this.interval);
                                                    this.interval = null;
                                                }
                                            }
                                        };
                                    })()
                                """.trimIndent(), null)
                                // Inject image long press functionality
                                webViewRef.value?.evaluateJavascript("""
                                    javascript:(function() {
                                        var longPressTimer;
                                        var longPressElement;
                                        
                                        function handleLongPress(element) {
                                            if (element.tagName === 'IMG') {
                                                var src = element.src;
                                                if (src) {
                                                    window.waosDownloadBlob && window.waosDownloadBlob.imageLongPress(src);
                                                }
                                            }
                                        }
                                        
                                        document.addEventListener('touchstart', function(e) {
                                            longPressTimer = setTimeout(function() {
                                                handleLongPress(e.target);
                                            }, 500);
                                        });
                                        
                                        document.addEventListener('touchend', function(e) {
                                            clearTimeout(longPressTimer);
                                        });
                                        
                                        document.addEventListener('touchmove', function(e) {
                                            clearTimeout(longPressTimer);
                                        });
                                    })()
                                """.trimIndent(), null)
                                // Inject link long press functionality
                                webViewRef.value?.evaluateJavascript("""
                                    javascript:(function() {
                                        if (window._waosLinkLongPressAttached) return;
                                        window._waosLinkLongPressAttached = true;
                                        var linkTimer;
                                        var touchMoved = false;
                                        document.addEventListener('touchstart', function(e) {
                                            touchMoved = false;
                                            var el = e.target;
                                            while (el && el.tagName !== 'A') el = el.parentElement;
                                            if (el && el.href) {
                                                var href = el.href;
                                                linkTimer = setTimeout(function() {
                                                    if (!touchMoved && window.waosDownloadBlob) {
                                                        window.waosDownloadBlob.linkLongPress(href);
                                                    }
                                                }, 600);
                                            }
                                        }, true);
                                        document.addEventListener('touchend', function() { clearTimeout(linkTimer); }, true);
                                        document.addEventListener('touchmove', function() { touchMoved = true; clearTimeout(linkTimer); }, true);
                                    })()
                                """.trimIndent(), null)
                                // Inject text selection callback
                                webViewRef.value?.evaluateJavascript("""
                                    javascript:(function() {
                                        if (window._waosTextSelAttached) return;
                                        window._waosTextSelAttached = true;
                                        var selTimer;
                                        document.addEventListener('selectionchange', function() {
                                            clearTimeout(selTimer);
                                            selTimer = setTimeout(function() {
                                                var sel = window.getSelection ? window.getSelection().toString().trim() : '';
                                                if (sel.length > 0 && window.waosDownloadBlob) {
                                                    window.waosDownloadBlob.textSelected(sel);
                                                }
                                            }, 300);
                                        });
                                    })()
                                """.trimIndent(), null)
                            },
                            onDownloadStart = { filename, downloadUrl ->
                                if (downloadUrl.startsWith("blob:")) {
                                    // Handle blob URL download
                                    webViewRef.value?.evaluateJavascript("""
                                        (function() {
                                            fetch('$downloadUrl')
                                                .then(response => response.blob())
                                                .then(blob => {
                                                    const reader = new FileReader();
                                                    reader.onload = function() {
                                                        const base64 = reader.result.split(',')[1];
                                                        window.waosDownloadBlob && window.waosDownloadBlob('$filename', base64);
                                                    };
                                                    reader.readAsDataURL(blob);
                                                })
                                                .catch(error => console.error('Blob download failed:', error));
                                        })();
                                    """.trimIndent(), null)
                                } else {
                                    viewModel.handleDownload(downloadUrl, webAppRef.value)
                                }
                            }
                        )
                    webView.setDownloadListener { url, _, _, _, _ ->
                        viewModel.handleDownload(url, webAppRef.value)
                    }
                    webView.webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                                msg?.let {
                                    viewModel.addConsoleMessage(
                                        ConsoleMessageData(
                                            message = it.message(),
                                            level = it.messageLevel().ordinal,
                                            sourceId = it.sourceId() ?: "",
                                            lineNumber = it.lineNumber()
                                        )
                                    )
                                }
                                return super.onConsoleMessage(msg)
                            }
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                viewModel.onProgressChanged(newProgress)
                            }
                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallback: ValueCallback<Array<Uri>>?,
                                fileChooserParams: WebChromeClient.FileChooserParams?
                            ): Boolean = true
                            override fun onPermissionRequest(request: PermissionRequest?) {
                                val app = webAppRef.value
                                val granted = request?.resources?.filter { resource ->
                                    when (resource) {
                                        PermissionRequest.RESOURCE_VIDEO_CAPTURE -> app?.isCameraPermission == true
                                        PermissionRequest.RESOURCE_AUDIO_CAPTURE -> app?.isMicrophonePermission == true
                                        else -> false
                                    }
                                }
                                if (!granted.isNullOrEmpty()) request?.grant(granted.toTypedArray())
                                else request?.deny()
                            }
                        }
                    webView.addJavascriptInterface(object {
                        @JavascriptInterface
                        fun downloadBlob(filename: String, base64Data: String) {
                            viewModel.handleBlobDownload(filename, base64Data, webAppRef.value)
                        }
                        @JavascriptInterface
                        fun imageLongPress(imageUrl: String) {
                            viewModel.handleImageLongPress(imageUrl, webAppRef.value)
                        }
                        @JavascriptInterface
                        fun linkLongPress(url: String) {
                            if (url.isNotBlank()) {
                                viewModel.handleLinkLongPress(url)
                            }
                        }
                        @JavascriptInterface
                        fun textSelected(text: String) {
                            if (text.isNotBlank()) {
                                viewModel.handleTextSelected(text)
                            }
                        }
                    }, "waosDownloadBlob")
                    webView
                },
                update = { webView ->
                    webAppRef.value = webApp
                    (webView.webViewClient as? WebViewClientWithDownload)?.adblockEnabled = isAdblockEnabled
                    webApp?.let { app ->
                        webView.settings.javaScriptEnabled = app.isJavaScriptEnabled
                        webView.settings.builtInZoomControls = app.isEnableZooming
                        webView.settings.setSupportZoom(true)
                        webView.settings.displayZoomControls = false
                        if (!app.userAgent.isNullOrBlank() && !isDesktopMode) {
                            webView.settings.userAgentString = app.userAgent
                        }
                    }
                    if (isDesktopMode) {
                        webView.settings.userAgentString =
                            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
                        webView.settings.useWideViewPort = true
                        webView.settings.loadWithOverviewMode = true
                        // Inject desktop mode enhancements
                        webView.evaluateJavascript("""
                            javascript:(function() {
                                var meta = document.querySelector('meta[name=viewport]');
                                if (!meta) {
                                    meta = document.createElement('meta');
                                    meta.name = 'viewport';
                                    document.head.appendChild(meta);
                                }
                                meta.content = 'width=device-width, initial-scale=0.5, maximum-scale=3.0, user-scalable=yes';
                                // Force desktop layout without breaking the page
                                var style = document.createElement('style');
                                style.textContent = 'body { min-width: 100vw !important; overflow-x: auto; } @media (max-width: 768px) { body { font-size: 14px; } }';
                                document.head.appendChild(style);
                            })()
                        """.trimIndent(), null)
                    } else {
                        webView.settings.useWideViewPort = true
                        webView.settings.loadWithOverviewMode = true
                    }
                    if (webViewState.shouldGoBack) { webView.goBack(); viewModel.clearActionFlags() }
                    if (webViewState.shouldGoForward) { webView.goForward(); viewModel.clearActionFlags() }
                    webViewState.shouldLoadUrl?.let { url -> webView.loadUrl(url); viewModel.clearActionFlags() }
                    if (webViewState.shouldReload) { webView.reload(); viewModel.clearActionFlags() }
                    if (webViewState.shouldRefresh) { webView.reload(); viewModel.clearRefreshFlag() }
                    webViewState.javaScriptToExecute?.let { js ->
                        webView.evaluateJavascript(js, null)
                        viewModel.clearJavaScriptCommand()
                    }
                    viewModel.updateNavigationState(webView.canGoBack(), webView.canGoForward())
                    webViewState.shouldShareUrl?.let { urlToShare ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, urlToShare)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share URL"))
                        viewModel.clearActionFlags()
                    }
                    webViewState.shouldCopyUrl?.let { urlToCopy ->
                        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("URL", urlToCopy))
                        viewModel.clearActionFlags()
                    }
                    if (webViewState.shouldPrint) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            val pm = context.getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
                            pm.print("WAOS Document", webView.createPrintDocumentAdapter("WAOS Print"), null)
                        }
                        viewModel.clearActionFlags()
                    }
                    if (webViewState.shouldZoomIn) { webView.zoomIn(); viewModel.clearActionFlags() }
                    if (webViewState.shouldZoomOut) { webView.zoomOut(); viewModel.clearActionFlags() }
                    if (webViewState.shouldTakeScreenshot) {
                        val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        webView.draw(canvas)
                        val appName = webApp?.name?.replace(Regex("[^a-zA-Z0-9]"), "_") ?: "App"
                        val filePath = ScreenshotUtil.saveScreenshot(context, appName, bitmap)
                        if (filePath != null) {
                            android.widget.Toast.makeText(context, "Screenshot saved to Downloads/WAOS/$appName/Screenshots/", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Failed to save screenshot", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        viewModel.clearActionFlags()
                    }
                    if (webViewState.shouldShowPageSource) {
                        webView.loadUrl("view-source:" + webView.url)
                        viewModel.clearActionFlags()
                    }
                    if (webViewState.shouldSavePage) {
                        val filename = "page_${System.currentTimeMillis()}.mht"
                        webView.saveWebArchive(filename)
                        viewModel.clearActionFlags()
                    }
                    // Reader mode, translate, adblock would need additional implementation
                    if (webViewState.shouldToggleReaderMode) {
                        viewModel.clearActionFlags()
                    }
                    if (webViewState.shouldTranslate) {
                        viewModel.clearActionFlags()
                    }
                    if (webViewState.shouldToggleAdblock) {
                        viewModel.clearActionFlags()
                    }
                    // WAOS Session Export: extract localStorage + sessionStorage via JS then save encrypted snapshot
                    if (webViewState.shouldExportSession) {
                        val currentUrl = webViewState.currentUrl.ifBlank { webApp?.url ?: "" }
                        val ua = webView.settings.userAgentString ?: ""
                        val app = webApp
                        if (app != null && currentUrl.isNotBlank()) {
                            webView.evaluateJavascript(SessionManager.buildLocalStorageExtractJs()) { lsJson ->
                                webView.evaluateJavascript(SessionManager.buildSessionStorageExtractJs()) { ssJson ->
                                    val sessionMgr = SessionManager(context, app.id, app.name)
                                    val lsClean = lsJson?.trim('"')?.replace("\\\"", "\"") ?: "{}"
                                    val ssClean = ssJson?.trim('"')?.replace("\\\"", "\"") ?: "{}"
                                    val snapshot = sessionMgr.buildSessionSnapshot(currentUrl, ua, lsClean, ssClean)
                                    sessionMgr.saveLastSessionSnapshot(snapshot)
                                    val path = sessionMgr.exportSession(snapshot)
                                    viewModel.onSessionExportComplete(path)
                                }
                            }
                        } else {
                            viewModel.onSessionExportComplete(null)
                        }
                    }
                    // WAOS Session Import: inject localStorage + sessionStorage restore JS after page load
                    webViewState.shouldImportSession?.let { restoreData ->
                        if (!webViewState.isLoading) {
                            webView.evaluateJavascript(restoreData.localStorageRestoreJs, null)
                            webView.evaluateJavascript(restoreData.sessionStorageRestoreJs, null)
                            viewModel.onSessionImportApplied()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (webViewState.isLoading && webViewState.progress < 100) {
                LinearProgressIndicator(
                    progress = { webViewState.progress / 100f },
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
                    color = CyanPrimary,
                    trackColor = CyanPrimary.copy(alpha = 0.15f)
                )
            }

            Column(modifier = Modifier.align(Alignment.TopCenter)) {
                AnimatedVisibility(
                    visible = showFindBar,
                    enter = slideInVertically { height -> -height } + fadeIn(),
                    exit = slideOutVertically { height -> -height } + fadeOut()
                ) {
                    WaosFindBar(
                        query = findQuery,
                        onQueryChange = { q ->
                            findQuery = q
                            webViewRef.value?.findAllAsync(q)
                        },
                        onFindNext = { webViewRef.value?.findNext(true) },
                        onFindPrev = { webViewRef.value?.findNext(false) },
                        onClose = {
                            showFindBar = false
                            findQuery = ""
                            webViewRef.value?.clearMatches()
                        }
                    )
                }
            }

            webViewState.error?.let { error ->
                WaosErrorPanel(error = error, onRetry = { viewModel.refresh() })
            }
        }

        if (showConsole) {
            WaosConsolePanel(
                messages = consoleMessages,
                onExecuteCommand = { viewModel.executeJavaScript(it) },
                onClearLogs = { viewModel.clearConsoleMessages() },
                modifier = Modifier.height(280.dp)
            )
        }

        WaosBottomBar(
            canGoBack = webViewState.canGoBack,
            canGoForward = webViewState.canGoForward,
            isDesktopMode = isDesktopMode,
            isAdblockEnabled = isAdblockEnabled,
            isAutoScrollEnabled = isAutoScrollEnabled,
            isAutoClickEnabled = isAutoClickEnabled,
            developerModeEnabled = developerModeEnabled,
            showConsole = showConsole,
            floatingWindowsEnabled = floatingWindowsEnabled,
            showMoreMenu = showMoreMenu,
            onMoreMenuToggle = { showMoreMenu = !showMoreMenu },
            onMoreMenuDismiss = { showMoreMenu = false },
            onGoBack = { viewModel.goBack() },
            onGoForward = { viewModel.goForward() },
            onHome = { viewModel.goHome() },
            onRefresh = { viewModel.refresh() },
            onFind = { showFindBar = !showFindBar },
            onDesktop = { viewModel.toggleDesktopMode() },
            onAdblock = { viewModel.toggleAdblock() },
            onAutoScroll = { viewModel.toggleAutoScroll(autoScrollSpeed) },
            onAutoClick = { viewModel.toggleAutoClick() },
            onConsole = { viewModel.toggleConsole() },
            onShare = { viewModel.sharePage() },
            onCopyUrl = { viewModel.copyUrl() },
            onOpenBrowser = {
                val currentUrl = webViewState.currentUrl.ifBlank { webApp?.url ?: return@WaosBottomBar }
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl)))
            },
            onFloat = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(context)) {
                    // Request overlay permission
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                    context.startActivity(intent)
                } else {
                    webApp?.let { app ->
                        val intent = Intent(context, com.cylonid.nativealpha.service.FloatingWindowService::class.java).apply {
                            action = com.cylonid.nativealpha.service.FloatingWindowService.ACTION_ADD_WINDOW
                            putExtra("webAppId", app.id)
                            putExtra("webAppUrl", app.url)
                            putExtra("webAppName", app.name)
                        }
                        context.startService(intent)
                    }
                }
            },
            onCredentials = { viewModel.openCredentialKeeper() },
            onClipboard = { viewModel.openClipboardManager() },
            onDownloads = { viewModel.openDownloadHistory() },
            onScreenshot = { viewModel.takeScreenshot() },
            onZoomIn = { viewModel.zoomIn() },
            onZoomOut = { viewModel.zoomOut() },
            onPrint = { viewModel.printPage() },
            onHistory = { /* TODO: Implement history */ },
            onPageSource = { viewModel.showPageSource() },
            onSavePage = { viewModel.savePage() },
            onReaderMode = { viewModel.toggleReaderMode() },
            onTranslate = { viewModel.translate() },
            onAdBlockSettings = { viewModel.toggleAdblockSettings() },
            onExportSession = { viewModel.requestSessionExport() },
            onImportSession = {
                // Import the most recently exported session for this app (self-import / transfer)
                val app = webApp
                if (app != null) {
                    val sessionDir = java.io.File(context.filesDir, "waos_sessions/${app.id}")
                    val latestExport = sessionDir.listFiles()
                        ?.filter { it.name.endsWith(".waos") && it.name != "last_session.waos" }
                        ?.maxByOrNull { it.lastModified() }
                    if (latestExport != null) {
                        viewModel.requestSessionImport(latestExport.absolutePath, context)
                    } else {
                        android.widget.Toast.makeText(context, "No exported session found. Export first.", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
private fun WaosTopBar(
    title: String,
    url: String,
    isLoading: Boolean,
    progress: Int,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark)
            .border(width = 1.dp, color = CardBorder, shape = RoundedCornerShape(0.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (url.isNotBlank()) {
                    Text(
                        text = url.replace("https://", "").replace("http://", ""),
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (isLoading) {
                Text(
                    text = "$progress%",
                    color = CyanPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(StatusActive, androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaosBottomBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    isDesktopMode: Boolean,
    isAdblockEnabled: Boolean,
    isAutoScrollEnabled: Boolean,
    isAutoClickEnabled: Boolean,
    developerModeEnabled: Boolean,
    showConsole: Boolean,
    floatingWindowsEnabled: Boolean,
    showMoreMenu: Boolean,
    onMoreMenuToggle: () -> Unit,
    onMoreMenuDismiss: () -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onHome: () -> Unit,
    onRefresh: () -> Unit,
    onFind: () -> Unit,
    onDesktop: () -> Unit,
    onAdblock: () -> Unit,
    onAutoScroll: () -> Unit,
    onAutoClick: () -> Unit,
    onConsole: () -> Unit,
    onShare: () -> Unit,
    onCopyUrl: () -> Unit,
    onOpenBrowser: () -> Unit,
    onFloat: () -> Unit,
    onCredentials: () -> Unit,
    onClipboard: () -> Unit,
    onDownloads: () -> Unit,
    onScreenshot: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onPrint: () -> Unit,
    onHistory: () -> Unit,
    onPageSource: () -> Unit,
    onSavePage: () -> Unit,
    onReaderMode: () -> Unit,
    onTranslate: () -> Unit,
    onAdBlockSettings: () -> Unit,
    onExportSession: () -> Unit,
    onImportSession: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark)
            .border(width = 1.dp, color = CardBorder, RoundedCornerShape(0.dp))
            .navigationBarsPadding()
    ) {
        Divider(color = CardBorder, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WaosToolbarBtn(onClick = onGoBack, enabled = canGoBack) {
                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarBtn(onClick = onGoForward, enabled = canGoForward) {
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarBtn(onClick = onHome) {
                Icon(Icons.Default.Home, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarBtn(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarBtn(onClick = onFind) {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarDivider()
            WaosToolbarBtn(onClick = onDesktop, active = isDesktopMode, activeColor = CyanPrimary) {
                Icon(if (isDesktopMode) Icons.Default.Computer else Icons.Default.Smartphone, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarBtn(onClick = onAdblock, active = isAdblockEnabled, activeColor = StatusActive) {
                Icon(Icons.Default.Block, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarBtn(onClick = onAutoScroll, active = isAutoScrollEnabled, activeColor = CyanPrimary) {
                Icon(if (isAutoScrollEnabled) Icons.Default.PlayArrow else Icons.Default.Pause, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarBtn(onClick = onAutoClick, active = isAutoClickEnabled, activeColor = VioletSecondary) {
                Icon(if (isAutoClickEnabled) Icons.Default.TouchApp else Icons.Default.DoNotTouch, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarDivider()
            WaosToolbarBtn(onClick = onZoomIn) {
                Icon(Icons.Default.ZoomIn, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarBtn(onClick = onZoomOut) {
                Icon(Icons.Default.ZoomOut, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarDivider()
            WaosToolbarBtn(onClick = onShare) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarBtn(onClick = onCopyUrl) {
                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarBtn(onClick = onOpenBrowser) {
                Icon(Icons.Default.Launch, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarDivider()
            WaosToolbarBtn(onClick = onCredentials) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarBtn(onClick = onClipboard) {
                Icon(Icons.Default.ContentPaste, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarBtn(onClick = onDownloads) {
                Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
            }
            WaosToolbarBtn(onClick = onScreenshot) {
                Icon(Icons.Default.Camera, null, modifier = Modifier.size(18.dp))
            }
            if (developerModeEnabled) {
                WaosToolbarBtn(onClick = onConsole, active = showConsole, activeColor = CyanPrimary) {
                    Icon(if (showConsole) Icons.Default.CodeOff else Icons.Default.Code, null, modifier = Modifier.size(18.dp))
                }
            }
            if (floatingWindowsEnabled) {
                WaosToolbarBtn(onClick = onFloat) {
                    Icon(Icons.Default.Launch, null, modifier = Modifier.size(18.dp))
                }
            }
            WaosToolbarBtn(onClick = onMoreMenuToggle) {
                Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(18.dp))
            }
        }

        DropdownMenu(
            expanded = showMoreMenu,
            onDismissRequest = onMoreMenuDismiss,
            modifier = Modifier.background(CardSurface)
        ) {
            DropdownMenuItem(
                text = { Text("Print Page", color = TextPrimary) },
                onClick = { onPrint(); onMoreMenuDismiss() },
                leadingIcon = { Icon(Icons.Default.Print, null, tint = TextSecondary) }
            )
            DropdownMenuItem(
                text = { Text("Page History", color = TextPrimary) },
                onClick = { onHistory(); onMoreMenuDismiss() },
                leadingIcon = { Icon(Icons.Default.History, null, tint = TextSecondary) }
            )
            DropdownMenuItem(
                text = { Text("Page Source", color = TextPrimary) },
                onClick = { onPageSource(); onMoreMenuDismiss() },
                leadingIcon = { Icon(Icons.Default.Code, null, tint = TextSecondary) }
            )
            DropdownMenuItem(
                text = { Text("Save Page", color = TextPrimary) },
                onClick = { onSavePage(); onMoreMenuDismiss() },
                leadingIcon = { Icon(Icons.Default.Save, null, tint = TextSecondary) }
            )
            DropdownMenuItem(
                text = { Text("Reader Mode", color = TextPrimary) },
                onClick = { onReaderMode(); onMoreMenuDismiss() },
                leadingIcon = { Icon(Icons.Default.Description, null, tint = TextSecondary) }
            )
            DropdownMenuItem(
                text = { Text("Translate", color = TextPrimary) },
                onClick = { onTranslate(); onMoreMenuDismiss() },
                leadingIcon = { Icon(Icons.Default.Language, null, tint = TextSecondary) }
            )
            DropdownMenuItem(
                text = { Text("AdBlock Settings", color = TextPrimary) },
                onClick = { onAdBlockSettings(); onMoreMenuDismiss() },
                leadingIcon = { Icon(Icons.Default.Block, null, tint = TextSecondary) }
            )
            Divider(color = CardBorder, thickness = 1.dp)
            DropdownMenuItem(
                text = { Text("Export Session", color = TextPrimary) },
                onClick = { onExportSession(); onMoreMenuDismiss() },
                leadingIcon = { Icon(Icons.Default.Save, null, tint = CyanPrimary) }
            )
            DropdownMenuItem(
                text = { Text("Import Session", color = TextPrimary) },
                onClick = { onImportSession(); onMoreMenuDismiss() },
                leadingIcon = { Icon(Icons.Default.ContentPaste, null, tint = VioletSecondary) }
            )
        }
    }
}

@Composable
private fun WaosToolbarBtn(
    onClick: () -> Unit,
    enabled: Boolean = true,
    active: Boolean = false,
    activeColor: Color = CyanPrimary,
    content: @Composable () -> Unit
) {
    val tint = when {
        !enabled -> TextMuted.copy(alpha = 0.3f)
        active -> activeColor
        else -> TextSecondary
    }
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(40.dp)
            .then(
                if (active) Modifier.background(activeColor.copy(0.12f), RoundedCornerShape(8.dp))
                else Modifier
            )
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides tint
        ) {
            content()
        }
    }
}

@Composable
private fun WaosToolbarDivider() {
    Box(
        modifier = Modifier
            .height(20.dp)
            .width(1.dp)
            .background(CardBorder)
            .padding(horizontal = 4.dp)
    )
}

@Composable
private fun WaosFindBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFindNext: () -> Unit,
    onFindPrev: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(0.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Find in page…", color = TextMuted, fontSize = 13.sp) },
            modifier = Modifier.weight(1f).height(48.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 14.sp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = CardBorder,
                cursorColor = CyanPrimary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        IconButton(onClick = onFindPrev, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.KeyboardArrowUp, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onFindNext, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun WaosConsolePanel(
    messages: List<ConsoleMessageData>,
    onExecuteCommand: (String) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var command by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = modifier
            .background(Color(0xFF060A12))
            .border(1.dp, CardBorder, RoundedCornerShape(0.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1421))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(CyanPrimary, androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(Modifier.width(6.dp))
                Text("JS Console", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                if (messages.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(CyanPrimary.copy(0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text("${messages.size}", color = CyanPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        val allLogs = messages.joinToString("\n") { msg ->
                            val lvl = when (msg.level) {
                                ConsoleMessage.MessageLevel.ERROR.ordinal -> "ERR"
                                ConsoleMessage.MessageLevel.WARNING.ordinal -> "WRN"
                                else -> "LOG"
                            }
                            "[$lvl] ${msg.sourceId}:${msg.lineNumber} ${msg.message}"
                        }
                        val cm = context.getSystemService(android.content.ClipboardManager::class.java)
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("Console", allLogs))
                        android.widget.Toast.makeText(context, "Logs copied", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                }
                IconButton(
                    onClick = onClearLogs,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = ErrorRed.copy(0.8f), modifier = Modifier.size(14.dp))
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(6.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(messages) { message ->
                val levelName = when (message.level) {
                    ConsoleMessage.MessageLevel.ERROR.ordinal -> "ERR"
                    ConsoleMessage.MessageLevel.WARNING.ordinal -> "WRN"
                    else -> "LOG"
                }
                val bgColor = when (message.level) {
                    ConsoleMessage.MessageLevel.ERROR.ordinal -> ErrorRed.copy(0.07f)
                    ConsoleMessage.MessageLevel.WARNING.ordinal -> Color(0xFFFFB800).copy(0.07f)
                    else -> Color.Transparent
                }
                val textColor = when (message.level) {
                    ConsoleMessage.MessageLevel.ERROR.ordinal -> ErrorRed
                    ConsoleMessage.MessageLevel.WARNING.ordinal -> Color(0xFFFFB800)
                    else -> Color(0xFFAABBCC)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor, RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "[$levelName]",
                        color = textColor.copy(0.6f),
                        fontSize = 9.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        message.message,
                        color = textColor,
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        androidx.compose.material3.HorizontalDivider(color = CardBorder.copy(0.5f), thickness = 1.dp)

        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                placeholder = { Text("// Enter JS snippet…", color = TextMuted, fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                minLines = 1,
                maxLines = 4,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = CardBorder.copy(0.5f),
                    focusedContainerColor = Color(0xFF0A1020),
                    unfocusedContainerColor = Color(0xFF0A1020),
                    cursorColor = CyanPrimary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(Modifier.width(6.dp))
            IconButton(
                onClick = {
                    if (command.isNotBlank()) { onExecuteCommand(command); command = "" }
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(listOf(GradCyanStart, GradCyanEnd)),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun WaosErrorPanel(error: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardSurface)
                .border(1.dp, ErrorRed.copy(0.3f), RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(ErrorRed.copy(0.15f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Error, null, tint = ErrorRed, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Failed to Load", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(error, color = TextSecondary, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Icon(Icons.Default.Refresh, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun injectDarkMode(webView: WebView) {
    webView.evaluateJavascript(
        """
        (function() {
            const style = document.createElement('style');
            style.textContent = `
                html { filter: invert(1) hue-rotate(180deg); }
                img, video, canvas, svg { filter: invert(1) hue-rotate(180deg); }
            `;
            document.head.appendChild(style);
        })();
        """.trimIndent(),
        null
    )
}
