package com.dxl.scaffold.base

import android.content.Context
import com.tencent.mmkv.MMKV

/**
 *
 * @author duxiaolong
 */
object BaseApp {
    lateinit var applicationContext: Context

    fun init(applicationContext: Context) {
        //初始化MMKV
        MMKV.initialize(applicationContext)
    }
}