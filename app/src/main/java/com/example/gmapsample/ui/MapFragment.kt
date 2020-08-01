package com.example.gmapsample.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ColorFilter
import android.media.Image
import android.os.Bundle
import android.os.Handler
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
import com.example.gmapsample.db.FirebaseDatabase
import com.example.gmapsample.model.IClusterItem
import com.example.gmapsample.model.UserLocation
import com.example.gmapsample.ui.component.IClusterRender
import com.example.gmapsample.ui.component.RoundedDrawable
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.fragment_map.*

class MapFragment : Fragment(), OnMapReadyCallback,
    ClusterManager.OnClusterClickListener<IClusterItem>, GoogleMap.OnInfoWindowClickListener {
    private val requestCode = 2536
    private var mUserLocation: UserLocation? = null
    private val LIVE_LOCATION_INTERVAL = 3000L

    lateinit var mapView: MapView
    private var mGoogleMap: GoogleMap? = null
    private lateinit var mMapBounds: LatLngBounds
    private lateinit var cloudFirebaseDb: FirebaseFirestore

    private lateinit var logoutImage: ImageView
    private lateinit var userImage: ImageView

    private lateinit var mClusterItem: IClusterItem
    private var mClusterManagerRender: IClusterRender? = null
    private var mClusterManager: ClusterManager<IClusterItem>? = null

    private val handler = Handler()
    private lateinit var liveLocationRunnable: Runnable

    private val usersLocationList: ArrayList<UserLocation> = ArrayList()
    private val mClusterMarkers = ArrayList<IClusterItem>()

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
        userImage.setOnClickListener {
            startActivityForResult(
                Intent(activity!!, SelectPictureFragment::class.java),
                requestCode
            )
        }
        logoutImage = view.findViewById(R.id.logout)
        logoutImage.setBackgroundDrawable(RoundedDrawable(25f))
        logoutImage.setColorFilter(Color.rgb(194, 78, 29))
        logoutImage.setOnClickListener{
            startActivity(Intent(activity!!, LoginActivity::class.java))
            activity!!.supportFragmentManager.popBackStack();
            activity!!.finish()
            UserConfig.getInstance().clear()
        }
        cloudFirebaseDb = FirebaseFirestore.getInstance()
        getUsersLocations()
        initGoogleMap(savedInstanceState)
        mUserLocation = UserConfig.getInstance().currentUserLocation!!
        return view
    }

    private fun getUsersLocations() {
        FirebaseDatabase.getInstance().retrieveLocations()
            .addOnSuccessListener { documents ->
                for (child in documents.documents) {
                    val userLocation: UserLocation = child.toObject(UserLocation::class.java)!!
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
        if (mGoogleMap != null) {
            if (mClusterManager == null) {
                mClusterManager = ClusterManager(activity, mGoogleMap)
            }
            if (mClusterManagerRender == null) {
                mClusterManagerRender = IClusterRender(activity!!, mGoogleMap, mClusterManager)
                mClusterManager!!.renderer = mClusterManagerRender
            }
            mGoogleMap!!.setOnInfoWindowClickListener(this)
        }
        for (userLocation in usersLocationList) {
            val clusterItem = IClusterItem(
                LatLng(
                    userLocation.geoPoint.latitude,
                    userLocation.geoPoint.longitude
                ), userLocation.user
            )
            mClusterManager!!.addItem(clusterItem)
            mClusterMarkers.add(clusterItem)
            if (userLocation.user.user_id == UserConfig.getInstance().currentUser.user_id) {
                mClusterItem = clusterItem
            }
        }
        mClusterManager!!.cluster()
    }

    private fun startLiveLocationRunnable() {
        handler.postDelayed(Runnable {
            retrieveLiveLocations()
            handler.postDelayed(liveLocationRunnable, LIVE_LOCATION_INTERVAL)
        }.also { liveLocationRunnable = it }, LIVE_LOCATION_INTERVAL)
    }

    private fun stopLiveLocationRunnable() {
        handler.removeCallbacks(liveLocationRunnable)
    }

    private fun retrieveLiveLocations() {
        try {
            FirebaseDatabase.getInstance().retrieveLocations().addOnSuccessListener { collection ->
                for (child in collection) {
                    val userLocation = child.toObject(UserLocation::class.java)
                    for (clusterItem in mClusterMarkers) {
                        if (clusterItem.user.user_id == userLocation.user.user_id) {
                            val latLng = LatLng(
                                userLocation.geoPoint.latitude,
                                userLocation.geoPoint.longitude
                            )
                            clusterItem.position = latLng
                            mClusterManagerRender!!.updateMarker(clusterItem)
                        }
                    }
                }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == this.requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                val avatarId = data!!.getIntExtra("picture_id", -1)
                if (avatarId != -1) {
                    UserConfig.getInstance().currentUser.avatar = avatarId.toString()
                    FirebaseDatabase.getInstance().updateUser(UserConfig.getInstance().currentUser)
                        .addOnCompleteListener {
                            if (it.isComplete || it.isSuccessful) {
                                userImage.setImageDrawable(activity!!.getDrawable(UserConfig.getInstance().currentUser.avatar!!.toInt()))
                                mClusterItem.iconPicture = avatarId
                                mClusterManagerRender!!.updateMarker(mClusterItem)
                            } else {
                                Toast.makeText(
                                    activity!!,
                                    "Sorry, profile image not changed." + it.exception!!.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
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
        startLiveLocationRunnable()
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
        stopLiveLocationRunnable()
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
        val firstName: String = cluster.items.iterator().next().user.username!!

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