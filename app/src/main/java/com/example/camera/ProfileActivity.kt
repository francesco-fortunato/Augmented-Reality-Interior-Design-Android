package com.example.camera

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class ProfileActivity : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var nameSurnameTextView: TextView
    private lateinit var newProjectButton: Button
    private lateinit var myProjectsButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views
        usernameTextView = findViewById(R.id.textViewUsername)
        emailTextView = findViewById(R.id.textViewEmail)
        nameSurnameTextView = findViewById(R.id.textViewNameSurname)
        logoutButton = findViewById(R.id.btnLogout)

        // Fetch user profile and update UI
        fetchUserProfile()

        newProjectButton = findViewById(R.id.btnNewProject)
        myProjectsButton = findViewById(R.id.btnMyProjects)

        // Set up new project button click listener
        newProjectButton.setOnClickListener {
            // Start ProductActivity when "Start a New Project" button is clicked
            val intent = Intent(this, ProductActivity::class.java)
            startActivity(intent)
        }

        // Set up my projects button click listener
        myProjectsButton.setOnClickListener {
            // Display a toast with "TBD" when "My Projects" button is clicked
            Toast.makeText(this, "TBD", Toast.LENGTH_SHORT).show()
        }

        // Set up logout button click listener
        logoutButton.setOnClickListener {
            // Handle logout logic, e.g., clear SharedPreferences and navigate to login screen
        }
    }

    private fun fetchUserProfile() {
        // Make a network request to Flask /profile
        // Use an HTTP client library like Retrofit or OkHttp for network requests

        //  User data class with 'username', 'email', 'name', and 'surname' properties
        val profileUrl = "https://frafortu.pythonanywhere.com/profile"
        // Get the JWT from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("jwtToken", "")

        // Make a GET request to fetch user profile
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(profileUrl)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $authToken") // Include the JWT in the Authorization header
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("Response", responseBody ?: "Response body is null")

                // Parse the JSON response to get user profile information
                try {
                    val jsonResponse = JSONObject(responseBody)
                    val username = jsonResponse.getString("username")
                    val email = jsonResponse.getString("email")
                    val name = jsonResponse.getString("name")
                    val surname = jsonResponse.getString("surname")

                    // Update UI with user profile information
                    runOnUiThread {
                        usernameTextView.text = "HI $name"
                        emailTextView.text = "Email: $email"
                        nameSurnameTextView.text = "username: $username"
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }
}
