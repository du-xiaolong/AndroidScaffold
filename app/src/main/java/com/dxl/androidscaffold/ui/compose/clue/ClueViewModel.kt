package com.dxl.androidscaffold.ui.compose.clue

import com.dxl.androidscaffold.net.Api
import com.dxl.androidscaffold.net.HttpClient
import com.dxl.scaffold.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClueViewModel : BaseViewModel() {

    var currentPage = 1

    private val _articleListStateFlow = MutableStateFlow(emptyList<Article>())
    val articleListStateFlow = _articleListStateFlow.asStateFlow()

    init {
        requestArticleList(isRefresh = true)
    }

    fun requestArticleList(isRefresh: Boolean = true) {
        if (isRefresh) {
            currentPage = 1
        }
        launch({
            val topList = if (currentPage == 1) {
                HttpClient.getApiService<Api>().getArticleTop().data.onEach { it.top = true }
            } else emptyList()
            val articlePage =
                HttpClient.getApiService<Api>().getArticleList(currentPage).data
            if (!articlePage.over) {
                currentPage++
            }
            val articleList =
                if (isRefresh) topList + articlePage.datas else _articleListStateFlow.value + articlePage.datas

            _articleListStateFlow.value = articleList

        }, showLoadingDialog = currentPage == 1)
    }


}