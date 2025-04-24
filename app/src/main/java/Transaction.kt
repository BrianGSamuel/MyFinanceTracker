package com.example.financetracker

import java.io.Serializable
import java.util.Date

data class Transaction(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val amount: Double,
    val category: String,
    val type: String, // "income" or "expense"
    val date: Date = Date()
) : Serializable