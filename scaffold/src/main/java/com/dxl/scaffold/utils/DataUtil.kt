package com.dxl.scaffold.utils

/**
 * @author duxiaolong
 */
//数据不能为空
fun <T> T?.requireNotNull(lazyMessage: (() -> Any)? = null): T {
    if (this == null) {
        val message = lazyMessage?.let { it().toString() } ?: "数据异常！错误码776"
        throw IllegalArgumentException(message)
    } else {
        return this
    }
}

//数据不能为空
fun String?.requireNotEmpty(lazyMessage: (() -> Any)? = null): String {
    if (this.isNullOrEmpty()) {
        val message = lazyMessage?.let { it().toString() } ?: "数据异常！错误码778"
        throw IllegalArgumentException(message)
    } else {
        return this
    }
}