package com.example.gmapsample.db

import com.example.gmapsample.DispatchQueue
import com.example.gmapsample.R
import com.example.gmapsample.UserConfig
import com.example.gmapsample.model.User
import com.example.gmapsample.model.UserLocation
import com.example.gmapsample.ui.LoginActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class FirebaseDatabase {
    private var cloudDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userTableName: String = LoginActivity.appContext.getString(R.string.collection_user)
    private val locationTableNam =
        LoginActivity.appContext.getString(R.string.collection_user_locations)
    private val dispatchQueue = DispatchQueue("database")

    private constructor()

    companion object {
        @Volatile
        private var instance: FirebaseDatabase? = null

        public fun getInstance(): FirebaseDatabase {
            var localInstance = instance
            if (localInstance == null) {
                synchronized(FirebaseDatabase::class.java) {
                    localInstance = instance
                    if (localInstance == null) {
                        instance = FirebaseDatabase()
                        localInstance = instance
                    }
                }
            }
            return localInstance!!
        }
    }

    fun getUser(username: String?, password: String?): Task<QuerySnapshot> {
        return cloudDatabase.collection(userTableName).whereEqualTo("username", username)
            .whereEqualTo("password", password)
            .get()
    }

    fun saveUser(user: User): Task<DocumentReference> {
        val usersRef = cloudDatabase.collection(userTableName)
        return usersRef.add(user).addOnSuccessListener { documentReference ->
            val id: String = documentReference.id
            cloudDatabase.collection(userTableName).document(id).update("user_id", id)
        }
    }

    fun saveUserLocations(userLocation: UserLocation): Task<Void> {
        val locationRef = cloudDatabase
            .collection(locationTableNam)
            .document(userLocation.user.user_id.toString())

        UserConfig.getInstance().currentUserLocation = userLocation
        return locationRef.set(userLocation)
    }

}