package com.example.camera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val baseUrl = "https://frafortu.pythonanywhere.com/login" // Replace with your actual PythonAnywhere URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEditText: EditText = findViewById(R.id.editTextUsername)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val loginButton: Button = findViewById(R.id.buttonLogin)
        val registerButton: Button = findViewById(R.id.buttonRegister)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Make a POST request to the Flask backend for login
            loginToServer(username, password)
        }

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Make a POST request to the Flask backend for login
            registerToServer(username, password)
        }
    }

    private fun loginToServer(username: String, password: String) {
        val json = JSONObject()
        json.put("username", username)
        json.put("password", password)

        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(baseUrl)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showToast("Failed to connect to the server")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("Response", responseBody ?: "Response body is null")

                try {
                    val jsonResponse = JSONObject(responseBody)
                    val success = jsonResponse.getBoolean("success")

                    if (success) {
                        // Login successful, proceed to the main activity
                        runOnUiThread {
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish() // Close the login activity
                        }
                    } else {
                        // Login unsuccessful, show a toast message
                        showToast(jsonResponse.getString("message"))
                    }
                } catch (e: JSONException) {
                    // Handle the JSON parsing error
                    Log.e("JSON Parsing Error", e.message ?: "Unknown error")
                }

            }

        })
    }

    private fun registerToServer(username: String, password: String) {
        val json = JSONObject()
        json.put("username", username)
        json.put("password", password)

        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url("https://frafortu.pythonanywhere.com/register")
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showToast("Failed to connect to the server")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("Response", responseBody ?: "Response body is null")

                try {
                    val jsonResponse = JSONObject(responseBody)
                    val success = jsonResponse.getBoolean("success")

                    if (success) {
                        // Registration successful, you might want to handle this accordingly
                        showToast("Registration successful")
                    } else {
                        // Registration unsuccessful, show a toast message
                        showToast(jsonResponse.getString("message"))
                    }
                } catch (e: JSONException) {
                    // Handle the JSON parsing error
                    Log.e("JSON Parsing Error", e.message ?: "Unknown error")
                }
            }
        })
    }


    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
