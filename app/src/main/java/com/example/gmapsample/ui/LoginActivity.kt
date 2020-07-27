package com.example.gmapsample.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gmapsample.R
import com.example.gmapsample.UserConfig
import com.example.gmapsample.db.FirebaseDatabase
import com.example.gmapsample.model.User
import com.example.gmapsample.ui.component.RoundedDrawable
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random


class LoginActivity : AppCompatActivity() {

    private lateinit var mRealTimeDatabase: DatabaseReference

    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button
    private lateinit var userNameTxtView: EditText
    private lateinit var passwordTxtView: EditText
    private lateinit var confirmTextView: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var userImage: ImageView
    private lateinit var loginExplain: TextView
    private val requestCode = 2232
    private var profilePictureId = R.mipmap.ic_login

    var registerViewed = false

    companion object {
        lateinit var appContext: Context
    }

    private val TAG = "TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext
        setContentView(R.layout.activity_login)
        mRealTimeDatabase = Firebase.database.reference


        loginBtn = findViewById(R.id.loginButton)
        registerBtn = findViewById(R.id.registerButton)
        userNameTxtView = findViewById(R.id.username)
        passwordTxtView = findViewById(R.id.password)
        confirmTextView = findViewById(R.id.confirm_password)
        progressBar = findViewById(R.id.progressBar)
        progressBar.setBackgroundDrawable(RoundedDrawable(30f))
        userImage = findViewById(R.id.user_image)
        loginExplain = findViewById(R.id.login_explain)

        userImage.setOnClickListener {
            startActivityForResult(Intent(this, SelectPictureFragment::class.java), requestCode)
        }

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
                    showProgressBar(true)
                    val random = Random(12).nextInt()
                    val user = User(
                        username.toString(),
                        password.toString(),
                        "temp@gmil.com",
                        profilePictureId.toString(),
                        Random(1132).nextInt().toString()
                    )
                    FirebaseDatabase.getInstance().saveUser(user).addOnCompleteListener {
                        showProgressBar(false)
                        if (it.isSuccessful || it.isComplete) {
                            showToast("Register was successful")
                            hideRegister()
                        } else {
                            showToast("Register was not successful, Try again")
                        }
                    }
                }
            }
        }
    }

    private fun verifyLogin(username: String, password: String) {
        showProgressBar(true)
        var user: User? = null

        FirebaseDatabase.getInstance().getUser(username, password)
            .addOnSuccessListener { documents ->
                for (child in documents) {
                    user = child.toObject(User::class.java)
                }
                showProgressBar(false)
                if (user != null) {
                    UserConfig.getInstance().currentUser = user as User
                    startActivity(Intent(this@LoginActivity, LaunchActivity::class.java))
                    finish()
                } else {
                    showToast("username or password was invalid")
                    passwordTxtView.setText("")
                }
            }
    }

    private fun showProgressBar(shown: Boolean) {
        if (shown) {
            progressBar.visibility = View.VISIBLE
        } else {
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
        loginExplain.visibility = View.VISIBLE
        userImage.visibility = View.GONE
    }

    private fun showRegister() {
        registerViewed = true
        userNameTxtView.setText("")
        passwordTxtView.setText("")
        confirmTextView.setText("")
        confirmTextView.visibility = View.VISIBLE
        loginBtn.visibility = View.GONE
        registerBtn.gravity = Gravity.CENTER
        userImage.visibility = View.VISIBLE
        userImage.setImageDrawable(getDrawable(R.mipmap.ic_login))
        loginExplain.visibility = View.GONE
        registerBtn.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.blue1))
    }

    private fun showToast(message: String) {
        if (message.isEmpty()) {
            return
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                profilePictureId = data!!.getIntExtra("picture_id", R.mipmap.ic_login)
                userImage.setImageDrawable(getDrawable(profilePictureId))
            }

        }
    }

    override fun onBackPressed() {
        if (registerViewed) {
            hideRegister()
        } else {
            super.onBackPressed()
        }
    }

    private fun howToUserRealTimeDatabase(username: String, password: String) {
        val usersRef: DatabaseReference = mRealTimeDatabase.child("Users")
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