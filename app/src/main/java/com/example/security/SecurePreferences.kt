package com.example.security

import android.content.Context
import java.security.MessageDigest

class SecurePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("safe_notes_secure_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "secured_pin_hash"
        private const val KEY_BIOMETRICS_ENABLED = "biometrics_enabled"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts_count"
        private const val KEY_SELF_DESTRUCT_ENABLED = "self_destruct_enabled"
        private const val KEY_DARK_MODE = "dark_mode_preference"
        private const val KEY_THEME = "app_theme_selection"
        const val MAX_FAILED_ATTEMPTS = 5
    }

    fun hasSetPin(): Boolean {
        return prefs.getString(KEY_PIN_HASH, null) != null
    }

    fun savePin(pin: String): Boolean {
        if (pin.length < 4) return false
        val hashed = hashString(pin)
        prefs.edit().putString(KEY_PIN_HASH, hashed).apply()
        resetFailedAttempts()
        return true
    }

    fun verifyPin(pin: String): Boolean {
        val savedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val enteredHash = hashString(pin)
        val matches = savedHash == enteredHash
        if (matches) {
            resetFailedAttempts()
        } else {
            incrementFailedAttempts()
        }
        return matches
    }

    val isSelfDestructEnabled: Boolean
        get() = prefs.getBoolean(KEY_SELF_DESTRUCT_ENABLED, false)

    fun setSelfDestructEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SELF_DESTRUCT_ENABLED, enabled).apply()
    }

    val isBiometricsEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRICS_ENABLED, true) // Enable by default if hardware exists

    fun setBiometricsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRICS_ENABLED, enabled).apply()
    }

    val failedAttempts: Int
        get() = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)

    fun incrementFailedAttempts() {
        val attempts = failedAttempts + 1
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, attempts).apply()
    }

    fun resetFailedAttempts() {
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).apply()
    }

    fun shouldSelfDestruct(): Boolean {
        return isSelfDestructEnabled && failedAttempts >= MAX_FAILED_ATTEMPTS
    }

    fun clearAllSecureData() {
        prefs.edit().clear().apply()
    }

    fun getDarkModePreference(): Boolean? {
        if (!prefs.contains(KEY_DARK_MODE)) return null
        return prefs.getBoolean(KEY_DARK_MODE, true)
    }

    fun setDarkModePreference(dark: Boolean?) {
        if (dark == null) {
            prefs.edit().remove(KEY_DARK_MODE).apply()
        } else {
            prefs.edit().putBoolean(KEY_DARK_MODE, dark).apply()
        }
    }

    fun getThemePreference(): String {
        return prefs.getString(KEY_THEME, "sage") ?: "sage"
    }

    fun setThemePreference(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
    }

    private fun hashString(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
