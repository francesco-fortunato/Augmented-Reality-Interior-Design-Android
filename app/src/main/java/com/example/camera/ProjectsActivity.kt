package com.example.camera

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ProjectsActivity : AppCompatActivity() {

    private lateinit var projectsTextView: TextView
    private lateinit var backToProfileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        // Initialize views
        projectsTextView = findViewById(R.id.textViewProjects)
        backToProfileButton = findViewById(R.id.btnBackToProfile)

        // Fetch user projects and update UI
        fetchUserProjects()

        // Set up back to profile button click listener
        backToProfileButton.setOnClickListener {
            // Navigate back to ProfileActivity when "Back to Profile" button is clicked
            finish()
        }
    }

    private fun fetchUserProjects() {
        // Make a network request to Flask /projects
        // Use an HTTP client library like Retrofit or OkHttp for network requests

        val projectsUrl = "https://frafortu.pythonanywhere.com/projects"
        // Get the JWT from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("jwtToken", "")

        // Make a GET request to fetch user projects
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(projectsUrl)
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

                // Parse the JSON response to get user projects information
                try {
                    val jsonResponse = JSONObject(responseBody)

                    if (jsonResponse.getBoolean("success")) {
                        // If the "success" key is true, check if "projects" is a JSON array
                        if (jsonResponse.has("projects") && jsonResponse.get("projects") is JSONArray) {
                            val jsonArray = jsonResponse.getJSONArray("projects")
                            val projectsList = mutableListOf<String>()

                            for (i in 0 until jsonArray.length()) {
                                val projectName = jsonArray.getJSONObject(i).getString("project_name")
                                projectsList.add(projectName)
                            }

                            // Update UI with user projects information
                            runOnUiThread {
                                if (projectsList.isNotEmpty()) {
                                    val projectsText = projectsList.joinToString(separator = "\n")
                                    projectsTextView.text = projectsText
                                } else {
                                    projectsTextView.text = "No projects available."
                                }
                            }
                        } else {
                            Log.e("ProjectsActivity", "Invalid format: 'projects' key not found or not a JSONArray")
                        }
                    } else {
                        Log.e("ProjectsActivity", "Request was not successful")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }
}
