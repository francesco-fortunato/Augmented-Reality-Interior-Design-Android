package com.example.camera


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView


//private const val kModelFile = "https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/black_sofa.glb?alt=media&token=2f40c941-06e3-4c09-aa05-52f4cdd86bf0"
private const val kMaxModelInstances = 10

class ARActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var kModelFile by remember { mutableStateOf("https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/black_sofa.glb?alt=media&token=e1368472-f80b-491d-ad78-2854286c95ea") }
            // A surface container using the 'background' color from the theme
            Box(

                modifier = Modifier.fillMaxSize(),
            ) {
                // The destroy calls are automatically made when their disposable effect leaves
                // the composition or its key changes.

                val engine = rememberEngine()
                val modelLoader = rememberModelLoader(engine)
                val materialLoader = rememberMaterialLoader(engine)
                val cameraNode = rememberARCameraNode(engine)
                val childNodes = rememberNodes()
                val view = rememberView(engine)
                val collisionSystem = rememberCollisionSystem(view)

                var planeRenderer by remember { mutableStateOf(true) }

                val modelInstances = remember { mutableListOf<ModelInstance>() }


                var trackingFailureReason by remember {
                    mutableStateOf<TrackingFailureReason?>(null)
                }
                var frame by remember { mutableStateOf<Frame?>(null) }
                ARScene(
                    modifier = Modifier.fillMaxSize(),
                    childNodes = childNodes,
                    engine = engine,
                    view = view,
                    modelLoader = modelLoader,
                    collisionSystem = collisionSystem,
                    sessionConfiguration = { session, config ->
                        /* config.depthMode =
                                when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                                    true -> Config.DepthMode.AUTOMATIC
                                    else -> Config.DepthMode.DISABLED
                                }*/
                        config.depthMode = Config.DepthMode.AUTOMATIC
                        config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                        config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                        config.focusMode = Config.FocusMode.AUTO
                    },
                    cameraNode = cameraNode,
                    planeRenderer = planeRenderer,
                    onTrackingFailureChanged = {
                        trackingFailureReason = it
                    },
                    onSessionUpdated = { session, updatedFrame ->
                        frame = updatedFrame

                        if (childNodes.isEmpty()) {
                            updatedFrame.getUpdatedPlanes()
                                .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                                ?.let { it.createAnchorOrNull(it.centerPose) }?.let { anchor ->
                                        childNodes += createAnchorNode(
                                            engine = engine,
                                            modelLoader = modelLoader,
                                            materialLoader = materialLoader,
                                            modelInstances = modelInstances,
                                            anchor = anchor,
                                            model = kModelFile

                                        )
                                    }
                        }
                    },
                    onGestureListener = rememberOnGestureListener(
                        onSingleTapConfirmed = { motionEvent, node ->
                            if (node == null) {
                                val hitResults = frame?.hitTest(motionEvent.x, motionEvent.y)
                                hitResults?.firstOrNull {
                                    it.isValid(
                                        depthPoint = false,
                                        point = false
                                    )
                                }?.createAnchorOrNull()
                                    ?.let { anchor ->
                                        planeRenderer = false
                                        childNodes += createAnchorNode(
                                            engine = engine,
                                            modelLoader = modelLoader,
                                            materialLoader = materialLoader,
                                            modelInstances = modelInstances,
                                            anchor = anchor,
                                            model = kModelFile
                                        )

                                    }
                            }
                        })


                )

                   Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {

                            Button(
                                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent),
                                onClick = { kModelFile= "https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/black_sofa.glb?alt=media&token=e1368472-f80b-491d-ad78-2854286c95ea"},
                                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp)

                            ) {
                                val imageModifier= Modifier.size(200.dp)
                                Image(painterResource(id= R.drawable.black_sofa),
                                    contentDescription = null,
                                    modifier = imageModifier)
                            }

                            Button(
                                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent) ,
                                onClick = { kModelFile= "https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/office_chair.glb?alt=media&token=7bb7eee6-b57a-4e1e-ac21-0f159e2624bb"},
                                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                            ) {
                                val imageModifier= Modifier.size(200.dp)
                                Image(painterResource(id= R.drawable.office_chair),
                                    contentDescription = null,
                                    modifier = imageModifier)
                            }

                            Button(
                                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent) ,
                                onClick = { kModelFile= "https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/sideboard.glb?alt=media&token=dc717563-3459-42e4-91e6-fcf63d48a9bd"},
                                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                            ) {
                                val imageModifier= Modifier.size(200.dp)
                                Image(painterResource(id= R.drawable.sideboard),
                                    contentDescription = null,
                                    modifier = imageModifier)
                            }

                            Button(
                                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent) ,
                                onClick = { kModelFile= "https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/sectional.glb?alt=media&token=d9efc97b-07ad-44aa-bd84-768a5d916062"},
                                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                            ) {
                                val imageModifier= Modifier.size(200.dp)
                                Image(painterResource(id= R.drawable.sectional),
                                    contentDescription = null,
                                    modifier = imageModifier)
                            }


                            Button(
                                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent),
                                onClick = { kModelFile= "https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/sofa1.glb?alt=media&token=0e299740-c6bc-46e5-9211-713a09e67bc7"},
                                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp)

                            ) {
                                val imageModifier= Modifier.size(200.dp)
                                Image(painterResource(id= R.drawable.sofa1),
                                    contentDescription = null,
                                    modifier = imageModifier)
                            }

                            Button(
                                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent) ,
                                onClick = { kModelFile= "https://firebasestorage.googleapis.com/v0/b/mac-proj-5f6eb.appspot.com/o/folding_table.glb?alt=media&token=b7474aa2-bd90-4884-a6ef-f55539088d49"},
                                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                            ) {
                                val imageModifier= Modifier.size(200.dp)
                                Image(painterResource(id= R.drawable.folding_table),
                                    contentDescription = null,
                                    modifier = imageModifier)
                            }

                        }

                    }

                Text(
                    modifier = Modifier
                        .systemBarsPadding()
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 32.dp, end = 32.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                    color = Color.White,
                    text = trackingFailureReason?.let {
                        it.getDescription(LocalContext.current)
                    } ?: if (childNodes.isEmpty()) {
                        stringResource(R.string.point_your_phone_down)
                    } else {
                        stringResource(R.string.tap_anywhere_to_add_model)
                    }
                )
            }

        }


    }

    /*   private fun changeModelFile(newModelFile: String) {
        kModelFile = newModelFile
    }*/
    fun createAnchorNode(
        engine: Engine,
        modelLoader: ModelLoader,
        materialLoader: MaterialLoader,
        modelInstances: MutableList<ModelInstance>,
        anchor: Anchor,
        model: String
    ): AnchorNode {
        // Log.d("createAnchorNode", "Creating anchor node for $model")
        val anchorNode = AnchorNode(engine = engine, anchor = anchor)
        // Asynchronously load instanced models
        modelLoader.loadInstancedModelAsync(model, 2) { loadedModelInstances ->
            // Use the loaded instances to create the ModelNode
            val modelNode = ModelNode(
                modelInstance = loadedModelInstances.component2(),

                // Scale to fit in a 0.5 meters cube
                //scaleToUnits = 0.3f,

                ).apply {
                // Model Node needs to be editable for independent rotation from the anchor rotation
                isEditable = true
            }

            // Create bounding box node
            val boundingBoxNode = CubeNode(
                engine,
                size = modelNode.extents,
                center = modelNode.center,
                materialInstance = materialLoader.createColorInstance(Color.White.copy(alpha = 0.5f))
            ).apply {
                isVisible = false
            }

            // Add nodes to the anchor
            modelNode.addChildNode(boundingBoxNode)
            anchorNode.addChildNode(modelNode)

            // Handle onEditingChanged event
            listOf(modelNode, anchorNode).forEach {
                it.onEditingChanged = { editingTransforms ->
                    boundingBoxNode.isVisible = editingTransforms.isNotEmpty()
                }
            }
        }

        return anchorNode
    }
}
