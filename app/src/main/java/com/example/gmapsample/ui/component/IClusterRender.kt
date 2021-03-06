package com.example.gmapsample.ui.component

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.setPadding
import com.example.gmapsample.util.Utils
import com.example.gmapsample.model.IClusterItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator


class IClusterRender : DefaultClusterRenderer<IClusterItem> {
    private var iconGenerator: IconGenerator
    private var imageView: ImageView
    private var markerWidth: Int = 0
    private var markerHeight: Int = 0

    constructor(
        context: Context?,
        map: GoogleMap?,
        clusterManager: ClusterManager<IClusterItem>?
    ) : super(context, map, clusterManager) {
        iconGenerator = IconGenerator(context)
        imageView = ImageView(context)
        imageView.layoutParams = ViewGroup.LayoutParams(Utils.dp(35f), Utils.dp(35f))
        val padding = Utils.dp(2f)
        imageView.setPadding(padding)
        iconGenerator.setContentView(imageView)
    }

    override fun onBeforeClusterItemRendered(item: IClusterItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        imageView.setImageResource(item.user.avatar!!.toInt())
        val makeIcon = iconGenerator.makeIcon()
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(makeIcon)).title(item.title)
    }

    override fun shouldRenderAsCluster(cluster: Cluster<IClusterItem>): Boolean {
        return false
    }

    fun updateMarker(clusterItem: IClusterItem) {
        val marker: Marker = getMarker(clusterItem)
        marker.position = clusterItem.position
        imageView.setImageResource(clusterItem.iconPicture)
        val makeIcon = iconGenerator.makeIcon()
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(makeIcon))
    }
}