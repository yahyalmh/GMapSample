package com.example.gmapsample.model

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator


class User : Parcelable {
    var email: String? = null
    var user_id: String? = null
    var username: String? = null
    var avatar: String? = null

    constructor(
        email: String?,
        user_id: String?,
        username: String?,
        avatar: String?
    ) {
        this.email = email
        this.user_id = user_id
        this.username = username
        this.avatar = avatar
    }

    constructor() {}
    protected constructor(`in`: Parcel) {
        email = `in`.readString()
        user_id = `in`.readString()
        username = `in`.readString()
        avatar = `in`.readString()
    }

    override fun toString(): String {
        return "User{" +
                "email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", avatar='" + avatar + '\'' +
                '}'
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(email)
        dest.writeString(user_id)
        dest.writeString(username)
        dest.writeString(avatar)
    }

    companion object {
        val CREATOR: Creator<User?> = object : Creator<User?> {
            override fun createFromParcel(`in`: Parcel): User? {
                return User(`in`)
            }

            override fun newArray(size: Int): Array<User?> {
                return arrayOfNulls(size)
            }
        }

    }

}