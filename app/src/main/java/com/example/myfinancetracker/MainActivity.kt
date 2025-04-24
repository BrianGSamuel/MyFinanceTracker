package com.example.financetracker

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.io.Serializable

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up ViewModel and lifecycle owner
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Set up RecyclerView adapter with click listener
        adapter = TransactionAdapter(viewModel) { transaction ->
            val intent = Intent(this, AddTransactionActivity::class.java).apply {
                putExtra("transaction", transaction as Serializable)
            }
            startActivity(intent)
        }
        binding.rvTransactions.adapter = adapter

        // Set up Spinner for filtering
        val filterOptions = arrayOf("All", "Income", "Expense")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spFilter.adapter = spinnerAdapter

        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedFilter = filterOptions[position]
                viewModel.setFilter(selectedFilter)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                viewModel.setFilter("All")
            }
        }

        // Set up swipe-to-delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val transaction = adapter.currentList[position]
                val transactions = adapter.currentList.toMutableList()
                transactions.removeAt(position)
                adapter.submitList(transactions)

                DataManager.deleteTransaction(this@MainActivity, transaction.id)

                Snackbar.make(binding.root, "Transaction deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        transactions.add(position, transaction)
                        adapter.submitList(transactions)
                        DataManager.saveTransaction(this@MainActivity, transaction)
                    }
                    .setBackgroundTint(Color.WHITE)
                    .setTextColor(Color.BLACK)
                    .setActionTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_blue_light))
                    .show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val paint = android.graphics.Paint().apply {
                    color = ContextCompat.getColor(this@MainActivity, R.color.expense_color) // Red #F44336
                }

                // Draw red background
                c.drawRect(
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat(),
                    paint
                )

                // Draw delete icon within red area
                val deleteIcon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_delete)
                if (deleteIcon != null && dX < 0) { // Only draw when swiping left
                    val iconSize = (25 * resources.displayMetrics.density).toInt() // 25dp in pixels
                    val iconMargin = (itemView.height - iconSize) / 2
                    val iconTop = itemView.top + iconMargin
                    val iconBottom = iconTop + iconSize
                    val redAreaWidth = -dX.toInt()
                    val iconRight = itemView.right - iconMargin
                    val iconLeft = (iconRight - iconSize).coerceAtLeast(itemView.right - redAreaWidth + iconMargin)
                    if (iconLeft >= itemView.right - redAreaWidth) {
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        deleteIcon.draw(c)
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvTransactions)

        // Set up FAB click listener
        binding.btnAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        // Observe filtered transactions
        viewModel.filteredTransactions.observe(this) { transactions ->
            adapter.submitList(transactions)
        }

        // Set up Bottom Navigation Bar
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true // Already on Home
                R.id.nav_analytics -> {
                    startActivity(Intent(this, AnalyticsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_transactions -> {
                    startActivity(Intent(this, TransactionsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_backup -> {
                startActivity(Intent(this, BackupActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}