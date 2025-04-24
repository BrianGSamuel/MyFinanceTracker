package com.example.financetracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.financetracker.databinding.ActivityLoginBinding
import java.io.File
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private val usersFileName = "users.txt"
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Biometric Authentication
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@LoginActivity, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Biometric authentication error: $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(this@LoginActivity, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Biometric authentication succeeded")
                navigateToBudgetList()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Biometric authentication failed")
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your fingerprint")
            .setNegativeButtonText("Use Password")
            .build()

        // Check if biometric is available
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "Biometric authentication available")
            }
            else -> {
                Log.w(TAG, "Biometric authentication not available")
                binding.fingerprintButton.isEnabled = false // Disable button if biometrics unavailable
            }
        }

        // Fingerprint Button
        binding.fingerprintButton.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
            Log.d(TAG, "Fingerprint button clicked")
        }

        // Login Button
        binding.loginButton.setOnClickListener {
            val email = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            if (email.isNotBlank() && password.isNotBlank()) {
                if (validateCredentials(email, password)) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Email/password login successful for user: $email")
                    navigateToBudgetList()
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "Invalid login attempt for user: $email")
                }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Empty email or password")
            }
        }

        // Sign Up Button
        binding.registerButton.setOnClickListener {
            val email = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            if (email.isNotBlank() && password.isNotBlank()) {
                if (registerUser(email, password)) {
                    Toast.makeText(this, "Sign up successful! Please login.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "User signed up: $email")
                    binding.usernameInput.text?.clear()
                    binding.passwordInput.text?.clear()
                } else {
                    Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "Sign up failed: Email $email already exists")
                }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Empty email or password during sign up")
            }
        }

    }

    private fun validateCredentials(email: String, password: String): Boolean {
        try {
            val file = File(filesDir, usersFileName)
            if (file.exists()) {
                file.readLines().forEach { line ->
                    val parts = line.split(",")
                    if (parts.size == 2 && parts[0] == email && parts[1] == password) {
                        return true
                    }
                }
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error validating credentials: ${e.message}")
            return false
        }
    }

    private fun registerUser(email: String, password: String): Boolean {
        try {
            val file = File(filesDir, usersFileName)
            val existingUsers = mutableListOf<String>()
            if (file.exists()) {
                existingUsers.addAll(file.readLines())
                if (existingUsers.any { it.startsWith("$email,") }) {
                    return false // Email exists
                }
            }
            existingUsers.add("$email,$password")
            file.writeText(existingUsers.joinToString("\n"))
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error registering user: ${e.message}")
            return false
        }
    }

    private fun navigateToBudgetList() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}