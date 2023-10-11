package com.dxl.scaffold.ui

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * 带下划线的textView
 * @author duxiaolong
 */
class DeleteLinedTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    init {
        paint.flags = paint.flags or Paint.STRIKE_THRU_TEXT_FLAG
        paint.isAntiAlias = true
    }
}