package com.example.camera



import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseApp.*
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import org.w3c.dom.Comment


class MainActivity : ComponentActivity() {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private lateinit var cameraButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var productInfoTextView: TextView


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        // Find TextView and PreviewView
        productInfoTextView = findViewById(R.id.productInfoTextView)
        previewView = findViewById(R.id.previewView)
        cameraButton = findViewById(R.id.cameraButton)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Call function to print all objects inside Firebase
        printAllObjects()

        cameraButton.setOnClickListener {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
            }
        }


        // Initialize Camera Provider
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)



    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview
                )
            } catch (exc: Exception) {
                // Handle exceptions
            }
        }, ContextCompat.getMainExecutor(this))
        // For now, let's just display a Toast message indicating the camera is opening
        Toast.makeText(this, "Opening Camera", Toast.LENGTH_SHORT).show()
    }

    private fun printAllObjects() {
        val rootReference = database

        val rootListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val stringBuilder = StringBuilder()
                //Log.d("Firebase", "onDataChange called")
                for (productSnapshot in dataSnapshot.children) {
                   // Log.d("Firebase", "Product snapshot: $productSnapshot")
                    val productId = productSnapshot.key
                    val productData = productSnapshot.getValue(Product::class.java)

                    // Print product information

                    stringBuilder.append("Product ID: $productId\n")
                    stringBuilder.append("ID: ${productData?.id}\n")
                    stringBuilder.append("Image: ${productData?.img}\n")
                    stringBuilder.append("Price: ${productData?.price}\n")
                    stringBuilder.append("Rating: ${productData?.rating}\n")
                    stringBuilder.append("Short Description: ${productData?.shortdesc}\n")
                    stringBuilder.append("Title: ${productData?.title}\n")
                    stringBuilder.append("------------------------------\n")
                }
                runOnUiThread {
                    productInfoTextView.text = stringBuilder.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Log.w("Firebase", "loadData:onCancelled", databaseError.toException())
            }
        }

        rootReference.addValueEventListener(rootListener)
    }



    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }


}

data class Product(
    val id: Int = 0,
    val img: String = "",
    val price: Double = 0.0,
    val rating: Double = 0.0,
    val shortdesc: String = "",
    val title: String = ""
)



