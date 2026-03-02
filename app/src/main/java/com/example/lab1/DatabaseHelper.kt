package com.example.lab1

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "coordinates.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "coordinates"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_LATITUDE REAL NOT NULL, " +
                "$COLUMN_LONGITUDE REAL NOT NULL)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertCoordinate(latitude: Double, longitude: Double): Long {
        val values = ContentValues().apply {
            put(COLUMN_LATITUDE, latitude)
            put(COLUMN_LONGITUDE, longitude)
        }
        val db = writableDatabase
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllCoordinates(): List<SidePair> {
        val list = mutableListOf<SidePair>()
        val db = readableDatabase
        val cursor: Cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        with(cursor) {
            while (moveToNext()) {
                val lat = getDouble(getColumnIndexOrThrow(COLUMN_LATITUDE))
                val lon = getDouble(getColumnIndexOrThrow(COLUMN_LONGITUDE))
                list.add(SidePair(lat, lon))
            }
            close()
        }
        return list
    }

}