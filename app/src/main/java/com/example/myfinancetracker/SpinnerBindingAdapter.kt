package com.example.financetracker

import android.widget.AdapterView
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener

object SpinnerBindingAdapter {
    @BindingAdapter("selectedItem")
    @JvmStatic
    fun setSelectedItem(spinner: Spinner, item: String?) {
        if (item == null || spinner.adapter == null) return
        for (i in 0 until spinner.adapter.count) {
            if (spinner.adapter.getItem(i).toString() == item) {
                if (spinner.selectedItemPosition != i) {
                    spinner.setSelection(i)
                }
                break
            }
        }
    }

    @InverseBindingAdapter(attribute = "selectedItem", event = "selectedItemAttrChanged")
    @JvmStatic
    fun getSelectedItem(spinner: Spinner): String? {
        return spinner.selectedItem?.toString()
    }

    @BindingAdapter("selectedItemAttrChanged")
    @JvmStatic
    fun setSelectedItemListener(spinner: Spinner, listener: InverseBindingListener?) {
        if (listener == null) {
            spinner.onItemSelectedListener = null
        } else {
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    listener.onChange()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    listener.onChange()
                }
            }
        }
    }
}