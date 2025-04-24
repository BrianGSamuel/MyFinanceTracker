package com.example.financetracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.databinding.ActivityAddTransactionBinding
import java.io.Serializable

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private val viewModel: AddTransactionViewModel by viewModels()
    private val categories = listOf("Food", "Transport", "Bills", "Entertainment", "Other", "Salary", "Income")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Set up Spinner adapter
        binding.spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Initialize transaction
        @Suppress("DEPRECATION")
        val transaction = intent.getSerializableExtra("transaction") as? Transaction
        viewModel.initTransaction(transaction)

        // Set up RadioGroup listener
        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            viewModel.type.value = if (checkedId == R.id.rbIncome) "income" else "expense"
        }

        // Observe messages
        viewModel.successMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            finish()
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}