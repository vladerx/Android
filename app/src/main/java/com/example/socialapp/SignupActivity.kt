package com.example.socialapp

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.socialapp.databinding.ActivitySignupBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID


class SignupActivity : AppCompatActivity() {

    lateinit var signupBinding: ActivitySignupBinding
    val auth :FirebaseAuth = FirebaseAuth.getInstance()
    val db = Firebase.firestore
    val firestoreUsers = Firebase.firestore.collection("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signupBinding = ActivitySignupBinding.inflate(layoutInflater)
        val view = signupBinding.root
        setContentView(view)

        signupBinding.edtxtEmailSu.setText(intent.getStringExtra("userEmail"))
        signupBinding.edtxtPasswordSu.setText(intent.getStringExtra("userPassword"))

        signupBinding.signupBtnSu.setOnClickListener {

        }
    }

}