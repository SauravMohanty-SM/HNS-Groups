package com.example.hnsgroups

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.forgot_password.*


class ForgerPasswordFlagment: BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonForgotPassword.setOnClickListener {

            val emails = ForgotEmail.text.toString()

            if (emails == "") {
                ForgotEmail.error = "Please Enter Email Id"
            } else {

                val progressBar = ProgressDialog(context)
                progressBar.setTitle("Finding")
                progressBar.setMessage("Please wait while we finding your account !!!")
                progressBar.setCanceledOnTouchOutside(false)
                progressBar.show()

                FirebaseAuth.getInstance().sendPasswordResetEmail(emails)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("TAG", "Email sent.")
                            progressBar.dismiss()
                            Toast.makeText(context, "Mail Sent to your mail", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("TAG", "Email not sent.")
                            progressBar.dismiss()
                            Toast.makeText(context, "We can't find your mail. Please Sign Up", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

}