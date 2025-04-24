package com.example.financetracker

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.databinding.ActivityTransactionsBinding
import com.google.android.material.snackbar.Snackbar
import java.io.Serializable

class TransactionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionsBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter
    private val TAG = "TransactionsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityTransactionsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.viewModel = viewModel
            binding.lifecycleOwner = this

            // Set up RecyclerView with LinearLayoutManager
            binding.rvTransactions.layoutManager = LinearLayoutManager(this)
            adapter = TransactionAdapter(viewModel) { transaction ->
                try {
                    val intent = Intent(this, AddTransactionActivity::class.java).apply {
                        putExtra("transaction", transaction as Serializable)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to AddTransactionActivity: ${e.message}", e)
                }
            }
            binding.rvTransactions.adapter = adapter

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

                    DataManager.deleteTransaction(this@TransactionsActivity, transaction.id)

                    Snackbar.make(binding.root, "Transaction deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            transactions.add(position, transaction)
                            adapter.submitList(transactions)
                            DataManager.saveTransaction(this@TransactionsActivity, transaction)
                        }
                        .setBackgroundTint(Color.WHITE)
                        .setTextColor(Color.BLACK)
                        .setActionTextColor(ContextCompat.getColor(this@TransactionsActivity, android.R.color.holo_blue_light))
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
                        color = ContextCompat.getColor(this@TransactionsActivity, R.color.expense_color) // Red #F44336
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
                    val deleteIcon = ContextCompat.getDrawable(this@TransactionsActivity, R.drawable.ic_delete)
                    if (deleteIcon != null && dX < 0) {
                        val iconSize = (25 * resources.displayMetrics.density).toInt()
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
                try {
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to AddTransactionActivity: ${e.message}", e)
                }
            }

            // Observe transactions
            viewModel.transactions.observe(this) { transactions ->
                adapter.submitList(transactions)
            }

            // Set up Bottom Navigation Bar
            binding.bottomNavigation.selectedItemId = R.id.nav_transactions
            binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
                try {
                    when (item.itemId) {
                        R.id.nav_home -> {
                            startActivity(Intent(this, MainActivity::class.java))
                            overridePendingTransition(0, 0) // Disable transition animation
                            finish()
                            true
                        }
                        R.id.nav_analytics -> {
                            startActivity(Intent(this, AnalyticsActivity::class.java))
                            overridePendingTransition(0, 0) // Disable transition animation
                            finish()
                            true
                        }
                        R.id.nav_transactions -> true // Already on Transactions
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
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TransactionsActivity: ${e.message}", e)
            finish()
        }
    }
}