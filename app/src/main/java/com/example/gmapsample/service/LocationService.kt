package com.example.gmapsample.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.gmapsample.R
import com.example.gmapsample.UserConfig
import com.example.gmapsample.db.FirebaseDatabase
import com.example.gmapsample.model.User
import com.example.gmapsample.model.UserLocation
import com.google.android.gms.location.*
import com.google.firebase.firestore.*

class LocationService : Service() {
    private lateinit var cloudFirebase: FirebaseFirestore
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val UPDATE_INTERVAL: Long = 4 * 1000/*4 second*/
    private val FASTEST_INTERVAL: Long = 2 * 1000 /*2 second*/

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        cloudFirebase = FirebaseFirestore.getInstance()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val CHANNEL_ID = "channel_01"
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "My Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(notificationChannel)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("get update location")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()

            startForeground(1254, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requestLocation()
        return START_NOT_STICKY
    }

    private fun requestLocation() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = UPDATE_INTERVAL
        locationRequest.fastestInterval = FASTEST_INTERVAL
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val lastLocation = locationResult!!.lastLocation

                val userLocation = UserLocation()
                userLocation.geoPoint = GeoPoint(lastLocation.latitude, lastLocation.longitude)
                userLocation.user = UserConfig.getInstance().currentUser
                userLocation.timestamp = null
                FirebaseDatabase.getInstance().saveUserLocations(userLocation)
                UserConfig.getInstance().currentUserLocation = userLocation
            }

        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback, Looper.myLooper()
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}