package com.example.lab1
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CoordinateDao {
    @Insert
    suspend fun insert(coordinate: Coordinate)

    @Query("SELECT * FROM coordinates ORDER BY id DESC")
    suspend fun getAll(): List<Coordinate>

    @Query("DELETE FROM coordinates")
    suspend fun deleteAll()
}