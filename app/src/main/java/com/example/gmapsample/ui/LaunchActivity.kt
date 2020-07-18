package com.example.gmapsample.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
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
import com.example.gmapsample.UserConfig
import com.example.gmapsample.Utils
import com.example.gmapsample.model.UserLocation
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint


class LaunchActivity : FragmentActivity() {
    private val TAG = "MainActivity"

    companion object {
        lateinit var appContext: Context
    }

    private var mLocationPermissionGranted = false
    private var isMapEnabled = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mDb: FirebaseFirestore
    private var mUserLocation: UserLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext
        mDb = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContentView(R.layout.activity_main)
    }


    private fun getChatRooms() {

    }

    private fun getUserDetails() {
        if (mUserLocation == null) {
            mUserLocation = UserLocation()
            mUserLocation!!.user = UserConfig.getInstance().currentUser

            getLastKnownLocation()
        }
    }

    private fun saveUserLocations() {
        if (mUserLocation != null) {
            val locationRef = mDb
                .collection(getString(R.string.collection_user_locations))
                .document()

            locationRef.set(mUserLocation!!)
            UserConfig.getInstance().currentUserLocation = mUserLocation!!
            startMapFragment()
        } else {
            getLastKnownLocation()
        }
    }

    private fun startMapFragment() {
        val fr: Fragment = MapFragment()
        val fm: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction? = fm.beginTransaction()
        fragmentTransaction!!.replace(R.id.fragment_container, fr)
        fragmentTransaction.commit()
    }

    private fun getLastKnownLocation() {
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

        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result!!
                val geoPoint = GeoPoint(result.latitude, result.longitude)
                Log.d(TAG, geoPoint.toString())
                mUserLocation!!.geoPoint = geoPoint
                mUserLocation!!.timestamp = null
                saveUserLocations()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkGps()) {
            if (mLocationPermissionGranted) {
                getChatRooms()
                getUserDetails()
            } else {
                requestLocationPermission()
            }
        }
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

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
            getChatRooms()
            getUserDetails()
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
                    getUserDetails()
                } else {
                    requestLocationPermission()
                }
            }
        }
    }
}