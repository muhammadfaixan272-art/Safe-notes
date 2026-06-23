package com.example.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "SafeNotesMasterKey"

    private val keyStore: KeyStore = KeyStore.getInstance(KEY_PROVIDER).apply {
        load(null)
    }

    private fun getSecretKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: generateSecretKey()
    }

    private fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_PROVIDER)
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    fun encrypt(rawString: String): EncryptedData {
        if (rawString.isEmpty()) return EncryptedData("", "")
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val ciphertextBytes = cipher.doFinal(rawString.toByteArray(Charsets.UTF_8))
            val ivBytes = cipher.iv

            val ciphertextBase64 = Base64.encodeToString(ciphertextBytes, Base64.NO_WRAP)
            val ivBase64 = Base64.encodeToString(ivBytes, Base64.NO_WRAP)
            EncryptedData(ciphertextBase64, ivBase64)
        } catch (e: Exception) {
            EncryptedData("", "")
        }
    }

    fun decrypt(ciphertextBase64: String, ivBase64: String): String {
        if (ciphertextBase64.isEmpty() || ivBase64.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            val ivBytes = Base64.decode(ivBase64, Base64.NO_WRAP)
            val ciphertextBytes = Base64.decode(ciphertextBase64, Base64.NO_WRAP)
            val gcmSpec = GCMParameterSpec(128, ivBytes)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), gcmSpec)
            val decryptedBytes = cipher.doFinal(ciphertextBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "[Decryption Error]"
        }
    }
}

data class EncryptedData(
    val ciphertext: String,
    val iv: String
)
