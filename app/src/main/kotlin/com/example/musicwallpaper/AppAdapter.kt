package com.example.musicwallpaper

import android.widget.CheckBox
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private val items: List<AppItem>,
    private val onChanged: (List<AppItem>) -> Unit
) : RecyclerView.Adapter<AppAdapter.VH>() {

    class VH(val checkBox: CheckBox) : RecyclerView.ViewHolder(checkBox)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val cb = CheckBox(parent.context)
        return VH(cb)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.checkBox.text = item.label
        holder.checkBox.isChecked = item.selected

        holder.checkBox.setOnCheckedChangeListener(null)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.selected = isChecked
            onChanged(items)
        }
    }

    override fun getItemCount() = items.size
}