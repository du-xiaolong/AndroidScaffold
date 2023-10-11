package com.dxl.scaffold.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/**
 * @author duxiaolong
 */
/**
 * onStart开始时回调
 *
 * @param invoke
 * @receiver
 */
fun LifecycleOwner.repeatOnStart(invoke: suspend () -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            invoke.invoke()
        }
    }
}

/**
 * onResume开始时回调
 *
 * @param invoke
 * @receiver
 */
fun LifecycleOwner.repeatOnResume(invoke: suspend () -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            invoke.invoke()
        }
    }
}