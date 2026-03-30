package com.example.lab1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FigureAdapter(
    private var figures: List<Figure>,
    private val onItemClick: (Figure) -> Unit,
    private val onDeleteClick: (Figure) -> Unit
) : RecyclerView.Adapter<FigureAdapter.FigureViewHolder>() {

    class FigureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvFigureName)
        val tvArea: TextView = itemView.findViewById(R.id.tvFigureArea)
        val tvPointsCount: TextView = itemView.findViewById(R.id.tvPointsCount)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FigureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.figure_card, parent, false)
        return FigureViewHolder(view)
    }

    override fun onBindViewHolder(holder: FigureViewHolder, position: Int) {
        val figure = figures[position]
        holder.tvName.text = figure.figureName
        val areaText = holder.itemView.context.getString(R.string.area_prefix) + " " + String.format("%.4f", figure.area)
        holder.tvArea.text = areaText
        val pointsCount = figure.points.split(";").size
        val pointsText = holder.itemView.context.getString(R.string.points_prefix) + " $pointsCount"
        holder.tvPointsCount.text = pointsText
        holder.itemView.setOnClickListener { onItemClick(figure) }
        holder.btnDelete.setOnClickListener { onDeleteClick(figure) }
    }

    override fun getItemCount() = figures.size

    fun updateList(newList: List<Figure>) {
        figures = newList
        notifyDataSetChanged()
    }
}