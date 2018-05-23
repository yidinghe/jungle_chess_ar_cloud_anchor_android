package com.ar.animal.chess.model

import android.content.Context
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable

class ChessmanNode(var context: Context,
                   var animal: Animal,
                   var chessRenderable: ModelRenderable) : Node() {

    override fun onActivate() {
        if (scene == null) {
            throw IllegalStateException("Scene is null!")
        }
        renderable = chessRenderable
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