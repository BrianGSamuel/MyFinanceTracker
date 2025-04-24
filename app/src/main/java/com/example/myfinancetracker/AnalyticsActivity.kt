package com.example.financetracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.databinding.ActivityAnalyticsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class AnalyticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnalyticsBinding
    private val viewModel: MainViewModel by viewModels()
    private val categories = listOf("Food", "Transport", "Bills", "Entertainment", "Other")
    private val TAG = "AnalyticsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityAnalyticsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Set up data binding
            binding.viewModel = viewModel
            binding.lifecycleOwner = this

            // Set up Bottom Navigation Bar
            binding.bottomNavigation.selectedItemId = R.id.nav_analytics
            binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
                try {
                    when (item.itemId) {
                        R.id.nav_home -> {
                            startActivity(Intent(this, MainActivity::class.java))
                            overridePendingTransition(0, 0) // Disable transition animation
                            finish()
                            true
                        }
                        R.id.nav_analytics -> true // Already on Analytics
                        R.id.nav_transactions -> {
                            startActivity(Intent(this, TransactionsActivity::class.java))
                            overridePendingTransition(0, 0) // Disable transition animation
                            true
                        }
                        R.id.nav_settings -> {
                            startActivity(Intent(this, SettingsActivity::class.java))
                            overridePendingTransition(0, 0) // Disable transition animation
                            finish()
                            true
                        }
                        else -> false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation error: ${e.message}", e)
                    false
                }
            }

            // Observe transactions to update both charts
            viewModel.transactions.observe(this) { transactions ->
                updatePieChart(transactions)
                updateBarChart(transactions)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AnalyticsActivity: ${e.message}", e)
            finish()
        }
    }

    private fun updatePieChart(transactions: List<Transaction>?) {
        try {
            if (transactions == null) {
                binding.pieChart.clear()
                binding.pieChart.setNoDataText("No transactions available")
                return
            }

            val expenseByCategory = categories.associateWith { category ->
                transactions.filter { it.type == "expense" && it.category == category }
                    .sumOf { it.amount }.toFloat()
            }.filter { it.value > 0 }

            val entries = expenseByCategory.map { (category, amount) ->
                PieEntry(amount, category)
            }

            if (entries.isNotEmpty()) {
                val dataSet = PieDataSet(entries, "Expenses by Category").apply {
                    setColors(*ColorTemplate.MATERIAL_COLORS)
                    valueTextSize = 12f
                    valueTextColor = Color.BLACK
                }

                val pieData = PieData(dataSet)
                binding.pieChart.apply {
                    data = pieData
                    description.isEnabled = false
                    isRotationEnabled = true
                    setEntryLabelColor(Color.BLACK)
                    animateY(1000)
                    invalidate()
                }
            } else {
                binding.pieChart.clear()
                binding.pieChart.setNoDataText("No expenses to display")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating pie chart: ${e.message}", e)
            binding.pieChart.clear()
            binding.pieChart.setNoDataText("Error loading chart")
        }
    }

    private fun updateBarChart(transactions: List<Transaction>?) {
        try {
            if (transactions == null) {
                binding.barChart.clear()
                binding.barChart.setNoDataText("No transactions available")
                return
            }

            val expenseByCategory = categories.associateWith { category ->
                transactions.filter { it.type == "expense" && it.category == category }
                    .sumOf { it.amount }.toFloat()
            }.filter { it.value > 0 }

            val entries = expenseByCategory.entries.mapIndexed { index, (category, amount) ->
                BarEntry(index.toFloat(), amount)
            }

            if (entries.isNotEmpty()) {
                val dataSet = BarDataSet(entries, "Expenses by Category").apply {
                    setColors(*ColorTemplate.MATERIAL_COLORS)
                    valueTextSize = 12f
                    valueTextColor = Color.BLACK
                }

                val barData = BarData(dataSet).apply {
                    barWidth = 0.4f
                }

                binding.barChart.apply {
                    data = barData
                    description.isEnabled = false
                    setFitBars(true)
                    xAxis.apply {
                        valueFormatter = IndexAxisValueFormatter(expenseByCategory.keys.toList())
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        granularity = 1f
                        labelCount = expenseByCategory.size
                    }
                    axisLeft.apply {
                        setDrawGridLines(false)
                        axisMinimum = 0f
                    }
                    axisRight.isEnabled = false
                    animateY(1000)
                    invalidate()
                }
            } else {
                binding.barChart.clear()
                binding.barChart.setNoDataText("No expenses to display")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating bar chart: ${e.message}", e)
            binding.barChart.clear()
            binding.barChart.setNoDataText("Error loading chart")
        }
    }
}