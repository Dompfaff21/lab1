package com.example.lab1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SidesAdapter(private val sides: MutableList<SidePair>) :
    RecyclerView.Adapter<SidesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val Sides: TextView = itemView.findViewById(R.id.Sides)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_point, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sidePair = sides[position]
        holder.Sides.text = holder.itemView.context.getString(
            R.string.side_display,
            sidePair.width,
            sidePair.length
        )
    }

    override fun getItemCount() = sides.size

    fun addSidePair(sidePair: SidePair) {
        sides.add(sidePair)
        notifyItemInserted(sides.size - 1)
    }
}