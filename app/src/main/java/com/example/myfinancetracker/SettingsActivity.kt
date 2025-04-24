package com.example.financetracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    private val TAG = "SettingsActivity"

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        try {
            uri?.let {
                viewModel.setProfilePictureUri(it)
                binding.ivProfilePicture.setImageURI(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting profile picture: ${e.message}", e)
            Toast.makeText(this, "Failed to set profile picture", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivitySettingsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.viewModel = viewModel
            binding.lifecycleOwner = this

            // Set context and load settings
            viewModel.setBindingContext(this)

            // Set up currency spinner
            val currencies = listOf("USD", "EUR", "GBP", "INR")
            binding.spCurrency.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
                .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            // Set up profile picture selection
            binding.btnSelectPicture.setOnClickListener {
                pickImage.launch("image/*")
            }

            // Set up logout button
            binding.btnLogout.setOnClickListener {
                try {
                    startActivity(Intent(this, LoginActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Logout error: ${e.message}", e)
                    Toast.makeText(this, "Failed to logout", Toast.LENGTH_SHORT).show()
                }
            }

            // Set up Bottom Navigation Bar
            binding.bottomNavigation.selectedItemId = R.id.nav_settings
            binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
                try {
                    when (item.itemId) {
                        R.id.nav_home -> {
                            startActivity(Intent(this, MainActivity::class.java))
                            overridePendingTransition(0, 0)
                            finish()
                            true
                        }
                        R.id.nav_analytics -> {
                            startActivity(Intent(this, AnalyticsActivity::class.java))
                            overridePendingTransition(0, 0)
                            finish()
                            true
                        }
                        R.id.nav_transactions -> {
                            startActivity(Intent(this, TransactionsActivity::class.java))
                            overridePendingTransition(0, 0)
                            finish()
                            true
                        }
                        R.id.nav_settings -> true
                        else -> false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation error: ${e.message}", e)
                    false
                }
            }

            // Observe save success
            viewModel.saveSuccess.observe(this) { success ->
                if (success) {
                    Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save settings", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SettingsActivity: ${e.message}", e)
            finish()
        }
    }
}