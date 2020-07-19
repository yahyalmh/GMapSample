package com.example.gmapsample.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

class UserLocation() :Parcelable{

    lateinit var user: User
    lateinit var geoPoint: GeoPoint

    @ServerTimestamp
    var timestamp: Date? = null

    constructor(parcel: Parcel) : this() {
        user = parcel.readParcelable(User::class.java.classLoader)!!
    }

    constructor(user: User, geoPoint: GeoPoint, timestamp: Date) : this() {
        this.timestamp = timestamp
        this.user = user
        this.geoPoint = geoPoint
    }

    override fun toString(): String {
        return "UserLocation(user=$user, geoPoint=$geoPoint, timestamp=$timestamp)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(user, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserLocation> {
        override fun createFromParcel(parcel: Parcel): UserLocation {
            return UserLocation(parcel)
        }

        override fun newArray(size: Int): Array<UserLocation?> {
            return arrayOfNulls(size)
        }
    }
}