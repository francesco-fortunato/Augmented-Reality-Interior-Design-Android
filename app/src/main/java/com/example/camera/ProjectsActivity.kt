package com.example.camera

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ProjectsActivity : AppCompatActivity() {

    private lateinit var projectsAdapter: ProjectsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var backToProfileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewProjects)
        backToProfileButton = findViewById(R.id.btnBackToProfile)

        // Set up RecyclerView and Adapter
        projectsAdapter = ProjectsAdapter { projectName ->
            // Handle click event for the project
            Log.d("ProjectsActivity", "Clicked on project: $projectName")
            fetchProjectDetails(projectName)
        }

        recyclerView.adapter = projectsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch user projects and update UI
        fetchUserProjects()

        // Set up back to profile button click listener
        backToProfileButton.setOnClickListener {
            // Navigate back to ProfileActivity when "Back to Profile" button is clicked
            finish()
        }
    }

    private fun fetchProjectDetails(projectName: String) {
        // Make a network request to Flask /project/{project_name}
        // Use an HTTP client library like Retrofit or OkHttp for network requests

        val projectDetailsUrl = "https://frafortu.pythonanywhere.com/project/$projectName"
        // Get the JWT from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("jwtToken", "")

        // Make a GET request to fetch project details
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(projectDetailsUrl)
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

                // Parse the JSON response to get project details information
                try {
                    val jsonResponse = JSONObject(responseBody)

                    if (jsonResponse.getBoolean("success")) {
                        // Extract relevant information from the JSON response
                        val projectId = jsonResponse.getInt("project_id")
                        val projectTitle = jsonResponse.getString("project_title")

                        // Check if the JSON response contains anchor details
                        if (jsonResponse.has("anchors") && jsonResponse.get("anchors") is JSONArray) {
                            val anchorsArray = jsonResponse.getJSONArray("anchors")
                            val anchorsList = mutableListOf<Pair<String, String>>() // Pair of anchorId and model

                            // Iterate through the anchorsArray and add each anchor to the list
                            for (i in 0 until anchorsArray.length()) {
                                val anchorObject = anchorsArray.getJSONObject(i)
                                val anchorId = anchorObject.getString("anchor_id")
                                val model = anchorObject.getString("model")
                                anchorsList.add(Pair(anchorId, model))
                            }

                            // Start ARActivity and pass the relevant data at the end
                            startARActivity(projectId, projectTitle, anchorsList)
                        } else {
                            // Handle the case where no anchor details are present
                            Log.e("ProjectsActivity", "Invalid format: 'anchors' key not found or not a JSONArray")
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    // Handle JSON parsing exception
                }
            }

        })
    }


    private fun startARActivity(projectId: Int, projectTitle: String, anchorIdList: List<Pair<String, String>>) {
        // Convert the list of pairs to a format that can be easily serialized
        val anchorIdArrayList = ArrayList<HashMap<String, String>>()

        for (pair in anchorIdList) {
            val hashMap = hashMapOf<String, String>()
            hashMap["anchor_id"] = pair.first
            hashMap["model"] = pair.second
            anchorIdArrayList.add(hashMap)
        }

        // Create an Intent to start ARActivity
        val arIntent = Intent(this, ARActivity::class.java)

        // Pass relevant data as extras to ARActivity
        arIntent.putExtra("project_id", projectId)
        arIntent.putExtra("project_title", projectTitle)
        arIntent.putExtra("anchor_id_list", anchorIdArrayList)

        // Start ARActivity
        startActivity(arIntent)
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
                            val projectsList = mutableListOf<String>()
                            for (i in 0 until jsonArray.length()) {
                                val projectName = jsonArray.getJSONObject(i).getString("project_name")
                                projectsList.add(projectName)
                            }

                            runOnUiThread {
                                // Ensure UI updates are done on the main thread
                                projectsAdapter.updateData(projectsList)
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
