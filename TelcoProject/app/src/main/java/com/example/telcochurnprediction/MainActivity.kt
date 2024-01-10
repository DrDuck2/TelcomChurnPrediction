package com.example.telcochurnprediction

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginpage)

        auth = FirebaseAuth.getInstance()

        val loginButton = findViewById<Button>(R.id.loginPageLoginButton)
        val loginPassword = findViewById<EditText>(R.id.loginPagePasswordEditText)
        val loginEmail = findViewById<EditText>(R.id.loginPageEmailEditText)
        val progressBar = findViewById<ProgressBar>(R.id.loginPageProgressBar)



        loginButton.setOnClickListener{
            progressBar.isActivated = true
            val email = loginEmail.text.toString()
            val password = loginPassword.text.toString()

            //val email = "testuser@test.com"
            //val password ="testuser"

            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this){task ->
                if(task.isSuccessful){
                    val user = auth.currentUser
                    Log.d(TAG, "User signed in: $user")
                    switchWindow()
                    progressBar.isActivated = false
                } else{
                    Log.w(TAG, "Authentication failed", task.exception)
                    Toast.makeText(this, "Wrong Credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun switchWindow(){
        val intent = Intent(this@MainActivity, SelectionActivity::class.java)
        startActivity(intent)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finishAffinity()"))
    override fun onBackPressed() {
        finishAffinity()
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}