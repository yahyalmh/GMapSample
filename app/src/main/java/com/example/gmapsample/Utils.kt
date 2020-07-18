package com.example.gmapsample

import android.content.Context
import android.location.LocationManager
import android.widget.Toast
import com.example.gmapsample.ui.LaunchActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability


class Utils {
    companion object {

        private var mContext: Context = LaunchActivity.appContext

        fun isGPSEnabled(): Boolean {
            val manager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return false
            }
            return true
        }


        fun isPlayServiceAvailable(): Boolean {

            val available =
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext)
            when {
                available == ConnectionResult.SUCCESS -> {
                    //everything is fine and the user can make map requests
                    return true
                }
                GoogleApiAvailability.getInstance().isUserResolvableError(available) -> {
                    //an error occurred but we can resolve it
                    return false
                }
                else -> {
                    Toast.makeText(mContext, "You can't make map requests", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            return false
        }
    }

}