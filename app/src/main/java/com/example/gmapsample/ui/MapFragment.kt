package com.example.gmapsample.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.gmapsample.Constants.MAPVIEW_BUNDLE_KEY
import com.example.gmapsample.R
import com.example.gmapsample.UserConfig
import com.example.gmapsample.model.UserLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {
    private var mUserLocation: UserLocation? = null
    private lateinit var mMapBounds: LatLngBounds
    private lateinit var mGoogleMap: GoogleMap
    lateinit var mapView: MapView
    lateinit var listView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.users_map)
//        listView = view.findViewById(R.id.user_list_recycler_view)

        initGoogleMap(savedInstanceState)
        mUserLocation = UserConfig.getInstance().currentUserLocation!!
        return view
    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this@MapFragment)
    }

    override fun onMapReady(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                activity!!.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                activity!!.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        map.isMyLocationEnabled = true
        mGoogleMap = map
        map.setOnMapLoadedCallback { setCameraView()}
    }

    private fun setCameraView() {
        val radius = 0.01f
        if (mUserLocation != null) {
            val bottom = mUserLocation!!.geoPoint.latitude - radius
            val left = mUserLocation!!.geoPoint.longitude - radius
            val top = mUserLocation!!.geoPoint.latitude + radius
            val right = mUserLocation!!.geoPoint.longitude + radius
            mMapBounds = LatLngBounds(LatLng(bottom, left), LatLng(top, right))
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mMapBounds, 10))
            mGoogleMap.addMarker(MarkerOptions().position(LatLng(mUserLocation!!.geoPoint.latitude, mUserLocation!!.geoPoint.longitude)).title("You"))

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}