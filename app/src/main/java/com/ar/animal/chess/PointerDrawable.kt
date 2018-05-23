package com.ar.animal.chess

import android.graphics.*
import android.graphics.drawable.Drawable

class PointerDrawable : Drawable() {

    private val paint = Paint()
    private var enabled: Boolean = false

    fun isEnabled(): Boolean {
        return enabled
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun setAlpha(p0: Int) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setColorFilter(p0: ColorFilter?) {
    }

    override fun draw(canvas: Canvas?) {
        canvas?.let {
            val cx = canvas.width / 2
            val cy = canvas.height / 2
            if (enabled) {
                paint.color = Color.GRAY
                canvas.drawCircle(cx.toFloat(), cy.toFloat(), 10.0f, paint)
            } else {
                paint.color = Color.GRAY
                canvas.drawText("X", cx.toFloat(), cy.toFloat(), paint)
            }
        }
    }

}