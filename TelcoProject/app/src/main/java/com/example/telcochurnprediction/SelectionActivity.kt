package com.example.telcochurnprediction

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class SelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selectionpage)


        val personalInfoButton = findViewById<Button>(R.id.personalInformationButton)
        val singleInputButton = findViewById<Button>(R.id.singleInputButton)

        personalInfoButton.setOnClickListener{
            val intent = Intent(this@SelectionActivity, PersonalInfoActivity::class.java)
            startActivity(intent)
        }

        singleInputButton.setOnClickListener{
            val intent = Intent(this@SelectionActivity, SingleInputActivity::class.java)
            startActivity(intent)
        }
    }
}