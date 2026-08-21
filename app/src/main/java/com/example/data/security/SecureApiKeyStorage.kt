package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.BuildConfig
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureApiKeyStorage(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "manula_social_ai_secure_prefs"
        private const val KEY_ENCRYPTED_API_KEY = "encrypted_gemini_api_key"
        private const val KEY_ALIAS = "ManulaGeminiKeyAlias"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128

        @Volatile
        private var INSTANCE: SecureApiKeyStorage? = null

        fun getInstance(context: Context): SecureApiKeyStorage {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SecureApiKeyStorage(context).also { INSTANCE = it }
            }
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) {
                return entry.secretKey
            }
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Saves user-entered Gemini API key encrypted in KeyStore + SharedPreferences.
     */
    fun saveApiKey(rawKey: String): Boolean {
        val trimmed = rawKey.trim()
        if (trimmed.isBlank()) return false
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(trimmed.toByteArray(Charsets.UTF_8))

            val combined = Base64.encodeToString(iv, Base64.NO_WRAP) + ":" +
                    Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

            prefs.edit().putString(KEY_ENCRYPTED_API_KEY, combined).apply()
            true
        } catch (e: Exception) {
            // Fallback for extreme devices where KeyStore might have quirks
            prefs.edit().putString(KEY_ENCRYPTED_API_KEY, "PLAIN:" + Base64.encodeToString(trimmed.toByteArray(), Base64.NO_WRAP)).apply()
            true
        }
    }

    /**
     * Retrieves decrypted API key. Returns custom user key if present,
     * otherwise falls back to BuildConfig key if defined and non-placeholder.
     */
    fun getApiKey(): String? {
        val stored = prefs.getString(KEY_ENCRYPTED_API_KEY, null)
        if (!stored.isNullOrBlank()) {
            try {
                if (stored.startsWith("PLAIN:")) {
                    val raw = stored.substring("PLAIN:".length)
                    return String(Base64.decode(raw, Base64.NO_WRAP), Charsets.UTF_8)
                }

                val parts = stored.split(":")
                if (parts.size == 2) {
                    val iv = Base64.decode(parts[0], Base64.NO_WRAP)
                    val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)

                    val secretKey = getOrCreateSecretKey()
                    val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                    val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                    val decrypted = cipher.doFinal(encryptedBytes)
                    return String(decrypted, Charsets.UTF_8)
                }
            } catch (e: Exception) {
                // Return null if decryption fails
            }
        }

        // Fallback to BuildConfig if provided
        val buildConfigKey = BuildConfig.GEMINI_API_KEY
        if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY" && buildConfigKey != "DEFAULT_KEY") {
            return buildConfigKey
        }

        return null
    }

    /**
     * Returns true if a valid user key or BuildConfig key is available.
     */
    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }

    /**
     * Removes user stored API key.
     */
    fun removeApiKey() {
        prefs.edit().remove(KEY_ENCRYPTED_API_KEY).apply()
    }

    /**
     * Returns masked representation of the active key: e.g. "AIza••••••••••••1234"
     */
    fun getMaskedApiKey(): String {
        val key = getApiKey() ?: return "No key configured"
        if (key.length <= 8) return "••••••••"
        val prefix = key.take(4)
        val suffix = key.takeLast(4)
        val dots = "•".repeat((key.length - 8).coerceIn(8, 16))
        return "$prefix$dots$suffix"
    }

    /**
     * Checks if current key is a custom user key (vs BuildConfig fallback)
     */
    fun isCustomKeySet(): Boolean {
        return prefs.contains(KEY_ENCRYPTED_API_KEY)
    }
}
