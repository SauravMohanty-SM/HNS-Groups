package com.example.hnsgroups

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*
import com.google.firebase.auth.FirebaseAuth as FirebaseAuth1


class SignUp : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth1
    private lateinit var database : DatabaseReference
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference


        buttonSignup.setOnClickListener {
            createFirebaseUser()
        }

        haveAccount.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
        }

    }

    @IgnoreExtraProperties
    data class User(val username: String? = null, val email: String? = null) {
        // Null default values create a no-argument default constructor, which is needed
        // for deserialization from a DataSnapshot.
    }

    fun writeNewUser(name: String) {
        val user = User(name)

        mAuth = FirebaseAuth.getInstance()
        val currentUserID = mAuth!!.currentUser.uid
        database.child("users").child(currentUserID).setValue(user)

        updateUI()
    }

    private fun createFirebaseUser() {

        val email = userEmail.text.toString()
        val password = userPassword.text.toString()
        val username = userName.text.toString()

        if (email == "") {
            userEmail.error = "Please Enter Email"
        } else if (username.length > 16) {
            userName.error = "User name must be within 15 characters."
        }
        else if (password.length < 8) {
            userPassword.error = "Password should minimum 8 character"
        } else if (username == "") {
            userName.error = "Please Enter user Name"
        } else {

            val progressBar = ProgressDialog(this)
            progressBar.setTitle("Creating Account")
            progressBar.setMessage("Please wait while we creating your account !!!")
            progressBar.setCanceledOnTouchOutside(false)
            progressBar.show()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "createUserWithEmail:success")
                        Toast.makeText(
                            this,
                            "Sign Up Successful, Please Sign In",
                            Toast.LENGTH_SHORT
                        ).show()
                        writeNewUser(username)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "createUserWithEmail:failure", task.exception)
                        progressBar.dismiss()
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }

    }

    private fun updateUI() {

        startActivity(Intent(this, LogInActivity::class.java))
        finish()

    }


}