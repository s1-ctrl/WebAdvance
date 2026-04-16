package com.cylonid.nativealpha.waos.model

import android.content.Context
import com.cylonid.nativealpha.waos.util.CredentialEncryption
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object CredentialRepository {
    private const val CREDENTIALS_FILENAME = "waos_credentials.json"

    private fun getStorageFile(context: Context): File {
        return File(context.filesDir, CREDENTIALS_FILENAME)
    }

    fun loadCredentials(context: Context, appId: Int, pin: String): MutableList<CredentialItem> {
        val file = getStorageFile(context)
        if (!file.exists()) return mutableListOf()
        return try {
            val json = file.readText()
            val type = object : TypeToken<MutableList<EncryptedCredentialItem>>() {}.type
            val encryptedList: MutableList<EncryptedCredentialItem> = Gson().fromJson(json, type) ?: mutableListOf()
            encryptedList.filter { it.appId == appId }.mapNotNull { encrypted ->
                try {
                    val decrypted = CredentialEncryption.decrypt(encrypted.payload, pin)
                    Gson().fromJson(decrypted, CredentialItem::class.java)
                } catch (e: Exception) {
                    null
                }
            }.toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveCredential(context: Context, credential: CredentialItem, pin: String) {
        val all = loadAllEncryptedCredentials(context)
        val payload = CredentialEncryption.encrypt(Gson().toJson(credential), pin)
        all.add(EncryptedCredentialItem(credential.appId, payload, credential.timestamp))
        getStorageFile(context).writeText(Gson().toJson(all))
    }

    fun deleteCredential(context: Context, credential: CredentialItem) {
        val all = loadAllEncryptedCredentials(context)
        val filtered = all.filterNot { it.appId == credential.appId && it.timestamp == credential.timestamp }
        getStorageFile(context).writeText(Gson().toJson(filtered))
    }

    fun updateCredential(context: Context, credential: CredentialItem, pin: String) {
        val all = loadAllEncryptedCredentials(context)
        val payload = CredentialEncryption.encrypt(Gson().toJson(credential), pin)
        val updated = all.map {
            if (it.appId == credential.appId && it.timestamp == credential.timestamp) {
                EncryptedCredentialItem(credential.appId, payload, credential.timestamp)
            } else {
                it
            }
        }
        getStorageFile(context).writeText(Gson().toJson(updated))
    }

    fun saveAllEncryptedCredentials(context: Context, credentials: List<EncryptedCredentialItem>) {
        getStorageFile(context).writeText(Gson().toJson(credentials))
    }
        val file = getStorageFile(context)
        if (!file.exists()) return mutableListOf()
        return try {
            val json = file.readText()
            val type = object : TypeToken<MutableList<EncryptedCredentialItem>>() {}.type
            Gson().fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }
}
