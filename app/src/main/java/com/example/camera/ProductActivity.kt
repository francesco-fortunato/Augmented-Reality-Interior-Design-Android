package com.example.camera

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductActivity : ComponentActivity() {
    var title: String? = null
    var shortdesc: String? = null
    var rating: Double? = null
    var price: Double? = null
    var img: String? = null
    var id: Int = 0

    var Playground: Button? = null
    var recyclerView: RecyclerView? = null
    var productList: MutableList<Product>? = null
    var adapter: ProductAdapter? = null

    //private lateinit var productInfoTextView: TextView
    private lateinit var database: DatabaseReference


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_product)

        Playground = findViewById(R.id.btnPlayground)

        productList = ArrayList<Product>()

        recyclerView = findViewById(R.id.recyclerView)

        database = FirebaseDatabase.getInstance().reference


        printAllObjects()
    }



    private fun printAllObjects() {
        val rootReference = database

        val rootListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val stringBuilder = StringBuilder()
                Log.d("Firebase", "onDataChange called")
                for (productSnapshot in dataSnapshot.children) {
                    Log.d("Firebase", "Product snapshot: $productSnapshot")

                    val productData = productSnapshot.getValue(Product::class.java)
                    Log.d("Firebase", "Product data: ${productData?.getTitle()}")

                   //var product= Product(productData.id,productData)

                    if (productData != null) {
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


fun loadData(){
    val run = Runnable {
        adapter = ProductAdapter(this, productList!!)
        recyclerView!!.adapter = adapter
        recyclerView!!.layoutManager = LinearLayoutManager(applicationContext)
    }
    runOnUiThread(run)
}
}



