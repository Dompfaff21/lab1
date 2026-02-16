package com.example.lab1

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var widthEditText: EditText
    private lateinit var lengthEditText: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCalculate: Button
    private lateinit var btnChangeLanguage: Button
    private lateinit var recyclerView: RecyclerView

    private val sides = mutableListOf<SidePair>()
    private lateinit var adapter: SidesAdapter

    private val SIDES_KEY = "sides_list"
    private val PREFS_NAME = "app_prefs"
    private val LANGUAGE_KEY = "language"

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(SIDES_KEY, ArrayList(sides))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        @Suppress("DEPRECATION")
        savedInstanceState.getParcelableArrayList<SidePair>(SIDES_KEY)?.let {
            sides.clear()
            sides.addAll(it)
            @Suppress("NotifyDataSetChanged")
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLanguage()
        setContentView(R.layout.activity_main)

        widthEditText = findViewById(R.id.Width)
        lengthEditText = findViewById(R.id.Length)
        btnSave = findViewById(R.id.btnSave)
        btnCalculate = findViewById(R.id.btnCalculate)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = SidesAdapter(sides)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnSave.setOnClickListener {
            val side1Str = widthEditText.text.toString()
            val side2Str = lengthEditText.text.toString()

            if (side1Str.isEmpty() || side2Str.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_fill_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val side1 = side1Str.toDouble()
                val side2 = side2Str.toDouble()
                val sidePair = SidePair(side1, side2)
                adapter.addSidePair(sidePair)
                widthEditText.text.clear()
                lengthEditText.text.clear()
            } catch (_: NumberFormatException) {
                Toast.makeText(this, getString(R.string.toast_invalid_number), Toast.LENGTH_SHORT).show()
            }
        }

        btnCalculate.setOnClickListener {

        }

        btnChangeLanguage.setOnClickListener {
            toggleLanguage()
        }
    }

    private fun toggleLanguage() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val currentLang = prefs.getString(LANGUAGE_KEY, "ru") ?: "ru"
        val newLang = if (currentLang == "ru") "en" else "ru"
        saveLanguage(newLang)
        setLocale(newLang)
    }

    private fun saveLanguage(lang: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, lang).apply()
    }

    private fun loadLanguage() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lang = prefs.getString(LANGUAGE_KEY, "ru") ?: "ru"
        setLocale(lang)
    }

    private fun setLocale(lang: String) {
        val locale = Locale.forLanguageTag(lang)
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}