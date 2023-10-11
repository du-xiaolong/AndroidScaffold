package com.dxl.scaffold.utils

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.dxl.scaffold.R

/**
 * @author duxiaolong
 */
private const val THROTTLE_WINDOW = 600

/**
 * 防抖点击
 * 添加了点击效果
 */
fun <T : View> T.clickAnim(action: (value: T) -> Unit) {
    setOnClickListener {
        val key = R.id.video_view_throttle_first_id
        val windowStartTime = getTag(key) as? Long ?: 0
        val currentTime = System.currentTimeMillis()
        val delta = currentTime - windowStartTime
        if (delta >= THROTTLE_WINDOW) {
            setTag(key, currentTime)
            val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(this, "alpha", 0.3f, 1f)

            objectAnimator.interpolator = DecelerateInterpolator()
            objectAnimator.duration = 300
            objectAnimator.start()

            @Suppress("UNCHECKED_CAST")
            action.invoke(it as T)
        }
    }
}

/**
 * 点击效果，无防抖
 */
fun <T : View> T.clickNoThrottleAnim(action: (value: T) -> Unit) {
    setOnClickListener {
        val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(this, "alpha", 0.3f, 1f)

        objectAnimator.interpolator = DecelerateInterpolator()
        objectAnimator.duration = 300
        objectAnimator.start()

        @Suppress("UNCHECKED_CAST")
        action.invoke(it as T)
    }
}

/**
 * 防抖点击
 * 没有点击效果
 */
fun View.click(action: (value: View) -> Unit) {
    setOnClickListener {
        val key = R.id.video_view_throttle_first_id
        val windowStartTime = getTag(key) as? Long ?: 0
        val currentTime = System.currentTimeMillis()
        val delta = currentTime - windowStartTime
        if (delta >= THROTTLE_WINDOW) {
            setTag(key, currentTime)
            action.invoke(it)
        }
    }
}