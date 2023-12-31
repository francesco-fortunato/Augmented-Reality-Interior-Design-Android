package com.example.camera



import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseApp.*


class MainActivity : ComponentActivity() {
    var text: TextView? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        text = findViewById(R.id.textView)
        val intent = Intent(applicationContext, ProductActivity::class.java)
        startActivity(intent)
    }



}



