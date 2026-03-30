package com.example.lab1

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class FigureDetailActivity : AppCompatActivity() {

    private lateinit var tvFigureName: TextView
    private lateinit var tvArea: TextView
    private lateinit var recyclerViewPoints: RecyclerView
    private lateinit var adapter: PointsAdapter
    private lateinit var btnChangeLanguage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        loadLanguage()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_figure_detail)

        tvFigureName = findViewById(R.id.tvDetailFigureName)
        tvArea = findViewById(R.id.tvDetailArea)
        recyclerViewPoints = findViewById(R.id.recyclerViewPoints)
        recyclerViewPoints.layoutManager = LinearLayoutManager(this)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguageDetail)

        val figureId = intent.getIntExtra("figure_id", 0)
        if (figureId == 0) {
            finish()
            return
        }

        loadFigure(figureId)

        btnChangeLanguage.setOnClickListener {
            toggleLanguage()
        }
    }

    private fun loadFigure(figureId: Int) {
        lifecycleScope.launch {
            val figure = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@FigureDetailActivity).figureDao().getFigureById(figureId)
            }
            figure?.let {
                tvFigureName.text = it.figureName
                val areaText = getString(R.string.area_prefix) + " " + String.format("%.6f", it.area)
                tvArea.text = areaText
                val pointsList = it.points.split(";").mapNotNull { pointStr ->
                    val parts = pointStr.split(",")
                    if (parts.size == 2) {
                        SidePair(parts[0].toDoubleOrNull() ?: 0.0, parts[1].toDoubleOrNull() ?: 0.0)
                    } else null
                }
                adapter = PointsAdapter(pointsList)
                recyclerViewPoints.adapter = adapter
            }
        }
    }

    // Language methods
    private fun toggleLanguage() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val currentLang = prefs.getString(LANGUAGE_KEY, "ru") ?: "ru"
        val newLang = if (currentLang == "ru") "en" else "ru"
        saveLanguage(newLang)
        setLocale(newLang)
        recreate()
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

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val LANGUAGE_KEY = "language"
    }
}