package com.example.financetracker

import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter

object ImageViewBindingAdapter {
    @BindingAdapter("categoryIcon")
    @JvmStatic
    fun setCategoryIcon(imageView: ImageView, category: String?) {
        val drawableRes = when (category) {
            "Food" -> R.drawable.ic_food
            "Transport" -> R.drawable.ic_transport
            "Bills" -> R.drawable.ic_bills
            "Entertainment" -> R.drawable.ic_entertainment
            "Salary" -> R.drawable.ic_salary
            "Income" -> R.drawable.ic_payment
            else -> R.drawable.ic_other
        }
        imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, drawableRes))
    }
}