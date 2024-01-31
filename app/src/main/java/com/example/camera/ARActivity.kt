
package com.example.camera

import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
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
import com.google.ar.core.Config
import com.google.ar.core.TrackingFailureReason
import dev.romainguy.kotlin.math.Float2
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.gesture.GestureDetector
import io.github.sceneview.gesture.MoveGestureDetector
import io.github.sceneview.gesture.RotateGestureDetector
import io.github.sceneview.gesture.ScaleGestureDetector
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import kotlinx.coroutines.launch

private var kmodel="https://sceneview.github.io/assets/models/DamagedHelmet.glb"
class ARActivity : AppCompatActivity(R.layout.ar_activity) {

    lateinit var b : ImageButton
    lateinit var sceneView: ARSceneView
    lateinit var loadingView: View
    lateinit var instructionText: TextView
    lateinit var horiz_hide_show: LinearLayout
    lateinit var button_hide_show : Button
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
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
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
                            addAnchorNode(anchor)
                        }
                    }
                }

                override fun onDoubleTap(e: MotionEvent, node: Node?) {

                }

                override fun onDoubleTapEvent(e: MotionEvent, node: Node?) {

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

                }

                override fun onMoveEnd(detector: MoveGestureDetector, e: MotionEvent, node: Node?) {

                }

                override fun onRotateBegin(
                    detector: RotateGestureDetector,
                    e: MotionEvent,
                    node: Node?
                ) {

                }

                override fun onRotate(
                    detector: RotateGestureDetector,
                    e: MotionEvent,
                    node: Node?
                ) {

                }

                override fun onRotateEnd(
                    detector: RotateGestureDetector,
                    e: MotionEvent,
                    node: Node?
                ) {

                }

                override fun onScaleBegin(
                    detector: ScaleGestureDetector,
                    e: MotionEvent,
                    node: Node?
                ) {

                }

                override fun onScale(detector: ScaleGestureDetector, e: MotionEvent, node: Node?) {

                }

                override fun onScaleEnd(
                    detector: ScaleGestureDetector,
                    e: MotionEvent,
                    node: Node?
                ) {

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

    fun addAnchorNode(anchor: Anchor) {
        sceneView.addChildNode(
            AnchorNode(sceneView.engine, anchor)
                .apply {
                    isEditable = true
                    lifecycleScope.launch {
                        isLoading = true
                        sceneView.modelLoader.loadModelInstance(
                            kmodel
                        )?.let { modelInstance ->
                            addChildNode(
                                ModelNode(
                                    modelInstance = modelInstance,
                                    // Scale to fit in a 0.5 meters cube
                                    //  scaleToUnits = 0.5f,
                                    // Bottom origin instead of center so the model base is on floor
                                    centerOrigin = Position(y = -0.5f)
                                ).apply {
                                    isEditable = true
                                }
                            )
                        }
                        isLoading = false
                    }
                    anchorNode = this
                }
        )
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
