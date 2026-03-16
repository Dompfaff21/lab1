package com.example.lab1
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var latitudeEditText: EditText
    private lateinit var longitudeEditText: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCalculate: Button
    private lateinit var btnChangeLanguage: Button

    private lateinit var btnClearAll: Button
    private lateinit var recyclerView: RecyclerView

    private val sides = mutableListOf<SidePair>()
    private lateinit var adapter: SidesAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private var isLocationUpdatesActive = false

    // Room database
    private lateinit var database: AppDatabase
    private lateinit var dao: CoordinateDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLanguage()
        setContentView(R.layout.activity_main)

        // Инициализация Room
        database = AppDatabase.getInstance(this)
        dao = database.coordinateDao()

        loadCoordinatesFromDatabase()

        latitudeEditText = findViewById(R.id.latitude)
        longitudeEditText = findViewById(R.id.longitude)
        btnSave = findViewById(R.id.btnSave)
        btnCalculate = findViewById(R.id.btnCalculate)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        btnClearAll = findViewById(R.id.btnClearAll)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = SidesAdapter(sides)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(10000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateLocationFields(location)
                }
            }
        }

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

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        dao.insert(Coordinate(latitude = lat, longitude = lon))
                    }
                    loadCoordinatesFromDatabase()
                    latitudeEditText.text.clear()
                    longitudeEditText.text.clear()
                }
            } catch (_: NumberFormatException) {
                Toast.makeText(this, getString(R.string.toast_invalid_number), Toast.LENGTH_SHORT).show()
            }
        }

        btnCalculate.setOnClickListener {

        }

        btnChangeLanguage.setOnClickListener {
            toggleLanguage()
        }

        btnClearAll.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Очистить все")
                .setMessage("Вы уверены, что хотите удалить все координаты?")
                .setPositiveButton("Да") { _, _ ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            dao.deleteAll()
                        }
                        loadCoordinatesFromDatabase()
                        Toast.makeText(
                            this@MainActivity,
                            "Все записи удалены",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Нет", null)
                .show()
        }
    }

    private fun loadCoordinatesFromDatabase() {
        lifecycleScope.launch {
            val coordinates = withContext(Dispatchers.IO) {
                dao.getAll()
            }
            sides.clear()
            // Преобразуем Coordinate в SidePair (можно использовать map)
            sides.addAll(coordinates.map { SidePair(it.latitude, it.longitude) })
            adapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        updateUITexts()
    }

    private fun requestLocationAndUpdateFields() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
                startLocationUpdates()
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
                updateLocationFields(it)
            } ?: run {
                Toast.makeText(this, getString(R.string.location_unavailable), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLocationFields(location: Location) {
        latitudeEditText.setText(location.latitude.toString())
        longitudeEditText.setText(location.longitude.toString())
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (!isLocationUpdatesActive) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            isLocationUpdatesActive = true
        }
    }

    private fun stopLocationUpdates() {
        if (isLocationUpdatesActive) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isLocationUpdatesActive = false
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
                    startLocationUpdates()
                } else {
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
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

    private fun updateUITexts() {
        btnChangeLanguage.setText(R.string.btn_language)
        btnSave.setText(R.string.btn_save)
        btnCalculate.setText(R.string.btn_calculate)
        btnClearAll.setText(R.string.btn_clear_all)

        latitudeEditText.hint = getString(R.string.hint_latitude)
        longitudeEditText.hint = getString(R.string.hint_longitude)

        adapter.notifyDataSetChanged()
    }

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val LANGUAGE_KEY = "language"
    }
}