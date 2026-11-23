package org.fossify.home.helpers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import kotlin.math.min

/**
 * Applies an optional icon shape mask to launcher icons. The mask is applied while drawing,
 * so it works with the View/Canvas launcher without breaking drawable recycling or Glide.
 */
object IconShape {

    fun shapePath(shape: Int, width: Int, height: Int): Path {
        val path = Path()
        val w = width.toFloat()
        val h = height.toFloat()
        when (shape) {
            ICON_SHAPE_CIRCLE -> path.addOval(
                RectF(0f, 0f, w, h),
                Path.Direction.CCW
            )

            ICON_SHAPE_ROUNDED_SQUARE -> path.addRoundRect(
                RectF(0f, 0f, w, h),
                w * 0.22f,
                h * 0.22f,
                Path.Direction.CCW
            )

            ICON_SHAPE_SQUIRCLE -> {
                // Continuous-curvature superellipse approximation (iOS-style squircle).
                val r = min(w, h) * 0.42f
                val k = r * 0.5522848f
                path.moveTo(r, 0f)
                path.lineTo(w - r, 0f)
                path.cubicTo(w - r + k, 0f, w, r - k, w, r)
                path.lineTo(w, h - r)
                path.cubicTo(w, h - r + k, w - r + k, h, w - r, h)
                path.lineTo(r, h)
                path.cubicTo(r - k, h, 0f, h - r + k, 0f, h - r)
                path.lineTo(0f, r)
                path.cubicTo(0f, r - k, r - k, 0f, r, 0f)
                path.close()
            }

            else -> path.addRect(0f, 0f, w, h, Path.Direction.CCW)
        }
        return path
    }

    /** Draws a drawable through the configured mask, preserving its already-set bounds. */
    fun drawShaped(canvas: Canvas, drawable: Drawable?, shape: Int) {
        if (drawable == null) return
        if (shape == ICON_SHAPE_DEFAULT) {
            drawable.draw(canvas)
            return
        }
        val bounds = drawable.bounds
        val width = bounds.width()
        val height = bounds.height()
        if (width <= 0 || height <= 0) {
            drawable.draw(canvas)
            return
        }
        canvas.save()
        canvas.clipPath(shapePath(shape, width, height))
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        drawable.draw(canvas)
        canvas.restore()
    }

    /** Returns a pre-masked bitmap, used by the drawer where icons are rendered by Glide. */
    fun shapeBitmap(source: Bitmap, shape: Int): Bitmap {
        if (shape == ICON_SHAPE_DEFAULT) return source
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.save()
        canvas.clipPath(shapePath(shape, source.width, source.height))
        canvas.drawBitmap(source, 0f, 0f, paint)
        canvas.restore()
        source.recycle()
        return output
    }
}
