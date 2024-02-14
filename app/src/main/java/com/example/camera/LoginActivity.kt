package com.example.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {


    private val client = OkHttpClient()
    private val baseUrl = "https://frafortu.pythonanywhere.com/login"
    private lateinit var  auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        findViewById<SignInButton>(R.id.SignGoogle).setOnClickListener {
              signInGoogle()

        }


        val usernameEditText: EditText = findViewById(R.id.username)
        val passwordEditText: EditText = findViewById(R.id.password)
        val loginButton: Button = findViewById(R.id.buttonLogin)
        val registerButton: Button = findViewById(R.id.buttonRegister)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Make a POST request to the Flask backend for login
            loginToServer(username, password)
        }

        registerButton.setOnClickListener {
            // Launch the registration activity
            val intent = Intent(applicationContext, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }



    private fun signInGoogle(){
        val signIntent = googleSignInClient.signInIntent
        launcher.launch(signIntent)

    }
private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleResults(task)
    }
}
    private fun handleResults(task: Task<GoogleSignInAccount>){
        if(task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if(account !=null){
                updateUI(account)
            }
        }else{
            Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
        auth.signInWithCredential(credential).addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(this,"Oauth riuscita",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
            }
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
                        showToast("Login successful")
                        val token = jsonResponse.getString("token")
                        saveTokenToSharedPreferences(token)
                        // Login successful, proceed to the profile activity
                        fetchUserProfile()
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

    private fun saveTokenToSharedPreferences(token: String) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("jwtToken", token)
        editor.apply()
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

                    runOnUiThread {
                        // Create an intent to start ProfileActivity and put user profile data as extras
                        val intent = Intent(applicationContext, ProfileActivity::class.java)
                        intent.putExtra("username", get_username)
                        intent.putExtra("email", email)
                        intent.putExtra("name", name)
                        intent.putExtra("surname", surname)
                        startActivity(intent)
                        finish() // Close the login activity
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
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
