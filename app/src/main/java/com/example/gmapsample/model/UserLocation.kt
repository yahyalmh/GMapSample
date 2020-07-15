package com.example.gmapsample.model

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

class UserLocation {

    lateinit var user: User
    lateinit var geoPoint: GeoPoint

    @ServerTimestamp
    var timestamp: Date? = null

    constructor()
    constructor(user: User, geoPoint: GeoPoint, timestamp: Date) {
        this.timestamp = timestamp
        this.user = user
        this.geoPoint = geoPoint
    }

    override fun toString(): String {
        return "UserLocation(user=$user, geoPoint=$geoPoint, timestamp=$timestamp)"
    }
}