package com.example.financetracker

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter
import java.util.concurrent.atomic.AtomicLong

object DataManager {
    private const val PREFS_NAME = "FinancePrefs"
    private const val KEY_TRANSACTIONS = "transactions"
    private const val KEY_BUDGET = "budget"
    private const val KEY_CURRENCY = "currency"
    private const val BACKUP_FILE = "finance_backup.json"
    private const val TAG = "DataManager"
    private val idGenerator = AtomicLong(System.currentTimeMillis())

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    fun initialize(context: Context) {
        _transactions.value = getTransactions(context)
        Log.d(TAG, "initialize: Loaded ${(_transactions.value?.size ?: 0)} transactions")
    }

    private fun getPrefs(context: Context): SharedPreferences {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Log.d(TAG, "getPrefs: Context=${context.applicationContext}")
        return prefs
    }

    fun clearData(context: Context) {
        val success = getPrefs(context).edit().clear().commit()
        Log.d(TAG, "clearData: SharedPreferences cleared, Success=$success")
        _transactions.value = emptyList()
    }

    fun saveTransaction(context: Context, transaction: Transaction) {
        val transactions = getTransactions(context).toMutableList()
        val newTransaction = transaction.copy(id = idGenerator.getAndIncrement())
        transactions.add(newTransaction)
        val json = Gson().toJson(transactions)
        val success = getPrefs(context).edit().putString(KEY_TRANSACTIONS, json).commit()
        Log.d(TAG, "saveTransaction: JSON=$json, Success=$success")
        _transactions.value = transactions
        checkBudget(context)
    }

    fun updateTransaction(context: Context, updatedTransaction: Transaction) {
        val transactions = getTransactions(context).toMutableList()
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            transactions[index] = updatedTransaction
            val json = Gson().toJson(transactions)
            val success = getPrefs(context).edit().putString(KEY_TRANSACTIONS, json).commit()
            Log.d(TAG, "updateTransaction: JSON=$json, Success=$success")
            _transactions.value = transactions
            checkBudget(context)
        }
    }

    fun deleteTransaction(context: Context, transactionId: Long) {
        val transactions = getTransactions(context).toMutableList()
        transactions.removeIf { it.id == transactionId }
        val json = Gson().toJson(transactions)
        val success = getPrefs(context).edit().putString(KEY_TRANSACTIONS, json).commit()
        Log.d(TAG, "deleteTransaction: JSON=$json, Success=$success")
        _transactions.value = transactions
        checkBudget(context)
    }

    fun getTransactions(context: Context): List<Transaction> {
        val json = getPrefs(context).getString(KEY_TRANSACTIONS, null)
        Log.d(TAG, "getTransactions: JSON=$json")
        if (json == null) {
            return emptyList()
        }
        return try {
            val type = object : TypeToken<List<Transaction>>() {}.type
            val transactions = Gson().fromJson<List<Transaction>>(json, type) ?: emptyList()
            Log.d(TAG, "getTransactions: Loaded ${transactions.size} transactions")
            transactions
        } catch (e: Exception) {
            Log.e(TAG, "getTransactions: Failed to parse JSON", e)
            emptyList()
        }
    }

    fun saveBudget(context: Context, budget: Float) {
        val success = getPrefs(context).edit().putFloat(KEY_BUDGET, budget).commit()
        Log.d(TAG, "saveBudget: Budget=$budget, Success=$success")
        checkBudget(context)
    }

    fun getBudget(context: Context): Float {
        val budget = getPrefs(context).getFloat(KEY_BUDGET, 0f)
        Log.d(TAG, "getBudget: Budget=$budget")
        return budget
    }

    fun saveCurrency(context: Context, currency: String) {
        val success = getPrefs(context).edit().putString(KEY_CURRENCY, currency).commit()
        Log.d(TAG, "saveCurrency: Currency=$currency, Success=$success")
    }

    fun getCurrency(context: Context): String {
        val currency = getPrefs(context).getString(KEY_CURRENCY, "$") ?: "$"
        Log.d(TAG, "getCurrency: Currency=$currency")
        return currency
    }

    fun exportBackup(context: Context): Pair<Boolean, String> {
        try {
            val state = Environment.getExternalStorageState()
            if (state != Environment.MEDIA_MOUNTED) {
                Log.d(TAG, "exportBackup: External storage not mounted, State=$state")
                return Pair(false, "External storage unavailable")
            }
            val transactions = getTransactions(context)
            val json = Gson().toJson(transactions)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val backupFile = File(downloadsDir, BACKUP_FILE)
            backupFile.parentFile?.mkdirs()
            if (!backupFile.parentFile?.exists()!!) {
                Log.d(TAG, "exportBackup: Failed to create directory ${backupFile.parentFile?.absolutePath}")
                return Pair(false, "Failed to create backup directory")
            }
            FileWriter(backupFile).use { it.write(json) }
            Log.d(TAG, "exportBackup: Success, Path=${backupFile.absolutePath}, JSON=$json")
            return Pair(true, "Backup exported to Downloads: ${backupFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "exportBackup: Failed", e)
            return Pair(false, "Export failed: ${e.message}")
        }
    }

    fun importBackup(context: Context, uri: Uri): Pair<Boolean, String> {
        try {
            val contentResolver = context.contentResolver
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "importBackup: Read JSON=$json")
                val type = object : TypeToken<List<Transaction>>() {}.type
                val transactions: List<Transaction> = Gson().fromJson(json, type)
                    ?: return Pair(false, "Invalid backup data")
                val success = getPrefs(context).edit().putString(KEY_TRANSACTIONS, json).commit()
                if (!success) {
                    Log.d(TAG, "importBackup: Failed to write to SharedPreferences")
                    return Pair(false, "Failed to save backup data")
                }
                _transactions.value = transactions
                checkBudget(context)
                Log.d(TAG, "importBackup: Success, Transactions=${transactions.size}")
                return Pair(true, "Backup imported successfully")
            } ?: return Pair(false, "Failed to read file")
        } catch (e: Exception) {
            Log.e(TAG, "importBackup: Failed", e)
            return Pair(false, "Import failed: ${e.message}")
        }
    }

    private fun checkBudget(context: Context) {
        val budget = getBudget(context)
        if (budget <= 0) return
        val transactions = getTransactions(context)
        val totalExpenses = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        val expensePercentage = (totalExpenses / budget) * 100
        if (expensePercentage >= 90) {

        }
        Log.d(TAG, "checkBudget: Budget=$budget, Expenses=$totalExpenses, Percentage=$expensePercentage")
    }
}