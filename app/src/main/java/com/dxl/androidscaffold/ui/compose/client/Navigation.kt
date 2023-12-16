package com.dxl.androidscaffold.ui.compose.client

import com.dxl.androidscaffold.ui.compose.clue.Article


/**
 *
 * @author duxiaolong
 * @date 2023/12/15
 */
data class Navigation(
    val cid: Int,
    val name: String,
    var select: Boolean,
    val articles: List<Article>
)
