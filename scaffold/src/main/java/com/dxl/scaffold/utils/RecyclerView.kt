package com.dxl.scaffold.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.dxl.scaffold.ui.LinearItemDecoration

/**
 * @author duxiaolong
 */

/**
 * RecyclerView添加分割线
 */
fun RecyclerView.addDivider(
    paddingLeftDp: Float = 0f,
    paddingRightDp: Float = 0f,
    dividerHeightPx: Int = 1,
    @ColorInt dividerColor: Int = Color.parseColor("#28979797"),
    showLastLine: Boolean = false
) {
    this.addItemDecoration(
        LinearItemDecoration.Builder(context)
            .setSpan(dividerHeightPx)
            .setLeftPadding(paddingLeftDp.dp)
            .setRightPadding(paddingRightDp.dp)
            .setColor(dividerColor)
            .setShowLastLine(showLastLine)
            .build()
    )
}