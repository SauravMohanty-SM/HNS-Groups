package com.example.hnsgroups

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_log_in.*

class LogInActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        auth = Firebase.auth

        val bottomsheetflagment = ForgerPasswordFlagment()

        signUp.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }


        button.setOnClickListener {
            UpdateFirebase()
        }

        forgotPassword.setOnClickListener {
            bottomsheetflagment.show(supportFragmentManager, "BottomSheetDialog")
        }
    }

    private fun UpdateFirebase() {

        val EmailLogin = emailLogin.text.toString()
        val EmailPassword = emailPassword.text.toString()

        if (EmailLogin == "") {

            emailLogin.error = "Please enter Email"

        } else if (EmailPassword == "") {

            emailPassword.error = "Please enter Password"

        } else {

            val progressBar = ProgressDialog(this)
            progressBar.setTitle("Logging In")
            progressBar.setMessage("Please wait while we logging U In !!!")
            progressBar.setCanceledOnTouchOutside(false)
            progressBar.show()

            auth.signInWithEmailAndPassword(EmailLogin, EmailPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithEmail:success")
                        val user = auth.currentUser
                        progressBar.dismiss()
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithEmail:failure", task.exception)
                        progressBar.dismiss()
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }

        }


    }

    private fun updateUI(user: FirebaseUser?) {

        startActivity(Intent(this, MainActivity_for_Users::class.java))
        finish()

    }


}