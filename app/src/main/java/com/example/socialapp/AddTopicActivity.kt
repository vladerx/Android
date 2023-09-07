package com.example.socialapp


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.socialapp.databinding.ActivityAddTopicBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID


class AddTopicActivity : AppCompatActivity() {
    lateinit var activityAddTopicBinding : ActivityAddTopicBinding
    val imagesStorage : StorageReference = FirebaseStorage.getInstance().reference.child("Images")
    var imageUri : String = ""
    lateinit var resultLauncher : ActivityResultLauncher<Intent>
    var requestCode : Int = 0
    var topicId : String = ""
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityAddTopicBinding = ActivityAddTopicBinding.inflate(layoutInflater)
        val view = activityAddTopicBinding.root
        setContentView(view)

        requestCode = intent.getIntExtra("reqCode", 0)
        if (requestCode == 2){
            topicId  = intent.getStringExtra("topicId").toString()
            supportActionBar?.title = "Add Task"
            activityAddTopicBinding.addTopicButton.text = "Add Task"
        }
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    imageUri = data.data.toString()
                    Glide.with(activityAddTopicBinding.addTopicImage.context).load(imageUri).fitCenter()
                        .into(activityAddTopicBinding.addTopicImage)
                }
            }
        }

        activityAddTopicBinding.addTopicButton.setOnClickListener {
            if (activityAddTopicBinding.addTopicTitle.text.toString() != "" && activityAddTopicBinding.addTopicDescription.text.toString() != ""){
                addImageToStorage()
                activityAddTopicBinding.addProgbar.visibility = View.VISIBLE
            } else {
                Toast.makeText(applicationContext,"Fields Cannot Be Empty!", Toast.LENGTH_SHORT).show()
            }
        }

        val uploadImage : ImageView =  view.findViewById(R.id.add_topic_image)
        uploadImage.setOnClickListener { view ->
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "image/*"
            resultLauncher.launch(galleryIntent)
        }

    }

    fun addTopicToFireDatabase(topicId : String, topictitle : String, topicDescription : String, imageUri : String) {
        val topic = hashMapOf(
            "topicId" to topicId,
            "topicTitle" to topictitle,
            "topicDescription" to topicDescription,
            "topicUri" to imageUri,
            "topicMembers" to "",
            "timeStamp" to FieldValue.serverTimestamp(),
        )
        db.collection("Topics").document(topicId)
            .set(topic)
            .addOnSuccessListener {
                Log.d("TAG", "DocumentSnapshot successfully written!")
                activityAddTopicBinding.addProgbar.visibility = View.INVISIBLE
                Toast.makeText(applicationContext,"Group has been successfully created!", Toast.LENGTH_SHORT).show()
                val openMainActivity = Intent(this@AddTopicActivity,TopicActivity::class.java)
                openMainActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivityIfNeeded(openMainActivity, 0)
                finish()
            }
            .addOnFailureListener { e -> Log.w("TAG", "Error writing document", e) }
    }

    fun addPostToFireDatabase(postId : String, postTitle : String, postDescription : String, imageUri : String) {
        val post = hashMapOf(
            "postId" to postId,
            "postTitle" to postTitle,
            "postDescription" to postDescription,
            "postUri" to imageUri,
            "timeStamp" to FieldValue.serverTimestamp(),
        )
        db.collection("Topics").document(topicId).collection("Posts").document(postId)
            .set(post)
            .addOnSuccessListener {
                Log.d("TAG", "DocumentSnapshot successfully written!")
                activityAddTopicBinding.addProgbar.visibility = View.INVISIBLE
                Toast.makeText(applicationContext,"task has been successfully created!", Toast.LENGTH_SHORT).show()
                val openMainActivity = Intent(this@AddTopicActivity,PostActivity::class.java)
                openMainActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivityIfNeeded(openMainActivity, 0)
                finish()
            }
            .addOnFailureListener { e -> Log.w("TAG", "Error writing document", e) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home->{
                var openMainActivity : Intent
                if (requestCode == 2){
                    openMainActivity = Intent(this@AddTopicActivity,PostActivity::class.java)
                    openMainActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                } else {
                    openMainActivity = Intent(this@AddTopicActivity,TopicActivity::class.java)
                    openMainActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT

                }
                startActivityIfNeeded(openMainActivity, 0)
                finish()
            }
        }
        return true
    }

    fun addImageToStorage() {
        val id: String = UUID.randomUUID().toString()
        if (imageUri == "") {
            if (requestCode == 2) {
                addPostToFireDatabase(
                    id,
                    activityAddTopicBinding.addTopicTitle.text.toString(),
                    activityAddTopicBinding.addTopicDescription.text.toString(),
                    "https://firebasestorage.googleapis.com/v0/b/socialappshare.appspot.com/o/pencilimg.png?alt=media&token=8176ac62-e059-43bd-b2ef-a221e1278a04&_gl=1*1pflny1*_ga*MTA4OTc3NTA2Ny4xNjgzNjU3Mjkw*_ga_CW55HF8NVT*MTY4NTYzMjQwMS4zNy4xLjE2ODU2MzM4MjcuMC4wLjA."
                )
            } else {
                addTopicToFireDatabase(
                    id,
                    activityAddTopicBinding.addTopicTitle.text.toString(),
                    activityAddTopicBinding.addTopicDescription.text.toString(),
                    "https://firebasestorage.googleapis.com/v0/b/socialappshare.appspot.com/o/pencilimg.png?alt=media&token=8176ac62-e059-43bd-b2ef-a221e1278a04&_gl=1*1pflny1*_ga*MTA4OTc3NTA2Ny4xNjgzNjU3Mjkw*_ga_CW55HF8NVT*MTY4NTYzMjQwMS4zNy4xLjE2ODU2MzM4MjcuMC4wLjA."
                )
            }
        } else {
            imagesStorage.child(id).putFile(Uri.parse(imageUri)).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    imagesStorage.child(id).downloadUrl.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (requestCode == 2) {
                                addPostToFireDatabase(
                                    id,
                                    activityAddTopicBinding.addTopicTitle.text.toString(),
                                    activityAddTopicBinding.addTopicDescription.text.toString(),
                                    task.result.toString()
                                )
                            } else {
                                addTopicToFireDatabase(
                                    id,
                                    activityAddTopicBinding.addTopicTitle.text.toString(),
                                    activityAddTopicBinding.addTopicDescription.text.toString(),
                                    task.result.toString()
                                )
                            }

                        }
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        task.exception?.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }

}