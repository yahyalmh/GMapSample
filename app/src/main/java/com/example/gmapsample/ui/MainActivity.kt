package com.example.gmapsample.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gmapsample.Constants.*
import com.example.gmapsample.R
import com.example.gmapsample.Utils
import com.google.android.gms.common.GoogleApiAvailability


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    companion object {
        lateinit var appContext: Context
    }

    private var mLocationPermissionGranted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext;
        setContentView(R.layout.activity_main)
    }


    private fun getChatRooms() {

    }


    override fun onResume() {
        super.onResume()
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                getChatRooms()
            } else {
                getLocationPermission()
            }
        }
    }

    private fun checkMapServices(): Boolean {
        if (Utils.isPlayServiceAvailable()) {
            if (Utils.isMapsEnabled()) {
                return true
            } else {
                val builder: AlertDialog.Builder = AlertDialog.Builder(applicationContext)
                builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                        val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
                    })
                val alert: AlertDialog = builder.create()
                alert.show()
            }
        } else {
            val dialog = GoogleApiAvailability.getInstance().getErrorDialog(
                this@MainActivity,
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext),
                ERROR_DIALOG_REQUEST
            )
            dialog.show()
        }
        return false
    }

    private fun getLocationPermission() {
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
                    getLocationPermission()
                }
            }
        }
    }
}