package com.dxl.androidscaffold.ui.waterMark

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.dxl.scaffold.utils.dp
import kotlin.math.sqrt

/**
 * 水印drawable
 * @author duxiaolong
 * @date 2023/10/13
 * @param text 水印文字
 * @param watermarkColor 水印颜色
 * @param backgroundColor view背景颜色
 * @param textSize 水印文字大小，单位px
 * @param space 水印间距
 */
class WaterMarkDrawable(
    private val text: String,
    @ColorInt private val backgroundColor: Int,
    @ColorInt watermarkColor: Int,
    textSize: Int,
    private val space: Int
) :
    Drawable() {

    enum class WatermarkType {
        BACKGROUND, FOREGROUND
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun get(
            text: String,
            @ColorInt backgroundColor: Int = Color.TRANSPARENT,
            @ColorInt watermarkColor: Int = Color.parseColor("#EEEEEE"),
            textSize: Int = 14.dp,
            space: Int = 200
        ) =
            WaterMarkDrawable(text, backgroundColor, watermarkColor, textSize, space)

        @SuppressLint("NewApi")
        fun View.setWatermark(
            text: String,
            @ColorInt backgroundColor: Int = Color.TRANSPARENT,
            @ColorInt watermarkColor: Int = Color.parseColor("#EEEEEE"),
            textSize: Int = 14.dp,
            space: Int = 200,
            type: WatermarkType = WatermarkType.BACKGROUND
        ) {
            if (type == WatermarkType.BACKGROUND) {
                foreground = null
                background = get(text, backgroundColor, watermarkColor, textSize, space)
            } else {
                background = null
                foreground = get(text, backgroundColor, watermarkColor, textSize, space)
            }
        }

        @SuppressLint("NewApi")
        fun Activity.setWaterMark(
            text: String,
            @ColorInt backgroundColor: Int = Color.TRANSPARENT,
            @ColorInt watermarkColor: Int = Color.parseColor("#EEEEEE"),
            textSize: Int = 14.dp,
            space: Int = 200,
            type: WatermarkType = WatermarkType.BACKGROUND
        ) {
            val rootView = this.findViewById<View>(android.R.id.content)
            if (rootView is ViewGroup && rootView.childCount > 0) {
                val view = rootView.getChildAt(0)
                if (type == WatermarkType.BACKGROUND) {
                    view.foreground = null
                    view.background = get(text, backgroundColor, watermarkColor, textSize, space)
                } else {
                    view.background = null
                    view.foreground = get(text, backgroundColor, watermarkColor, textSize, space)
                }
            }
        }



    }

    private val paint: Paint
    private val textRect: Rect

    init {
        paint = Paint().apply {
            this.color = watermarkColor
            this.isAntiAlias = true
            this.textSize = textSize.toFloat()
        }
        textRect = Rect()
        paint.getTextBounds(text, 0, text.length, textRect)
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()
        canvas.drawColor(backgroundColor)
        canvas.save()
        canvas.rotate(-45f, (width / 2).toFloat(), (height / 2).toFloat())

        val squareSize = (height + width) / sqrt(2f)

        val yStart = (-(squareSize - height) / 2f).toInt()
        val yEnd = (height + (squareSize - height) / 2f).toInt()

        val xStart = (-(squareSize - width) / 2f).toInt()
        val xEnd = (width + (squareSize - width) / 2f).toInt()

        for (y in yStart + textRect.height()..yEnd step textRect.height() + space) {
            for (x in (xStart..xEnd step textRect.width() + space)) {
                canvas.drawText(text, x.toFloat(), y.toFloat(), paint)
            }
        }

        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("PixelFormat.UNKNOWN", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }
}


