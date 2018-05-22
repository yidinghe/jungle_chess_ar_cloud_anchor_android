package com.ar.animal.chess.model

import android.content.Context
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable

class Chessman(var context: Context,
           var animalDbModel: AnimalDbModel,
           var chessRenderable: ModelRenderable): Node(){

    override fun onActivate(){
        if (scene == null) {
            throw IllegalStateException("Scene is null!")
        }
        renderable = chessRenderable
    }
}