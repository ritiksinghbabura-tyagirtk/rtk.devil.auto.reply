package com.rtk.devil.autoreply.integration.telegrambot

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores the Telegram bot token encrypted using an Android Keystore AES key.
 * The token is not logged and is not stored in source code.
 */
class BotTokenStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        "rtk_bot_secret_store",
        Context.MODE_PRIVATE
    )

    @Synchronized
    fun saveToken(token: String) {
        val clean = token.trim()
        require(clean.isNotEmpty()) { "Bot token cannot be empty." }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(clean.toByteArray(Charsets.UTF_8))

        val encodedCiphertext = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        val encodedIv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)

        check(
            prefs.edit()
                .putString(KEY_CIPHERTEXT, encodedCiphertext)
                .putString(KEY_IV, encodedIv)
                .commit()
        ) { "Could not save encrypted token." }
    }

    @Synchronized
    fun readToken(): String? {
        val ciphertext = prefs.getString(KEY_CIPHERTEXT, null) ?: return null
        val ivString = prefs.getString(KEY_IV, null) ?: return null

        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(128, Base64.decode(ivString, Base64.NO_WRAP))
            )
            String(
                cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP)),
                Charsets.UTF_8
            )
        } catch (_: Exception) {
            null
        }
    }

    @Synchronized
    fun hasToken(): Boolean = !readToken().isNullOrBlank()

    @Synchronized
    fun clearToken() {
        prefs.edit().clear().commit()
        // Keep the Keystore key so an interrupted save cannot invalidate unrelated state.
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "rtk_devil_bot_token_aes_v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_CIPHERTEXT = "token_ciphertext"
        private const val KEY_IV = "token_iv"
    }
}
