package com.example.gmapsample.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
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

    private lateinit var usersTableName: String
    private lateinit var mCloudFirebase: FirebaseFirestore
    private lateinit var mRealTimeDatabase: DatabaseReference

    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button
    private lateinit var userNameTxtView: EditText
    private lateinit var passwordTxtView: EditText
    private lateinit var confirmTextView: EditText
    private lateinit var progressBar: ProgressBar
    var registerViewed = false


    private val TAG = "TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        usersTableName = getString(R.string.collection_user)
        mRealTimeDatabase = Firebase.database.reference
        mCloudFirebase = FirebaseFirestore.getInstance()

        loginBtn = findViewById(R.id.loginButton)
        registerBtn = findViewById(R.id.registerButton)
        userNameTxtView = findViewById(R.id.username)
        passwordTxtView = findViewById(R.id.password)
        confirmTextView = findViewById(R.id.confirm_password)
        progressBar = findViewById(R.id.progressBar)

        loginBtn.setOnClickListener {
            val username = userNameTxtView.text.toString()
            val password = passwordTxtView.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                showToast("Some field are empty")
            } else {
                verifyLogin(username, password)
            }
        }
        registerBtn.setOnClickListener {
            if (!registerViewed) {
                showRegister()
            } else {
                val username = userNameTxtView.text
                val password = passwordTxtView.text
                val confPass = confirmTextView.text
                if (username.isEmpty() || password.isEmpty() || confPass.isEmpty()) {
                    showToast("Some field are empty")
                } else if (password.toString() != confPass.toString()) {
                    showToast("passwords are not equal")
                    passwordTxtView.setText("")
                    confirmTextView.setText("")
                } else {
                    insertUsers(username.toString(), password.toString())
                }
            }
        }

    }

    private fun verifyLogin(username: String, password: String) {
        showProgressBar(true)
        var user: User? = null

        mCloudFirebase.collection(usersTableName).whereEqualTo("username", username)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { documents ->
                for (child in documents) {
                    user = child.toObject(User::class.java)
                }
                if (user != null) {
                    showProgressBar(false)
                    UserConfig.getInstance().currentUser = user as User
                    startActivity(Intent(this@LoginActivity, LaunchActivity::class.java))
                    finish()
                } else {
                    showToast("username or password was invalid")
                    passwordTxtView.setText("")
                }
            }
    }

    private fun insertUsers(username: String, password: String) {
        showProgressBar(true)
        val user = User(username, password, "temp@gmil.com", "4545466", "142")

        val usersRef = FirebaseFirestore.getInstance()
            .collection(usersTableName)
            .document()

        usersRef.set(user).addOnCompleteListener {
            showProgressBar(false)
            if (it.isSuccessful || it.isComplete) {
                showToast("Register was successful")
                hideRegister()
            } else {
                showToast("Register was not successful, Try again")
            }
        }

    }

    private fun showProgressBar(shown: Boolean) {
        if (shown) {
            progressBar.visibility = View.VISIBLE
        }else{
            progressBar.visibility = View.GONE
        }
    }

    private fun hideRegister() {
        registerViewed = false
        confirmTextView.visibility = View.GONE
        loginBtn.visibility = View.VISIBLE
        registerBtn.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.Grey))
        userNameTxtView.setText("")
        passwordTxtView.setText("")
        confirmTextView.setText("")
    }

    private fun showRegister() {
        registerViewed = true
        userNameTxtView.setText("")
        passwordTxtView.setText("")
        confirmTextView.setText("")
        confirmTextView.visibility = View.VISIBLE
        loginBtn.visibility = View.GONE
        registerBtn.gravity = Gravity.CENTER
        registerBtn.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.blue1))
    }

    private fun showToast(message: String) {
        if (message.isEmpty()) {
            return
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (registerViewed) {
            hideRegister()
        }else{
            super.onBackPressed()
        }
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