package com.dxl.androidscaffold.net

import com.dxl.androidscaffold.ui.compose.client.Navigation
import com.dxl.androidscaffold.ui.compose.client.Tree
import com.dxl.androidscaffold.ui.compose.clue.Article
import retrofit2.http.GET
import retrofit2.http.Path

/**
 *
 * @author duxiaolong
 * @date 2023/11/29
 */
interface Api {

    @GET("article/top/json")
    suspend fun getArticleTop(): ApiResponse<List<Article>>

    @GET("article/list/{page}/json")
    suspend fun getArticleList(@Path("page") page: Int): ApiResponse<Page<Article>>

    @GET("navi/json")
    suspend fun getNavigation(): ApiResponse<List<Navigation>>

    @GET("tree/json")
    suspend fun getNavTree(): ApiResponse<List<Tree>>
}