package com.example.hnsgroups

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_main_for__users.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class MainActivity_for_Users : AppCompatActivity() {

    private lateinit var mDatabase : DatabaseReference
    private lateinit var mRDatabase : DatabaseReference
    private lateinit var mDatabaseRef : DatabaseReference
    private var mAuth: FirebaseAuth? = null
    private var actualImage: File? = null

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userArrayList: ArrayList<User>

    private lateinit var currentUserID : String

    private var Pending : Int = 0
    private var Done : Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_for__users)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            startActivity(Intent(this, DataReceivedFirst::class.java))
        }

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser.uid
        Log.e("TAG", "The user UID is : " + currentUserID)
        NameReceivedFromFireBase(currentUserID)

        mDatabaseRef = FirebaseDatabase.getInstance().reference.child("ForUsers").child(currentUserID)


        val storageRef: StorageReference = FirebaseStorage.getInstance("gs://hns-groups-v2.appspot.com").getReference()
        val url = "ProfileImage/" + currentUserID + ".jpg"
        downloadImageFromServer(url, storageRef)

        userRecyclerView = findViewById(R.id.AssignmentsList)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.setHasFixedSize(true)

        userArrayList = arrayListOf<User>()

        getDataFromFirebase()


        changeProfilePic.setOnClickListener {

            startActivityForResult(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ), 1000
            )

        }

        countData()

    }

    private fun getDataFromFirebase() {

        mRDatabase = FirebaseDatabase.getInstance().getReference("ForUsers").child(currentUserID)

        mRDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    for (userSnapshot in snapshot.children) {

                        val user = userSnapshot.getValue(User::class.java)
                        userArrayList.add(user!!)
                    }

                    userRecyclerView.adapter = MyAdepter(userArrayList)

                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })

    }

    private fun countData() {

        mDatabaseRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()) {
                    DisplayMessages(snapshot)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()) {
                    DisplayMessages(snapshot)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })

    }

    private fun DisplayMessages(snapshot: DataSnapshot) {

        val iterator = snapshot.getChildren().iterator()

        while (iterator.hasNext()) {
            val page = (iterator.next() as DataSnapshot).value as String?
            val price = (iterator.next() as DataSnapshot).value as String?
            val status = (iterator.next() as DataSnapshot).value as String?
            val subject = (iterator.next() as DataSnapshot).value as String?

            if (status == "Pending") {
                Pending += 1
                Log.d("FFB", "In Pending")
            } else {
                Done += 1
                Log.d("FFB", "In Done")
            }
        }

        pending.text = Pending.toString()
        done.text = Done.toString()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {

                Log.d("TAG", "Image is : " + data)
                val image = data?.data
                ProfilePicture.setImageURI(image)
                ProgressBarInUser.visibility = View.VISIBLE

                try {
                    if (data != null) {

                        actualImage = FileUtil.from(this, data.data)
                        Log.d("TAG", "Actual image is : " + actualImage)

                        val compressedImage = Compressor(this)
                            .setMaxWidth(640)
                            .setMaxHeight(480)
                            .setQuality(75)
                            .compressToBitmap(actualImage);

                        val baos = ByteArrayOutputStream()
                        compressedImage.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                        val data = baos.toByteArray()

                        updateImageInFirebase(data)

                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    }

    private fun updateImageInFirebase(image: ByteArray) {

        val currentUserID = mAuth!!.currentUser.uid
        val url = "ProfileImage/" + currentUserID + ".jpg"
        val storageRef: StorageReference = FirebaseStorage.getInstance().getReference().child(url)
        if (image != null) {
            storageRef.putBytes(image).addOnSuccessListener {
                ProgressBarInUser.visibility = View.GONE
                Toast.makeText(this, "Updated Successfully", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "Image in firebase : " + it)
            }
        }

    }

    private fun downloadImageFromServer(url: String, storageRef: StorageReference) {

        storageRef.root.child(url).downloadUrl.addOnSuccessListener {
            // Got the download URL for 'users/me/profile.png'
            Log.d("TAG", "The image URI is : " + it)
            Picasso.get().load(it).into(ProfilePicture)
            ProgressBarInUser.visibility = View.GONE
        }.addOnFailureListener {
            // Handle any errors
            Log.d("TAG", "Failed to get image " + it)
            Toast.makeText(this, "No Profile Picture Uploaded", Toast.LENGTH_LONG).show()
            ProgressBarInUser.visibility = View.GONE
        }

    }

    private fun NameReceivedFromFireBase(userId: String) {

        mDatabase = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("TAG", "onChildAdded:" + dataSnapshot.key!!)

                val name = dataSnapshot.value
                Log.d("TAG", "onChildAdded:" + name)

                UserNames.text = name as CharSequence?
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("TAG", "onChildChanged: ${dataSnapshot.key}")

                val name = dataSnapshot.value
                Log.d("TAG", "onChildAdded:" + name)

                UserNames.text = name as CharSequence?

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d("TAG", "onChildRemoved:" + dataSnapshot.key!!)


            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("TAG", "onChildMoved:" + dataSnapshot.key!!)


            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("TAG", "postComments:onCancelled", databaseError.toException())
                Toast.makeText(
                    this@MainActivity_for_Users, "Failed to load your details",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        mDatabase.addChildEventListener(childEventListener)


    }

}



