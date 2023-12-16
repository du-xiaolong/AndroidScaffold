package com.dxl.androidscaffold.net

/**
 *
 * @author duxiaolong
 * @date 2023/11/29
 */
data class ApiResponse<T>(
    val errorCode: Int,
    val errorMsg: String?,
    val data: T
)


data class Page<T>(
    val curPage: Int,
    val offset: Int,
    val over: Boolean,
    val pageCount: Int,
    val size: Int,
    val total: Int,
    val datas: List<T>
)