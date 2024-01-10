package com.example.telcochurnprediction

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PersonalInfoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.personalinformationpage)


        val name = findViewById<EditText>(R.id.editTextName)
        val surname = findViewById<EditText>(R.id.editTextSurname)
        val age = findViewById<EditText>(R.id.editTextAge)
        val submitButton = findViewById<Button>(R.id.submitButton)

        val currentUser = auth.currentUser
        if(currentUser != null){
            val userId = currentUser.uid

            db.collection("UserData")
                .document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        name.setText(documentSnapshot.getString("Name"))
                        surname.setText(documentSnapshot.getString("Surname"))

                        val ageValue = documentSnapshot.getString("Age")
                        age.setText(ageValue)
                    }
                }
        }


        submitButton.setOnClickListener {
            val data = hashMapOf(
                "Name" to name.text.toString(),
                "Surname" to surname.text.toString(),  // Use List instead of array
                "Age" to age.text.toString()
            )

            if (currentUser != null) {
                val userId = currentUser.uid
                db.collection("UserData")
                    .document(userId)
                    .set(data)
            }
        }

    }
}