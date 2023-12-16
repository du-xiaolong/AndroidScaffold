package com.dxl.androidscaffold.ui.compose.clue

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CluePage(viewModel: ClueViewModel = viewModel()) {
    val isRefreshing by viewModel.dialogStatus.asFlow().collectAsState(initial = false)
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
        viewModel.requestArticleList(isRefresh = true)
    })
    val articleList by viewModel.articleListStateFlow.collectAsState()
    val lazyColumnState = rememberLazyListState()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = lazyColumnState,
            modifier = Modifier
                .pullRefresh(state = pullRefreshState, enabled = true)
                .fillMaxSize(),
            content = {
                itemsIndexed(
                    items = articleList,
                    key = { _: Int, item: Article ->
                        item.id
                    },
                    itemContent = { _: Int, item: Article ->
                        Text(text = item.title)
                    }
                )
            }
        )
        PullRefreshIndicator(refreshing = isRefreshing, state = pullRefreshState)
    }
}

@Composable
fun ArticleCard() {

}