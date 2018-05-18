package com.ar.animal.chess

import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.ar.animal.chess.PointerDrawable
import com.ar.animal.chess.Utils

import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.Trackable
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {
    private var installRequested: Boolean = false

    private var gestureDetector: GestureDetector? = null
    private var loadingMessageSnackbar: Snackbar? = null
    private var arSceneView: ArSceneView? = null
    private var tilesRenderable: ModelRenderable? = null

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

        val tiles_7x9 = ModelRenderable.builder().setSource(this, Uri.parse("Field_1268.sfb")).build()

        CompletableFuture.allOf(tiles_7x9).handle<Any> { notUsed, throwable ->
            if (throwable != null) {
                Utils.displayError(this, "Unable to load renderable", throwable)
                return@handle null
            }

            try {
                tilesRenderable = tiles_7x9.get()
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

                    for (plane in frame.getUpdatedTrackables(Plane::class.java)) {
                        Log.d(TAG, "capture plane")
                        for (anchor in plane.anchors) {
                            Log.d(TAG, "capture anchor")
                            val anchorNode = AnchorNode(anchor)
                            anchorNode.setParent(arSceneView!!.scene)
                            val singleTile = createSingleTile()
                            anchorNode.addChild(singleTile)
                        }
                        return true
                    }
                }
            }
        }
        return true
    }

    private fun createSingleTile(): Node {
        val base = Node()
        val tile = Node()
        tile.setParent(base)
        tile.localPosition = Vector3(0.0f, 0.5f, 0.0f)
        tile.renderable = tilesRenderable
        return base
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
