package com.cylonid.nativealpha.ui.screens

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.cylonid.nativealpha.manager.FileViewerManager
import com.cylonid.nativealpha.ui.theme.*
import com.github.barteksc.pdfviewer.PDFView
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalFileViewerScreen(
    filePath: String,
    onBackPressed: () -> Unit,
    viewModel: UniversalFileViewerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val initialFile = remember(filePath) { File(filePath) }
    val folderFiles = remember(filePath) { getFolderFiles(initialFile) }
    var activeFile by remember(filePath) { mutableStateOf(initialFile) }
    var activeFileIndex by remember(filePath) {
        mutableStateOf(
            folderFiles.indexOfFirst { it.absolutePath == initialFile.absolutePath }.coerceAtLeast(0)
        )
    }
    var darkMode by remember { mutableStateOf(false) }
    var showInfoPanel by remember { mutableStateOf(true) }
    val mimeType = getMimeType(activeFile.extension)

    LaunchedEffect(activeFile) {
        viewModel.loadFile(activeFile)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color.White.copy(alpha = 0.95f))
                    .border(1.dp, Color.Black.copy(0.1f))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackPressed,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(0.1f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.Black.copy(0.2f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Rounded.ArrowBack, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                activeFile.name,
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${activeFileIndex + 1} of ${folderFiles.size}  •  ${
                                    getMimeType(activeFile.extension)?.substringAfter("/")?.uppercase() ?: activeFile.extension.uppercase()
                                }",
                                color = Color.Black.copy(0.6f),
                                fontSize = 10.sp
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                if (activeFileIndex > 0) {
                                    activeFileIndex -= 1
                                    activeFile = folderFiles[activeFileIndex]
                                }
                            },
                            enabled = activeFileIndex > 0,
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (activeFileIndex > 0) Color.Black.copy(0.1f) else Color.Black.copy(0.05f),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(1.dp, Color.Black.copy(0.2f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                Icons.Rounded.SkipPrevious,
                                null,
                                tint = if (activeFileIndex > 0) Color.Black else Color.Black.copy(0.3f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                if (activeFileIndex < folderFiles.lastIndex) {
                                    activeFileIndex += 1
                                    activeFile = folderFiles[activeFileIndex]
                                }
                            },
                            enabled = activeFileIndex < folderFiles.lastIndex,
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (activeFileIndex < folderFiles.lastIndex) Color.Black.copy(0.1f) else Color.Black.copy(0.05f),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(1.dp, Color.Black.copy(0.2f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                Icons.Rounded.SkipNext,
                                null,
                                tint = if (activeFileIndex < folderFiles.lastIndex) Color.Black else Color.Black.copy(0.3f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                val uri = FileProvider.getUriForFile(
                                    context, "${context.packageName}.fileprovider", activeFile
                                )
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = mimeType ?: "*/*"
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share file"))
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black.copy(0.1f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color.Black.copy(0.2f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Rounded.Share, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = { FileViewerManager(context).openWithExternalApp(activeFile, mimeType) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black.copy(0.1f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color.Black.copy(0.2f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Rounded.OpenInNew, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("File Path", activeFile.absolutePath)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "Path copied", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black.copy(0.1f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color.Black.copy(0.2f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Rounded.ContentCopy, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                viewModel.duplicateFile(activeFile)?.let {
                                    android.widget.Toast.makeText(context, "Duplicated: ${it.name}", android.widget.Toast.LENGTH_SHORT).show()
                                } ?: android.widget.Toast.makeText(context, "Failed to duplicate", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black.copy(0.1f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color.Black.copy(0.2f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Rounded.FileCopy, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = { darkMode = !darkMode },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (darkMode) Color.Black.copy(0.2f) else Color.Black.copy(0.1f),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(1.dp, Color.Black.copy(0.2f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                if (darkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = { showInfoPanel = !showInfoPanel },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (showInfoPanel) Color.Black.copy(0.2f) else Color.Black.copy(0.1f),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(1.dp, Color.Black.copy(0.2f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                Icons.Rounded.Info,
                                null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = Color.Black.copy(0.1f))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (darkMode) Color.Black else BgDeep)
            ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    mimeType == "image/svg+xml" -> {
                        SvgViewer(file = activeFile)
                    }
                    mimeType?.startsWith("image/") == true -> {
                        ImageViewer(
                            file = activeFile,
                            folderFiles = folderFiles,
                            onOpenPrevious = {
                                if (activeFileIndex > 0) {
                                    activeFileIndex -= 1
                                    activeFile = folderFiles[activeFileIndex]
                                }
                            },
                            onOpenNext = {
                                if (activeFileIndex < folderFiles.lastIndex) {
                                    activeFileIndex += 1
                                    activeFile = folderFiles[activeFileIndex]
                                }
                            }
                        )
                    }
                    mimeType?.startsWith("video/") == true -> {
                        VideoViewer(
                            file = activeFile,
                            folderFiles = folderFiles,
                            onOpenPrevious = {
                                if (activeFileIndex > 0) {
                                    activeFileIndex -= 1
                                    activeFile = folderFiles[activeFileIndex]
                                }
                            },
                            onOpenNext = {
                                if (activeFileIndex < folderFiles.lastIndex) {
                                    activeFileIndex += 1
                                    activeFile = folderFiles[activeFileIndex]
                                }
                            },
                            viewModel = viewModel
                        )
                    }
                    mimeType?.startsWith("audio/") == true -> {
                        AudioPlayer(
                            file = activeFile,
                            folderFiles = folderFiles,
                            onOpenPrevious = {
                                if (activeFileIndex > 0) {
                                    activeFileIndex -= 1
                                    activeFile = folderFiles[activeFileIndex]
                                }
                            },
                            onOpenNext = {
                                if (activeFileIndex < folderFiles.lastIndex) {
                                    activeFileIndex += 1
                                    activeFile = folderFiles[activeFileIndex]
                                }
                            },
                            viewModel = viewModel
                        )
                    }
                    mimeType == "application/pdf" -> {
                        PdfViewer(file = activeFile)
                    }
                    mimeType == "text/html" || mimeType == "multipart/related" || activeFile.extension.lowercase() == "htm" -> {
                        HtmlViewer(file = activeFile)
                    }
                    mimeType?.startsWith("text/") == true || activeFile.extension.lowercase() in listOf("txt", "log", "csv", "json", "xml", "md") -> {
                        TextViewer(file = activeFile)
                    }
                    mimeType == "application/zip" || mimeType == "application/x-rar-compressed" || mimeType == "application/x-7z-compressed" || mimeType == "application/x-tar" -> {
                        ArchiveViewer(file = activeFile)
                    }
                    mimeType == "application/vnd.android.package-archive" -> {
                        ApkInfoViewer(file = activeFile)
                    }
                    else -> {
                        UnsupportedFileViewer(activeFile, mimeType)
                    }
                }

                if (showInfoPanel) {
                    FileInfoPanel(
                        file = activeFile,
                        mimeType = mimeType,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
            }
        }
    }
}

@Composable
fun UnsupportedFileViewer(file: File, mimeType: String?) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.InsertDriveFile,
            contentDescription = "File",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Cannot preview this file type internally",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "File: ${file.name}\nType: ${mimeType ?: "Unknown"}\nSize: ${formatFileSize(file.length())}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            try {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val openIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType ?: "*/*")
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(openIntent)
            } catch (e: Exception) { e.printStackTrace() }
        }) {
            Text("Open with External App")
        }
    }
}

@Composable
fun FileInfoPanel(file: File, mimeType: String?, modifier: Modifier = Modifier) {
    val permissions = remember(file) { getFilePermissions(file) }
    val exifData = remember(file) { if (mimeType?.startsWith("image/") == true) getImageExifData(file) else emptyMap() }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "Info")
                Spacer(modifier = Modifier.width(8.dp))
                Text("File Info", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Name: ${file.name}", style = MaterialTheme.typography.bodyMedium)
            Text("Type: ${mimeType ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
            Text("Size: ${formatFileSize(file.length())}", style = MaterialTheme.typography.bodySmall)
            Text("Modified: ${formatModifiedDate(file.lastModified())}", style = MaterialTheme.typography.bodySmall)
            Text("Path: ${file.absolutePath}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Permissions: $permissions", style = MaterialTheme.typography.bodySmall)

            if (exifData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("EXIF metadata", style = MaterialTheme.typography.titleSmall)
                exifData.forEach { (key, value) ->
                    Text("$key: $value", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun getFolderFiles(file: File): List<File> {
    return file.parentFile?.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase(Locale.getDefault()) }))?.toList()
        ?: emptyList()
}

private fun getFilePermissions(file: File): String {
    return buildString {
        append(if (file.canRead()) "r" else "-")
        append(if (file.canWrite()) "w" else "-")
        append(if (file.canExecute()) "x" else "-")
    }
}

private fun getImageExifData(file: File): Map<String, String> {
    return try {
        ExifInterface(file.absolutePath).run {
            listOf(
                ExifInterface.TAG_DATETIME_ORIGINAL,
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LONGITUDE
            ).mapNotNull { tag ->
                getAttribute(tag)?.let { value ->
                    tag to value
                }
            }.toMap()
        }
    } catch (e: Exception) {
        emptyMap()
    }
}

private fun createColorMatrix(filterMode: String, brightness: Float, contrast: Float): ColorMatrix {
    val baseMatrix = floatArrayOf(
        contrast, 0f, 0f, 0f, brightness * 255f,
        0f, contrast, 0f, 0f, brightness * 255f,
        0f, 0f, contrast, 0f, brightness * 255f,
        0f, 0f, 0f, 1f, 0f
    )

    val matrix = when (filterMode) {
        "Grayscale" -> {
            val gray = ColorMatrix().apply { setToSaturation(0f) }
            gray
        }
        "Sepia" -> ColorMatrix(floatArrayOf(
            0.393f * contrast, 0.769f * contrast, 0.189f * contrast, 0f, brightness * 255f,
            0.349f * contrast, 0.686f * contrast, 0.168f * contrast, 0f, brightness * 255f,
            0.272f * contrast, 0.534f * contrast, 0.131f * contrast, 0f, brightness * 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        else -> ColorMatrix(baseMatrix)
    }

    return matrix
}

@Composable
fun ImageViewer(
    file: File,
    folderFiles: List<File>,
    onOpenPrevious: () -> Unit,
    onOpenNext: () -> Unit
) {
    val bitmap = remember(file) { BitmapFactory.decodeFile(file.absolutePath) }
    val exifData = remember(file) { getImageExifData(file) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableStateOf(0f) }
    var brightness by remember { mutableStateOf(0f) }
    var contrast by remember { mutableStateOf(1f) }
    var filterMode by remember { mutableStateOf("Normal") }
    var slideshowActive by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(false) } // Collapsed by default

    LaunchedEffect(slideshowActive) {
        if (slideshowActive && folderFiles.size > 1) {
            while (isActive) {
                delay(3500)
                onOpenNext()
            }
        }
    }

    if (bitmap == null) {
        BrokenImageViewer()
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Image",
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 6f)
                        offset += pan
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2f
                        offset = Offset.Zero
                    })
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                    rotationZ = rotation
                ),
            colorFilter = ColorFilter.colorMatrix(createColorMatrix(filterMode, brightness, contrast))
        )

        // Top controls
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = onOpenPrevious,
                modifier = Modifier.size(44.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
            }

            FloatingActionButton(
                onClick = onOpenNext,
                modifier = Modifier.size(44.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next")
            }

            FloatingActionButton(
                onClick = { slideshowActive = !slideshowActive },
                modifier = Modifier.size(44.dp),
                containerColor = if (slideshowActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
            ) {
                Icon(Icons.Default.Slideshow, contentDescription = "Slideshow")
            }

            // Toggle controls button
            FloatingActionButton(
                onClick = { showControls = !showControls },
                modifier = Modifier.size(44.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
            ) {
                Icon(
                    if (showControls) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle controls"
                )
            }
        }

        // Bottom controls - collapsible
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { rotation = (rotation + 90f) % 360f },
                        icon = { Icon(Icons.Default.ScreenRotation, contentDescription = "Rotate") },
                        text = { Text("Rotate") }
                    )
                    ExtendedFloatingActionButton(
                        onClick = { filterMode = when (filterMode) {
                            "Normal" -> "Grayscale"
                            "Grayscale" -> "Sepia"
                            else -> "Normal"
                        } },
                        icon = { Icon(Icons.Default.Tune, contentDescription = "Filter") },
                        text = { Text(filterMode) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Brightness", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = brightness,
                        onValueChange = { brightness = it },
                        valueRange = -0.5f..0.5f,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Contrast", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = contrast,
                        onValueChange = { contrast = it },
                        valueRange = 0.5f..2f,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (exifData.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("EXIF: ${exifData.entries.joinToString { "${it.key}=${it.value}" }}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun BrokenImageViewer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.BrokenImage,
            contentDescription = "Broken image",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Failed to load image",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun SvgViewer(file: File) {
    val svgContent = remember(file) {
        try {
            file.readText()
        } catch (e: Exception) {
            "<svg><text x=\"10\" y=\"20\" fill=\"red\">Failed to load SVG</text></svg>"
        }
    }

    AndroidView(
        factory = { ctx ->
            android.webkit.WebView(ctx).apply {
                settings.javaScriptEnabled = false
                settings.domStorageEnabled = false
                loadDataWithBaseURL(null, svgContent, "image/svg+xml", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun VideoViewer(
    file: File,
    folderFiles: List<File>,
    onOpenPrevious: () -> Unit,
    onOpenNext: () -> Unit,
    viewModel: UniversalFileViewerViewModel
) {
    val context = LocalContext.current
    var videoView by remember { mutableStateOf<android.widget.VideoView?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(1) }
    var playbackSpeed by remember { mutableStateOf(1f) }
    var screenshotMessage by remember { mutableStateOf<String?>(null) }
    val savedPosition = remember(file) { viewModel.getSavedPosition(file) }

    LaunchedEffect(videoView, isPlaying) {
        if (isPlaying) {
            videoView?.start()
        } else {
            videoView?.pause()
        }
    }

    LaunchedEffect(videoView) {
        while (isActive) {
            delay(250)
            val position = videoView?.currentPosition ?: 0
            if (position != currentPosition) currentPosition = position
            duration = videoView?.duration ?: duration
        }
    }

    DisposableEffect(videoView) {
        onDispose {
            if (currentPosition > 0) {
                viewModel.savePosition(file, currentPosition)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                android.widget.VideoView(ctx).apply {
                    videoView = this
                    setVideoURI(file.toUri())
                    setOnPreparedListener { mp ->
                        duration = mp.duration
                        mp.isLooping = false
                        if (savedPosition > 0 && savedPosition < duration) {
                            seekTo(savedPosition)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mp.playbackParams = mp.playbackParams.setSpeed(playbackSpeed)
                        }
                        start()
                        isPlaying = true
                    }
                    setOnCompletionListener {
                        isPlaying = false
                    }
                }
            },
            update = { view ->
                if (isPlaying && !view.isPlaying) view.start()
                if (!isPlaying && view.isPlaying) view.pause()
                // Playback speed not supported for VideoView
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (folderFiles.isNotEmpty()) onOpenPrevious() }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous file")
                    }
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause"
                        )
                    }
                    IconButton(onClick = { if (folderFiles.isNotEmpty()) onOpenNext() }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next file")
                    }
                }
                Text(formatDuration(currentPosition), style = MaterialTheme.typography.bodySmall)
            }

            Slider(
                value = currentPosition.toFloat().coerceIn(0f, duration.toFloat()),
                onValueChange = {
                    currentPosition = it.toInt()
                    videoView?.seekTo(currentPosition)
                },
                valueRange = 0f..duration.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Speed", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = playbackSpeed,
                    onValueChange = { playbackSpeed = it },
                    valueRange = 0.5f..2f,
                    modifier = Modifier.weight(1f)
                )
                Text(String.format("%.1fx", playbackSpeed), style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    captureVideoFrame(context, file, currentPosition)?.let {
                        screenshotMessage = "Saved screenshot to ${it.absolutePath}"
                    }
                }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Screenshot")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Capture")
                }
            }

            screenshotMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(message, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun captureVideoFrame(context: Context, file: File, positionMs: Int): File? {
    return try {
        val retriever = MediaMetadataRetriever().apply { setDataSource(file.absolutePath) }
        val frame = retriever.getFrameAtTime(positionMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        retriever.release()
        frame?.let {
            val output = File(context.cacheDir, "video_frame_${file.nameWithoutExtension}_${positionMs}.png")
            output.outputStream().use { stream -> it.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, stream) }
            output
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun AudioPlayer(
    file: File,
    folderFiles: List<File>,
    onOpenPrevious: () -> Unit,
    onOpenNext: () -> Unit,
    viewModel: UniversalFileViewerViewModel
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var duration by remember { mutableStateOf(0) }
    var currentPosition by remember { mutableStateOf(0) }
    var shuffle by remember { mutableStateOf(false) }
    var repeatOne by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(false) } // Hidden by default
    val metadata = remember(file) { loadAudioMetadata(file) }
    val albumArt = remember(file) { loadAudioAlbumArt(file) }

    DisposableEffect(file) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, file.toUri())
            prepare()
            duration = this.duration
            setOnCompletionListener {
                isPlaying = false
                if (repeatOne) {
                    seekTo(0)
                    start()
                    isPlaying = true
                }
            }
        }

        onDispose {
            viewModel.savePosition(file, currentPosition)
            mediaPlayer?.release()
        }
    }

    LaunchedEffect(mediaPlayer, isPlaying) {
        while (isActive) {
            delay(200)
            currentPosition = mediaPlayer?.currentPosition ?: 0
            if (mediaPlayer?.isPlaying == true && !isPlaying) {
                isPlaying = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Album art or background
        if (albumArt != null) {
            Image(
                bitmap = albumArt.asImageBitmap(),
                contentDescription = "Album art",
                modifier = Modifier.fillMaxSize(),
                alpha = 0.3f
            )
        }

        // Tap to show/hide controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { showControls = !showControls }
                }
        )

        // Controls overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (albumArt != null) {
                        Image(
                            bitmap = albumArt.asImageBitmap(),
                            contentDescription = "Album art",
                            modifier = Modifier.size(64.dp)
                        )
                    } else {
                        Icon(Icons.Default.MusicNote, contentDescription = "Audio", modifier = Modifier.size(52.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text(metadata.title ?: file.nameWithoutExtension, style = MaterialTheme.typography.titleMedium)
                        Text(metadata.album ?: metadata.artist ?: "Audio file", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onOpenPrevious,
                        modifier = Modifier
                            .size(48.dp)
                            .background(CardSurface.copy(0.8f), RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    ) { Icon(Icons.Default.SkipPrevious, contentDescription = "Previous track", tint = TextPrimary) }
                    IconButton(
                        onClick = {
                            if (isPlaying) mediaPlayer?.pause() else mediaPlayer?.start()
                            isPlaying = !isPlaying
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(CyanPrimary, RoundedCornerShape(16.dp))
                    ) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play/Pause", tint = Color.Black) }
                    IconButton(
                        onClick = onOpenNext,
                        modifier = Modifier
                            .size(48.dp)
                            .background(CardSurface.copy(0.8f), RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    ) { Icon(Icons.Default.SkipNext, contentDescription = "Next track", tint = TextPrimary) }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { mediaPlayer?.seekTo((currentPosition - 10000).coerceAtLeast(0)) },
                        modifier = Modifier
                            .size(44.dp)
                            .background(CardSurface.copy(0.8f), RoundedCornerShape(10.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.Replay10, contentDescription = "Back 10 seconds", tint = TextPrimary)
                    }
                    IconButton(
                        onClick = { mediaPlayer?.seekTo((currentPosition + 10000).coerceAtMost(duration)) },
                        modifier = Modifier
                            .size(44.dp)
                            .background(CardSurface.copy(0.8f), RoundedCornerShape(10.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.Forward10, contentDescription = "Forward 10 seconds", tint = TextPrimary)
                    }
                    IconButton(
                        onClick = { shuffle = !shuffle },
                        modifier = Modifier
                            .size(44.dp)
                            .background(if (shuffle) CyanPrimary else CardSurface.copy(0.8f), RoundedCornerShape(10.dp))
                            .border(1.dp, if (shuffle) CyanPrimary else CardBorder, RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = if (shuffle) Color.Black else TextPrimary)
                    }
                    IconButton(
                        onClick = { repeatOne = !repeatOne },
                        modifier = Modifier
                            .size(44.dp)
                            .background(if (repeatOne) CyanPrimary else CardSurface.copy(0.8f), RoundedCornerShape(10.dp))
                            .border(1.dp, if (repeatOne) CyanPrimary else CardBorder, RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.Repeat, contentDescription = "Repeat", tint = if (repeatOne) Color.Black else TextPrimary)
                    }
                }

                Slider(
                    value = currentPosition.toFloat().coerceIn(0f, duration.toFloat()),
                    onValueChange = {
                        currentPosition = it.toInt()
                        mediaPlayer?.seekTo(currentPosition)
                    },
                    valueRange = 0f..duration.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatDuration(currentPosition), style = MaterialTheme.typography.bodySmall)
                    Text(formatDuration(duration), style = MaterialTheme.typography.bodySmall)
                }

                if (metadata.genre != null || metadata.year != null) {
                    Text("${metadata.genre.orEmpty()} ${metadata.year.orEmpty()}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private data class AudioMetadata(
    val title: String?,
    val artist: String?,
    val album: String?,
    val genre: String?,
    val year: String?
)

private fun loadAudioMetadata(file: File): AudioMetadata {
    var showControls by remember { mutableStateOf(false) } // Hidden by default

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                PDFView(context, null).apply {
                    pdfView = this
                    fromFile(file)
                        .defaultPage(currentPage)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .onLoad { pageCount = it }
                        .load()
                }
            },
            update = {
                pdfView?.let { view ->
                    if (view.currentPage != currentPage) view.jumpTo(currentPage, true)
                    view.zoomTo(zoomFactor)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Tap to show/hide controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { showControls = !showControls }
                }
        )

        // Bottom controls - hidden by default
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { if (currentPage > 0) currentPage -= 1 }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous page")
                    }
                    Text("Page ${currentPage + 1} of ${pageCount.coerceAtLeast(1)}", style = MaterialTheme.typography.bodySmall)
                    IconButton(onClick = { if (currentPage < pageCount - 1) currentPage += 1 }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next page")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Zoom", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = zoomFactor,
                        onValueChange = { zoomFactor = it.coerceIn(1f, 4f) },
                        valueRange = 1f..4f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(String.format("%.1fx", zoomFactor), style = MaterialTheme.typography.bodySmall)
                }
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { if (currentPage > 0) currentPage -= 1 }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous page")
                }
                Text("Page ${currentPage + 1} of ${pageCount.coerceAtLeast(1)}", style = MaterialTheme.typography.bodySmall)
                IconButton(onClick = { if (currentPage < pageCount - 1) currentPage += 1 }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next page")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Zoom", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = zoomFactor,
                    onValueChange = { zoomFactor = it.coerceIn(1f, 4f) },
                    valueRange = 1f..4f,
                    modifier = Modifier.weight(1f)
                )
                Text(String.format("%.1fx", zoomFactor), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun TextViewer(file: File) {
    val text = remember(file) { readTextSafe(file) }
    var searchQuery by remember { mutableStateOf("") }
    var fontSize by remember { mutableStateOf(16f) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search text") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Close, contentDescription = "Clear search")
            }
        }

        Text(
            text = "Font size: ${fontSize.toInt()}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Slider(
            value = fontSize,
            onValueChange = { fontSize = it },
            valueRange = 12f..28f,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        val primaryColor = MaterialTheme.colorScheme.primary
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface

        val annotatedText = remember(text, searchQuery, primaryColor, onSurfaceColor) {
            if (searchQuery.isBlank()) {
                androidx.compose.ui.text.AnnotatedString(text)
            } else {
                val query = searchQuery.lowercase(Locale.getDefault())
                val source = text
                val builder = androidx.compose.ui.text.AnnotatedString.Builder()
                var searchStart = 0
                val lowerText = source.lowercase(Locale.getDefault())
                while (searchStart < lowerText.length) {
                    val index = lowerText.indexOf(query, searchStart)
                    if (index < 0) {
                        builder.append(source.substring(searchStart))
                        break
                    }
                    builder.append(source.substring(searchStart, index))
                    builder.pushStyle(
                        androidx.compose.ui.text.SpanStyle(
                            background = primaryColor.copy(alpha = 0.25f),
                            color = onSurfaceColor
                        )
                    )
                    builder.append(source.substring(index, index + query.length))
                    builder.pop()
                    searchStart = index + query.length
                }
                builder.toAnnotatedString()
            }
        }

        SelectionContainer(modifier = Modifier.fillMaxSize()) {
            Text(
                text = annotatedText,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            )
        }
    }
}

private fun readTextSafe(file: File): String {
    return try {
        file.readText()
    } catch (e: Exception) {
        "Failed to read file: ${e.message}"
    }
}

@Composable
fun HtmlViewer(file: File) {
    val htmlContent = remember(file) { readTextSafe(file) }
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Text(
                text = "Web Viewer: JavaScript disabled for security.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AndroidView(
            factory = { ctx ->
                android.webkit.WebView(ctx).apply {
                    settings.javaScriptEnabled = false
                    settings.domStorageEnabled = false
                    webViewClient = android.webkit.WebViewClient()
                    loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ArchiveViewer(file: File) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val archiveContents = remember(file) {
        try { getArchiveContents(file) } catch (e: Exception) { emptyList() }
    }
    val filtered = remember(archiveContents, searchQuery) {
        archiveContents.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Archive Contents: ${file.name}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search archive entries") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Text("No matching entries", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filtered) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.InsertDriveFile, contentDescription = "File", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { extractArchive(file, context) }, modifier = Modifier.fillMaxWidth()) {
                Text("Extract Archive")
            }
        }
    }
}

@Composable
fun ApkInfoViewer(file: File) {
    val context = LocalContext.current
    val apkInfo = remember(file) {
        try {
            getApkInfo(context, file)
        } catch (e: Exception) {
            ApkInfo("Failed to read APK", "", 0L, file.length(), emptyList())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "APK Information", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Package Name: ${apkInfo.packageName}", style = MaterialTheme.typography.bodyLarge)
                Text("Version: ${apkInfo.versionName} (${apkInfo.versionCode})", style = MaterialTheme.typography.bodyMedium)
                Text("Size: ${formatFileSize(apkInfo.size)}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Permissions:", style = MaterialTheme.typography.titleMedium)
                apkInfo.permissions.forEach { permission ->
                    Text("• $permission", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { installApk(context, file) }, modifier = Modifier.weight(1f)) {
                Text("Install APK")
            }
            Button(onClick = { shareApk(context, file) }, modifier = Modifier.weight(1f)) {
                Text("Share APK")
            }
        }
    }
}

private fun getMimeType(extension: String): String? {
    return when (extension.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "svg" -> "image/svg+xml"
        "bmp" -> "image/bmp"
        "tiff", "tif" -> "image/tiff"
        "mp4" -> "video/mp4"
        "webm" -> "video/webm"
        "mkv" -> "video/x-matroska"
        "avi" -> "video/avi"
        "mov" -> "video/quicktime"
        "flv" -> "video/x-flv"
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "ogg", "oga" -> "audio/ogg"
        "aac" -> "audio/aac"
        "flac" -> "audio/flac"
        "m4a" -> "audio/mp4"
        "pdf" -> "application/pdf"
        "txt" -> "text/plain"
        "html", "htm" -> "text/html"
        "mhtml" -> "multipart/related"
        "json" -> "application/json"
        "xml" -> "application/xml"
        "csv" -> "text/csv"
        "md" -> "text/markdown"
        "zip" -> "application/zip"
        "rar" -> "application/x-rar-compressed"
        "7z" -> "application/x-7z-compressed"
        "tar" -> "application/x-tar"
        "apk" -> "application/vnd.android.package-archive"
        else -> null
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes == 0L) return "0 B"
    val k = 1024.0
    val sizes = arrayOf("B", "KB", "MB", "GB", "TB")
    val i = (Math.log(bytes.toDouble()) / Math.log(k)).toInt()
    return String.format("%.1f %s", bytes / Math.pow(k, i.toDouble()), sizes[i])
}

private fun formatDuration(millis: Int): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60)
    } else {
        String.format("%d:%02d", minutes, seconds % 60)
    }
}

private fun formatModifiedDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

private fun getArchiveContents(file: File): List<String> {
    return try {
        when (file.extension.lowercase()) {
            "zip" -> {
                java.util.zip.ZipFile(file).use { zip ->
                    zip.entries().asSequence().map { it.name }.toList()
                }
            }
            else -> emptyList() // For now, only ZIP supported
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun extractArchive(file: File, context: Context) {
    // Simple extraction to downloads folder
    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
    val extractDir = File(downloadsDir, "WAOS_Extracted/${file.nameWithoutExtension}")

    try {
        extractDir.mkdirs()
        when (file.extension.lowercase()) {
            "zip" -> {
                java.util.zip.ZipFile(file).use { zip ->
                    zip.entries().asSequence().forEach { entry ->
                        if (!entry.isDirectory) {
                            val outputFile = File(extractDir, entry.name)
                            outputFile.parentFile?.mkdirs()
                            zip.getInputStream(entry).use { input ->
                                outputFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                    }
                }
            }
        }
        // Show success message
        android.widget.Toast.makeText(context, "Archive extracted to ${extractDir.absolutePath}", android.widget.Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Failed to extract archive: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}

data class ApkInfo(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val size: Long,
    val permissions: List<String>
)

private fun getApkInfo(context: Context, file: File): ApkInfo {
    val pm = context.packageManager
    val packageInfo = pm.getPackageArchiveInfo(file.absolutePath, android.content.pm.PackageManager.GET_PERMISSIONS)

    return if (packageInfo != null) {
        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
        ApkInfo(
            packageName = packageInfo.packageName ?: "Unknown",
            versionName = packageInfo.versionName ?: "Unknown",
            versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            },
            size = file.length(),
            permissions = permissions
        )
    } else {
        ApkInfo("Failed to read APK info", "", 0L, file.length(), emptyList())
    }
}

private fun installApk(context: Context, file: File) {
    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private fun shareApk(context: Context, file: File) {
    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "application/vnd.android.package-archive"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share APK"))
}

@HiltViewModel
class UniversalFileViewerViewModel @Inject constructor() : ViewModel() {

    var currentFile by mutableStateOf<File?>(null)
        private set

    private val savedPositions = mutableMapOf<String, Int>()

    fun loadFile(file: File) {
        currentFile = file
        // Additional loading logic can be added here
    }

    fun savePosition(file: File, position: Int) {
        savedPositions[file.absolutePath] = position
    }

    fun getSavedPosition(file: File): Int {
        return savedPositions[file.absolutePath] ?: 0
    }

    fun duplicateFile(file: File): File? {
        return try {
            val copyName = file.nameWithoutExtension + " (copy)" + if (file.extension.isNotBlank()) ".${file.extension}" else ""
            val duplicateFile = File(file.parentFile, copyName)
            if (!duplicateFile.exists()) {
                file.copyTo(duplicateFile)
            }
            duplicateFile
        } catch (e: Exception) {
            null
        }
    }

    fun deleteFile(file: File): Boolean {
        return try {
            if (file.isDirectory) file.deleteRecursively() else file.delete()
        } catch (e: Exception) {
            false
        }
    }
}