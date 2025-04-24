package com.example.financetracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.databinding.ItemTransactionBinding
import java.io.Serializable

class TransactionAdapter(
    private val viewModel: MainViewModel,
    private val onItemClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(TransactionDiffCallback()) {

    class ViewHolder(
        private val binding: ItemTransactionBinding,
        private val onItemClick: (Transaction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction, viewModel: MainViewModel) {
            binding.transaction = transaction
            binding.viewModel = viewModel
            binding.executePendingBindings()
            binding.root.setOnClickListener {
                onItemClick(transaction)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel)
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}