package com.dxl.androidscaffold.ui.compose.client

/**
 *
 * @author duxiaolong
 * @date 2023/12/15
 */
data class Tree(
    val name: String,
    val id: Int,
    val courseId: Int,
    val children:List<Children>
) {
    data class Children(
        val name: String,
        val id: Int
    )
}
