package com.ar.animal.chess.model

import android.animation.FloatEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.TextView
import com.ar.animal.chess.R
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.math.Vector3Evaluator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable

class ChessmanNode(var context: Context,
                   var animal: Animal,
                   var chessRenderable: ModelRenderable,
                   var animalScale:Float = 0.00f,
                   var moveDirection:MoveAnimeType = MoveAnimeType.STILL) : Node(), Node.OnTapListener {

    private val TAG = ChessmanNode::class.java.simpleName
    var chessmanPanel:Node? = null
    var moveAnimation: ObjectAnimator? = null
    var chessmanMoveListener: ChessmanMoveListener? = null

    internal fun setChessmanMoveListener( listener : ChessmanMoveListener) {
        this.chessmanMoveListener = listener
    }

    override fun onActivate() {
        if (scene == null) {
            throw IllegalStateException("Scene is null!")
        }

        if(chessmanPanel == null){
            chessmanPanel = Node()
            chessmanPanel!!.setParent(this)
            chessmanPanel!!.isEnabled = false
            chessmanPanel!!.localPosition = Vector3(0.0f, 0.125f, 0.0f)

            ViewRenderable.builder()
                    .setView(context, R.layout.panel_chessman)
                    .build()
                    .thenAccept(
                            { renderable ->
                                chessmanPanel!!.renderable = renderable
                                val ll_chessman_panel = renderable.view
                                val chessman_name = ll_chessman_panel.findViewById<TextView>(R.id.chessman_name)
                                val chessman_rule = ll_chessman_panel.findViewById<TextView>(R.id.chessman_rule)
                                val chessman_sp_rule = ll_chessman_panel.findViewById<TextView>(R.id.chessman_sp_rule)
                                chessman_name.text = animal.animalType.name
                                chessman_rule.text = "One square horizontally or vertically"
                                chessman_sp_rule.text = "None"
                                when(animal.animalType){
                                    AnimalType.RAT->
                                        chessman_sp_rule.text = "May go onto a water square"
                                    AnimalType.LION, AnimalType.TIGER->
                                        chessman_sp_rule.text = "May jump over a river by moving horizontally or vertically"
                                    AnimalType.LEOPARD->
                                        chessman_sp_rule.text = "May jump over a river by moving horizontally but not vertically "
                                }

                                val btn_forward = ll_chessman_panel.findViewById<ImageButton>(R.id.btn_forward)
                                val btn_back = ll_chessman_panel.findViewById<ImageButton>(R.id.btn_back)
                                val btn_left = ll_chessman_panel.findViewById<ImageButton>(R.id.btn_left)
                                val btn_right = ll_chessman_panel.findViewById<ImageButton>(R.id.btn_right)

                                btn_forward.setOnClickListener{
                                    Log.d(TAG,"btn_forward OnClick")
                                    moveDirection = MoveAnimeType.FORWARD
                                    startMoveAnimation(moveDirection)
                                    chessmanMoveListener!!.onChessmanMove(this, MoveAnimeType.FORWARD)
                                    }
                                btn_back.setOnClickListener{
                                    moveDirection = MoveAnimeType.BACK
                                    startMoveAnimation(moveDirection)
                                    chessmanMoveListener!!.onChessmanMove(this, MoveAnimeType.BACK)}
                                btn_left.setOnClickListener{
                                    moveDirection = MoveAnimeType.LEFT
                                    startMoveAnimation(moveDirection)
                                    chessmanMoveListener!!.onChessmanMove(this, MoveAnimeType.LEFT)}
                                btn_right.setOnClickListener{
                                    moveDirection = MoveAnimeType.RIGHT
                                    startMoveAnimation(moveDirection)
                                    chessmanMoveListener!!.onChessmanMove(this, MoveAnimeType.RIGHT)}

                            })
                    .exceptionally(
                            { throwable -> throw AssertionError("Could not load animal panel.", throwable) })
        }
        setOnTapListener(this)
        renderable = chessRenderable
    }

    /*
    Tap chess node, display move options panel
     */
    override fun onTap(p0: HitTestResult?, p1: MotionEvent?) {
        if (chessmanPanel == null) {
            return
        }
        Log.d(TAG,"chessman onTap")
        chessmanPanel!!.isEnabled = !chessmanPanel!!.isEnabled
    }

    /*
    Start chessman moving animation, if any
     */
    override fun onUpdate(p0: FrameTime?) {
        if (chessmanPanel == null) {
            return
        }

        if (scene == null) {
            return
        }
        val cameraPosition = scene.camera.worldPosition
        val cardPosition = chessmanPanel!!.getWorldPosition()
        val direction = Vector3.subtract(cameraPosition, cardPosition)
        val lookRotation = Quaternion.lookRotation(direction, Vector3.up())
        chessmanPanel!!.setWorldRotation(lookRotation)
    }

    fun startMoveAnimation(moveType:MoveAnimeType){
        if(moveType == MoveAnimeType.STILL) return
        Log.d(TAG,"createAnimators_forward")
        moveAnimation = createAnimators(moveType)
        moveAnimation!!.target = this
        moveAnimation!!.duration = 1000
        moveAnimation!!.start()
        Log.d(TAG,"startAnimators_forward")
    }

    private fun stopMoveAnimation() {
        if (moveAnimation == null) {
            return
        }
        moveAnimation!!.cancel()
        moveAnimation = null
    }

    fun createAnimators(moveType:MoveAnimeType):ObjectAnimator{
        val startVector3: Vector3 = this.localPosition
        var endVector3:Vector3 =  this.localPosition
        val animation = ObjectAnimator()

        when(moveType){
            MoveAnimeType.FORWARD ->
                endVector3.set(0.0f, 0.0f, -1.0f/8)
            MoveAnimeType.BACK ->
                endVector3.set(0.0f, 0.0f, 1.0f/8)
            MoveAnimeType.LEFT ->
                endVector3.set(-1.0f/8, 0.0f, 0.0f)
            MoveAnimeType.RIGHT ->
                endVector3.set(1.0f/8, 0.0f, 0.0f)
        }
        animation.setObjectValues(startVector3, endVector3)
        animation.propertyName = "localPosition"
        animation.interpolator = LinearInterpolator()
        animation.setEvaluator(Vector3Evaluator())
        animation.setAutoCancel(true)
        return animation
    }

    interface ChessmanMoveListener{
        abstract fun onChessmanMove(node: ChessmanNode, moveType:MoveAnimeType)
    }
}


class TileNode(var context: Context,
               var distanceToCenter: Float,
               var tile: Tile,
               var tileRenderable: ModelRenderable) : Node() {

    override fun onActivate() {
        if (scene == null) {
            throw IllegalStateException("Scene is null!")
        }
        renderable = tileRenderable
    }
}
