package com.sleepsemek.nnroutetouristaiassistant.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat

fun Context.bitmapFromVector(
    @DrawableRes drawableId: Int,
    @ColorInt tintColor: Int? = null,
    widthPx: Int? = 128,
    heightPx: Int? = 128
): Bitmap {
    val drawable = ContextCompat.getDrawable(this, drawableId) ?: throw IllegalArgumentException("Drawable not found")

    tintColor?.let { DrawableCompat.setTint(drawable, it) }

    val bitmapWidth = widthPx ?: drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
    val bitmapHeight = heightPx ?: drawable.intrinsicHeight.takeIf { it > 0 } ?: 1

    val bitmap = createBitmap(bitmapWidth, bitmapHeight)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

fun Context.bitmapFromVectorDualColor(
    @DrawableRes backgroundDrawableId: Int,
    @ColorInt backgroundColor: Int? = null,
    @DrawableRes foregroundDrawableId: Int,
    @ColorInt foregroundColor: Int? = null,
    widthPx: Int? = 128,
    heightPx: Int? = 128
): Bitmap {
    val backgroundDrawable = ContextCompat.getDrawable(this, backgroundDrawableId)
        ?: throw IllegalArgumentException("Background drawable not found")
    val foregroundDrawable = ContextCompat.getDrawable(this, foregroundDrawableId)
        ?: throw IllegalArgumentException("Foreground drawable not found")

    backgroundColor?.let { DrawableCompat.setTint(backgroundDrawable, it) }
    foregroundColor?.let { DrawableCompat.setTint(foregroundDrawable, it) }

    val bitmapWidth = widthPx
        ?: maxOf(backgroundDrawable.intrinsicWidth.takeIf { it > 0 } ?: 1,
            foregroundDrawable.intrinsicWidth.takeIf { it > 0 } ?: 1)
    val bitmapHeight = heightPx
        ?: maxOf(backgroundDrawable.intrinsicHeight.takeIf { it > 0 } ?: 1,
            foregroundDrawable.intrinsicHeight.takeIf { it > 0 } ?: 1)

    val bitmap = createBitmap(bitmapWidth, bitmapHeight)
    val canvas = Canvas(bitmap)

    backgroundDrawable.setBounds(0, 0, canvas.width, canvas.height)
    backgroundDrawable.draw(canvas)

    foregroundDrawable.setBounds(0, 0, canvas.width, canvas.height)
    foregroundDrawable.draw(canvas)

    return bitmap
}
