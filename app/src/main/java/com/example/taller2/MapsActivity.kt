package com.example.taller2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller2.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var lastLocation: Location? = null
    private var currentLocation: Location? = null
    private var currentMarker: Marker? = null

    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightSensorListener: SensorEventListener

    companion object {
        const val REQUEST_CODE_LOCATION = 0
        const val DISTANCE_THRESHOLD = 15f
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return

                for (location in locationResult.locations) {
                    handleNewLocation(location)
                }
            }
        }

        lightSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (mMap != null) {
                    if (event.values[0] < 5000){
                        Log.i("MAPS", "DARK MAP " + event.values[0])
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.night_style))
                    } else {
                        Log.i("MAPS", "LIGHT MAP " + event.values[0])
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.retro_style))
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        binding.editTextMaps.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND) {
                val address = binding.editTextMaps.text.toString()
                if (address.isNotEmpty()) {
                    buscarDireccion(address)
                } else {
                    Toast.makeText(this, "Ingrese una dirección", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun buscarDireccion(direccion: String) {
        val mGeocoder = Geocoder(baseContext, Locale.getDefault())
        if (direccion.isNotEmpty()) {
            try {
                val geocodeListener = object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (addresses.isNotEmpty()) {
                            val addressResult = addresses[0]
                            val position = LatLng(addressResult.latitude, addressResult.longitude)

                            runOnUiThread {
                                Log.i("success marcador", "MARCADOR AGREGADO EN ${position.longitude} ${position.latitude}")

                                if (mMap != null) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
                                    mMap.addMarker(
                                        MarkerOptions().position(position).title("Marcador en LONG:${position.longitude} LAT:${position.latitude}")
                                    )
                                    distanceToMark(position)
                                } else {
                                    Toast.makeText(
                                        this@MapsActivity,
                                        "Mapa no está inicializado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@MapsActivity,
                                    "Dirección no encontrada",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        runOnUiThread {
                            Toast.makeText(
                                this@MapsActivity,
                                "Ubicación difícil de geocodificar.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                mGeocoder.getFromLocationName(direccion, 1, geocodeListener)
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            runOnUiThread {
                Toast.makeText(this, "La dirección está vacía", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun obtenerDireccionYAgregarMarcador(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())

        val geocodeListener = object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                if (addresses.isNotEmpty()) {
                    val address: Address = addresses[0]
                    val addressText = address.getAddressLine(0) ?: "Dirección no disponible"

                    runOnUiThread {
                        if (mMap != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                            mMap.addMarker(MarkerOptions().position(latLng).title(addressText))
                            distanceToMark(latLng)
                        } else {
                            Toast.makeText(
                                this@MapsActivity,
                                "Mapa no está inicializado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Log.i("success marcador", "MARCADOR AGREGADO EN ${addressText}")
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MapsActivity, "No se encontró ninguna dirección.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onError(errorMessage: String?) {
                runOnUiThread {
                    Toast.makeText(this@MapsActivity, "Ubicación difícil de geocodificar.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1, geocodeListener)
    }

    private fun distanceToMark(latLng: LatLng) {
        if (lastLocation != null) {
            val markerLocation = Location("").apply {
                latitude = latLng.latitude
                longitude = latLng.longitude
            }

            val distance = lastLocation!!.distanceTo(markerLocation)

            Toast.makeText(this, "Distancia al marcador (linea recta): $distance metros", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "No se ha obtenido la ubicación actual", Toast.LENGTH_SHORT).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.retro_style))

        requestLocationPermission()

        mMap.setOnMapLongClickListener { latLng ->
            obtenerDireccionYAgregarMarcador(latLng)
        }

        getLastLocation()
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                mMap.addMarker(MarkerOptions().position(currentLatLng).title("Ubicación Actual"))
            } else {
                startLocationUpdates()
            }
        }
    }

    private fun requestLocationPermission() {
        if (!::mMap.isInitialized) return

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            startLocationUpdates()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT)
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_LOCATION
                )
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun handleNewLocation(location: Location) {
        if (lastLocation == null || location.distanceTo(lastLocation!!) > DISTANCE_THRESHOLD) {
            lastLocation = location
            val currentLatLng = LatLng(location.latitude, location.longitude)

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

            currentMarker?.remove()
            currentMarker = mMap.addMarker(
                MarkerOptions().position(currentLatLng).title("Ubicación Actual")
            )

            saveLocationToJSON(location)
        }
    }

    private fun saveLocationToJSON(location: Location) {
        val latLng = JSONObject().apply {
            put("latitude", location.latitude)
            put("longitude", location.longitude)
            put("timestamp", getCurrentTimestamp())
        }

        val fileName = "locations.json"
        val file = File(filesDir, fileName)
        Log.i("LOCATION", "Ubicación: ${file}")

        val jsonArray: JSONArray = if (file.exists()) {
            val content = file.readText()
            JSONArray(content)
        } else {
            JSONArray()
        }

        jsonArray.put(latLng)

        file.writeText(jsonArray.toString(4))

        Log.d("MapsActivity", "Ubicación guardada: $latLng")
        Toast.makeText(this, "Ubicacion guardada", Toast.LENGTH_SHORT).show()

        val jsonContent = leerJsonDesdeAlmacenamientoInterno()
        if (jsonContent != null) {
            println("Contenido del archivo JSON: $jsonContent")
        } else {
            println("No se pudo leer el archivo JSON")
        }
    }

    fun leerJsonDesdeAlmacenamientoInterno(): String? {
        val fileName = "locations.json"
        val file = File(filesDir, fileName)

        if (file.exists()) {
            return file.readText()
        } else {
            println("El archivo no existe")
            return null
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestLocationPermission()
                    return
                }
                mMap.isMyLocationEnabled = true
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::mMap.isInitialized) return
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = false
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(lightSensorListener)
    }
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(lightSensorListener, lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL)
    }
}
