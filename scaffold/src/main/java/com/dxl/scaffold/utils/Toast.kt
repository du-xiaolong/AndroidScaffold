package com.dxl.scaffold.utils

import android.graphics.Color
import android.view.Gravity
import com.blankj.utilcode.util.ToastUtils

/**
 * 吐司
 * @author duxiaolong
 */

val toastColor = Color.parseColor("#999999")
fun String.toast() {
    ToastUtils.make()
        .setBgColor(toastColor)
        .setTextColor(Color.WHITE)
        .setDurationIsLong(true)
        .show(this)
}

fun String.toast(yOffset: Int) {
    ToastUtils.make()
        .setBgColor(toastColor)
        .setTextColor(Color.WHITE)
        .setGravity(Gravity.BOTTOM, 0, yOffset)
        .setDurationIsLong(true)
        .show(this)
}

fun String.toastCenter() {
    ToastUtils.make()
        .setBgColor(toastColor)
        .setTextColor(Color.WHITE)
        .setGravity(Gravity.CENTER, 0, 0)
        .setDurationIsLong(true)
        .show(this)
}
