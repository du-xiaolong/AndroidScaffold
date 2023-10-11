@file:JvmName("LogUtil")

package com.dxl.scaffold.utils

import android.util.Log
import com.dxl.scaffold.BuildConfig
import kotlin.math.min

const val BASE_TAG = "duuu"

fun lllog(message: Any?, tag: String = BASE_TAG) {
    if (BuildConfig.DEBUG)
        Log.d(tag.substring(0, min(50, tag.length)), message?.toString() ?: "message is null")
}

fun llloge(message: Any?, tag: String = BASE_TAG) {
    if (BuildConfig.DEBUG)
        Log.e(tag.substring(0, min(50, tag.length)), message?.toString() ?: "message is null")
}

fun Any?.lllog() {
    lllog(message = this)
}

fun Any?.llloge() {
    llloge(message = this)
}