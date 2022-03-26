package com.aemerse.muserse.customViews

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min


class RoundedImageView : AppCompatImageView {

    constructor(context: Context?) : super((context)!!)

    constructor(context: Context?, attrs: AttributeSet?) : super((context)!!, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        (context)!!, attrs, defStyle)

    override fun onDraw(canvas: Canvas) {
        val drawable: Drawable = drawable ?: return
        if (width == 0 || height == 0) {
            return
        }
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val w: Int = width
        val h: Int = height
        val roundBitmap: Bitmap = getCroppedBitmap(bitmap, w)
        canvas.drawBitmap(roundBitmap, 0f, 0f, null)
    }

    companion object {
        fun getCroppedBitmap(bmp: Bitmap, radius: Int): Bitmap {
            val sbmp = when {
                bmp.width != radius || bmp.height != radius -> {
                    val smallest: Float = min(bmp.width, bmp.height).toFloat()
                    val factor: Float = smallest / radius
                    Bitmap.createScaledBitmap(bmp,
                        (bmp.width / factor).toInt(),
                        (bmp.height / factor).toInt(), false)
                }
                else -> {
                    bmp
                }
            }
            val output: Bitmap = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val color = "#BAB399"
            val paint = Paint()
            val rect = Rect(0, 0, radius, radius)
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            paint.isDither = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = Color.parseColor(color)
            canvas.drawCircle(radius / 2 + 0.7f, radius / 2 + 0.7f,
                radius / 2 + 0.1f, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(sbmp, rect, rect, paint)
            return output
        }
    }
}