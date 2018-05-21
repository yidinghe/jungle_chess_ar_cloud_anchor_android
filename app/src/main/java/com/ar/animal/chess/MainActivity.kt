package com.ar.animal.chess

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast

import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {

    private val RC_PERMISSIONS = 0x123

    private var installRequested: Boolean = false

    private var gestureDetector: GestureDetector? = null
    private var loadingMessageSnackbar: Snackbar? = null
    private var arSceneView: ArSceneView? = null
    private var tilesGrassRenderable: ModelRenderable? = null
    private var tilesRiverRenderable: ModelRenderable? = null
    private var tilesTrapRenderable: ModelRenderable? = null
    private var tilesBasementRenderable: ModelRenderable? = null
    // True once scene is loaded
    private var hasFinishedLoading = false

    private val mPointer = PointerDrawable()
    private val isTracking: Boolean = false
    private val isHitting: Boolean = false
    private var hasPlacedTilesSystem = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        arSceneView = findViewById(R.id.ar_scene_view)

        val tiles_grass = ModelRenderable.builder().setSource(this, Uri.parse("Field_1268.sfb")).build()
        val tiles_river = ModelRenderable.builder().setSource(this, Uri.parse("Field_1268.sfb")).build()
        val tiles_trap = ModelRenderable.builder().setSource(this, Uri.parse("Field_1268.sfb")).build()
        val tiles_basement = ModelRenderable.builder().setSource(this, Uri.parse("Field_1268.sfb")).build()

        CompletableFuture.allOf(
                tiles_grass,
                tiles_river,
                tiles_trap,
                tiles_basement).handle<Any> { notUsed, throwable ->
            if (throwable != null) {
                Utils.displayError(this, "Unable to load renderable", throwable)
                return@handle null
            }

            try {
                tilesGrassRenderable = tiles_grass.get()
                tilesRiverRenderable = tiles_river.get()
                tilesTrapRenderable = tiles_trap.get()
                tilesBasementRenderable = tiles_basement.get()
                // Everything finished loading successfully.
                hasFinishedLoading = true

            } catch (ex: InterruptedException) {
                Utils.displayError(this, "Unable to load renderable", ex)
            } catch (ex: ExecutionException) {
                Utils.displayError(this, "Unable to load renderable", ex)
            }

            null
        }

        gestureDetector = GestureDetector(
                this,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        onSingleTap(e)
                        return true
                    }

                    override fun onDown(e: MotionEvent): Boolean {
                        return true
                    }
                })

        arSceneView!!
                .scene
                .setOnTouchListener { hitTestResult: HitTestResult, event: MotionEvent ->
                    // If the solar system hasn't been placed yet, detect a tap and then check to see if
                    // the tap occurred on an ARCore plane to place the solar system.

                    if (!hasPlacedTilesSystem) {
                        return@setOnTouchListener gestureDetector!!.onTouchEvent(event)
                    }
                    // Otherwise return false so that the touch event can propagate to the scene.
                    false
                }

        arSceneView!!
                .scene
                .setOnUpdateListener { frameTime ->
                    if (loadingMessageSnackbar == null) {
                        return@setOnUpdateListener
                    }

                    val frame = arSceneView!!.arFrame
                    if (frame == null) {
                        return@setOnUpdateListener
                    }

                    if (frame!!.camera.trackingState != TrackingState.TRACKING) {
                        return@setOnUpdateListener
                    }

                    for (plane in frame.getUpdatedTrackables(Plane::class.java)) {
                        if (plane.trackingState == TrackingState.TRACKING) {
                            hideLoadingMessage()
                        }
                    }
                }
        Utils.requestCameraPermission(this, RC_PERMISSIONS)
        //initializeGallery();
    }

    override fun onResume() {
        super.onResume()

        if (arSceneView!!.session == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                val session = Utils.createArSession(this, installRequested)
                if (session == null) {
                    installRequested = Utils.hasCameraPermission(this)
                    return
                } else {
                    arSceneView!!.setupSession(session)
                }
            } catch (e: UnavailableException) {
                Utils.handleSessionException(this, e)
            }

        }

        try {
            arSceneView!!.resume()
        } catch (ex: CameraNotAvailableException) {
            Utils.displayError(this, "Unable to get camera", ex)
            finish()
            return
        }

        if (arSceneView!!.session != null) {
            showLoadingMessage()
        }
    }

    public override fun onPause() {
        super.onPause()
        arSceneView!!.pause()
    }

    public override fun onDestroy() {
        super.onDestroy()
        arSceneView!!.destroy()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, results: IntArray) {
        if (!Utils.hasCameraPermission(this)) {
            if (!Utils.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                Utils.launchPermissionSettings(this)
            } else {
                Toast.makeText(
                        this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                        .show()
            }
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Standard Android full-screen functionality.
            window
                    .decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun onSingleTap(tap: MotionEvent) {
        if (!hasFinishedLoading) {
            return
        }
        val frame = arSceneView!!.arFrame
        if (frame != null) {
            if (!hasPlacedTilesSystem && tryPlaceTile(tap, frame)) {
                hasPlacedTilesSystem = true
            }
        }
    }

    private fun tryPlaceTile(tap: MotionEvent?, frame: Frame): Boolean {
        if (tap != null && frame.camera.trackingState == TrackingState.TRACKING) {
            for (hit in frame.hitTest(tap)) {
                Log.d(TAG, "capture Hit")
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    // Create the Anchor.
                    val anchor = hit.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arSceneView!!.scene)
                    val singleTile = createCenterTile()
                    anchorNode.addChild(singleTile)
                }
            }
        }
        return true
    }

    private fun createCenterTile(): Node {
        val base = Node()
        val centerTile = Node()
        centerTile.setParent(base)
        centerTile.localPosition = Vector3(0.0f, 0.0f, 0.0f)
        centerTile.renderable = tilesGrassRenderable
        createNeighbourTiles(centerTile)
        return base
    }

    
    private fun createNeighbourTiles(center:Node) {
        var name:String
        var tile:Tile
        var distanceToCenter: Double
        var modelRenderable:ModelRenderable
        var test: String

//        for (row in 0..8) {
//            for(col in 0..6){
//                name = row.toString() + "_" + col.toString()
//                distanceToCenter =  Math.sqrt(Math.pow((row - 4).toDouble(), 2.0)+Math.pow((col - 3).toDouble(), 2.0))
//
//                if((row == 4 && col == 0) ||(row == 4 && col == 6)){
//                    tile = Tile(this, name, distanceToCenter.toFloat(), TileType.TILE_BASEMENT, tilesBasementRenderable!!)
//                } else if((row == 3 && (col == 0 || col == 6)) ||
//                        (row == 4 && (col == 1 || col == 5)) ||
//                        (row == 5 && (col == 0 || col == 6))){
//                    tile = Tile(this, name, distanceToCenter.toFloat(), TileType.TILE_TRAP, tilesTrapRenderable!!)
//                } else if(col == 3){
//                    tile = Tile(this, name, distanceToCenter.toFloat(), TileType.TILE_RIVER, tilesRiverRenderable!!)
//                } else{
//                    tile = Tile(this, name, distanceToCenter.toFloat(), TileType.TILE_GRASS, tilesGrassRenderable!!)
//                }
//                //tile.localScale = Vector3(0.05f, 0.05f, 0.05f)
//                tile.localPosition = Vector3((col-3).toFloat(),(row - 4).toFloat(), 0F)
//                tile.setParent(center)
//            }
//        }

        tile = Tile(this, "TEST", 1.0F, TileType.TILE_GRASS, tilesRiverRenderable!!)
        tile.localPosition = Vector3(5F,5F,0F)
        tile.setParent(center)
    }
    
    private fun showLoadingMessage() {
        if (loadingMessageSnackbar != null && loadingMessageSnackbar!!.isShownOrQueued) {
            return
        }

        loadingMessageSnackbar = Snackbar.make(
                this@MainActivity.findViewById(android.R.id.content),
                "Searching for surfaces...",
                Snackbar.LENGTH_INDEFINITE)
        loadingMessageSnackbar!!.view.setBackgroundColor(-0x40cdcdce)
        loadingMessageSnackbar!!.show()
    }

    private fun hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return
        }

        loadingMessageSnackbar!!.dismiss()
        loadingMessageSnackbar = null
    }

    companion object {
        private val TAG = "MainActivity"
    }
}
