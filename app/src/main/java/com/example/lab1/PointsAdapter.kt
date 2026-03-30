package com.example.lab1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PointsAdapter(private val points: List<SidePair>) :
    RecyclerView.Adapter<PointsAdapter.PointViewHolder>() {

    class PointViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPoint: TextView = itemView.findViewById(R.id.Sides) // fixed ID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_point, parent, false)
        return PointViewHolder(view)
    }

    override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
        val point = points[position]
        holder.tvPoint.text = holder.itemView.context.getString(
            R.string.side_display,
            point.latitude,
            point.longitude
        )
    }

    override fun getItemCount() = points.size
}