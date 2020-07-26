package com.example.gmapsample.model

import com.example.gmapsample.UserConfig
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class IClusterItem : ClusterItem {
    private var position: LatLng
    private var title: String
    private var snippet: String
    var iconPicture = 0
    var user: User

    constructor(position: LatLng, user: User) {
        this.position = position
        this.title = user.username.toString()
        if (user.user_id == UserConfig.getInstance().currentUser.user_id) {
            this.snippet = "This is you"
        } else {
            this.snippet = "Determine route to " + user.username + "?";
        }
        this.iconPicture = user.avatar!!.toInt()
        this.user = user
    }

    override fun getSnippet(): String? {
        return snippet
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getPosition(): LatLng {
        return position
    }

}