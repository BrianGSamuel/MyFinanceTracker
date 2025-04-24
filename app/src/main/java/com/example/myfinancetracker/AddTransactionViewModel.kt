package com.example.financetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class AddTransactionViewModel(application: Application) : AndroidViewModel(application) {
    val title = MutableLiveData<String>()
    val amount = MutableLiveData<String>()
    val type = MutableLiveData<String>()
    val category = MutableLiveData<String>()
    val isEditMode = MutableLiveData<Boolean>()
    val successMessage = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()
    private var transactionId: Long? = null

    fun initTransaction(transaction: Transaction?) {
        if (transaction != null) {
            isEditMode.value = true
            transactionId = transaction.id
            title.value = transaction.title
            amount.value = transaction.amount.toString()
            type.value = transaction.type
            category.value = transaction.category
        } else {
            isEditMode.value = false
            type.value = "expense"
            category.value = "Food"
        }
    }

    fun saveTransaction() {
        val titleText = title.value?.takeIf { it.isNotBlank() }
        if (titleText == null) {
            errorMessage.value = "Please enter a title"
            return
        }
        val amountValue = amount.value?.toDoubleOrNull()?.takeIf { it > 0 }
        if (amountValue == null) {
            errorMessage.value = "Please enter a valid positive amount"
            return
        }
        val typeText = type.value ?: "expense"
        val categoryText = category.value ?: "Food"

        val transaction = if (isEditMode.value == true && transactionId != null) {
            Transaction(
                id = transactionId!!,
                title = titleText,
                amount = amountValue,
                category = categoryText,
                type = typeText
            )
        } else {
            Transaction(
                title = titleText,
                amount = amountValue,
                category = categoryText,
                type = typeText
            )
        }

        if (isEditMode.value == true) {
            DataManager.updateTransaction(getApplication(), transaction)
        } else {
            DataManager.saveTransaction(getApplication(), transaction)
        }

        successMessage.value = if (isEditMode.value == true) "Transaction updated" else "Transaction added"
    }
}