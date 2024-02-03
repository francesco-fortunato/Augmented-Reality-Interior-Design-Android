package com.example.camera

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
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
    private lateinit var projectsList: MutableList<String>

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
            // Show a title input popup when "Start a New Project" button is clicked
            showTitleInputDialog()
        }

        // Set up my projects button click listener
        myProjectsButton.setOnClickListener {
            // Start ProjectsActivity when "My Projects" button is clicked
            val intent = Intent(this, ProjectsActivity::class.java)
            startActivity(intent)
        }

        // Set up logout button click listener
        logoutButton.setOnClickListener {
            // Handle logout logic, e.g., clear SharedPreferences and navigate to login screen
        }
    }

    private fun fetchUserProfile() {
        // Make a network request to Flask /profile
        // Use an HTTP client library like Retrofit or OkHttp for network requests

        // User data class with 'username', 'email', 'name', and 'surname' properties
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

    private fun showTitleInputDialog() {
        fetchUserProjects()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Project Title")

        // Set up the input
        val input = EditText(this)
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK") { _, _ ->
            val title = input.text.toString()
            if (projectsList.any { project -> project == title }) {
                // Show a warning Toast and return
                Toast.makeText(this, "Project with the same title already exists", Toast.LENGTH_SHORT).show()
            }
            else{
                // Do something with the title, e.g., send it to the server or use it locally
                Toast.makeText(this, "Project Title: $title", Toast.LENGTH_SHORT).show()

                // Start ARActivity for anchor resolution
                val intent = Intent(this, ARActivity::class.java).apply {
                    putExtra("projectTitle", title) // Pass the project title to ARActivity
                }
                startActivity(intent)
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        // Show the dialog
        builder.show()
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

                            // Update RecyclerView adapter with projects data
                            projectsList = mutableListOf<String>()
                            for (i in 0 until jsonArray.length()) {
                                val projectName = jsonArray.getJSONObject(i).getString("project_name")
                                projectsList.add(projectName)
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
