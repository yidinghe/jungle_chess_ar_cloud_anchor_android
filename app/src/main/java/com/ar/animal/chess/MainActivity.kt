package com.ar.animal.chess

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.PixelCopy
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.io.IOException


private var fragment: ArFragment? = null

private val pointer = PointerDrawable()
private var isTracking = false
private var isHitting = false

/*
FYI, we can put the sceneform models to two locations:
1. src/main/assets/andy.sfb
2. src/main/res/raw/andy.sfb
 */

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

        fragment?.let {
            it.arSceneView.scene.setOnUpdateListener { frameTime ->
                it.onUpdate(frameTime)
                this.onUpdate()
            }
        }

        fab.setOnClickListener {
            takePhoto()
        }

        initializeGallery()
    }

    private fun takePhoto() {
        val filename = Util().generateFilename()
        val view = fragment!!.arSceneView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()
        PixelCopy.request(view, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    Util().saveBitmapToDisk(bitmap, filename)
                } catch (e: IOException) {
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                    return@request
                }
                val snackbar = Snackbar.make(findViewById(android.R.id.content), "Photo saved", Snackbar.LENGTH_LONG)
                snackbar.setAction("Open in Photos") {
                    val photoFile = File(filename)
                    val photoURI = FileProvider.getUriForFile(this, this.packageName + ".ar.codelab.name.provider", photoFile)
                    val intent = Intent(Intent.ACTION_VIEW, photoURI)
                    intent.setDataAndType(photoURI, "image/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(intent)
                }
                snackbar.show()
            } else {
                Toast.makeText(this, "Failed to copyPixels: $copyResult", Toast.LENGTH_LONG).show()
            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
    }

    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri) {
        ModelRenderable.builder()
                .setSource(fragment.context, model)
                .build()
                .thenAccept({
                    addNodeToScene(fragment, anchor, it)
                })
                .exceptionally {
                    val alertBuilder = AlertDialog.Builder(this)
                    alertBuilder.setMessage(it.message).setTitle("error!")
                    alertBuilder.create().show()
                    return@exceptionally null
                }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }

    private fun initializeGallery() {

        btn_dog.setOnClickListener {
            addObject(Uri.parse("Dog.sfb"))
        }

        btn_cat.setOnClickListener {
            addObject(Uri.parse("Mesh_Cat.sfb"))
        }

        btn_elephant.setOnClickListener {
            addObject(Uri.parse("Elephant_01.sfb"))
        }

        btn_mouse.setOnClickListener {
            addObject(Uri.parse("Mouse.sfb"))
        }

        btn_tiger.setOnClickListener {
            addObject(Uri.parse("Mesh_BengalTiger.sfb"))
        }

        btn_lion.setOnClickListener {
            addObject(Uri.parse("Lion.sfb"))
        }

        btn_leopard.setOnClickListener {
            addObject(Uri.parse("Mesh_Leopard.sfb"))
        }

        btn_wolve.setOnClickListener {
            addObject(Uri.parse("Wolves.sfb"))
        }
    }

    private fun addObject(model: Uri) {
        val frame = fragment!!.arSceneView.arFrame
        val pt = getScreenCenter()
        val hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
        for (hit in hits) {
            val track = hit.trackable
            if (track is Plane && track.isPoseInPolygon(hit.hitPose)) {
                placeObject(fragment!!, hit.createAnchor(), model)
                break
            }
        }
    }

    private fun onUpdate() {
        val trackingChanged = updateTracking()
        val contentView = findViewById<View>(android.R.id.content)
        if (trackingChanged) {
            if (isTracking) {
                contentView.overlay.add(pointer)
            } else {
                contentView.overlay.remove(pointer)
            }
            contentView.invalidate()
        }

        if (isTracking) {
            val hitTestChanged = updateHitTest()
            if (hitTestChanged) {
                pointer.setEnabled(isHitting)
                contentView.invalidate()
            }
        }
    }

    private fun updateHitTest(): Boolean {
        val frame = fragment!!.arSceneView.arFrame
        val pt = getScreenCenter()
        val wasHitting = isHitting
        isHitting = false
        val hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())

        for (hit in hits) {
            val track = hit.trackable
            if (track is Plane && track.isPoseInPolygon(hit.hitPose)) {
                isHitting = true
                break
            }
        }

        return wasHitting != isHitting
    }

    private fun updateTracking(): Boolean {
        val frame = fragment!!.arSceneView.arFrame
        val wasTracking = isTracking
        isTracking = frame.camera.trackingState == TrackingState.TRACKING
        return isTracking != wasTracking
    }

    private fun getScreenCenter(): Point {
        val view = findViewById<View>(android.R.id.content)
        return Point(view.width / 2, view.height / 2)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
