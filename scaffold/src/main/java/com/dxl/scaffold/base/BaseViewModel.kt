package com.dxl.scaffold.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dxl.scaffold.utils.format
import com.dxl.scaffold.utils.llloge
import com.dxl.scaffold.utils.toastCenter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout


typealias Block<T> = suspend () -> T
typealias Error = suspend (e: Throwable) -> Unit
typealias Cancel = suspend (e: Throwable) -> Unit

/**
 * viewModel基类
 * @author duxiaolong
 */
open class BaseViewModel : ViewModel() {

    /**
     * 显示正在加载
     */
    var dialogStatus = MutableLiveData<Boolean>()

    /**
     * 错误事件
     */
    var errorLiveData = MutableLiveData<Throwable>()

    /**
     * 加载框内容
     */
    val progressLiveData = MutableLiveData<String?>()

    /**
     * 创建并执行协程
     * @param block 协程中执行
     * @param error 错误时执行
     * @param cancel 取消时只需
     * @param showErrorToast 是否弹出错误吐司
     * @return Job
     */
    fun launch(
        block: Block<Unit>,
        error: Error? = null,
        cancel: Cancel? = null,
        showErrorToast: Boolean = true,
        showLoadingDialog: Boolean = true,
        timeOut: Long = 0L,
        observeError: Boolean = true
    ): Job {
        return viewModelScope.launch {

            kotlin.runCatching {
                if (showLoadingDialog) dialogStatus.value = true
                if (timeOut > 0) {
                    withTimeout(timeOut) {
                        block.invoke()
                    }
                } else {
                    block.invoke()
                }
            }.onFailure {
                llloge(it)
                dialogStatus.value = false
                if (observeError) {
                    errorLiveData.value = it
                }
                when (it) {
                    is CancellationException -> {
                        cancel?.invoke(it)
                    }
                    else -> {
                        onError(it, showErrorToast)
                        error?.invoke(it)
                    }
                }
            }.onSuccess {
                dialogStatus.value = false
            }
        }
    }

    /**
     * 手动显示加载进度框
     */
    fun showProgressDialog() {
        dialogStatus.value = true
    }

    /**
     * 手动隐藏加载进度框
     */
    fun dismissProgressDialog() {
        dialogStatus.value = false
    }


    /**
     * 创建并执行协程
     * @param block 协程中执行
     * @return Deferred<T>
     */
    protected fun <T> async(block: Block<T>): Deferred<T> {
        return viewModelScope.async { block.invoke() }
    }

    protected suspend fun <T> Deferred<T>.await(onError: ((Throwable) -> Unit)? = null): T? {
        return kotlin.runCatching { this.await() }.onFailure { onError?.invoke(it) }.getOrNull()
    }


    /**
     * 统一处理错误
     * @param e 异常
     * @param showErrorToast 是否显示错误吐司
     */
    fun onError(e: Throwable, showErrorToast: Boolean = true) {
        if (showErrorToast) {
            e.format().toastCenter()
        }
    }

}

