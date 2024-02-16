package com.example.camera

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
    private lateinit var SharedProjectsButton: Button
    private lateinit var ListObjectsButton: Button
    private lateinit var logoutButton: Button
    private lateinit var projectsList: MutableList<String>
    private lateinit var username : String
    private lateinit var sessionAr: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Retrieve user profile data from intent extras
        username = intent.getStringExtra("username").toString()
        val email = intent.getStringExtra("email")
        val name = intent.getStringExtra("name")
        val surname = intent.getStringExtra("surname")

        // Initialize views
        usernameTextView = findViewById(R.id.textViewUsername)
        emailTextView = findViewById(R.id.textViewEmail)
        nameSurnameTextView = findViewById(R.id.textViewNameSurname)
        logoutButton = findViewById(R.id.btnLogout)

        usernameTextView.text = "Time to build, $name"
        emailTextView.text = "Email: $email"
        nameSurnameTextView.text = "username: $username"

        newProjectButton = findViewById(R.id.btnNewProject)
        myProjectsButton = findViewById(R.id.btnMyProjects)
        SharedProjectsButton = findViewById(R.id.btnSharedProjects)
        ListObjectsButton = findViewById(R.id.btnListObjects)
        sessionAr = findViewById(R.id.SessionBTn)

        // Set up new project button click listener
        newProjectButton.setOnClickListener {
            // Show a title input popup when "Start a New Project" button is clicked
            showTitleInputDialog()
        }

        sessionAr.setOnClickListener{
            //funzione che crea una session e chiama l activity di AR session
            showSessionOptionsDialog()

        }

        // Set up my projects button click listener
        myProjectsButton.setOnClickListener {
            // Start ProjectsActivity when "My Projects" button is clicked
            // and pass to it the List of projects
            fetchUserProjectsandStart()
        }

        // Set up Shared projects button click listener
        SharedProjectsButton.setOnClickListener {
            // Start ProjectsActivity when "My Projects" button is clicked
            fetchSharedUserProjectsandStart()
        }

        // Set up List Objects button click listener
        ListObjectsButton.setOnClickListener {
            // Launch the ListObjects activity (called product activity)
            val intent = Intent(applicationContext, ProductActivity::class.java)
            startActivity(intent)
        }

        // Set up logout button click listener
        logoutButton.setOnClickListener {

            // Clear the token stored in SharedPreferences
            clearTokenFromSharedPreferences()

            // Handle logout logic, e.g., clear SharedPreferences and navigate to login screen
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            showToast("Logout successful")
        }
    }

    private fun showSessionOptionsDialog() {
        val options = arrayOf("Create New Session", "Join Existing Session")
        AlertDialog.Builder(this)
            .setTitle("Session Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> createNewSession()
                    1 -> joinExistingSession()
                }
            }
            .show()
    }

    private fun createNewSession() {
        val sessionsReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("sessions")
        val newSessionMap = mapOf(
            "users" to mapOf(
                // Add user IDs or any relevant user data if needed
                "user1" to username,
                "user2" to "not defined"
            ),
            "models" to mapOf(
                // Add models or any initial data if needed
            )
        )
        // Generate a unique key for the new session
        val newSessionKey: String = sessionsReference.push().key!!
        sessionsReference.child(newSessionKey).setValue(newSessionMap)

        // Show dialog with the session key
        showSessionKeyDialog(newSessionKey)
    }

    private fun joinExistingSession() {
        // Prompt the user to enter the session key
        val inputEditText = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Enter Session Key")
            .setView(inputEditText)
            .setPositiveButton("Join") { _, _ ->
                val sessionKey = inputEditText.text.toString()
                // Start ARSessionActivity with sessionKey and participant mode
                startARSessionActivity(sessionKey, participantMode = true)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showSessionKeyDialog(sessionKey: String) {
        // Inflate the custom layout
        val view = LayoutInflater.from(this).inflate(R.layout.session_key_dialog, null)
        val sessionKeyTextView: TextView = view.findViewById(R.id.sessionKeyTextView)
        val copyButton: Button = view.findViewById(R.id.copyButton)
        val startButton: Button = view.findViewById(R.id.startButton)

        // Set session key text
        sessionKeyTextView.text = "Session key: $sessionKey"

        // Create the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(view as View)
            .create()

        // Set onClickListener for copy button
        copyButton.setOnClickListener {
            // Copy session key to clipboard
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Session Key", sessionKey)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Session key copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        // Set onClickListener for start button
        startButton.setOnClickListener {
            // Start ARSessionActivity with sessionKey and owner mode
            startARSessionActivity(sessionKey, participantMode = false)
            dialog.dismiss() // Dismiss the dialog when starting the session
        }

        // Show the dialog
        dialog.show()
    }

    private fun startARSessionActivity(sessionKey: String, participantMode: Boolean) {
        val intent : Intent
        if (participantMode){
            intent = Intent(this, AReSessionActivity::class.java)
        }
        else{
            intent = Intent(this, ARSessionActivity::class.java)
        }
        intent.putExtra("sessionId", sessionKey)
        intent.putExtra("participantMode", participantMode)
        startActivity(intent)
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
                    val get_username = jsonResponse.getString("username")
                    val email = jsonResponse.getString("email")
                    val name = jsonResponse.getString("name")
                    val surname = jsonResponse.getString("surname")

                    username = get_username

                    // Update UI with user profile information
                    runOnUiThread {
                        usernameTextView.text = "Time to build, $name"
                        emailTextView.text = "Email: $email"
                        nameSurnameTextView.text = "username: $get_username"
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun showTitleInputDialog() {
        fetchUserProjects()
        // Inflate the custom dialog layout
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_title_profile, null)

        // Initialize AlertDialog builder with the custom layout
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        // Get reference to EditText in the custom layout
        val inputTitle = dialogView.findViewById<EditText>(R.id.editTextTitle)

        // Set up the dialog appearance
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Transparent background
        dialog.show()

        // Set up the "OK" button click listener
        dialogView.findViewById<Button>(R.id.buttonOK).setOnClickListener {
            val title = inputTitle.text.toString()
            if (projectsList.any { project -> project == title }) {
                // Show a warning Toast and return
                Toast.makeText(this, "Project with the same title already exists", Toast.LENGTH_SHORT).show()
            } else {
                // Do something with the title, e.g., send it to the server or use it locally
                Toast.makeText(this, "Project Title: $title", Toast.LENGTH_SHORT).show()

                // Start ARActivity for anchor resolution
                val intent = Intent(this, ARActivity::class.java).apply {
                    putExtra("projectTitle", title) // Pass the project title to ARActivity
                }
                startActivity(intent)
                dialog.dismiss() // Dismiss the dialog
            }
        }

        // Set up the "Cancel" button click listener
        dialogView.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            dialog.dismiss() // Dismiss the dialog
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

    private fun fetchUserProjectsandStart() {
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
                                if (jsonArray.getJSONObject(i).getString("shared_with") != username){
                                    projectsList.add(projectName)
                                }
                            }

                            startProjectsActivity(projectsList)


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

    private fun startProjectsActivity(projectsList: MutableList<String>) {
        // Convert the list of triples to a format that can be easily serialized

        // Create an Intent to start ARActivity
        val intent = Intent(this, MyProjectsActivity::class.java)

        // Pass relevant data as extras to ARActivity
        intent.putExtra("project_list", ArrayList(projectsList))
        intent.putExtra("username", username)

        // Start ARActivity
        startActivity(intent)
    }

    private fun fetchSharedUserProjectsandStart() {
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
                            val projectsListShared = mutableListOf<String>()
                            for (i in 0 until jsonArray.length()) {
                                val projectName = jsonArray.getJSONObject(i).getString("project_name")
                                if (jsonArray.getJSONObject(i).getString("shared_with") == username && jsonArray.getJSONObject(i).getString("shared_with") != null){
                                    projectsListShared.add(projectName)
                                }
                            }

                            Log.d("user and projects", "$username and $projectsListShared")

                            startSharedProjectsActivity(projectsListShared)

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

    private fun startSharedProjectsActivity(projectsList: MutableList<String>) {

        // Create an Intent to start ARActivity
        val intent = Intent(this, SharedProjectsActivity::class.java)

        // Pass relevant data as extras to ARActivity
        intent.putExtra("project_list", ArrayList(projectsList))

        // Start SharedProjectsActivity
        startActivity(intent)
    }
    private fun clearTokenFromSharedPreferences() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("jwtToken")
        editor.apply()
    }
    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

}
