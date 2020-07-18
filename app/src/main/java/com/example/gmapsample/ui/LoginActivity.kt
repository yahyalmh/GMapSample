package com.example.gmapsample.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gmapsample.R
import com.example.gmapsample.UserConfig
import com.example.gmapsample.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {
    private lateinit var mCloudFirebase: FirebaseFirestore
    lateinit var loginBtn: Button
    lateinit var userNameTxtView: EditText
    lateinit var passwordTxtView: EditText
    private lateinit var mRealTimeDatabase: DatabaseReference
    private val usersTableName = "Users"
    private val TAG = "TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mRealTimeDatabase = Firebase.database.reference
        mCloudFirebase = FirebaseFirestore.getInstance()

        userNameTxtView = findViewById(R.id.username)
        passwordTxtView = findViewById(R.id.password)
        loginBtn = findViewById(R.id.loginButton)

        loginBtn.setOnClickListener {
            val username = userNameTxtView.text.toString()
            val password = passwordTxtView.text.toString()

            //  insertUsers()
            verifyLogin(username, password)
        }

    }

    private fun verifyLogin(username: String, password: String) {
        var user: User? = null
        mCloudFirebase.collection(usersTableName).whereEqualTo("username", username)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { documents ->
                for (child in documents) {
                    user = child.toObject(User::class.java)
                }
                if (user != null) {
                    UserConfig.getInstance().currentUser = user as User
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "username or password was invalid",
                        Toast.LENGTH_SHORT
                    ).show()
                    userNameTxtView.setText("")
                    passwordTxtView.setText("")
                }
            }
    }

    private fun insertUsers() {
        val userOne = User("yaya", "1234", "yaya@gmail.com", "12324555", "122")
        val userTwo = User("ali", "4567", "ali@gmail.com", "12324555", "123")

        var usersRef = FirebaseFirestore.getInstance()
            .collection(usersTableName)
            .document()

        usersRef.set(userTwo)

        usersRef = FirebaseFirestore.getInstance()
            .collection(usersTableName)
            .document()
        usersRef.set(userOne)
    }

    private fun howToUserRealTimeDatabase(username: String, password: String) {
        val usersRef: DatabaseReference = mRealTimeDatabase.child(usersTableName)
        val query =
            usersRef.orderByChild("username").equalTo("ali")

        val postListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var user: User? = null
                if (dataSnapshot.hasChildren()) {
                    for (child in dataSnapshot.children) {
                        user = child.getValue(User::class.java)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(
                    "TAG",
                    "loadPost:onCancelled",
                    databaseError.toException()
                )
            }
        }
        /*val usersRef: DatabaseReference = mDatabase.child("Users")

         val insertQuery = usersRef.setValue(mUser)
         insertQuery.addOnCompleteListener(OnCompleteListener {
             if (it.isSuccessful|| it.isComplete){
                 Log.d("TAG", "user Location saved")
             }
         })*/
        query.addListenerForSingleValueEvent(postListener)
    }
}