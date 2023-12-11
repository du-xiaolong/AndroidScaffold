package com.dxl.androidscaffold.app

import android.app.Application
import com.baidu.location.LocationClient
import com.baidu.mapapi.SDKInitializer

/**
 *
 * @author duxiaolong
 * @date 2023/12/11
 */
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()

        //百度定位隐私协议允许
        LocationClient.setAgreePrivacy(true)
        //百度地图隐私协议允许
        SDKInitializer.setAgreePrivacy(this, true)
        //地图sdk初始化
        SDKInitializer.initialize(this)
    }

}