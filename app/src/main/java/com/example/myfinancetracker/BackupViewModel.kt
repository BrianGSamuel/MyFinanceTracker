package com.example.financetracker

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class BackupViewModel(application: Application) : AndroidViewModel(application) {
    val message = MutableLiveData<String>()

    fun exportBackup() {
        val (success, errorMessage) = DataManager.exportBackup(getApplication())
        message.value = errorMessage
    }

    fun importBackup(uri: Uri) {
        val (success, errorMessage) = DataManager.importBackup(getApplication(), uri)
        message.value = errorMessage
    }

    fun clearData() {
        DataManager.clearData(getApplication())
        message.value = "Data cleared successfully"
    }
}