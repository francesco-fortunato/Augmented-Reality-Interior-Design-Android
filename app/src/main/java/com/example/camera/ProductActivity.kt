package com.example.camera

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ProductActivity : ComponentActivity() {
    var title: String = ""
    var shortdesc: String = ""
    var rating: Double = 0.0
    var price: Double = 0.0
    var img: Int = 0
    var id: Int = 0

    var recyclerView: RecyclerView? = null
    var productList: MutableList<Product>? = null
    var adapter: ProductAdapter? = null

    //private lateinit var productInfoTextView: TextView
    private lateinit var database: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_product)

        var Playground: Button = findViewById(R.id.btnPlayground)



        recyclerView = findViewById(R.id.recyclerView)

        database = FirebaseDatabase.getInstance().reference

        printAllObjects()

        Playground.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(this@ProductActivity, ARActivity::class.java)
            startActivity(intent)
        })
    }


    private fun printAllObjects() {
        val rootReference = database

        val rootListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                productList = ArrayList<Product>()
                Log.d("Firebase", "onDataChange called")
                for (productSnapshot in dataSnapshot.children) {
                    val key = productSnapshot.key

                    // Check if the key starts with "prod"
                    if (key != null && key.startsWith("prod")) {
                        id = productSnapshot.child("id").getValue<Int>()!!
                        title = productSnapshot.child("title").getValue<String>()!!
                        shortdesc = productSnapshot.child("shortdesc").getValue<String>()!!
                        rating = productSnapshot.child("rating").getValue<Double>()!!
                        price = productSnapshot.child("price").getValue<Double>()!!

                        val productData = Product(id, getDrawableResourceForProduct(title), price, rating, shortdesc, title)
                        productList!!.add(productData)
                    }
                }
                Log.d("InvokeSuccess -> ", "contenuto lista: ${productList.toString()}")
                loadData()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Log.w("Firebase", "loadData:onCancelled", databaseError.toException())
            }
        }

        rootReference.addValueEventListener(rootListener)
    }

    private fun getDrawableResourceForProduct(title: String): Int {
        // Implement your logic here to determine the drawable resource ID
        // For example, you could switch on the product title or use some other criteria
        return when (title) {
            "Chair" -> R.drawable.office_chair
            "table" -> R.drawable.folding_table
            "Couch" -> R.drawable.black_sofa
            "Corner table" -> R.drawable.sideboard
            "sofa1" -> R.drawable.sofa1
            "black vase" -> R.drawable.black_vase
            else -> R.drawable.black_sofa // Default image resource ID
        }
    }

    fun loadData(){
        val run = Runnable {
            adapter = ProductAdapter(this, productList!!)
            recyclerView!!.adapter = adapter
            recyclerView!!.layoutManager = LinearLayoutManager(applicationContext)
        }
        runOnUiThread(run)
    }

    //We don't need it here. Useful in profile page to get user information through token auth
    private fun fetchUserProfile() {
        // Make a network request to Flask /profile
        // Use an HTTP client library like Retrofit or OkHttp for network requests

        //  User data class with a 'username' property
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

                // Parse the JSON response to get the username
                try {
                    val jsonResponse = JSONObject(responseBody)
                    val username = jsonResponse.getString("username")
                    val email = jsonResponse.getString("email")
                    val name = jsonResponse.getString("name")
                    val surname = jsonResponse.getString("surname")

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }
}



