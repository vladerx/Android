package com.example.socialapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class TopicActivity : AppCompatActivity(){
    lateinit var layoutManager: LinearLayoutManager
    var itemCount: Int = 0
    lateinit var recyclerView : RecyclerView
    lateinit var topicAdapter : TopicAdapter


    val firestoreTopics = Firebase.firestore.collection("Topics")
    var topics = ArrayList<Topic>()
    val imagesStorage : StorageReference = FirebaseStorage.getInstance().reference.child("Images")

    var topicMembers = HashMap<String, String>()
    companion object{
        var userNickname = ""
        var userRole = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic)

        userNickname = intent.getStringExtra("mainNickname")!!
        userRole = intent.getStringExtra("mainRole")!!
        recyclerView = findViewById(R.id.topic_recyclerview)

        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        getTopicsOnFireDatabaseChanges()
        recyclerView.addOnItemTouchListener(object : OnItemTouchListener{
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if(e.actionMasked == MotionEvent.ACTION_UP){
                    val view = rv.findChildViewUnder(e.x, e.y)
                    if (view != null){
                        val holder = rv.getChildViewHolder(view)
                        val topic = topics[holder.adapterPosition]
                        val intent = Intent(this@TopicActivity, PostActivity::class.java)
                        intent.putExtra("topicId", topic.topicId)
                        intent.putExtra("topicMembers", topicMembers[topic.topicId])
                        intent.putExtra("topicTitle", topic.topicTitle)
                        startActivity(intent)
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                TODO("Not yet implemented")
            }


            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                TODO("Not yet implemented")
            }

        })

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (userRole == "admin"){
                    var alretDialog = AlertDialog.Builder(ContextThemeWrapper(this@TopicActivity, R.style.MyAlertDialogStyle))
                    alretDialog.setTitle("Task delete action!").setMessage("Please confirm task delete action!").setIcon(R.drawable.warning_dialog).
                    setCancelable(false).setNegativeButton("Cancel", DialogInterface.OnClickListener{ dialogueInterface, which ->
                        topicAdapter.notifyItemChanged(viewHolder.adapterPosition)
                        dialogueInterface.cancel()
                    }).setPositiveButton("Proceed", DialogInterface.OnClickListener{ dialogueInterface, which ->
                        val topic = topics[viewHolder.adapterPosition]
                        deleteTopicById(topic.topicId)
                    })
                    alretDialog.create().show()
                } else {
                    topicAdapter.notifyItemChanged(viewHolder.adapterPosition)
                }
            }

        }).attachToRecyclerView(recyclerView)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.signout, menu)
        val itemAddtopic = menu?.findItem(R.id.add_topic_item)
        val itemAdmincp = menu?.findItem(R.id.admincp_item)
        if (userRole != "admin"){
            itemAddtopic?.isVisible = false
            itemAdmincp?.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.sign_out_item -> {
                var alretDialog = AlertDialog.Builder(ContextThemeWrapper(this@TopicActivity, R.style.MyAlertDialogStyle))
                alretDialog.setTitle("Sign-out Action!").setMessage("Please confirm sign-out action!").setIcon(R.drawable.warning_dialog).
                setCancelable(false).setNegativeButton("Cancel", DialogInterface.OnClickListener{ dialogueInterface, which ->
                    dialogueInterface.cancel()
                }).setPositiveButton("Proceed", DialogInterface.OnClickListener{ dialogueInterface, which ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this@TopicActivity,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                })
                alretDialog.create().show()
            }

            R.id.add_topic_item ->{
                val intent = Intent(this@TopicActivity,AddTopicActivity::class.java)
                intent.putExtra("reqCode", 1)
                startActivity(intent)
            }
            R.id.admincp_item ->{
                val intent = Intent(this@TopicActivity,AdmincpActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    fun deleteTopicById(topicId : String){
        firestoreTopics.document(topicId).collection("Posts").addSnapshotListener { value, error ->
            if (value != null){
                for (doc in value.documents){
                    val firestorePost = firestoreTopics.document(topicId).collection("Posts").document(doc.id)
                    firestorePost.collection("Messages").addSnapshotListener { value, error ->
                        if (value != null){
                            for (doc in value.documents){
                                firestorePost.collection("Messages").document(doc.id).delete()
                            }
                        }
                        if (error != null) {
                            Log.w("TAG", "Listen failed.", error)
                        }
                    }
                    imagesStorage.child(doc.id).delete()
                    firestorePost.delete()
                }

            }
            if (error != null) {
                Log.w("TAG", "Listen failed.", error)
            }
        }
        firestoreTopics.document(topicId).delete().addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(applicationContext,"Group deleted successfully!", Toast.LENGTH_LONG).show()
            }
        }
        imagesStorage.child(topicId).delete()
    }

    fun getTopicsOnFireDatabaseChanges(){
        firestoreTopics.orderBy("timeStamp", Query.Direction.ASCENDING).addSnapshotListener { value, error ->
            if (value != null){
                topics.clear()
                for (doc in value.documents){
                    val topic : Map<String, String> = doc.data as Map<String, String>
                    if (checkTopicMembers(topic["topicMembers"] as String, topic["topicId"] as String)) {
                        topics.add(
                            Topic(
                                topic["topicId"] as String,
                                topic["topicTitle"] as String,
                                topic["topicDescription"] as String,
                                topic["topicUri"] as String,
                                topic["topicMembers"] as String
                            )
                        )
                    }
                }
                itemCount = topics.size
                topicAdapter = TopicAdapter(topics)
                recyclerView.adapter = topicAdapter
                recyclerView.smoothScrollToPosition(itemCount)
            }
            if (error != null) {
                Log.w("TAGGY", "Listen failed.", error)
            }
        }
    }
    fun checkTopicMembers(members : String, topicId : String) : Boolean{
        var buildName = ""
        var isMemeberFound = false
        topicMembers[topicId] = members
        for (char in members){
            if (char != ','){
                buildName += char
            } else {
                if (userNickname == buildName) {
                    isMemeberFound = true
                }
                buildName = ""
            }
        }
        if(userRole == "admin" || userRole == "group manager"){
            isMemeberFound = true
        }
        return isMemeberFound
    }

}


