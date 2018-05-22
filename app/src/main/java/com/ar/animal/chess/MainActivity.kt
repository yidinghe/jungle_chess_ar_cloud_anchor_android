package com.ar.animal.chess

import android.content.Intent
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

import com.ar.animal.chess.model.*

import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.ar.animal.chess.model.Tile
import com.ar.animal.chess.model.TileType
import com.ar.animal.chess.storage.ChessStorageManager
import com.ar.animal.chess.util.Utils
import com.ar.animal.chess.util.d
import com.ar.animal.chess.util.e
import com.google.ar.core.*

import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.content_main.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import com.google.ar.sceneform.ux.RotationController

import kotlinx.android.synthetic.main.content_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private val RC_PERMISSIONS = 0x123
    private val RC_SIGN_IN = 234

    val providers: List<AuthUI.IdpConfig> = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

    private var installRequested: Boolean = false

    private var gestureDetector: GestureDetector? = null
    private var loadingMessageSnackbar: Snackbar? = null
    private var arSceneView: ArSceneView? = null
    /*
    Chess tiles
     */
    private var tilesGrassRenderable: ModelRenderable? = null
    private var tilesRiverRenderable: ModelRenderable? = null
    private var tilesTrapRenderable: ModelRenderable? = null
    private var tilesBasementRenderable: ModelRenderable? = null

    /*
    Chessman
     */
    private var playeAChessmen:MutableList<Chessman> = java.util.ArrayList<Chessman>()
    private var playeBChessmen:MutableList<Chessman> = java.util.ArrayList<Chessman>()
    private var playeAmouseRenderable: ModelRenderable? = null
    private var playeAcatRenderable: ModelRenderable? = null
    private var playeAdogRenderable: ModelRenderable? = null
    private var playeAwolveRenderable: ModelRenderable? = null
    private var playeAleopardRenderable: ModelRenderable? = null
    private var playeAtigerRenderable: ModelRenderable? = null
    private var playeAlionRenderable: ModelRenderable? = null
    private var playeAelephantRenderable: ModelRenderable? = null

    private var playeBmouseRenderable: ModelRenderable? = null
    private var playeBcatRenderable: ModelRenderable? = null
    private var playeBdogRenderable: ModelRenderable? = null
    private var playeBwolveRenderable: ModelRenderable? = null
    private var playeBleopardRenderable: ModelRenderable? = null
    private var playeBtigerRenderable: ModelRenderable? = null
    private var playeBlionRenderable: ModelRenderable? = null
    private var playeBelephantRenderable: ModelRenderable? = null

    // True once scene is loaded
    private var hasFinishedLoading = false

    private val mPointer = PointerDrawable()
    private val isTracking: Boolean = false
    private val isHitting: Boolean = false
    private var hasPlacedTilesSystem = false
    private var appAnchorState = AppAnchorState.NONE
    private var cloudAnchor: Anchor? = null
    private var arSession: Session? = null
    private var storageManager: ChessStorageManager? = null
    private val TAG = MainActivity::class.java.simpleName
    private var mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)


        mAuth = FirebaseAuth.getInstance();
        if (mAuth!!.currentUser != null) {

            showSnackbar("Welcome back ${mAuth!!.currentUser!!.displayName}")
        } else {
            //Sign In
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN)
        }



        arSceneView = findViewById(R.id.ar_scene_view)
        storageManager = ChessStorageManager(this)

        val tiles_grass = ModelRenderable.builder().setSource(this, Uri.parse("trees1.sfb")).build()
        val tiles_river = ModelRenderable.builder().setSource(this, Uri.parse("Wave.sfb")).build()
        val tiles_trap = ModelRenderable.builder().setSource(this, Uri.parse("Field_1268.sfb")).build()
        val tiles_basement = ModelRenderable.builder().setSource(this, Uri.parse("model.sfb")).build()

        val playA_chessman_mouse = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Hamster.sfb")).build()
        val playA_chessman_cat = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Cat.sfb")).build()
        val playA_chessman_dog = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Wolf.sfb")).build()
        val playA_chessman_wolf = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Hamster.sfb")).build()
        val playA_chessman_leopard = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Leopard.sfb")).build()
        val playA_chessman_tiger = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Hamster.sfb")).build()
        val playA_chessman_lion = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Lion.sfb")).build()
        val playA_chessman_elephant = ModelRenderable.builder().setSource(this, Uri.parse("Elephant.sfb")).build()

        val playB_chessman_mouse = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Hamster.sfb")).build()
        val playB_chessman_cat = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Cat.sfb")).build()
        val playB_chessman_dog = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Wolf.sfb")).build()
        val playB_chessman_wolf = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Hamster.sfb")).build()
        val playB_chessman_leopard = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Leopard.sfb")).build()
        val playB_chessman_tiger = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Hamster.sfb")).build()
        val playB_chessman_lion = ModelRenderable.builder().setSource(this, Uri.parse("Mesh_Lion.sfb")).build()
        val playB_chessman_elephant = ModelRenderable.builder().setSource(this, Uri.parse("Elephant.sfb")).build()

        btn_checkAnchor.setOnClickListener {
            checkUpdatedAnchor()
        }

        btn_resolveAnchor.setOnClickListener {
            resolveAnchor()
        }

        CompletableFuture.allOf(
                tiles_grass,
                tiles_river,
                tiles_trap,
                tiles_basement,
                playA_chessman_mouse,
                playA_chessman_cat,
                playA_chessman_dog,
                playA_chessman_wolf,
                playA_chessman_leopard,
                playA_chessman_tiger,
                playA_chessman_lion,
                playA_chessman_elephant,
                playB_chessman_mouse,
                playB_chessman_cat,
                playB_chessman_dog,
                playB_chessman_wolf,
                playB_chessman_leopard,
                playB_chessman_tiger,
                playB_chessman_lion,
                playB_chessman_elephant).handle<Any> { notUsed, throwable ->
            if (throwable != null) {
                Utils.displayError(this, "Unable to load renderable", throwable)
                return@handle null
            }

            try {
                tilesGrassRenderable = tiles_grass.get()
                tilesRiverRenderable = tiles_river.get()
                tilesTrapRenderable = tiles_trap.get()
                tilesBasementRenderable = tiles_basement.get()

                playeAmouseRenderable = playA_chessman_mouse.get()
                playeAcatRenderable = playA_chessman_cat.get()
                playeAdogRenderable = playA_chessman_dog.get()
                playeAwolveRenderable = playA_chessman_wolf.get()
                playeAleopardRenderable = playA_chessman_leopard.get()
                playeAtigerRenderable = playA_chessman_tiger.get()
                playeAlionRenderable = playA_chessman_lion.get()
                playeAelephantRenderable = playA_chessman_elephant.get()

                playeBmouseRenderable = playB_chessman_mouse.get()
                playeBcatRenderable = playB_chessman_cat.get()
                playeBdogRenderable = playB_chessman_dog.get()
                playeBwolveRenderable = playB_chessman_wolf.get()
                playeBleopardRenderable = playB_chessman_leopard.get()
                playeBtigerRenderable = playB_chessman_tiger.get()
                playeBlionRenderable = playB_chessman_lion.get()
                playeBelephantRenderable = playB_chessman_elephant.get()
                // EvBrything finished loading successfully.
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

//        startActivity(Intent(this,LoginActivity.javaClass))

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            var response = IdpResponse.fromResultIntent(data)

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                //continue
                var metadata = mAuth!!.getCurrentUser()!!.getMetadata();
                if (metadata!!.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                    // The user is new, show them a fancy intro screen!
                } else {
                    // This is an existing user, show them a welcome back screen.
                }
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getError()?.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

                showSnackbar(R.string.unknown_error);
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }

    fun showSnackbar(string: Int) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), string, Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    fun showSnackbar(string: String) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), string, Snackbar.LENGTH_LONG)
        snackbar.show()
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
                    arSession = session
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

        if (cloudAnchor != null) {
            d(TAG, "Already had cloudAnchor, no need to host again.")
            return false
        }

        if (tap != null && frame.camera.trackingState == TrackingState.TRACKING) {
            for (hit in frame.hitTest(tap)) {
                Log.d(TAG, "capture Hit")
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    // Create the Anchor.
                    val anchor = hit.createAnchor()
                    hostCloudAnchor(anchor)
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

    private fun createChessmen(tile: Tile){

        var mouseA:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeAmouseRenderable!!)
        var catA:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeAcatRenderable!!)
        var dogA:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeAdogRenderable!!)
        var wolfA:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeAwolveRenderable!!)
        var leopardA:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeAleopardRenderable!!)
        var tigerA:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeAtigerRenderable!!)
        var lionA:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeAlionRenderable!!)
        var elephantA:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeAelephantRenderable!!)

        val chessmanArrayA = arrayOf(mouseA,catA, dogA, wolfA, leopardA, tigerA, lionA, elephantA)
        playeAChessmen = Arrays.asList(*chessmanArrayA)

        var mouseB:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeBmouseRenderable!!)
        var catB:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeBcatRenderable!!)
        var dogB:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeBdogRenderable!!)
        var wolfB:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeBwolveRenderable!!)
        var leopardB:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeBleopardRenderable!!)
        var tigerB:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeBtigerRenderable!!)
        var lionB:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeBlionRenderable!!)
        var elephantB:Chessman = Chessman(this,
                AnimalDbModel(0,0, AnimalStats.ALIVE, AnimalType.MOUSE.ordinal),
                playeBelephantRenderable!!)

        val chessmanArrayB = arrayOf(mouseB,catB, dogB, wolfB, leopardB, tigerB, lionB, elephantB)
        playeBChessmen = Arrays.asList(*chessmanArrayB)
    }

    private fun createNeighbourTiles(center: Node) {
        var name: String
        var tile: Tile
        var tile2: Tile
        var distanceToCenter: Double
        var test: String

        for (row in 0..8) {
            for (col in 0..6) {
                name = row.toString() + "_" + col.toString()
                distanceToCenter = Math.sqrt(Math.pow((row - 4).toDouble(), 2.0) + Math.pow((col - 3).toDouble(), 2.0))

                if (row == 0 && col == 3) {

                    tile = Tile(this, name, distanceToCenter.toFloat(), TileType.TILE_BASEMENT, tilesBasementRenderable!!)
                    tile.localPosition = Vector3((col - 3).toFloat() / 4, 0.25F, (row - 4).toFloat() / 4)
                    tile.localRotation = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 90f)
                    tile.renderable = tilesBasementRenderable
                } else if(row == 8 && col == 3){
                    tile = Tile(this, name, distanceToCenter.toFloat(), TileType.TILE_BASEMENT, tilesBasementRenderable!!)
                    tile.localPosition = Vector3((col - 3).toFloat() / 4, 0.25F, (row - 4).toFloat() / 4)
                    tile.localRotation = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 270f)
                    tile.renderable = tilesBasementRenderable
                } else if ((col == 2 && (row == 0 || row == 8)) ||
                        (col == 3 && (row == 1 || row == 7)) ||
                        (col == 4 && (row == 0 || row == 8))) {
                    tile = Tile(this, name, distanceToCenter.toFloat(), TileType.TILE_TRAP, tilesTrapRenderable!!)
                    tile.renderable = tilesTrapRenderable
                    tile.localPosition = Vector3((col - 3).toFloat() / 4, 0F, (row - 4).toFloat() / 4)
                } else if (row == 4) {
                    tile = Tile(this, name, distanceToCenter.toFloat(), TileType.TILE_RIVER, tilesRiverRenderable!!)
                    tile.renderable = tilesRiverRenderable
                    tile.localPosition = Vector3((col - 3).toFloat() / 4, 0F, (row - 4).toFloat() / 4)
                } else {
                    tile = Tile(this, name, distanceToCenter.toFloat(), TileType.TILE_GRASS, tilesGrassRenderable!!)
                    tile.renderable = tilesGrassRenderable
                    tile.localPosition = Vector3((col - 3).toFloat() / 4, 0F, (row - 4).toFloat() / 4)
                }
                tile.setParent(center)
            }
        }
//        tile = Tile(this, "test", 5.0f, TileType.TILE_GRASS, tilesGrassRenderable!!)
//        tile.localPosition = Vector3(0f, 0f, 0.25F)
//        tile.setParent(center)
//
//        tile2 = Tile(this, "test2", 5.0f, TileType.TILE_GRASS, tilesRiverRenderable!!)
//        tile2.localPosition = Vector3(0.20f, 0f, 0F)
//        tile2.setParent(center)
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


    private fun setNewAnchor(newAnchor: Anchor) {
        if (cloudAnchor != null) {
            cloudAnchor!!.detach()
        }
        cloudAnchor = newAnchor
        appAnchorState = AppAnchorState.NONE
    }

    private fun placeBoard() {
        val anchorNode = AnchorNode(cloudAnchor)
        anchorNode.setParent(arSceneView!!.scene)
        val singleTile = createCenterTile()
        anchorNode.addChild(singleTile)
    }


    private fun hostCloudAnchor(anchor: Anchor) {
        val session = arSceneView!!.session

        val newAnchor = session.hostCloudAnchor(anchor)
        setNewAnchor(newAnchor)
        placeBoard()
        Snackbar.make(findViewById(android.R.id.content), "hostCloudAnchor", Snackbar.LENGTH_SHORT).show()
        d(TAG, "setNewAnchor: hostCloudAnchor HOSTING")
        appAnchorState = AppAnchorState.HOSTING
    }

    private fun resolveAnchor() {
        if (cloudAnchor != null) {
            e(TAG, "Already had cloud anchor, need clear anchor first.")
            return
        }
        val dialogFragment = ResolveDialogFragment()
        dialogFragment.setOkListener(this::onResolveOkPressed)
        dialogFragment.showNow(supportFragmentManager, "Resolve")
    }

    private fun onResolveOkPressed(dialogValue: String) {
        val shortCode = dialogValue.toInt()

        storageManager?.getCloudAnchorId(shortCode, object : ChessStorageManager.CloudAnchorIdListener {
            override fun onCloudAnchorIdAvailable(cloudAnchorId: String?) {
                if (arSession == null) {
                    e(TAG, "onResolveOkPressed failed due to arSession is null")
                } else {
                    val resolveAnchor = arSession!!.resolveCloudAnchor(cloudAnchorId)
                    setNewAnchor(resolveAnchor)
                    d(TAG, "onResolveOkPressed: resolving anchor")
                    appAnchorState = AppAnchorState.RESOLVING
                }
            }

        })
    }

    private fun checkUpdatedAnchor() {
        if (appAnchorState != AppAnchorState.HOSTING && appAnchorState != AppAnchorState.RESOLVING || cloudAnchor == null)
            return
        val cloudState = cloudAnchor!!.cloudAnchorState

        if (appAnchorState == AppAnchorState.HOSTING) {
            if (cloudState.isError) {
                appAnchorState = AppAnchorState.NONE
                Snackbar.make(findViewById(android.R.id.content), "Anchor hosted error:  state: $cloudState", Snackbar.LENGTH_SHORT).show()
                e(TAG, "Anchor hosted error:  CloudId: $cloudState")
            } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                storageManager?.nextRoomId(object : ChessStorageManager.ShortCodeListener {
                    override fun onShortCodeAvailable(shortCode: Int?) {
                        if (shortCode == null) {
                            Snackbar.make(findViewById(android.R.id.content), "Could not obtain a short code.", Snackbar.LENGTH_SHORT).show()
                            e(TAG, "Could not obtain a short code.")
                        } else {
                            storageManager!!.storeCloudAnchorIdUsingRoomId(shortCode, cloudAnchor!!.cloudAnchorId)
                            d(TAG, "Anchor hosted stored shortCode: $shortCode" +
                                    " CloudId: ${cloudAnchor!!.cloudAnchorId}")
                            Snackbar.make(findViewById(android.R.id.content), "Anchor hosted stored shortCode: $shortCode" +
                                    " CloudId: ${cloudAnchor!!.cloudAnchorId}", Snackbar.LENGTH_SHORT).show()

                        }
                    }
                })
                appAnchorState = AppAnchorState.HOSTED
                d(TAG, "Anchor hosted stored  CloudId:  ${cloudAnchor!!.cloudAnchorId}")
            } else {
                d(TAG, "Host Anchor state: $cloudState")
            }
        } else if (appAnchorState == AppAnchorState.RESOLVING) {
            if (cloudState.isError) {
                appAnchorState = AppAnchorState.NONE
                Snackbar.make(findViewById(android.R.id.content), "Anchor resolving error:  state: $cloudState", Snackbar.LENGTH_SHORT).show()
                e(TAG, "Anchor hosted error:  CloudId: $cloudState")
            } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                appAnchorState = AppAnchorState.RESOLVED
                Snackbar.make(findViewById(android.R.id.content), "Anchor resolved successfully!", Snackbar.LENGTH_SHORT).show()
                d(TAG, "Anchor resolved successfully!")
                placeBoard()
            } else {
                d(TAG, "Resolve Anchor state: $cloudState")
            }
        }

    }


}
