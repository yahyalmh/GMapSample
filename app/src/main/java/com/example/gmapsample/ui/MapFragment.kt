package com.example.gmapsample.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.gmapsample.Constants.MAPVIEW_BUNDLE_KEY
import com.example.gmapsample.R
import com.example.gmapsample.UserConfig
import com.example.gmapsample.Utils
import com.example.gmapsample.model.IClusterItem
import com.example.gmapsample.model.UserLocation
import com.example.gmapsample.ui.component.CustomRender
import com.example.gmapsample.ui.component.IClusterRender
import com.example.gmapsample.ui.component.RoundedDrawable
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager

class MapFragment : Fragment(), OnMapReadyCallback,ClusterManager.OnClusterClickListener<IClusterItem>,  GoogleMap.OnInfoWindowClickListener{
    private lateinit var cloudFirebaseDb: FirebaseFirestore
    private var mUserLocation: UserLocation? = null
    private lateinit var mMapBounds: LatLngBounds
    private var mGoogleMap: GoogleMap? = null
    lateinit var mapView: MapView
    private lateinit var userImage:ImageView
    private var mClusterManager: ClusterManager<IClusterItem>? = null
    private var mClusterManagerRender: CustomRender? = null

    private val usersLocationList: ArrayList<UserLocation> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.users_map)
        userImage = view.findViewById(R.id.user_image)
        userImage.setBackgroundDrawable(RoundedDrawable(25f))
        userImage.setImageDrawable(activity!!.getDrawable(UserConfig.getInstance().currentUser.avatar!!.toInt()))
        cloudFirebaseDb = FirebaseFirestore.getInstance()
        getUsersLocations()
        initGoogleMap(savedInstanceState)
        mUserLocation = UserConfig.getInstance().currentUserLocation!!
        return view
    }

    private fun getUsersLocations() {
        cloudFirebaseDb.collection(getString(R.string.collection_user_locations))
            .get()
            .addOnSuccessListener { documents ->
                for (child in documents.documents){
                    val userLocation :UserLocation = child.toObject(UserLocation::class.java)!!
                    usersLocationList.add(userLocation)
                }
            }
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
        map.setOnMapLoadedCallback {
            setCameraView()
            addUserMarkers()
        }
    }

    private fun setCameraView() {
        val radius = 0.03f
        if (mUserLocation != null) {
            val bottom = mUserLocation!!.geoPoint.latitude - radius
            val left = mUserLocation!!.geoPoint.longitude - radius
            val top = mUserLocation!!.geoPoint.latitude + radius
            val right = mUserLocation!!.geoPoint.longitude + radius
            mMapBounds = LatLngBounds(LatLng(bottom, left), LatLng(top, right))
            mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(mMapBounds, 10))
        }
    }

    private fun addUserMarkers() {
        if (mGoogleMap != null){
            if (mClusterManager == null){
                mClusterManager = ClusterManager(activity, mGoogleMap)
            }
            if (mClusterManagerRender == null){
                mClusterManagerRender = CustomRender(activity!!, mGoogleMap, mClusterManager)
                mClusterManager!!.renderer = mClusterManagerRender
            }
            mGoogleMap!!.setOnInfoWindowClickListener(this)
        }
        for (userLocation in usersLocationList) {
            val clusterItem = IClusterItem(LatLng(userLocation.geoPoint.latitude, userLocation.geoPoint.longitude), userLocation.user)
            mClusterManager!!.addItem(clusterItem)
        }
        mClusterManager!!.cluster()
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

    override fun onInfoWindowClick(p0: Marker?) {

    }

    override fun onClusterClick(cluster: Cluster<IClusterItem>): Boolean {
        // Show a toast with some info when the cluster is clicked.
        val firstName: String = cluster.items.iterator().next().user.username!!
        Toast.makeText(
            activity!!,
            cluster.size.toString() + " (including " + firstName + ")",
            Toast.LENGTH_SHORT
        ).show()

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        val builder = LatLngBounds.builder()
        for (item in cluster.items) {
            builder.include(item.position)
        }
        // Get the LatLngBounds
        val bounds = builder.build()

        // Animate camera to the bounds
        try {
            mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

}