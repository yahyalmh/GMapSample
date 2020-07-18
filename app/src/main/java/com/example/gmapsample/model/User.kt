package com.example.gmapsample.model

import android.R.attr.author
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.provider.ContactsContract


class User : Parcelable {
    var email: String? = null
    var user_id: String? = null
    var username: String? = null
    var avatar: String? = null
    var password: String? = null

    constructor(
        username: String?,
        password: String?,
        email: String?,
        avatar: String?,
        user_id: String?
    ) {
        this.email = email
        this.user_id = user_id
        this.username = username
        this.avatar = avatar
        this.password = password
    }

    fun toMap(): Map<String, String?>? {
        val result: HashMap<String, String?> = HashMap()
        result["username"] = username
        result["password"] = password
        result["user_id"] = user_id
        result["email"] = email
        result["avatar"] = avatar
        return result
    }

    constructor() {}

    protected constructor(`in`: Parcel) {
        email = `in`.readString()
        user_id = `in`.readString()
        username = `in`.readString()
        avatar = `in`.readString()
        password = `in`.readString()
    }

    override fun toString(): String {
        return "User{" +
                "email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
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
        dest.writeString(password)
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