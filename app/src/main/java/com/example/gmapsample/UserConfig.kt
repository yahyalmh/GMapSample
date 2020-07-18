package com.example.gmapsample

import com.example.gmapsample.model.User
import com.example.gmapsample.model.UserLocation

class UserConfig private constructor() {
    public lateinit var currentUser: User
    var currentUserLocation : UserLocation? = null

    companion object {

        @Volatile
        private var instance: UserConfig? = null;

        public fun getInstance(): UserConfig {
            var localInstance = instance

            if (localInstance == null) {
                synchronized(UserConfig::class.java) {
                    localInstance = instance
                    if (localInstance == null) {
                        instance = UserConfig()
                        localInstance = instance
                    }
                }
            }

            return localInstance!!
        }
    }
}