package com.example.gmapsample.util

import android.content.Context
import android.location.LocationManager
import android.widget.Toast
import com.example.gmapsample.ui.LoginActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt


class Utils {
    companion object {

        private var mContext: Context = LoginActivity.appContext

        val density = mContext.resources.displayMetrics.density;
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

        public fun dp(value: Float): Int {
            return if (value == 0f) {
                0
            } else ceil(density * value).toInt()
        }

        public fun dpr(value: Float): Int {
            return if (value == 0f) {
                0
            } else (density * value).roundToInt().toInt()
        }

        public fun dp2(value: Float): Int {
            return if (value == 0f) {
                0
            } else floor(density * value).toInt()
        }
    }
}