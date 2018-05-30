package com.ar.animal.chess.util

import android.animation.TypeEvaluator
import com.google.ar.sceneform.math.Vector3


class Vector3Evaluator : TypeEvaluator<Vector3> {

    override fun evaluate(fraction: Float, startValue: Vector3?, endValue: Vector3?): Vector3? {

        val startX = startValue!!.x
        val startY = startValue!!.y
        val startZ = startValue!!.z

        val diffX = Math.abs(startValue!!.x - endValue!!.x);
        val diffY = Math.abs(startValue!!.y - endValue!!.y);
        val diffZ = Math.abs(startValue!!.z - endValue!!.z);

        val x = startX + diffX*fraction
        val y = startY + diffY*fraction
        val z = startZ + diffZ*fraction

        return Vector3(x, y, z)
    }
}