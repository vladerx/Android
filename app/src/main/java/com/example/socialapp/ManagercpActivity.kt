package com.example.socialapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast

import com.example.socialapp.databinding.ActivityManagercpBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ManagercpActivity : AppCompatActivity() {
    lateinit var groupMemberListView : ListView
    lateinit var memberListView : ListView
    lateinit var groupMemberAdapter : ListViewAdapter
    lateinit var memberArrayAdapter : ListViewAdapter
    var groupMemberNicknames = ArrayList<String>()
    var memberNicknames = ArrayList<String>()
    val firestoreUsers = Firebase.firestore.collection("Users")
    val firestoreTopics = Firebase.firestore.collection("Topics")
    var topicId = ""
    lateinit var managercpBinding: ActivityManagercpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        managercpBinding = ActivityManagercpBinding.inflate(layoutInflater)
        val view = managercpBinding.root
        setContentView(view)

        groupMemberListView = findViewById(R.id.listview_group_manager)
        memberListView = findViewById(R.id.listview_member_manager)

        getNamesFromString(intent.getStringExtra("groupMembers").toString())
        topicId = intent.getStringExtra("groupId").toString()
        managercpBinding.txtviwSelectedManager.text = intent.getStringExtra("groupTitle").toString()

        loadUsersFireDatabase()

        groupMemberListView.setOnItemClickListener { parent, view, position, id ->
            val itemNickname = parent.getItemAtPosition(position).toString()
            memberNicknames.add(itemNickname)
            memberArrayAdapter.notifyDataSetChanged()
            groupMemberNicknames.remove(itemNickname)
            groupMemberAdapter.notifyDataSetChanged()
            updateGroupMemebers(groupMemberNicknames)
        }

        memberListView.setOnItemClickListener { parent, view, position, id ->
            val itemNickname = parent.getItemAtPosition(position).toString()
            groupMemberNicknames.add(itemNickname)
            groupMemberAdapter.notifyDataSetChanged()
            memberNicknames.remove(itemNickname)
            memberArrayAdapter.notifyDataSetChanged()
            if (groupMemberNicknames.size <= 100){
                updateGroupMemebers(groupMemberNicknames)
            } else {
                Toast.makeText(applicationContext,"Group cannot have more than 100 members", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home->{
                val openMainActivity = Intent(this@ManagercpActivity,PostActivity::class.java)
                openMainActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivityIfNeeded(openMainActivity, 0)
                finish()
            }
        }
        return true
    }

    fun loadUsersFireDatabase(){
        firestoreUsers.get().addOnSuccessListener { document ->
            if (document != null) {
                var userData : Map<String, String>
                var userNickname : String
                var userRole : String
                memberNicknames.clear()
                for (doc in document.documents){
                    userData  = doc.data as Map<String, String>
                    userNickname = userData["userNickname"]!!
                    userRole = userData["userRole"]!!
                    if (userRole != "group manager" && userRole != "admin" && !groupMemberNicknames.contains(userNickname)){
                        memberNicknames.add(userNickname)
                    }
                }
                memberArrayAdapter = ListViewAdapter(memberNicknames)
                memberListView.adapter = memberArrayAdapter
            } else {
                memberArrayAdapter = ListViewAdapter(memberNicknames)
                memberListView.adapter = memberArrayAdapter
            }
        }
            .addOnFailureListener { exception ->
                Log.i("TAG", exception.message, exception)
                memberArrayAdapter = ListViewAdapter(memberNicknames)
                memberListView.adapter = memberArrayAdapter
            }
    }

    fun getNamesFromString(members : String){
        if (members != "") {
            var buildName = ""
            for (char in members) {
                if (char != ',') {
                    buildName += char
                } else {
                    groupMemberNicknames.add(buildName)
                    buildName = ""
                }
            }
        }
        groupMemberAdapter = ListViewAdapter(groupMemberNicknames)
        groupMemberListView.adapter = groupMemberAdapter
    }

    fun updateGroupMemebers(groupMembers : List<String>){
        var buildGroupMember = ""
        for (member in groupMembers){
            buildGroupMember += "$member,"
        }
        firestoreTopics.document(topicId).update("topicMembers", buildGroupMember).addOnSuccessListener {
            Toast.makeText(applicationContext,"Group members have been updated!", Toast.LENGTH_LONG).show()
        }
            .addOnFailureListener { e -> Toast.makeText(applicationContext,e.toString(), Toast.LENGTH_SHORT).show() }
    }
}