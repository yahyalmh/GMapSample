package com.example.gmapsample.ui.component

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.setPadding
import com.example.gmapsample.R
import com.example.gmapsample.Utils
import com.example.gmapsample.model.IClusterItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import kotlin.math.min


/**
 * Draws profile photos inside markers (using IconGenerator).
 * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
 */
open class CustomRender : DefaultClusterRenderer<IClusterItem> {
    private var mClusterManager: ClusterManager<IClusterItem>? = null
    private var iconGenerator: IconGenerator
    private var mClusterIconGenerator: IconGenerator
    private val imageView: ImageView
    private val mClusterImageView: ImageView
    private var context: Context
    private val size = Utils.dp(50f)


    constructor(
        context: Context,
        map: GoogleMap?,
        clusterManager: ClusterManager<IClusterItem>?
    ) : super(context, map, clusterManager) {
        this.context = context
        iconGenerator = IconGenerator(context)
        imageView = ImageView(context)
        imageView.layoutParams = ViewGroup.LayoutParams(size, size)
        val padding = Utils.dp(5f)
        imageView.setPadding(padding, padding, padding, padding)
        iconGenerator.setContentView(imageView)

        mClusterIconGenerator = IconGenerator(context)
        val multiProfile: View = LayoutInflater.from(context).inflate(R.layout.multi_profile, null)
        mClusterIconGenerator.setContentView(multiProfile)
        mClusterImageView = multiProfile.findViewById(R.id.image)
    }

    override fun onBeforeClusterItemRendered(
        item: IClusterItem,
        markerOptions: MarkerOptions
    ) {

        imageView.setImageResource(item.user.avatar!!.toInt())
        val makeIcon = iconGenerator.makeIcon()
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(makeIcon)).title(item.title)
    }

    override fun onClusterItemUpdated(item: IClusterItem, marker: Marker) {
        imageView.setImageResource(item.user.avatar!!.toInt())
        val makeIcon = iconGenerator.makeIcon()

        marker.setIcon(BitmapDescriptorFactory.fromBitmap(makeIcon))
        marker.title = item.user.username
    }


    override fun onBeforeClusterRendered(
        cluster: Cluster<IClusterItem>,
        markerOptions: MarkerOptions
    ) {
        // Draw multiple people.
        // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
        markerOptions.icon(getClusterIcon(cluster))
    }

    override fun onClusterUpdated(
        cluster: Cluster<IClusterItem>,
        marker: Marker
    ) {
        // Same implementation as onBeforeClusterRendered() (to update cached markers)
        marker.setIcon(getClusterIcon(cluster))
    }

    /**
     * Get a descriptor for multiple people (a cluster) to be used for a marker icon. Note: this
     * method runs on the UI thread. Don't spend too much time in here (like in this example).
     *
     * @param cluster cluster to draw a BitmapDescriptor for
     * @return a BitmapDescriptor representing a cluster
     */
    private fun getClusterIcon(cluster: Cluster<IClusterItem>): BitmapDescriptor {
        val profilePhotos: MutableList<Drawable> =
            ArrayList(min(4, cluster.size))
        val width = size
        val height = size
        for (item in cluster.items) {
            // Draw 4 at most.
            if (profilePhotos.size == 4) break
            val drawable: Drawable = context.resources.getDrawable(item.user.avatar!!.toInt())
            drawable.setBounds(0, 0, width, height)
            profilePhotos.add(drawable)
        }

        val multiDrawable = MultiDrawable(profilePhotos)
        multiDrawable.setBounds(0, 0, width, height)
        mClusterImageView.setImageDrawable(multiDrawable)
        val icon = mClusterIconGenerator.makeIcon(cluster.size.toString())
        return BitmapDescriptorFactory.fromBitmap(icon)
    }

    protected override fun shouldRenderAsCluster(cluster: Cluster<IClusterItem>): Boolean {
        // Always render clusters.
        return cluster.size > 1
    }
}