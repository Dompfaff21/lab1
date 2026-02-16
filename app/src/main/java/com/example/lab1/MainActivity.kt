package com.example.lab1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var latitudeEditText: EditText
    private lateinit var longitudeEditText: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCalculate: Button
    private lateinit var btnChangeLanguage: Button
    private lateinit var recyclerView: RecyclerView

    private val sides = mutableListOf<SidePair>()
    private lateinit var adapter: SidesAdapter

    private val SIDES_KEY = "sides_list"
    private val PREFS_NAME = "app_prefs"
    private val LANGUAGE_KEY = "language"

    // Для геолокации
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

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

        latitudeEditText = findViewById(R.id.latitude)
        longitudeEditText = findViewById(R.id.longitude)
        btnSave = findViewById(R.id.btnSave)
        btnCalculate = findViewById(R.id.btnCalculate)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = SidesAdapter(sides)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Инициализация клиента геолокации
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Запрос геопозиции при старте
        requestLocationAndUpdateFields()

        btnSave.setOnClickListener {
            val latStr = latitudeEditText.text.toString()
            val lonStr = longitudeEditText.text.toString()

            if (latStr.isEmpty() || lonStr.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_fill_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val lat = latStr.toDouble()
                val lon = lonStr.toDouble()
                val sidePair = SidePair(lat, lon)  // порядок важен: сначала широта, потом долгота
                adapter.addSidePair(sidePair)
                latitudeEditText.text.clear()
                longitudeEditText.text.clear()
            } catch (_: NumberFormatException) {
                Toast.makeText(this, getString(R.string.toast_invalid_number), Toast.LENGTH_SHORT).show()
            }
        }

        btnCalculate.setOnClickListener {
            // здесь остаётся логика расчёта, если она нужна
        }

        btnChangeLanguage.setOnClickListener {
            toggleLanguage()
        }
    }

    private fun requestLocationAndUpdateFields() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                // Заполняем поля: широта в latitude, долгота в longitude
                latitudeEditText.setText(it.latitude.toString())
                longitudeEditText.setText(it.longitude.toString())
            } ?: run {
                Toast.makeText(this, "Не удалось получить местоположение", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation()
                } else {
                    Toast.makeText(this, "Разрешение на геолокацию отклонено", Toast.LENGTH_SHORT).show()
                }
            }
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