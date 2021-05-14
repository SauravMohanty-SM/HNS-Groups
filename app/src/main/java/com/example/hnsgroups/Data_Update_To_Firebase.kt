package com.example.hnsgroups

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_data__update__to__firebase.*
import java.util.*
import kotlin.collections.HashMap

class Data_Update_To_Firebase : AppCompatActivity() {

    lateinit var Subjects : String
    lateinit var Phone : String
    lateinit var Type : String
    lateinit var PaperType : String
    lateinit var Material :String
    lateinit var Date : String
    private val UPI_PAYMENT = 0

    private var IsPaid = "Failed"

    private lateinit var database : DatabaseReference
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data__update__to__firebase)

        updateStrings()

        database = FirebaseDatabase.getInstance().reference

        googlePay.setOnClickListener {

            payThroughGooglePay()

        }

        paidLater.setOnClickListener {
            updateDataToFirebase()
        }

    }

    private fun payThroughGooglePay() {

        val GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user"

        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", "7978159055@paytm")
            .appendQueryParameter("pn", "Saurav Mohanty")
            .appendQueryParameter("tn", "For Assignment paying to HNS")
            .appendQueryParameter("am", "20")
            .appendQueryParameter("cu", "INR")
            .build()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        intent.setPackage(GOOGLE_PAY_PACKAGE_NAME)

        if (null != intent.resolveActivity(packageManager)) {
            startActivityForResult(intent, UPI_PAYMENT)
        } else {
            showErrorDialog()
        }

    }

    private fun showErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("G-PAY Only")
            .setMessage("Google Pay is not found.\nYou can pay by scanning the QR cord from any UPI App or can pay later.")
            .setPositiveButton(android.R.string.ok, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun updateStrings() {

        Subjects = intent.getStringExtra("Subject").toString()
        Phone = intent.getStringExtra("Phone").toString()
        Type = intent.getStringExtra("Type").toString()
        PaperType = intent.getStringExtra("PaperType").toString()
        Material = intent.getStringExtra("Material").toString()
        Date = intent.getStringExtra("Date").toString()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            UPI_PAYMENT -> if (RESULT_OK == resultCode || resultCode == 11) {
                if (data != null) {
                    val trxt = data.getStringExtra("response")

                    val dataList = ArrayList<String?>()
                    dataList.add(trxt)
                    upiPaymentDataSuccessOperation(dataList)
                } else {
                    Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun upiPaymentDataSuccessOperation(dataList: ArrayList<String?>) {

            var str: String? = dataList.get(0)

            var paymentCancel = ""
            if (str == null) str = "discard"
            var status = ""
            var approvalRefNo = ""
            val response = str.split("&".toRegex()).toTypedArray()
            for (i in response.indices) {
                val equalStr = response[i].split("=".toRegex()).toTypedArray()
                if (equalStr.size >= 2) {
                    if (equalStr[0].toLowerCase() == "Status".toLowerCase()) {
                        status = equalStr[1].toLowerCase()
                    } else if (equalStr[0].toLowerCase() == "ApprovalRefNo".toLowerCase() || equalStr[0].toLowerCase() == "txnRef".toLowerCase()) {
                        approvalRefNo = equalStr[1]
                    }
                } else {
                    paymentCancel = "Payment cancelled by user."
                }
            }
            if (status == "success") {

                Toast.makeText(
                    this,
                    "Thanks. Visit Again",
                    Toast.LENGTH_SHORT
                ).show()
                IsPaid = "Success"
                updateDataToFirebase()

            } else if ("Payment cancelled by user." == paymentCancel) {
                Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    this,
                    "Transaction failed.Please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateDataToFirebase() {

        mAuth = FirebaseAuth.getInstance()
        val currentUserID = mAuth!!.currentUser.uid

        val progressBar = ProgressDialog(this)
        progressBar.setTitle("Saving Assignment")
        progressBar.setMessage("Please wait, while we saving assignment.")
        progressBar.setCanceledOnTouchOutside(false)
        progressBar.show()


        val profileMap = HashMap<String, Any>()
        profileMap["Subject"] = Subjects
        profileMap["Phone No"] = Phone
        profileMap["Date of Submission"] = Date
        profileMap["Assignment Type"] = Type
        profileMap["Paper Type"] = PaperType
        profileMap["Paper Given"] = Material
        profileMap["Base Price Paid"] = IsPaid

        val userMap = HashMap<String, Any>()
        userMap["Subject"] = Subjects
        userMap["Pages"] = "00"
        userMap["Price"] = "00"
        userMap["Status"] = "Pending"

        database.child("ForUsers").child(currentUserID).child(Subjects).setValue(userMap)
            .addOnCompleteListener {

            }

        database.child("UserDetails").child(currentUserID).child(Subjects).setValue(profileMap)
            .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Thanks. Visit Again", Toast.LENGTH_SHORT).show()
                    progressBar.dismiss()
                    startActivity(Intent(this, MainActivity_for_Users::class.java))
                    finish()
                } else {
                    progressBar.dismiss()
                    Toast.makeText(this, "Submission Failed. Try Again.", Toast.LENGTH_SHORT).show()
                }
            })
    }
}