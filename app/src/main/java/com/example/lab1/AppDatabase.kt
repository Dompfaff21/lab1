package com.example.lab1

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Figure::class], // only keep Figure table
    version = 2,               // version increased
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun figureDao(): FigureDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "figures.db" // changed name to avoid confusion
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}