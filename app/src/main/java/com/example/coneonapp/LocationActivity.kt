package com.example.coneonapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.annotation.NonNull
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.coneonapp.databinding.ActivityLocationBinding
import com.example.coneonapp.utils.LocationForegroundService
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import io.socket.client.IO
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LocationActivity : AppCompatActivity() , OnMapReadyCallback{
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var socket: io.socket.client.Socket
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private val LOCATION_PERMISSION_REQUEST = 100
    private val LOCATION_SETTINGS_REQUEST = 101
    private var warehouseMarker: Marker? = null
    private var clientMarker: Marker? = null
    var isTracking = false
    private lateinit var binding: ActivityLocationBinding
    var latitude = 0.0
    var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.ivBack.setOnClickListener {
           finish()
        }

        binding.toggleButton.setOnClickListener {
            if(!isTracking){
                startLocationService()
                isTracking = true
                drawPathOnMap()
                // val origin = LatLng(28.6139, 77.2090) // Example: Delhi
                // val destination = LatLng(28.5355, 77.3910) // Example: Noida
                // drawRouteWithDirectionsAPI(origin,destination)
            }else{
                drawPathOnMap()
                stopLocationService()
                isTracking = false
            }
        }

        connectSocket()

    }

    @SuppressLint("SuspiciousIndentation")
    private fun drawRouteWithDirectionsAPI(origin: LatLng, destination: LatLng) {
        val apiKey = "AIzaSyBbZmTsIoCG_uZrMMrfP0jbb_gD3cQM9K4"
         val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&key=$apiKey"

            try {
            val connection = URL(url).openConnection() as HttpURLConnection
             connection.requestMethod = "GET"
             connection.connect()

             val response = connection.inputStream.bufferedReader().use { it.readText() }
             val jsonResponse = JSONObject(response)
             val routes = jsonResponse.getJSONArray("routes")
             if (routes.length() > 0) {
             val overviewPolyline = routes.getJSONObject(0).getJSONObject("overview_polyline")
             val encodedPoints = overviewPolyline.getString("points")
             val path = decodePolyline(encodedPoints)

             runOnUiThread {
             val polylineOptions = PolylineOptions().addAll(path)
            .width(10f)
            .color(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
             mMap.addPolyline(polylineOptions)
             mMap.addMarker(MarkerOptions().position(origin).title("Warehouse"))
             mMap.addMarker(MarkerOptions().position(destination).title("Client"))
             mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 10f))
             }
             }
             } catch (e: Exception) {
            e.printStackTrace()
             }

    }

    private fun decodePolyline(encoded: String): List<LatLng> {
         val poly = ArrayList<LatLng>()
        var index = 0
         val len = encoded.length
         var lat = 0
         var lng = 0

         while (index < len) {
             var b: Int
             var shift = 0
            var result = 0
             do {
             b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
             shift += 5
             } while (b >= 0x20)
             val dlat = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
             lat += dlat

             shift = 0
             result = 0
             do {
             b = encoded[index++].code - 63
             result = result or (b and 0x1f shl shift)
             shift += 5
             } while (b >= 0x20)
             val dlng = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
             lng += dlng

             val latLng = LatLng(lat / 1E5, lng / 1E5)
             poly.add(latLng)
            }

         return poly
         }


    private fun drawPathOnMap() {
        val origin = LatLng(latitude, longitude) // Example: Delhi
       // val origin = LatLng(28.6139, 77.2090) // Example: Delhi
        val destination = LatLng(28.5355, 77.3910) // Example: Noida

        val polylineOptions = PolylineOptions()
            .add(origin)
            .add(destination)
            .width(10f)
            .color(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            .geodesic(true)

        mMap.addPolyline(polylineOptions)
        mMap.addMarker(MarkerOptions().position(origin).title("Warehouse"))
        mMap.addMarker(MarkerOptions().position(destination).title("Client"))
        // Focus camera to include both points
        val bounds = LatLngBounds.Builder()
            .include(origin)
            .include(destination)
            .build()
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))

    }

    private fun startLocationService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, LocationForegroundService::class.java))
        } else {
            startService(Intent(this, LocationForegroundService::class.java))
        }
    }

    private fun stopLocationService(){
        val intentService = Intent(this, LocationForegroundService::class.java)
        stopService(intentService)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("mapready","mapready")
        mMap = googleMap
        // 1. Move camera to India initially
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(22.9734, 78.6569), 5.5f))
        mMap.uiSettings.isZoomControlsEnabled = true
        if (hasLocationPermission()) {
            checkLocationEnabledAndProceed()
        } else {
            requestLocationPermission()
        }

        //startLocationUpdates()

    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationEnabledAndProceed()
        }
    }

    private fun checkLocationEnabledAndProceed() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
           // getUserLocation()
            startLocationUpdates()
        } else {
            requestEnableLocation(this)
        }
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        mMap.isMyLocationEnabled = true
        var isCalledOne = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {


                val json = JSONObject().apply {
                    put("lat", it.latitude)
                    put("lng", it.longitude)
                }
                // Emit as warehouse or client based on user type
                socket.emit("warehouse_location", json)

                latitude = location.latitude
                longitude = it.longitude
                Toast.makeText(applicationContext,"Location $longitude}",Toast.LENGTH_LONG).show()
                if(isCalledOne) {
                     val userLatLng = LatLng(it.latitude, it.longitude)
                     mMap.addMarker(MarkerOptions().position(userLatLng).title("You are here"))
                     mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

                    isCalledOne = false
                }
            } ?: Log.d("Location", "Location is null")
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun requestEnableLocation(activity: Activity) {
         locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val settingsClient = LocationServices.getSettingsClient(activity)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
           // getUserLocation()
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(activity, LOCATION_SETTINGS_REQUEST)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("Location", "Could not resolve location settings: ${e.message}")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_SETTINGS_REQUEST && resultCode == RESULT_OK) {
           // getUserLocation()
            startLocationUpdates()
        }
    }


    private fun connectSocket() {
        val opts = IO.Options().apply {
            forceNew = true
            reconnection = true
        }
        socket = IO.socket("https://your-socket-server.com", opts)
        socket.connect()

        socket.on("client_location") { args ->
            val data = args[0] as JSONObject
            val lat = data.getDouble("lat")
            val lng = data.getDouble("lng")
          //  val location = LatLng(lat, lng)

            val location = LatLng(28.61, 77.20)

            runOnUiThread {
                clientMarker?.remove()
                clientMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title("Client Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )
            }
        }

        socket.on("warehouse_location") { args ->
            val data = args[0] as JSONObject
            val lat = data.getDouble("lat")
            val lng = data.getDouble("lng")
            val location = LatLng(latitude, longitude)
           // val location = LatLng(28.62, 77.20)

            runOnUiThread {
                warehouseMarker?.remove()
                warehouseMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title("Warehouse")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                )
            }
        }
    }

    private fun startLocationUpdates() {

        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }
         locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        var isCalledOne = true
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val json = JSONObject().apply {
                    put("lat", location.latitude)
                    put("lng", location.longitude)
                }
                // Emit as warehouse or client based on user type
                socket.emit("warehouse_location", json)

                latitude = location.latitude
                longitude = location.longitude
               // Toast.makeText(applicationContext,"Location $longitude}",Toast.LENGTH_LONG).show()
                if(isCalledOne) {

                    mMap.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                location.latitude,
                                location.longitude
                            )
                        ).title("Your Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    )
                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ), 12f
                        )
                    )
                    drawPathOnMap()
                    isCalledOne = false
                }

            }
        }


        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // If permission is granted, fetch location
            startLocationUpdates()
        } else {
            // If permission is denied, show message
            Toast.makeText(applicationContext,"Location permission denied}",Toast.LENGTH_LONG).show()
        }
    }*/

    fun pauseLocationUpdates() { fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun resumeLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }


    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

}
