package com.ar.animal.chess.model

import android.content.Context
import com.ar.animal.chess.model.TileType
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable

class Tile(var context: Context,
           var tileName: String,
           var distanceToCenter: Float,
           var tileType: TileType,
           var tileRenderable: ModelRenderable): Node(){

    override fun onActivate(){
        if (scene == null) {
            throw IllegalStateException("Scene is null!")
        }
        renderable = tileRenderable
    }
}