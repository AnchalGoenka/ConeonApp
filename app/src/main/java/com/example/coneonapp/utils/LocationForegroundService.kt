package com.example.coneonapp.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.coneonapp.LocationActivity
import com.google.android.gms.location.*
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class LocationForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
     private lateinit var locationCallback: LocationCallback
     private lateinit var socket: Socket


    override fun onCreate() {
         super.onCreate()
         fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
         createNotificationChannel()
         startForeground(1, createNotification())
         initSocket()
         startLocationUpdates()
    }

    private fun initSocket() {
         val opts = IO.Options().apply {
            forceNew = true
            reconnection = true
         }
          socket = IO.socket("https://your-socket-server.com", opts)
          socket.connect()
    }

    private fun startLocationUpdates() {
         val locationRequest = LocationRequest.create().apply {
           interval = 5000
           fastestInterval = 3000
           priority = LocationRequest.PRIORITY_HIGH_ACCURACY
         }

        locationCallback = object : LocationCallback() {
         override fun onLocationResult(result: LocationResult) {
          val location = result.lastLocation ?: return
           val json = JSONObject().apply {
            put("lat", location.latitude)
             put("lng", location.longitude)
             }
             socket.emit("warehouse_location", json)
         }
        }

         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // location permission not granted
            stopSelf()
             return
         }
         fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, LocationActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, "location_channel")
         .setContentTitle("Coneon Tracking")
         .setContentText("Sharing live location")
         .setSmallIcon(android.R.drawable.ic_menu_mylocation)
         .setContentIntent(pendingIntent)
         .setAutoCancel(true)
         .setPriority(NotificationCompat.PRIORITY_LOW)
         .build()
    }

     private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             val channel = NotificationChannel("location_channel", "Location Tracking", NotificationManager.IMPORTANCE_LOW).apply {
                 description ="Shows ongoing delivery tracking"
                 enableVibration(true)
                 lockscreenVisibility = Notification.VISIBILITY_PUBLIC
             }
             val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
             manager.createNotificationChannel(channel)

        }
     }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
         super.onDestroy()
         socket.disconnect()
         fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}