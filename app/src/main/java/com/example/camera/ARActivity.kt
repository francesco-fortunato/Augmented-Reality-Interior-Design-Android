
package com.example.camera

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnAttach
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Anchor
import com.google.ar.core.Anchor.CloudAnchorState
import com.google.ar.core.Config
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.RotationsOrder
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.arcore.rotation
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.localRotation
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.node.CloudAnchorNode
import io.github.sceneview.ar.scene.destroy
import io.github.sceneview.collision.Quaternion
import io.github.sceneview.collision.Vector3
import io.github.sceneview.gesture.GestureDetector
import io.github.sceneview.gesture.MoveGestureDetector
import io.github.sceneview.gesture.RotateGestureDetector
import io.github.sceneview.gesture.ScaleGestureDetector
import io.github.sceneview.math.Position
import io.github.sceneview.math.quaternion
import io.github.sceneview.math.toQuaternion
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

private var kmodel="https://sceneview.github.io/assets/models/DamagedHelmet.glb"
class ARActivity : AppCompatActivity(R.layout.ar_activity) {

    lateinit var b : ImageButton
    lateinit var b1 : Button
    lateinit var sceneView: ARSceneView
    lateinit var loadingView: View
    lateinit var instructionText: TextView
    lateinit var horiz_hide_show: LinearLayout
    lateinit var button_hide_show : Button
    private lateinit var lastCloudAnchorNode: Anchor
    private var currentScaleFactor = 0.7f
    var isRotating = false


    private val anchorsList = mutableListOf<Triple<AnchorNode?, String, Float3>>()

    var vis: Boolean = false
    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    var anchorNode: AnchorNode? = null
        set(value) {
            if (field != value) {
                field = value
                updateInstructions()
            }
        }

    var trackingFailureReason: TrackingFailureReason? = null
        set(value) {
            if (field != value) {
                field = value
                updateInstructions()
            }
        }

    fun updateInstructions() {
        instructionText.text = trackingFailureReason?.let {
            it.getDescription(this)
        } ?: if (anchorNode == null) {
            getString(R.string.point_your_phone_down)
        } else {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFullScreen(
            findViewById(R.id.rootView),
            fullScreen = true,
            hideSystemBars = false,
            fitsSystemWindows = false
        )

        instructionText = findViewById(R.id.instructionText)
        loadingView = findViewById(R.id.loadingView)
        sceneView = findViewById<ARSceneView?>(R.id.sceneView).apply {
            planeRenderer.isEnabled = true
            configureSession { session, config ->
                config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
                config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
                config.lightEstimationMode = Config.LightEstimationMode.AMBIENT_INTENSITY
                config.cloudAnchorMode= Config.CloudAnchorMode.ENABLED
                //config.geospatialMode = Config.GeospatialMode.ENABLED
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
            }
            onSessionUpdated = { _, frame ->
                /* if (anchorNode == null) {
                    frame.getUpdatedPlanes()
                        .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                        ?.let { plane ->
                            addAnchorNode(plane.createAnchor(plane.centerPose))
                        }
                }*/
            }



            onTrackingFailureChanged = { reason ->
                this@ARActivity.trackingFailureReason = reason
            }

            onGestureListener = object : GestureDetector.OnGestureListener {
                override fun onDown(e: MotionEvent, node: Node?) {

                }

                override fun onShowPress(e: MotionEvent, node: Node?) {


                }

                override fun onSingleTapUp(e: MotionEvent, node: Node?) {

                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    node: Node?,
                    distance: Float2
                ) {
                }

                override fun onLongPress(e: MotionEvent, node: Node?) {

                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    node: Node?,
                    velocity: Float2
                ) {
                }

                override fun onSingleTapConfirmed(e: MotionEvent, node: Node?) {
                    if (node == null) {
                        // If the tapped node is null, add an anchor node
                        val hitResults = frame?.hitTest(e.x,e.y)
                        hitResults?.firstOrNull(){
                            it.isValid (depthPoint = false,point=false)
                        }?.createAnchorOrNull()?.let{
                                anchor ->
                            addAnchorNode(anchor, Float3(0.37438163f, 0.37438163f, 0.37438163f))
                            lastCloudAnchorNode= anchor

                        }

                    }
                }

                override fun onDoubleTap(e: MotionEvent, node: Node?) {

                }

                override fun onDoubleTapEvent(e: MotionEvent, node: Node?) {
                    if(node!=null)
                    {
                        Log.d("Pose", "Product pose: MI HAI PRESO")
                        Log.d("Node", "Node= $node")
                        val dad: AnchorNode = node.parent as AnchorNode
                        val dadanchor : Anchor = dad.anchor
                        Log.d("DAD", "DAD= $dad")
                        Log.d("DAD", "DAD ANCHOR= $dadanchor")

                        anchorsList.removeIf { (anchor, _, _) ->
                            anchor.toString() == dadanchor.toString()
                        }
                        node.parent=null
                        node.destroy()
                    }
                }

                override fun onContextClick(e: MotionEvent, node: Node?) {

                }

                override fun onMoveBegin(
                    detector: MoveGestureDetector,
                    e: MotionEvent,
                    node: Node?
                ) {

                }

                override fun onMove(detector: MoveGestureDetector, e: MotionEvent, node: Node?) {
                    if (node != null) {
                        val modelnode : ModelNode = node as ModelNode

                        modelnode.scale

                        Log.d("SCALE","Scale to units: ${modelnode?.scale}")

                    }
                }

                @SuppressLint("SuspiciousIndentation")
                override fun onMoveEnd(detector: MoveGestureDetector, e: MotionEvent, node: Node?) {
                    if (node != null) {
                        val modelnode : ModelNode = node as ModelNode

                        modelnode.scale

                        Log.d("SCALE","Scale to units: ${modelnode?.scale}")

                        if (node.parent is AnchorNode){
                            Log.d("ANCHOR NODE NEW (in teoria)", "${anchorNode?.anchor}")
                            Log.d("ANCHOR NODE NEW (in teoria)", "new: ${anchorNode?.anchor?.pose}")
                            for ((anchornode, model, scaling) in anchorsList){
                                if (anchornode.toString() == node.parent.toString()){
                                    // Update the entry with the new AnchorNode
                                    anchorsList.remove(Triple(anchornode, model, scaling))
                                    anchorsList.add(Triple(node.parent as AnchorNode, model, scaling))
                                    break // Exit the loop once the replacement is done
                                }
                            }
                        }

                    }
                }

                override fun onRotateBegin(detector: RotateGestureDetector, e: MotionEvent, node: Node?) {
                }

                override fun onRotate(
                    detector: RotateGestureDetector,
                    e: MotionEvent,
                    node: Node?
                ) {
                }

                override fun onRotateEnd(detector: RotateGestureDetector, e: MotionEvent, node: Node?) {
                }



                override fun onScaleBegin(
                    detector: ScaleGestureDetector,
                    e: MotionEvent,
                    node: Node?
                ) {

                }

                override fun onScale(detector: ScaleGestureDetector, e: MotionEvent, node: Node?) {
                    if (node is ModelNode) {
                        scaleModelNode(node, detector)
                    } else if (node is AnchorNode) {
                        // Check if one of the children is a ModelNode
                        val modelNodeChild = node.childNodes.firstOrNull { it is ModelNode } as? ModelNode
                        modelNodeChild?.let { scaleModelNode(it, detector) }
                    }
                }

                override fun onScaleEnd(detector: ScaleGestureDetector, e: MotionEvent, node: Node?) {
                    if (node is ModelNode) {
                        scaleModelNode(node, detector)

                        if (node.parent is AnchorNode) {
                            updateAnchorList(node.parent as AnchorNode, node)
                        }
                    } else if (node is AnchorNode) {
                        // Check if one of the children is a ModelNode
                        val modelNodeChild = node.childNodes.firstOrNull { it is ModelNode } as? ModelNode
                        modelNodeChild?.let {
                            scaleModelNode(it, detector)
                            updateAnchorList(node, it)
                        }
                    }
                }

                private fun scaleModelNode(modelNode: ModelNode, detector: ScaleGestureDetector) {
                    val scaleFactor = detector.scaleFactor

                    // Adjust the scale based on the scaleFactor
                    val newScaleX = modelNode.scale.x * scaleFactor
                    val newScaleY = modelNode.scale.y * scaleFactor
                    val newScaleZ = modelNode.scale.z * scaleFactor

                    // Define your scale limits
                    val minScale = 0.37438163f
                    val maxScale = 1.5f

                    // Clamp the new scale values to stay within the limits
                    val clampedScaleX = newScaleX.coerceIn(minScale, maxScale)
                    val clampedScaleY = newScaleY.coerceIn(minScale, maxScale)
                    val clampedScaleZ = newScaleZ.coerceIn(minScale, maxScale)

                    // Set the clamped scale to the modelNode
                    modelNode.scale = Float3(
                        clampedScaleX.toFloat(),
                        clampedScaleY.toFloat(),
                        clampedScaleZ.toFloat()
                    )
                }

                private fun updateAnchorList(anchorNode: AnchorNode, modelNode: ModelNode) {
                    Log.d("SCALE", "Scale to units: ${modelNode?.scale}")

                    for ((anchornode, model, scaling) in anchorsList) {
                        if (anchornode.toString() == anchorNode.toString()) {
                            // Update the entry with the new AnchorNode
                            anchorsList.remove(Triple(anchornode, model, scaling))
                            anchorsList.add(Triple(anchornode, model, modelNode?.scale) as Triple<AnchorNode?, String, Float3>)
                            break // Exit the loop once the replacement is done
                        }
                    }
                }
            }
        }

        // Check if anchor ID is passed in the intent
        val projectTitle = intent.getStringExtra("projectTitle")
        val anchorIdList = intent.getSerializableExtra("anchor_id_list") as? ArrayList<HashMap<String, String>>

        if (!anchorIdList.isNullOrEmpty()) {
            // Anchor ID list is present, iterate over the list and resolve each anchor
            b1 = findViewById<Button?>(R.id.hostButton).apply {
                text = "LOAD PROJECT"
                setOnClickListener {
                    val session = sceneView.session ?: return@setOnClickListener

                    for (anchorData in anchorIdList) {
                        val anchorId = anchorData["anchor_id"]
                        kmodel = anchorData["model"].toString()
                        val scaling = anchorData["scaling"].toString()
                        Log.d("SCALE","RESOLVED SCALE STRING $scaling")
                        if (!anchorId.isNullOrBlank()) {
                            // Resolve the anchor using the anchorId
                            val resolvedAnchor = session.resolveCloudAnchor(anchorId)
                            if (resolvedAnchor != null) {
                                val resolvedpose = resolvedAnchor.pose
                                Log.d("POSE RESOLVED","Resolved pose = $resolvedpose")
                                // Anchor resolved successfully, add anchor node
                                val float3Object = scaling?.let { it1 -> parseFloat3FromString(it1) }
                                addAnchorNode(resolvedAnchor, float3Object)
                                Log.d("Resolve", "Resolved $anchorId $kmodel $projectTitle")
                            } else {
                                // Handle anchor resolution failure
                                val resolutionFailureToast = Toast.makeText(
                                    context,
                                    "Failed to resolve anchor: $anchorId",
                                    Toast.LENGTH_LONG
                                )
                                resolutionFailureToast.show()
                                Log.d("CloudAnchor", "Failed to resolve anchor: $anchorId")
                            }
                        }
                    }
                }
            }
        } else {
            // No anchor ID passed, proceed with hosting logic
            b1 = findViewById<Button?>(R.id.hostButton).apply {
                setOnClickListener {

                    // Disable the button during the onClickListener execution
                    isClickable = false
                    isEnabled = false

                    val session = sceneView.session ?: return@setOnClickListener
                    val frame = sceneView.frame ?: return@setOnClickListener

                    if (sceneView.session?.estimateFeatureMapQualityForHosting(frame.camera.pose) == Session.FeatureMapQuality.INSUFFICIENT) {
                        val insufficientVisualDataToast = Toast.makeText(
                            context,
                            R.string.insufficient_visual_data,
                            Toast.LENGTH_LONG
                        )
                        insufficientVisualDataToast.show()
                        Log.d("CloudAnchor", "Insufficient visual data for hosting")
                        // Enable the button after showing the toast
                        isClickable = true
                        isEnabled = true

                        return@setOnClickListener
                    }

                    val anchorDataList = mutableListOf<JSONObject>()

                    // Iterate over anchorsList
                    for ((anchorNode, selectedModel, scaling) in anchorsList) {
                        val session = sceneView.session ?: continue

                        if (anchorNode != null) {
                            sceneView.addChildNode(CloudAnchorNode(sceneView.engine, anchorNode.anchor).apply {
                                host(session) { cloudAnchorId, state ->
                                    Log.d("CloudAnchor", "STATE: $state, CloudAnchorId: $cloudAnchorId")
                                    when (state) {
                                        CloudAnchorState.SUCCESS -> {
                                            Log.d("CloudAnchor", "Cloud anchor hosted successfully: $cloudAnchorId")

                                            // Create a JSON object for the anchor data
                                            val anchorData = JSONObject().apply {
                                                put("anchor_id", cloudAnchorId)
                                                put("model", selectedModel)
                                                put("scaling", scaling)
                                            }

                                            // Add the anchor data to the list
                                            anchorDataList.add(anchorData)

                                            Log.d("Actual Anchor Data List", "$anchorDataList")


                                            // Check if all anchors are hosted successfully
                                            if (anchorDataList.size == anchorsList.size) {
                                                // All anchors hosted, send the data to the server
                                                val projectTitle = intent.getStringExtra("projectTitle")

                                                // Create a JSON object to send to the server
                                                val requestBody = JSONObject().apply {
                                                    put("anchors", anchorDataList)
                                                    put("project_title", projectTitle)
                                                }

                                                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                                val authToken = sharedPreferences.getString("jwtToken", "")

                                                // Make a POST request to the Flask /anchors endpoint
                                                val client = OkHttpClient()
                                                val request = Request.Builder()
                                                    .url("https://frafortu.pythonanywhere.com/project")
                                                    .header("Content-Type", "application/json")
                                                    .header("Authorization", "Bearer $authToken") // Include the JWT in the Authorization header
                                                    .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody.toString()))
                                                    .build()

                                                client.newCall(request).enqueue(object : Callback {
                                                    override fun onFailure(call: Call, e: IOException) {
                                                        e.printStackTrace()
                                                        // Handle failure
                                                    }

                                                    override fun onResponse(call: Call, response: Response) {
                                                        // Handle the response from the server
                                                        val responseBody = response.body?.string()
                                                        Log.d("Response", responseBody ?: "Response body is null")

                                                        try {
                                                            val jsonResponse = JSONObject(responseBody)
                                                            val success = jsonResponse.optBoolean("success", false)

                                                            if (success) {
                                                                // Show a success Toast
                                                                runOnUiThread {
                                                                    Toast.makeText(context, "Operation successful", Toast.LENGTH_SHORT).show()
                                                                }
                                                            } else {
                                                                // Show a failure Toast or handle the failure case as needed
                                                                runOnUiThread {
                                                                    Toast.makeText(context, "Operation failed", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        } catch (e: JSONException) {
                                                            e.printStackTrace()
                                                            // Handle JSON parsing error
                                                        } finally {
                                                            // Enable the button after processing the response
                                                            runOnUiThread {
                                                                isClickable = true
                                                                isEnabled = true
                                                            }
                                                        }
                                                    }
                                                })
                                            }
                                        }

                                        else -> {
                                            Log.d("CloudAnchor", "Cloud anchor hosting failed: $cloudAnchorId")
                                            val failureToast = Toast.makeText(
                                                context,
                                                "Cloud anchor hosting failed: $cloudAnchorId",
                                                Toast.LENGTH_LONG
                                            )
                                            failureToast.show()
                                            // Enable the button after showing the toast
                                            runOnUiThread {
                                                isClickable = true
                                                isEnabled = true
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }

                }
            }
        }


        b = findViewById<ImageButton?>(R.id.button1).apply { setOnClickListener{kmodel="https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/black_sofa.glb?alt=media&token=e1368472-f80b-491d-ad78-2854286c95ea"}  }
        b = findViewById<ImageButton?>(R.id.button2).apply { setOnClickListener{kmodel="https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/folding_table.glb?alt=media&token=b7474aa2-bd90-4884-a6ef-f55539088d49"}  }
        b = findViewById<ImageButton?>(R.id.button3).apply { setOnClickListener{kmodel="https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/office_chair.glb?alt=media&token=7bb7eee6-b57a-4e1e-ac21-0f159e2624bb"}  }
        b = findViewById<ImageButton?>(R.id.button4).apply { setOnClickListener{kmodel="https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/sectional.glb?alt=media&token=d9efc97b-07ad-44aa-bd84-768a5d916062"}  }
        b = findViewById<ImageButton?>(R.id.button5).apply { setOnClickListener{kmodel="https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/sideboard.glb?alt=media&token=dc717563-3459-42e4-91e6-fcf63d48a9bd"}  }
        b = findViewById<ImageButton?>(R.id.button6).apply { setOnClickListener{kmodel="https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/sofa1.glb?alt=media&token=0e299740-c6bc-46e5-9211-713a09e67bc7"}  }


        horiz_hide_show = findViewById(R.id.buttonsContainer)

        button_hide_show = findViewById<Button?>(R.id.btn).apply {
            setOnClickListener {
                if (!vis) {
                    // Hide menu and rotate button
                    ObjectAnimator.ofFloat(this, "rotation", 0f).start()
                    animate().translationY(200f)
                    horiz_hide_show.animate().translationY(horiz_hide_show.height.toFloat())
                        .withEndAction {
                            horiz_hide_show.visibility = View.GONE
                        }
                } else {
                    // Show menu and rotate button
                    ObjectAnimator.ofFloat(this, "rotation", 180f).start()
                    animate().translationY(0f)
                    horiz_hide_show.visibility = View.VISIBLE
                    horiz_hide_show.animate().translationY(0f)
                        .withEndAction{
                        }
                }
                vis = !vis
            }
        }
    }

    fun addAnchorNode(anchor: Anchor, scaling: Float3?) {
        val selectedModel = kmodel  // Save the current selected model
        sceneView.addChildNode(
            AnchorNode(sceneView.engine, anchor)
                .apply {
                    isEditable = true
                    isPositionEditable =true
                    isRotationEditable = false

                    lifecycleScope.launch {
                        isLoading = true
                        sceneView.modelLoader.loadModelInstance(
                            selectedModel
                        )?.let { modelInstance ->
                            addChildNode(
                                ModelNode(
                                    modelInstance = modelInstance,
                                    // Scale to fit in a 0.5 meters cube
                                    scaleToUnits = 1.0f,
                                    // Bottom origin instead of center so the model base is on the floor
                                    centerOrigin = Position(y = -0.5f)
                                ).apply {
                                    isEditable = true
                                    isRotationEditable = false


                                    if (scaling != null) {
                                        this.scale = scaling
                                    }


                                }
                            )
                        }
                        isLoading = false
                        isRotationEditable = false
                    }
                    anchorNode = this

                }
        )

        // Add the anchor and the selected model to the list
        val newAnchorTriple = Triple(anchorNode, selectedModel, scaling)
        anchorsList.add(newAnchorTriple as Triple<AnchorNode?, String, Float3>)

        // Log the contents of the anchorsList
        Log.d("AnchorsList", "Added new anchor: $newAnchorTriple. AnchorsList: $anchorsList")
        Log.d("AnchorsList", "new anchor pose: ${anchor.pose}")
        anchorNode?.childNodes?.forEach { childNode ->
            val translation = childNode.worldTransform.translation
            val rotation = childNode.worldTransform.rotation
            println("Child Node Translation and rotation: $translation")
            println("Child Node Translation and rotation: $rotation")
        }
    }

    fun parseFloat3FromString(input: String): Float3? {
        try {
            // Extract values from the string
            val regex = Regex("Float3\\(x=(-?\\d+\\.\\d+), y=(-?\\d+\\.\\d+), z=(-?\\d+\\.\\d+)\\)")
            val matchResult = regex.find(input)
            Log.d("MATCH","Result: $matchResult")
            if (matchResult != null) {
                val (x, y, z) = matchResult.destructured
                // Create a Float3 object
                val float3 = Float3(x.toFloat(), y.toFloat(), z.toFloat())

                // Log the successfully parsed Float3
                Log.d("parseFloat3", "Successfully parsed Float3: $float3")

                return float3
            }
        } catch (e: Exception) {
            // Log any exceptions that occurred during parsing
            Log.e("parseFloat3", "Error parsing Float3 from input: $input", e)
        }

        // Log that parsing failed and return null
        Log.d("parseFloat3", "Failed to parse Float3 from input: $input")
        return null
    }


    fun Fragment.setFullScreen(
        fullScreen: Boolean = true,
        hideSystemBars: Boolean = true,
        fitsSystemWindows: Boolean = true
    ) {
        requireActivity().setFullScreen(
            this.requireView(),
            fullScreen,
            hideSystemBars,
            fitsSystemWindows
        )
    }

    fun Activity.setFullScreen(
        rootView: View,
        fullScreen: Boolean = true,
        hideSystemBars: Boolean = true,
        fitsSystemWindows: Boolean = true
    ) {
        rootView.viewTreeObserver?.addOnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) {
                WindowCompat.setDecorFitsSystemWindows(window, fitsSystemWindows)
                WindowInsetsControllerCompat(window, rootView).apply {
                    if (hideSystemBars) {
                        if (fullScreen) {
                            hide(
                                WindowInsetsCompat.Type.statusBars() or
                                        WindowInsetsCompat.Type.navigationBars()
                            )
                        } else {
                            show(
                                WindowInsetsCompat.Type.statusBars() or
                                        WindowInsetsCompat.Type.navigationBars()
                            )
                        }
                        systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
            }
        }
    }

    fun View.doOnApplyWindowInsets(action: (systemBarsInsets: Insets) -> Unit) {
        doOnAttach {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                action(insets.getInsets(WindowInsetsCompat.Type.systemBars()))
                WindowInsetsCompat.CONSUMED
            }
        }
    }
}