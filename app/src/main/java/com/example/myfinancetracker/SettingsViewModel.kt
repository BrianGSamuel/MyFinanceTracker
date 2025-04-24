package com.example.financetracker

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    val name = MutableLiveData<String>("")
    val email = MutableLiveData<String>("")
    val phone = MutableLiveData<String>("")
    val currency = MutableLiveData<String>("USD")
    val budget = MutableLiveData<String>("0")
    val profilePictureUri = MutableLiveData<Uri?>(null)
    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private var bindingContext: Context? = null

    fun setBindingContext(context: Context) {
        bindingContext = context
        loadSettings(context)
    }

    private fun loadSettings(context: Context) {
        viewModelScope.launch {
            try {
                val sharedPrefs = context.getSharedPreferences("FinanceFlowPrefs", Context.MODE_PRIVATE)
                name.value = sharedPrefs.getString("name", "") ?: ""
                email.value = sharedPrefs.getString("email", "") ?: ""
                phone.value = sharedPrefs.getString("phone", "") ?: ""
                currency.value = sharedPrefs.getString("currency", "USD") ?: "USD"
                budget.value = sharedPrefs.getFloat("budget", 0f).toString()
                val uriString = sharedPrefs.getString("profile_picture_uri", null)
                profilePictureUri.value = uriString?.let { Uri.parse(it) }
            } catch (e: Exception) {
                _saveSuccess.value = false
            }
        }
    }

    fun setProfilePictureUri(uri: Uri) {
        profilePictureUri.value = uri
    }

    fun saveSettings() {
        viewModelScope.launch {
            try {
                val context = bindingContext ?: throw IllegalStateException("Context not set")
                val sharedPrefs = context.getSharedPreferences("FinanceFlowPrefs", Context.MODE_PRIVATE)
                with(sharedPrefs.edit()) {
                    putString("name", name.value)
                    putString("email", email.value)
                    putString("phone", phone.value)
                    putString("currency", currency.value)
                    putFloat("budget", budget.value?.toFloatOrNull() ?: 0f)
                    putString("profile_picture_uri", profilePictureUri.value?.toString())
                    commit() // Synchronous save for reliability (April 22, 2025, 08:44)
                }
                _saveSuccess.value = true
            } catch (e: Exception) {
                _saveSuccess.value = false
            }
        }
    }
}