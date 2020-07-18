package com.example.gmapsample

import com.example.gmapsample.model.User

class UserConfig private constructor() {
    public lateinit var currentUser: User

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