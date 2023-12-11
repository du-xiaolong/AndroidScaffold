package com.dxl.androidscaffold.assist

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.ServiceConnection
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.dxl.scaffold.utils.lllog

/**
 * https://juejin.cn/post/7169033859894345765
 * @author duxiaolong
 * @date 2023/12/11
 */
class AutoAccessibilityService: AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
        lllog("---------oncreate")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        lllog("---------onServiceConnected")
    }

    override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
        lllog("----------bindService")
        return super.bindService(service, conn, flags)
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        lllog("---------------${event?.eventType}")
        lllog("---------------${event?.className}")
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.className == "com.lunan.erp.ui.MainActivity") {
            val alnTab =
                event.source?.findAccessibilityNodeInfosByViewId("com.lunan.erp:id/menuAliLunan")
                    ?.firstOrNull()
            alnTab?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return
        }

        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED && event.source?.viewIdResourceName == "com.lunan.erp:id/menuAliLunan") {
            lllog("点击")
        }


    }

    override fun onInterrupt() {

    }
}