package com.example.financetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import java.text.SimpleDateFormat
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    // Filter state
    private val _filter = MutableLiveData<String>("All")
    val filteredTransactions: LiveData<List<Transaction>> = _filter.map { filter ->
        when (filter) {
            "Income" -> _transactions.value?.filter { it.type == "income" } ?: emptyList()
            "Expense" -> _transactions.value?.filter { it.type == "expense" } ?: emptyList()
            else -> _transactions.value ?: emptyList() // "All"
        }
    }

    private val _budgetText = MutableLiveData<String>()
    val budgetText: LiveData<String> = _budgetText

    private val _incomeText = MutableLiveData<String>()
    val incomeText: LiveData<String> = _incomeText

    private val _expensesText = MutableLiveData<String>()
    val expensesText: LiveData<String> = _expensesText

    private val _balanceText = MutableLiveData<String>()
    val balanceText: LiveData<String> = _balanceText

    private val _categorySummaryText = MutableLiveData<String>()
    val categorySummaryText: LiveData<String> = _categorySummaryText

    private val categories = listOf("Food", "Transport", "Bills", "Entertainment", "Other", "Salary", "Interest")
    private val currency: String
        get() = DataManager.getCurrency(getApplication())

    private var hasNotified80Percent = false

    init {
        DataManager.initialize(application)
        DataManager.transactions.observeForever { transactions ->
            _transactions.value = transactions
            updateUI(transactions)
        }
    }

    fun setBudget(budgetText: String) {
        val budget = budgetText.toFloatOrNull()
        if (budget != null && budget > 0) {
            DataManager.saveBudget(getApplication(), budget)
        }
    }

    fun setFilter(filter: String) {
        _filter.value = filter
    }

    fun deleteTransaction(transaction: Transaction) {
        DataManager.deleteTransaction(getApplication(), transaction.id)
    }

    fun editTransaction(transaction: Transaction) {
        // Handled in MainActivity
    }

    fun formatDate(date: java.util.Date): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    fun formatAmount(amount: Double): String {
        return "$currency${String.format("%.2f", amount)}"
    }

    private fun updateUI(transactions: List<Transaction>) {
        val budget = DataManager.getBudget(getApplication())
        _budgetText.value = if (budget > 0) "Budget: $currency${String.format("%.2f", budget)}" else "No budget set"

        val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        val balance = totalIncome - totalExpenses

        _incomeText.value = "$currency${String.format("%.2f", totalIncome)}"
        _expensesText.value = "$currency${String.format("%.2f", totalExpenses)}"
        _balanceText.value = "$currency${String.format("%.2f", balance)}"

        val categorySummary = categories.associateWith { category ->
            transactions.filter { it.type == "expense" && it.category == category }
                .sumOf { it.amount }
        }
        val categorySummaryText = categorySummary.filter { it.value > 0 }
            .map { "${it.key}: $currency${String.format("%.2f", it.value)}" }
            .joinToString("\n")
        _categorySummaryText.value = if (categorySummaryText.isNotEmpty()) categorySummaryText else "No expenses"

        if (totalIncome > 0 && totalExpenses >= 0.8 * totalIncome && !hasNotified80Percent) {
            send80PercentNotification()
            hasNotified80Percent = true
        } else if (totalExpenses < 0.8 * totalIncome) {
            hasNotified80Percent = false
        }
    }

    private fun send80PercentNotification() {
        NotificationHelper.sendNotification(
            context = getApplication(),
            title = "Expense Alert",
            message = "Your expenses have reached 80% of your income!",
            notificationId = 1001
        )
    }
}