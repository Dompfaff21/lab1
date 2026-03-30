package com.example.lab1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

class FigureListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FigureAdapter
    private lateinit var database: AppDatabase
    private lateinit var figureDao: FigureDao
    private lateinit var btnChangeLanguage: Button
    private lateinit var titleTextView: TextView
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        loadLanguage()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_figure_list)

        recyclerView = findViewById(R.id.recyclerViewFigures)
        recyclerView.layoutManager = LinearLayoutManager(this)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        titleTextView = findViewById(R.id.title)
        btnBack = findViewById(R.id.btnBack)

        database = AppDatabase.getInstance(this)
        figureDao = database.figureDao()

        adapter = FigureAdapter(
            mutableListOf(),
            onItemClick = { figure ->
                val intent = Intent(this, FigureDetailActivity::class.java)
                intent.putExtra("figure_id", figure.id)
                startActivity(intent)
            },
            onDeleteClick = { figure ->
                showDeleteConfirmation(figure)
            }
        )
        recyclerView.adapter = adapter

        loadFigures()

        btnChangeLanguage.setOnClickListener {
            toggleLanguage()
        }

        btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        loadFigures()
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        updateUITexts()
    }

    private fun loadFigures() {
        lifecycleScope.launch {
            val figures = withContext(Dispatchers.IO) {
                figureDao.getAll()
            }
            adapter.updateList(figures)
        }
    }

    private fun showDeleteConfirmation(figure: Figure) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_confirmation, figure.getLocalizedName(this))) // dynamic
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteFigure(figure)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteFigure(figure: Figure) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                figureDao.deleteById(figure.id)
            }
            loadFigures()
            Toast.makeText(this@FigureListActivity, R.string.deleted, Toast.LENGTH_SHORT).show()
        }
    }

    // ------------------ Language ------------------

    private fun toggleLanguage() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val currentLang = prefs.getString(LANGUAGE_KEY, "ru") ?: "ru"
        val newLang = if (currentLang == "ru") "en" else "ru"
        saveLanguage(newLang)
        setLocale(newLang)
        // No recreate() – UI will update in onConfigurationChanged
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

    private fun updateUITexts() {
        btnChangeLanguage.setText(R.string.btn_language)
        titleTextView.text = getString(R.string.saved_figures)
        btnBack.text = getString(R.string.back)
        loadFigures()   // this reloads the list, which triggers the adapter to rebind each item
    }

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val LANGUAGE_KEY = "language"
    }
}