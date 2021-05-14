package com.example.hnsgroups

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_data_received_first.*
import java.util.*

class DataReceivedFirst : AppCompatActivity() {

    lateinit var spinner : Spinner
    lateinit var spinner2 : Spinner

    var Subjects : String = ""
    var Phone : String = ""
    var Type : String = ""
    var PaperType : String = ""
    var MaterialStatus : String = ""
    var SubmissionDate : String = ""

    private var mAuth: FirebaseAuth? = null
    lateinit var currentUserID : String
    lateinit var progressBar : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_received_first)

        goback.setOnClickListener {
            onBackPressed()
        }

        spinner = findViewById(R.id.Spinner1)
        spinner2 = findViewById(R.id.Spinner2)

        addSpinner()

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONDAY)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        calenderButton.setOnClickListener {

            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->

                val date = "" + mDay + "/" + mMonth + "/" + mYear
                CalenderEditText.setText(date)

            }, year, month, day)
            dpd.show()

        }

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser.uid
    }

    private fun addSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.Type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        Type = "Assignment"
                    }
                    1 -> {
                        Type = "Lab Record"
                    }
                    2 -> {
                        Type = "Notes"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }


        ArrayAdapter.createFromResource(
            this,
            R.array.Paper_Type,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner2.adapter = adapter
        }
        spinner2.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        PaperType = "A4 Paper"
                    }
                    1 -> {
                        PaperType = "Lab Copy"
                    }
                    2 -> {
                        PaperType = "General Notes"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
    }

    fun onRadioButtonClicked(view: View) {

        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_pirates ->
                    if (checked) {
                        // Pirates are the best
                        MaterialStatus = "Yes"
                    }
                R.id.radio_ninjas ->
                    if (checked) {
                        // Ninjas rule
                        MaterialStatus = "No"
                    }
            }
        }

    }

    fun onSubmitButtonClicked(view: View) {

        Subjects = subjectFromUser.text.toString()
        Phone = phoneNumber.text.toString()
        SubmissionDate = CalenderEditText.text.toString()

        if (Subjects == "") {
            subjectFromUser.error = "Please Enter Subject"
        } else if (Phone.length != 10) {
            phoneNumber.error = "Please Enter a valid Phone No"
        } else if (SubmissionDate == "") {
            CalenderEditText.error = "Please Enter Submission Date"
        } else if (MaterialStatus == "") {
            Toast.makeText(this, "Please select Material Status", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("TAG", "Subject is : " + Subjects)
            Log.d("TAG", "Phone No is : " + Phone)
            Log.d("TAG", "Type is : " + Type)
            Log.d("TAG", "Paper is : " + PaperType)
            Log.d("TAG", "Material Status is : " + MaterialStatus)
            Log.d("TAG", "Submission date is : " + SubmissionDate)

            val intent = Intent(this, Data_Update_To_Firebase::class.java)
            intent.putExtra("Subject", Subjects)
            intent.putExtra("Phone", Phone)
            intent.putExtra("Type", Type)
            intent.putExtra("PaperType", PaperType)
            intent.putExtra("Material", MaterialStatus)
            intent.putExtra("Date", SubmissionDate)
            startActivity(intent)
        }



    }

    fun onUploadButtonClicked(view: View) {

        val intent = Intent()
        intent.type = "*/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), 100)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {

            val pdf = data?.data
            val fileName = data?.dataString.toString()

            progressBar = ProgressDialog(this)
            progressBar.setTitle("Uploading File")
            progressBar.setMessage("Please wait while we uploading your file")
            progressBar.setCanceledOnTouchOutside(false)
            progressBar.show()

            updateFirebaseStorage(pdf, fileName)

        }
    }

    private fun updateFirebaseStorage(pdf: Uri?, fileName : String) {

        val url = "UploadFiles/" + currentUserID + "/" + fileName
        val storageRef: StorageReference = FirebaseStorage.getInstance().getReference().child(url)

        if (pdf != null) {
            storageRef.putFile(pdf).addOnSuccessListener {

                Log.d("TAG", "Upload Success " + it)
                Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
                progressBar.dismiss()

            } .addOnFailureListener {

                Log.d("TAG", "Upload Failuer " + it)
                Toast.makeText(this, "Upload Fail. Please Send via WhatsApp", Toast.LENGTH_SHORT).show()
                progressBar.dismiss()

            }
        }

    }

}

