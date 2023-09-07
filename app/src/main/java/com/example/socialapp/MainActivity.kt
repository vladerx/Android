package com.example.socialapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.socialapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService

class MainActivity : AppCompatActivity() {

    lateinit var activityMainBinding : ActivityMainBinding
    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    val firestoreUsers = Firebase.firestore.collection("Users")
    var userNickname = ""
    var userRole = ""

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = activityMainBinding.root
        setContentView(view)
        askNotificationPermission()
        activityMainBinding.loginBtn.setOnClickListener{
            val userEmail = activityMainBinding.edtxtEmail.text.toString()
            val userPassword = activityMainBinding.edtxtPassword.text.toString()
            if (userEmail != "" && userPassword != "") {
                signinWithFirebase(userEmail, userPassword)
            } else {
                Toast.makeText(applicationContext, "Fields Cannot Be Empty", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        activityMainBinding.signupBtn.setOnClickListener{
            val userEmail = activityMainBinding.edtxtEmail.text.toString()
            val userPassword = activityMainBinding.edtxtPassword.text.toString()
            auth.signOut()
            val user = auth.currentUser
            if (user != null){
                if (user.isEmailVerified){
                    Toast.makeText(applicationContext,"Please choose unique nickname!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, NicknameActivity::class.java)
                    intent.putExtra("userEmail", userEmail)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Please verify your user by email!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            } else {
                if (userEmail != "" && userPassword != "") {
                    signupWithFirebase(userEmail, userPassword)
                } else {
                    Toast.makeText(applicationContext,"Email field cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        activityMainBinding.restpassBtn.setOnClickListener {
            val emailText = activityMainBinding.edtxtEmail.text.toString()
            if (emailText != ""){
                auth.sendPasswordResetEmail(activityMainBinding.edtxtEmail.text.toString()).addOnCompleteListener { task ->
                    Toast.makeText(applicationContext,"Password reset email sent to your email address!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(applicationContext,"Email field cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkIfUserInFireDatabase(email : String){
        firestoreUsers.get().addOnSuccessListener { document ->
            if (document != null) {
                var found = false
                var user : Users?
                for (doc in document.documents){
                    user = doc.toObject(Users::class.java)
                    if (user != null && user.userEmail == email){
                        found = true
                        userNickname = user.userNickname
                        userRole = user.userRole
                        Toast.makeText(applicationContext,"Successfully Signed In!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MainActivity, TopicActivity::class.java)
                        intent.putExtra("mainNickname", userNickname)
                        intent.putExtra("mainRole", userRole)
                        startActivity(intent)
                        finish()
                    }
                }
                if (!found){
                    Toast.makeText(applicationContext,"Please choose unique nickname!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, NicknameActivity::class.java)
                    intent.putExtra("userEmail", email)
                    startActivity(intent)
                    finish()
                }
            }
        }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext,exception.toString(), Toast.LENGTH_SHORT).show()
            }

    }

    fun signinWithFirebase(email : String, password : String){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {task ->
            if (task.isSuccessful){
                val user: FirebaseUser? = auth.currentUser
                if (user != null) {
                    if (user.isEmailVerified) {
                        checkIfUserInFireDatabase(email)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Please verify your user by email!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            } else {
                Toast.makeText(applicationContext,task.exception?.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null) {
            if (user.isEmailVerified){
                getUserInformationByEmail(user.email)
            } else {
                activityMainBinding.edtxtEmail.isVisible = true
                activityMainBinding.edtxtPassword.isVisible = true
                activityMainBinding.loginBtn.isVisible = true
                activityMainBinding.signupBtn.isVisible = true
                activityMainBinding.progressBar.isVisible = false
                activityMainBinding.restpassBtn.isVisible = true
            }
        } else {
            activityMainBinding.edtxtEmail.isVisible = true
            activityMainBinding.edtxtPassword.isVisible = true
            activityMainBinding.loginBtn.isVisible = true
            activityMainBinding.signupBtn.isVisible = true
            activityMainBinding.progressBar.isVisible = false
            activityMainBinding.restpassBtn.isVisible = true
        }
    }
    fun getUserInformationByEmail(email : String?) {
        firestoreUsers.get().addOnSuccessListener { document ->
            if (document != null) {
                var user : Users?
                for (doc in document.documents){
                    user = doc.toObject(Users::class.java)
                    if (user != null && user.userEmail == email){
                        userNickname = user.userNickname
                        userRole = user.userRole
                        val intent = Intent(this@MainActivity, TopicActivity::class.java)
                        intent.putExtra("mainNickname", userNickname)
                        intent.putExtra("mainRole", userRole)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext,exception.toString(), Toast.LENGTH_SHORT).show()
            }


    }
    fun signupWithFirebase(email: String, password : String){
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {task ->
            if (task.isSuccessful){
                verifyUserByEmail()
            } else {
                Toast.makeText(applicationContext,task.exception?.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun verifyUserByEmail(){
        val user: FirebaseUser? = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Verification email sent to " + user.email, Toast.LENGTH_SHORT).show()
            } else {
                Log.e("TAG", "Sending email verification task failed")
            }
        }
    }
    class MessagingServices : FirebaseMessagingService() {
        override fun onNewToken(token: String) {
            Log.d("TAG", "Refreshed token: $token")

        }
    }

}