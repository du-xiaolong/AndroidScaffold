package com.dxl.scaffold.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject

/**
 *
 * @author duxiaolong
 */

inline fun <reified T> String?.parseList(): List<T> {
    if (this.isNullOrBlank()) return emptyList()
    return kotlin.runCatching {
        JSON.parseArray(this, T::class.java).requireNotNull()
    }.getOrDefault(emptyList())
}

fun Any?.toJsonString(): String {
    if (this == null) return ""
    return JSON.toJSONString(this)
}

inline fun <reified T> String?.parseObject(): T? {
    if (this.isNullOrBlank()) return null
    return kotlin.runCatching {
        JSON.parseObject(this, T::class.java)
    }.getOrNull()
}

fun String?.parseJsonObject(): JSONObject {
    if (this.isNullOrBlank()) return JSONObject()
    return kotlin.runCatching { JSON.parseObject(this) }.getOrDefault(JSONObject())
}

