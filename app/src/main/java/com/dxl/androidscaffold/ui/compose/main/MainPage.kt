package com.dxl.androidscaffold.ui.compose.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dxl.androidscaffold.R
import com.dxl.androidscaffold.ui.compose.client.ClientPage
import com.dxl.androidscaffold.ui.compose.clue.CluePage
import com.dxl.androidscaffold.ui.compose.mine.MinePage
import com.dxl.androidscaffold.ui.compose.order.OrderPage
import com.dxl.androidscaffold.ui.compose.schedule.SchedulePage
import com.dxl.androidscaffold.ui.compose.theme.black
import com.dxl.androidscaffold.ui.compose.theme.primary100
import com.dxl.androidscaffold.ui.compose.theme.primary500
import com.dxl.androidscaffold.ui.compose.theme.white
import com.dxl.scaffold.utils.toast

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MainPage() {
    val navigationItems = listOf(
        Pair("线索", R.drawable.ic_tab_clue),
        Pair("客户", R.drawable.ic_tab_client),
        Pair("订单", R.drawable.ic_tab_order),
        Pair("日程", R.drawable.ic_tab_schedule),
        Pair("我的", R.drawable.ic_tab_mine)
    )
    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "主页")
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = primary500,
                    titleContentColor = white
                ),
                actions = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_search),
                        contentDescription = null,
                        tint = white,
                        modifier = Modifier
                            .size(42.dp)
                            .padding(all = 5.dp)
                            .clickable {
                                "search".toast()
                            }
                    )
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                content = {
                    navigationItems.forEachIndexed { index, pair ->
                        NavigationBarItem(
                            selected = index == selectedIndex,
                            onClick = { selectedIndex = index },
                            icon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = ImageVector.vectorResource(pair.second),
                                    contentDescription = null
                                )
                            },
                            label = {
                                Text(text = pair.first)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = primary500,
                                unselectedIconColor = black,
                                selectedTextColor = primary500,
                                unselectedTextColor = black,
                                indicatorColor = primary100
                            )
                        )
                    }
                }
            )
        },
        content = { paddingValues: PaddingValues ->
            val savableStateHolder = rememberSaveableStateHolder()
            Column(modifier = Modifier.padding(paddingValues)) {
                when(selectedIndex){
                    0 -> savableStateHolder.SaveableStateProvider(key = navigationItems[selectedIndex].first) {
                        CluePage()
                    }
                    1 -> savableStateHolder.SaveableStateProvider(key = navigationItems[selectedIndex].first) {
                        ClientPage()
                    }
                    2 -> savableStateHolder.SaveableStateProvider(key = navigationItems[selectedIndex].first) {
                        OrderPage()
                    }
                    3 -> savableStateHolder.SaveableStateProvider(key = navigationItems[selectedIndex].first) {
                        SchedulePage()
                    }
                    4 -> savableStateHolder.SaveableStateProvider(key = navigationItems[selectedIndex].first) {
                        MinePage()
                    }
                }
            }

        }
    )
}
