package com.example.gmapsample.ui

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.gmapsample.Constants.ERROR_DIALOG_REQUEST
import com.example.gmapsample.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.example.gmapsample.Constants.PERMISSIONS_REQUEST_ENABLE_GPS
import com.example.gmapsample.R
import com.example.gmapsample.util.Utils
import com.example.gmapsample.service.LocationService
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*


class LaunchActivity : FragmentActivity() {
    private val TAG = "MainActivity"

    private var mLocationPermissionGranted = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContentView(R.layout.activity_main)
        mLocationPermissionGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        startMapFragment()

    }


    private fun getChatRooms() {

    }


    private fun startLocationService() {
        if (!isLocationServiceRunning()) {
            val intent = Intent(this@LaunchActivity, LocationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this@LaunchActivity.startForegroundService(intent)
            } else {
                this@LaunchActivity.startService(intent)
            }
        }
    }

    private fun stopLocationService() {
        if (isLocationServiceRunning()) {
            val intent = Intent(this@LaunchActivity, LocationService::class.java)
            this@LaunchActivity.stopService(intent)
        }
    }

    private fun isLocationServiceRunning(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (serviceInfo in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceInfo.service.className == "com.example.gmapsample.service.LocationService") {
                return true
            }
        }
        return false
    }

    private fun startMapFragment() {
        val fr: Fragment = MapFragment()
        val fm: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction? = fm.beginTransaction()
        fragmentTransaction!!.replace(R.id.fragment_container, fr)
        fragmentTransaction.commit()
    }

    override fun onResume() {
        super.onResume()
        if (checkGps()) {
            if (mLocationPermissionGranted) {
                startLocationService()
                getChatRooms()
            } else {
                requestLocationPermission()
            }
        }
    }

    private fun checkGps(): Boolean {
        if (Utils.isPlayServiceAvailable()) {
            if (Utils.isGPSEnabled()) {
                return true
            } else {
                requestGps()
            }
        } else {
            val dialog = GoogleApiAvailability.getInstance().getErrorDialog(
                this@LaunchActivity,
                GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(applicationContext),
                ERROR_DIALOG_REQUEST
            )
            dialog.show()
        }
        return false
    }

    private fun requestGps() {
        val googleApiClient = GoogleApiClient.Builder(this@LaunchActivity)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 10000 / 2.toLong()
        val builder =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback {
            val status: Status = it.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> Log.i(
                    TAG,
                    "All location settings are satisfied."
                )
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(
                        TAG,
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(
                            this@LaunchActivity,
                            /*REQUEST_CHECK_SETTINGS*/1222
                        )
                    } catch (e: SendIntentException) {
                        Log.i(TAG, "PendingIntent unable to execute request.")
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(
                    TAG,
                    "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                )
            }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
            getChatRooms()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationPermissionGranted = true
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> {
                if (mLocationPermissionGranted) {
                    getChatRooms()
                } else {
                    requestLocationPermission()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationService()
    }
}