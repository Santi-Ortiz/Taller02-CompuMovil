//package com.example.taller2
//
//import android.content.pm.PackageManager
//import android.location.Location
//import android.os.Bundle
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.example.taller2.databinding.ActivityMapsBinding
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.MarkerOptions
//
//class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
//
//    private lateinit var mMap: GoogleMap
//    private lateinit var binding: ActivityMapsBinding
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//
//    companion object {
//        const val REQUEST_CODE_LOCATION = 0
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMapsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//
//        requestLocationPermission()
//    }
//
//    private fun requestLocationPermission() {
//        if (!::mMap.isInitialized) return
//
//        if (ContextCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            mMap.isMyLocationEnabled = true
//            getCurrentLocation()  // Obtener la ubicación actual si ya se tienen los permisos
//        } else {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(
//                    this,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION
//                )
//            ) {
//                Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT)
//                    .show()
//            } else {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                    REQUEST_CODE_LOCATION
//                )
//            }
//        }
//    }
//
//    private fun getCurrentLocation() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//
//        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
//            if (location != null) {
//                val currentLatLng = LatLng(location.latitude, location.longitude)
//
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
//
//                mMap.addMarker(
//                    MarkerOptions().position(currentLatLng).title("Ubicación Actual")
//                )
//            } else {
//                Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                mMap.isMyLocationEnabled = true
//                getCurrentLocation()
//            } else {
//                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    override fun onResumeFragments() {
//        super.onResumeFragments()
//        if (!::mMap.isInitialized) return
//        if (ContextCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            mMap.isMyLocationEnabled = false
//            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
//        }
//    }
//}

//package com.example.taller2
//
//import android.content.pm.PackageManager
//import android.location.Location
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.example.taller2.databinding.ActivityMapsBinding
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.MarkerOptions
//import org.json.JSONArray
//import org.json.JSONObject
//import java.io.File
//import java.io.FileOutputStream
//import java.io.OutputStreamWriter
//import java.text.SimpleDateFormat
//import java.util.*
//
//class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
//
//    private lateinit var mMap: GoogleMap
//    private lateinit var binding: ActivityMapsBinding
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private var lastLocation: Location? = null
//
//    companion object {
//        const val REQUEST_CODE_LOCATION = 0
//        const val DISTANCE_THRESHOLD = 30f
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMapsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//
//        requestLocationPermission()
//    }
//
//    private fun requestLocationPermission() {
//        if (!::mMap.isInitialized) return
//
//        if (ContextCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            mMap.isMyLocationEnabled = true
//            getCurrentLocation()
//        } else {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(
//                    this,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION
//                )
//            ) {
//                Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT)
//                    .show()
//            } else {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                    REQUEST_CODE_LOCATION
//                )
//            }
//        }
//    }
//
//    private fun getCurrentLocation() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//
//        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
//            if (location != null) {
//                if (lastLocation == null || location.distanceTo(lastLocation!!) > DISTANCE_THRESHOLD) {
//                    lastLocation = location
//                    val currentLatLng = LatLng(location.latitude, location.longitude)
//
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
//
//                    mMap.clear()
//                    mMap.addMarker(
//                        MarkerOptions().position(currentLatLng).title("Ubicación Actual")
//                    )
//
//                    //saveLocationToJSON(location)
//
//                }
//            } else {
//                Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun saveLocationToJSON(location: Location) {
//        val latLng = JSONObject().apply {
//            put("latitude", location.latitude)
//            put("longitude", location.longitude)
//            put("timestamp", getCurrentTimestamp())
//        }
//
//        val file = File(filesDir, "locations.json")
//
//        val jsonArray: JSONArray = if (file.exists()) {
//            val content = file.readText()
//            JSONArray(content)
//        } else {
//            JSONArray()
//        }
//
//        jsonArray.put(latLng)
//
//        file.writeText(jsonArray.toString())
//
//        Log.d("MapsActivity", "Ubicación guardada: $latLng")
//    }
//
//    private fun getCurrentTimestamp(): String {
//        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//        return sdf.format(Date())
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                mMap.isMyLocationEnabled = true
//                getCurrentLocation()
//            } else {
//                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    override fun onResumeFragments() {
//        super.onResumeFragments()
//        if (!::mMap.isInitialized) return
//        if (ContextCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            mMap.isMyLocationEnabled = false
//            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
//        }
//    }
//}

package com.example.taller2

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var lastLocation: Location? = null
    private var currentMarker: Marker? = null

    companion object {
        const val REQUEST_CODE_LOCATION = 0
        const val DISTANCE_THRESHOLD = 30f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        requestLocationPermission()
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
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
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

            if (currentMarker != null) {
                currentMarker?.remove()
            }
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
        Log.i("LOCATION", "Ubicacion: ${file}")

        val jsonArray: JSONArray = if (file.exists()) {
            val content = file.readText()
            JSONArray(content)
        } else {
            JSONArray()
        }

        jsonArray.put(latLng)

        file.writeText(jsonArray.toString())

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
    }
}
