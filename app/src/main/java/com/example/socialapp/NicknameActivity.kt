package com.example.socialapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.socialapp.databinding.ActivityNicknameBinding
import com.example.socialapp.databinding.ActivitySignupBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class NicknameActivity : AppCompatActivity() {
    lateinit var nicknameBinding: ActivityNicknameBinding

    var userEmail = ""
    val firestoreUsers = Firebase.firestore.collection("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nicknameBinding = ActivityNicknameBinding.inflate(layoutInflater)
        val view = nicknameBinding.root
        setContentView(view)

        userEmail = intent.getStringExtra("userEmail").toString()

        nicknameBinding.submitNickname.setOnClickListener {
            val nickname = nicknameBinding.editxtNickname.text.toString()
            if (nickname.length < 16 && nickname != "" && checkForvalidChars(nickname)){
                checkIfNicknameAvailable(userEmail, nickname)
            }
        }

    }

    fun checkForvalidChars(userNickname: String): Boolean {
        for (char in userNickname){
            if (char == ','){
                return false
            }
        }
        return true
    }

    fun checkIfNicknameAvailable(email: String, nickname : String){
        firestoreUsers.get().addOnSuccessListener { document ->
            if (document != null) {
                var user : Users?
                var found = false
                for (doc in document.documents){
                    user = doc.toObject(Users::class.java)
                    if (user != null && user.userNickname == nickname){
                        found = true
                    }
                }
                if (!found){
                    addUserToFireDatabase(email, nickname)
                } else {
                    Toast.makeText(applicationContext,"Nickname is Taken, Please Try Different One!", Toast.LENGTH_SHORT).show()
                }
            }
        }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext,exception.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    fun addUserToFireDatabase(email : String, nickname : String) {
        val id: String = UUID.randomUUID().toString()
        val user = Users(id,email, nickname, "member")
        firestoreUsers.document(id)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(applicationContext,"Account Has Been Created!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@NicknameActivity, TopicActivity::class.java)
                intent.putExtra("mainNickname", nickname)
                intent.putExtra("mainRole", "member")
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e -> Toast.makeText(applicationContext,e.toString(), Toast.LENGTH_SHORT).show() }
    }
}