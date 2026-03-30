package com.example.lab1

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "figures")
data class Figure(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val figureName: String,
    val area: Double,
    val points: String // points serialized as "lat1,lon1;lat2,lon2;..."
)