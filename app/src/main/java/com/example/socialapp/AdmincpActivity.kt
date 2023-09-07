package com.example.socialapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem

import android.widget.ListView
import android.widget.Toast
import com.example.socialapp.databinding.ActivityAdmincpBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class AdmincpActivity : AppCompatActivity() {
    lateinit var groupListView : ListView
    lateinit var memberListView : ListView
    lateinit var groupArrayAdapter : ListViewAdapter
    lateinit var memberArrayAdapter : ListViewAdapter
    var groupNicknames = ArrayList<String>()
    var memberNicknames = ArrayList<String>()
    var isNicknameSelected = false
    var selecteduser = ""
    val firestoreUsers = Firebase.firestore.collection("Users")
    var users = HashMap<String, ArrayList<String>>()

    lateinit var admincpBinding: ActivityAdmincpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        admincpBinding = ActivityAdmincpBinding.inflate(layoutInflater)
        val view = admincpBinding.root
        setContentView(view)
        groupListView = findViewById(R.id.listview_group_admin)
        memberListView = findViewById(R.id.listview_member_admin)
        loadUsersFireDatabase()

        groupListView.setOnItemClickListener { parent, view, position, id ->
            val itemNickname = parent.getItemAtPosition(position).toString()
            if (isNicknameSelected){
                val selectedNickname = admincpBinding.txtviwSelectedAdmin.text.toString()
                if (users[selectedNickname]!![1] == "member"){
                    memberNicknames.add(selectedNickname)
                    memberArrayAdapter.notifyDataSetChanged()
                } else{
                    groupNicknames.add(selectedNickname)
                }
            } else {
                isNicknameSelected = true
            }
            admincpBinding.txtviwSelectedAdmin.text = itemNickname
            groupNicknames.remove(itemNickname)
            groupArrayAdapter.notifyDataSetChanged()
        }

        memberListView.setOnItemClickListener { parent, view, position, id ->
            val itemNickname = parent.getItemAtPosition(position).toString()
            if (isNicknameSelected){
                val selectedNickname = admincpBinding.txtviwSelectedAdmin.text.toString()
                if (users[selectedNickname]!![1] == "group manager"){
                    groupNicknames.add(selectedNickname)
                    groupArrayAdapter.notifyDataSetChanged()
                } else{
                    memberNicknames.add(selectedNickname)
                }
            } else {
                isNicknameSelected = true
            }
            admincpBinding.txtviwSelectedAdmin.text = itemNickname
            memberNicknames.remove(itemNickname)
            memberArrayAdapter.notifyDataSetChanged()
        }

        admincpBinding.imgviwSelectedGroupAdmin.setOnClickListener {
            if (isNicknameSelected){
                val nickname = admincpBinding.txtviwSelectedAdmin.text.toString()
                admincpBinding.txtviwSelectedAdmin.text = ""
                isNicknameSelected = false
                groupNicknames.add(nickname)
                groupArrayAdapter.notifyDataSetChanged()
                if (users[nickname]!![1] == "member"){
                    changeUserRoleInFireDatabase("member", nickname)
                    users[nickname]!![1]="group manager"
                }
            } else {
                Toast.makeText(applicationContext, "Please Select User to Change Role!", Toast.LENGTH_SHORT).show()
            }
        }

        admincpBinding.imgviwSelectedMemberAdmin.setOnClickListener {
            if (isNicknameSelected){
                val nickname = admincpBinding.txtviwSelectedAdmin.text.toString()
                admincpBinding.txtviwSelectedAdmin.text = ""
                isNicknameSelected = false
                memberNicknames.add(nickname)
                memberArrayAdapter.notifyDataSetChanged()
                if (users[nickname]!![1] == "group manager"){
                    changeUserRoleInFireDatabase("group manager", nickname)
                    users[nickname]!![1]="member"
                }
            } else {
                Toast.makeText(applicationContext, "Please Select User to Change Role!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home->{
                val openMainActivity = Intent(this@AdmincpActivity,TopicActivity::class.java)
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
                users.clear()
                groupNicknames.clear()
                memberNicknames.clear()
                isNicknameSelected = false
                selecteduser = ""
                for (doc in document.documents){
                    userData  = doc.data as Map<String, String>
                    userNickname = userData["userNickname"]!!
                    users[userNickname] = arrayListOf(userData["userId"]!!,userData["userRole"]!!)
                    Log.i("TAGGY",users.toString())
                    if (users[userNickname]!![1] == "group manager"){
                        groupNicknames.add(userNickname)
                    } else if (users[userNickname]!![1] == "member") {
                        memberNicknames.add(userNickname)
                    }
                }
                groupArrayAdapter = ListViewAdapter(groupNicknames)
                memberArrayAdapter = ListViewAdapter(memberNicknames)
                groupListView.adapter = groupArrayAdapter
                memberListView.adapter = memberArrayAdapter
            } else {
                groupArrayAdapter = ListViewAdapter(groupNicknames)
                memberArrayAdapter = ListViewAdapter(memberNicknames)
                groupListView.adapter = groupArrayAdapter
                memberListView.adapter = memberArrayAdapter
            }
        }
            .addOnFailureListener { exception ->
                groupArrayAdapter = ListViewAdapter(groupNicknames)
                memberArrayAdapter = ListViewAdapter(memberNicknames)
                groupListView.adapter = groupArrayAdapter
                memberListView.adapter = memberArrayAdapter
            }
    }

    fun changeUserRoleInFireDatabase(currentRole : String, nickname : String){
        var newRole : String
        val id = users[nickname]!![0]
        if (currentRole == "member"){
            newRole = "group manager"
        } else {
            newRole = "member"
        }
        Log.i("TAGGY",newRole)
        firestoreUsers.document(id).update("userRole", newRole).addOnSuccessListener {
            Toast.makeText(applicationContext,"User has been reassigned!", Toast.LENGTH_LONG).show()
        }
            .addOnFailureListener { e -> Toast.makeText(applicationContext,e.toString(), Toast.LENGTH_SHORT).show() }
    }

}