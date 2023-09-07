package com.example.socialapp

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream


class PostActivity : AppCompatActivity() {
    lateinit var layoutManager: LinearLayoutManager
    var itemCount: Int = 0
    lateinit var recyclerView : RecyclerView
    lateinit var postAdapter : PostAdapter
    lateinit var topicId : String
    lateinit var topicMembers : String
    lateinit var topicTitle : String
    var posts = ArrayList<Post>()
    val firestoreTopics = Firebase.firestore.collection("Topics")
    val imagesStorage : StorageReference = FirebaseStorage.getInstance().reference.child("Images")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        topicId  = intent.getStringExtra("topicId").toString()
        topicTitle = intent.getStringExtra("topicTitle").toString()
        topicMembers = intent.getStringExtra("topicMembers").toString()
        recyclerView = findViewById(R.id.post_recycler)
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        getPostsOnFireDatabaseChanges()

        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if(e.actionMasked == MotionEvent.ACTION_UP){
                    val view = rv.findChildViewUnder(e.x, e.y)
                    if (view != null){
                        val holder = rv.getChildViewHolder(view)
                        val postHolder = postAdapter.holderList[holder.adapterPosition]
                        val post = posts[holder.adapterPosition]
                        val postBitmap = postHolder.postImage.drawable.toBitmap()
                        val bs = ByteArrayOutputStream()
                        postBitmap.compress(Bitmap.CompressFormat.PNG, 50, bs)
                        val intent = Intent(this@PostActivity, MessageActivity::class.java)
                        intent.putExtra("topicId", topicId)
                        intent.putExtra("postId", post.postId)
                        intent.putExtra("postTitle", post.postTitle)
                        intent.putExtra("postDescription", post.postDescription)
                        intent.putExtra("postBitmap", bs.toByteArray())
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

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (TopicActivity.userRole != "member"){
                    var alretDialog = AlertDialog.Builder(ContextThemeWrapper(this@PostActivity, R.style.MyAlertDialogStyle))
                    alretDialog.setTitle("Task delete action!").setMessage("Please confirm task delete action!").setIcon(R.drawable.warning_dialog).
                    setCancelable(false).setNegativeButton("Cancel", DialogInterface.OnClickListener{ dialogueInterface, which ->
                        postAdapter.notifyItemChanged(viewHolder.adapterPosition)
                        dialogueInterface.cancel()
                    }).setPositiveButton("Proceed", DialogInterface.OnClickListener{ dialogueInterface, which ->
                        val post = posts[viewHolder.adapterPosition]
                        deletePostById(post.postId)
                    })
                    alretDialog.create().show()

                } else {
                    postAdapter.notifyItemChanged(viewHolder.adapterPosition)
                }

            }

        }).attachToRecyclerView(recyclerView)
    }


    fun deletePostById(postId : String){
        val firestorePost = firestoreTopics.document(topicId).collection("Posts").document(postId)
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
        firestorePost.delete().addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(applicationContext,"Post deleted successfully!", Toast.LENGTH_LONG).show()
            }
        }
        imagesStorage.child(postId).delete()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.signout, menu)
        val itemAddPost = menu?.findItem(R.id.add_topic_item)
        val itemAdmincp = menu?.findItem(R.id.admincp_item)
        if (TopicActivity.userRole == "member"){
            itemAddPost?.isVisible = false
            itemAdmincp?.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.sign_out_item -> {
                var alretDialog = AlertDialog.Builder(ContextThemeWrapper(this@PostActivity, R.style.MyAlertDialogStyle))
                alretDialog.setTitle("Sign-out Action!").setMessage("Please confirm sign-out action!").setIcon(R.drawable.warning_dialog).
                setCancelable(false).setNegativeButton("Cancel", DialogInterface.OnClickListener{ dialogueInterface, which ->
                    dialogueInterface.cancel()
                }).setPositiveButton("Proceed", DialogInterface.OnClickListener{ dialogueInterface, which ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@PostActivity,MainActivity::class.java)
                startActivity(intent)
                finish()
                })
                alretDialog.create().show()
            }

            R.id.add_topic_item ->{
                val intent = Intent(this@PostActivity,AddTopicActivity::class.java)
                intent.putExtra("reqCode", 2)
                intent.putExtra("topicId", topicId)
                startActivity(intent)
            }
            android.R.id.home->{
                val openMainActivity = Intent(this@PostActivity,TopicActivity::class.java)
                openMainActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivityIfNeeded(openMainActivity, 0)
                finish()
            }
            R.id.admincp_item ->{
                var intent = Intent()
                if (TopicActivity.userRole == "admin"){
                    intent = Intent(this@PostActivity,AdmincpActivity::class.java)

                } else if (TopicActivity.userRole == "group manager") {
                    intent = Intent(this@PostActivity,ManagercpActivity::class.java)
                    intent.putExtra("groupId", topicId)
                    intent.putExtra("groupMembers", topicMembers)
                    intent.putExtra("groupTitle", topicTitle)
                }
                startActivity(intent)
            }
        }

        return true
    }


    fun getPostsOnFireDatabaseChanges(){
        firestoreTopics.document(topicId).collection("Posts").orderBy("timeStamp", Query.Direction.ASCENDING).addSnapshotListener { value, error ->
            if (value != null){
                posts.clear()
                for (doc in value.documents){
                    val post : Map<String, String> = doc.data as Map<String, String>
                    posts.add(Post(post["postId"] as String, post["postTitle"] as String, post["postDescription"] as String,post["postUri"] as String))
                }
                itemCount = posts.size
                postAdapter = PostAdapter(posts)
                recyclerView.adapter = postAdapter
                recyclerView.smoothScrollToPosition(itemCount)
            }
            if (error != null) {
                Log.w("TAG", "Listen failed.", error)
            }
        }
    }
}