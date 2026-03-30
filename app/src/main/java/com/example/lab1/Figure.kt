package com.example.lab1

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.content.Context

fun Figure.getLocalizedName(context: Context): String {
    val pointCount = points.split(";").size
    return when (pointCount) {
        1 -> context.getString(R.string.figure_point)
        2 -> context.getString(R.string.figure_circle)
        3 -> context.getString(R.string.figure_triangle)
        else -> context.getString(R.string.figure_polygon, pointCount)
    }
}

@Entity(tableName = "figures")
data class Figure(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val figureName: String,
    val area: Double,
    val points: String // points serialized as "lat1,lon1;lat2,lon2;..."
)