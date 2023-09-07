package com.example.socialapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.OnLayoutChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialapp.databinding.ActivityMessageBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID


class MessageActivity : AppCompatActivity() {
    lateinit var recyclerView : RecyclerView
    lateinit var messageAdapter : MessageAdapter
    lateinit var topicId : String
    lateinit var postId : String
    lateinit var postTitle : String
    lateinit var postDescription : String
    lateinit var postBitmap : Bitmap
    lateinit var messageBinding: ActivityMessageBinding
    val db = Firebase.firestore
    val firestoreDatabase = Firebase.firestore
    lateinit var firestoreMessages : CollectionReference
    var messages = ArrayList<Message>()
    lateinit var layoutManager : LinearLayoutManager
    var itemCount : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        messageBinding = ActivityMessageBinding.inflate(layoutInflater)
        val view = messageBinding.root
        setContentView(view)
        recyclerView = findViewById(R.id.message_recycler)
        topicId  = intent.getStringExtra("topicId").toString()
        postId = intent.getStringExtra("postId").toString()
        postTitle = intent.getStringExtra("postTitle").toString()
        postDescription = intent.getStringExtra("postDescription").toString()
        postBitmap = BitmapFactory.decodeByteArray(
            intent.getByteArrayExtra("postBitmap"), 0, intent
                .getByteArrayExtra("postBitmap")!!.size
        )
        messageBinding.messageTitle.text = postTitle
        messageBinding.messageImageview.setImageBitmap(postBitmap)

        firestoreMessages = db.collection("Topics").document(topicId).collection("Posts").document(postId).collection("Messages")

        messageBinding.sendCardview.setOnClickListener {
            val messageToSend = messageBinding.sendTextMultiline.text.toString()
            if (messageToSend != ""){
                addMessageToFireDatabase(messageToSend)
            }
        }

        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        getMessagesOnFireDatabaseChanges()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home->{
                val openMainActivity = Intent(this@MessageActivity,PostActivity::class.java)
                openMainActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivityIfNeeded(openMainActivity, 0)
            }
        }

        return true
    }


    fun addMessageToFireDatabase(messageText : String) {
        val message = hashMapOf(
            "messageText" to messageText,
            "messageSender" to TopicActivity.userNickname,
            "timeStamp" to FieldValue.serverTimestamp(),
        )
        firestoreMessages.document(UUID.randomUUID().toString())
            .set(message)
            .addOnSuccessListener {
                messages.add(Message(message["messageText"] as String,message["messageSender"] as String))
                messageAdapter.notifyItemInserted(++itemCount)
                recyclerView.scrollToPosition(itemCount-1)
                messageBinding.sendTextMultiline.setText("")
            }
            .addOnFailureListener { e -> Log.w("TAG", "Error writing document", e) }
    }

    fun getMessagesOnFireDatabaseChanges(){
        firestoreMessages.orderBy("timeStamp", Query.Direction.ASCENDING).addSnapshotListener { value, error ->
            if (value != null){
                messages.clear()
                for (doc in value.documents){
                    val message : Map<String, String> = doc.data as Map<String, String>
                    messages.add(Message(message["messageText"] as String, message["messageSender"] as String))
                }
                itemCount = messages.size
                messageAdapter = MessageAdapter(messages)
                recyclerView.adapter = messageAdapter
                recyclerView.smoothScrollToPosition(itemCount)
            }
            if (error != null) {
                Log.w("TAG", "Listen failed.", error)
            }
        }
    }
}