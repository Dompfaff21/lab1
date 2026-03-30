package com.example.lab1

import android.Manifest
import android.content.Context
import android.content.Intent
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
    private lateinit var btnAddPoint: Button
    private lateinit var btnCalculate: Button
    private lateinit var btnChangeLanguage: Button
    private lateinit var btnClearAll: Button
    private lateinit var recyclerView: RecyclerView

    // Temporary list of points (not saved to DB yet)
    private val points = mutableListOf<SidePair>()
    private lateinit var adapter: SidesAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private var isLocationUpdatesActive = false

    // Room database
    private lateinit var database: AppDatabase
    private lateinit var figureDao: FigureDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLanguage()
        setContentView(R.layout.activity_main)

        // Database
        database = AppDatabase.getInstance(this)
        figureDao = database.figureDao()

        // UI elements
        latitudeEditText = findViewById(R.id.latitude)
        longitudeEditText = findViewById(R.id.longitude)
        btnAddPoint = findViewById(R.id.btnSave) // button ID is still btnSave
        btnCalculate = findViewById(R.id.btnCalculate)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        btnClearAll = findViewById(R.id.btnClearAll)
        recyclerView = findViewById(R.id.recyclerView)

        // Adapter for the temporary points list
        adapter = SidesAdapter(points)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Location client
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

        // Get initial location
        requestLocationAndUpdateFields()

        // Buttons logic
        btnAddPoint.setOnClickListener {
            addPointFromInput()
        }

        btnCalculate.setOnClickListener {
            calculateAndSave()
        }

        btnClearAll.setOnClickListener {
            clearPoints()
        }

        btnChangeLanguage.setOnClickListener {
            toggleLanguage()
        }
    }

    private fun addPointFromInput() {
        val latStr = latitudeEditText.text.toString()
        val lonStr = longitudeEditText.text.toString()

        if (latStr.isEmpty() || lonStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_fill_fields), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val lat = latStr.toDouble()
            val lon = lonStr.toDouble()
            points.add(SidePair(lat, lon))
            adapter.notifyItemInserted(points.size - 1)
            latitudeEditText.text.clear()
            longitudeEditText.text.clear()
        } catch (_: NumberFormatException) {
            Toast.makeText(this, getString(R.string.toast_invalid_number), Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateAndSave() {
        if (points.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_fill_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val (figureNameResId, area) = when (points.size) {
            1 -> R.string.figure_point to 0.0
            2 -> {
                // Circle: first point = center, second = on circumference
                val center = points[0]
                val onCircumference = points[1]
                val radius = distanceBetween(center, onCircumference)
                val area = Math.PI * radius * radius
                R.string.figure_circle to area
            }
            3 -> {
                val area = calculatePolygonArea(points)
                R.string.figure_triangle to area
            }
            else -> {
                val area = calculatePolygonArea(points)
                // For polygons with more than 4 sides, we use the string with format
                // We'll handle it by getting the string with format
                R.string.figure_polygon to area
            }
        }

        val figureName = if (figureNameResId == R.string.figure_polygon) {
            getString(R.string.figure_polygon, points.size)
        } else {
            getString(figureNameResId)
        }

        val pointsString = points.joinToString(";") { "${it.latitude},${it.longitude}" }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                figureDao.insert(Figure(figureName = figureName, area = area, points = pointsString))
            }
            Toast.makeText(
                this@MainActivity,
                "${getString(R.string.btn_save)}: $figureName, ${getString(R.string.area_prefix)} ${String.format("%.6f", area)}",
                Toast.LENGTH_LONG
            ).show()
            clearPoints()
            startActivity(Intent(this@MainActivity, FigureListActivity::class.java))
        }
    }

    // Helper to compute distance between two SidePair points in meters
    private fun distanceBetween(p1: SidePair, p2: SidePair): Double {
        val loc1 = android.location.Location("")
        loc1.latitude = p1.latitude
        loc1.longitude = p1.longitude
        val loc2 = android.location.Location("")
        loc2.latitude = p2.latitude
        loc2.longitude = p2.longitude
        return loc1.distanceTo(loc2).toDouble()
    }

    private fun clearPoints() {
        points.clear()
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Points cleared", Toast.LENGTH_SHORT).show()
    }

    private fun saveFigure(figureName: String, area: Double, points: List<SidePair>) {
        // Serialize points: "lat1,lon1;lat2,lon2;..."
        val pointsString = points.joinToString(";") { "${it.latitude},${it.longitude}" }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                figureDao.insert(Figure(figureName = figureName, area = area, points = pointsString))
            }
            Toast.makeText(
                this@MainActivity,
                "Saved: $figureName, area = ${String.format("%.6f", area)}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Shoelace formula for polygon area (works for triangles and any n-gon)
    private fun calculatePolygonArea(points: List<SidePair>): Double {
        if (points.size < 3) return 0.0
        var area = 0.0
        val n = points.size
        for (i in 0 until n) {
            val p1 = points[i]
            val p2 = points[(i + 1) % n]
            area += p1.latitude * p2.longitude - p2.latitude * p1.longitude
        }
        return kotlin.math.abs(area) / 2.0
    }

    // ------------------ Location & permission ------------------

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

    // ------------------ Language ------------------

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
        btnAddPoint.setText(R.string.btn_save) // text is changed in strings.xml
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