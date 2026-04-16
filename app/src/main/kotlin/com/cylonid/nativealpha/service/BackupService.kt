package com.cylonid.nativealpha.service

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.cylonid.nativealpha.data.AppDatabase
import com.cylonid.nativealpha.manager.CredentialManager
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.repository.WebAppRepository
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupService @Inject constructor(
    private val context: Context,
    private val database: AppDatabase,
    private val webAppRepository: WebAppRepository,
    private val credentialManager: CredentialManager
) {

    private val gson = GsonBuilder()
        .serializeNulls()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create()
    private val backupDir = File(context.getExternalFilesDir(null), "backups")

    init {
        backupDir.mkdirs()
    }

    suspend fun createBackup(): String? = withContext(Dispatchers.IO) {
        try {
            if (!backupDir.exists()) backupDir.mkdirs()
            val timestamp = System.currentTimeMillis()
            val backupFile = File(backupDir, "waos_backup_$timestamp.waos")

            val webApps = webAppRepository.getAllWebApps().first()
            val downloads = com.cylonid.nativealpha.waos.model.DownloadRepository.loadDownloads(context)
            val clipboard = com.cylonid.nativealpha.waos.model.ClipboardRepository.loadClipboardItems(context)
            val credentials = loadAllCredentials() // We'll need to handle encryption
            val sessions = loadAllSessions()

            val settings = mapOf(
                "version" to 3,
                "timestamp" to timestamp,
                "format" to "waos",
                "appName" to "WAOS - Web App Operating System"
            )

            val backupData = mapOf(
                "settings" to settings,
                "webApps" to webApps,
                "downloads" to downloads,
                "clipboard" to clipboard,
                "credentials" to credentials,
                "sessions" to sessions
            )

            val json = gson.toJson(backupData)
            FileOutputStream(backupFile).use { it.write(json.toByteArray()) }

            backupFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createBackupToUri(folderUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext null
            if (!folder.canWrite()) return@withContext null
            val timestamp = System.currentTimeMillis()
            val filename = "waos_backup_$timestamp.waos"
            val fileDoc = folder.createFile("application/octet-stream", filename) ?: return@withContext null
            val uri = fileDoc.uri

            val webApps = webAppRepository.getAllWebApps().first()
            val downloads = com.cylonid.nativealpha.waos.model.DownloadRepository.loadDownloads(context)
            val clipboard = com.cylonid.nativealpha.waos.model.ClipboardRepository.loadClipboardItems(context)
            val credentials = loadAllCredentials()
            val sessions = loadAllSessions()

            val settings = mapOf(
                "version" to 3,
                "timestamp" to timestamp,
                "format" to "waos",
                "appName" to "WAOS - Web App Operating System"
            )
            val backupData = mapOf(
                "settings" to settings,
                "webApps" to webApps,
                "downloads" to downloads,
                "clipboard" to clipboard,
                "credentials" to credentials,
                "sessions" to sessions
            )
            val json = gson.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            uri.toString()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun restoreBackup(backupPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(backupPath)
            if (!backupFile.exists()) return@withContext false
            val json = FileInputStream(backupFile).use { it.readBytes().toString(Charsets.UTF_8) }
            restoreBackupFromJson(json)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun restoreBackupFromUri(fileUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(fileUri) ?: return@withContext false
            val json = inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            restoreBackupFromJson(json)
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun restoreBackupFromJson(json: String): Boolean {
        return try {
            val root = gson.fromJson(json, com.google.gson.JsonObject::class.java)
            
            // Restore web apps
            val webAppsJson = root.getAsJsonArray("webApps")
            if (webAppsJson != null) {
                val webAppsType = object : TypeToken<List<WebApp>>() {}.type
                val webApps: List<WebApp> = try {
                    gson.fromJson(webAppsJson, webAppsType)
                } catch (e: Exception) {
                    emptyList()
                }

                webApps.forEach { app ->
                    try {
                        webAppRepository.insertWebApp(app.copy(id = 0, thumbnail = null))
                    } catch (e: Exception) {
                        // Skip apps that fail to insert individually
                    }
                }
            }

            // Restore downloads
            val downloadsJson = root.getAsJsonArray("downloads")
            if (downloadsJson != null) {
                val downloadsType = object : TypeToken<List<com.cylonid.nativealpha.waos.model.DownloadRecord>>() {}.type
                val downloads: List<com.cylonid.nativealpha.waos.model.DownloadRecord> = try {
                    gson.fromJson(downloadsJson, downloadsType)
                } catch (e: Exception) {
                    emptyList()
                }
                downloads.forEach { download ->
                    try {
                        com.cylonid.nativealpha.waos.model.DownloadRepository.saveDownload(context, download)
                    } catch (e: Exception) {
                        // Skip
                    }
                }
            }

            // Restore clipboard
            val clipboardJson = root.getAsJsonArray("clipboard")
            if (clipboardJson != null) {
                val clipboardType = object : TypeToken<List<com.cylonid.nativealpha.waos.model.ClipboardItem>>() {}.type
                val clipboard: List<com.cylonid.nativealpha.waos.model.ClipboardItem> = try {
                    gson.fromJson(clipboardJson, clipboardType)
                } catch (e: Exception) {
                    emptyList()
                }
                clipboard.forEach { item ->
                    try {
                        com.cylonid.nativealpha.waos.model.ClipboardRepository.saveClipboardItem(context, item)
                    } catch (e: Exception) {
                        // Skip
                    }
                }
            }

            // Restore credentials (encrypted)
            val credentialsJson = root.getAsJsonArray("credentials")
            if (credentialsJson != null) {
                val credentialsType = object : TypeToken<List<com.cylonid.nativealpha.waos.model.EncryptedCredentialItem>>() {}.type
                val credentials: List<com.cylonid.nativealpha.waos.model.EncryptedCredentialItem> = try {
                    gson.fromJson(credentialsJson, credentialsType)
                } catch (e: Exception) {
                    emptyList()
                }
                // Save encrypted credentials directly
                val allEncrypted = com.cylonid.nativealpha.waos.model.CredentialRepository.loadAllEncryptedCredentials(context).toMutableList()
                allEncrypted.addAll(credentials)
                com.cylonid.nativealpha.waos.model.CredentialRepository.saveAllEncryptedCredentials(context, allEncrypted)
            }

            // Restore sessions
            val sessionsJson = root.getAsJsonObject("sessions")
            if (sessionsJson != null) {
                sessionsJson.keySet().forEach { appIdStr ->
                    try {
                        val appId = appIdStr.toLong()
                        val sessionJson = sessionsJson.get(appIdStr).asString
                        val session = gson.fromJson(sessionJson, com.google.gson.JsonObject::class.java)
                        val sessionManager = com.cylonid.nativealpha.webview.SessionManager(context, appId, "Restored App")
                        sessionManager.saveLastSessionSnapshot(session)
                    } catch (e: Exception) {
                        // Skip
                    }
                }
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteBackupFile(path: String) {
        File(path).delete()
    }

    suspend fun exportData() {
        createBackup()
    }

    suspend fun importData() {
        // Handled via file picker in UI
    }

    private fun loadAllCredentials(): List<com.cylonid.nativealpha.waos.model.EncryptedCredentialItem> {
        return try {
            com.cylonid.nativealpha.waos.model.CredentialRepository.loadAllEncryptedCredentials(context)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadAllSessions(): Map<Long, String> {
        val sessions = mutableMapOf<Long, String>()
        try {
            // Get all web apps and try to load their sessions
            val webApps = webAppRepository.getAllWebApps().first()
            webApps.forEach { app ->
                try {
                    val sessionManager = com.cylonid.nativealpha.webview.SessionManager(context, app.id, app.name)
                    val session = sessionManager.loadLastSessionSnapshot()
                    if (session != null) {
                        sessions[app.id] = gson.toJson(session)
                    }
                } catch (e: Exception) {
                    // Skip session if can't load
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return sessions
    }
}