package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ui.NotesApp
import com.example.ui.NotesViewModel

class MainActivity : FragmentActivity() {
    private lateinit var viewModel: NotesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[NotesViewModel::class.java]

        setContent {
            NotesApp(
                viewModel = viewModel,
                onTriggerBiometric = {
                    triggerBiometricAuth()
                }
            )
        }
    }

    private fun triggerBiometricAuth() {
        val biometricManager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        runOnUiThread {
                            // If user cancels, we don't spam toasts
                            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                Toast.makeText(this@MainActivity, "Lock Verification: $errString", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        runOnUiThread {
                            viewModel.unlockWithBiometrics()
                            Toast.makeText(this@MainActivity, "Decryption access authorized", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        // Biometric failed. No blocking toast needed as system provides sensory vibrator feedback.
                    }
                })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Decrypt Safe Notes")
                    .setSubtitle("Authenticate identity using biological or system passcode to decrypt files.")
                    .setAllowedAuthenticators(authenticators)
                    .build()

                try {
                    biometricPrompt.authenticate(promptInfo)
                } catch (e: Exception) {
                    Toast.makeText(this, "Biometric initialize error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                // Biometrics are not configured or supported on this architecture.
                // It will gracefully default back to our elegant styled numeric on-screen keypad.
            }
        }
    }
}
