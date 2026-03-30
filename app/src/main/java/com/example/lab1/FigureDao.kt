package com.example.lab1

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FigureDao {
    @Insert
    suspend fun insert(figure: Figure)

    @Query("SELECT * FROM figures ORDER BY id DESC")
    suspend fun getAll(): List<Figure>

    @Query("SELECT * FROM figures WHERE id = :id")
    suspend fun getFigureById(id: Int): Figure?

    @Query("DELETE FROM figures WHERE id = :id")
    suspend fun deleteById(id: Int)
}