package com.dxl.androidscaffold.ui.compose.main

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dxl.androidscaffold.ui.compose.route.ROUTE_MAIN

@Composable
fun MainNavPage() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ROUTE_MAIN,
        builder = {
            composable(ROUTE_MAIN) {
                MainPage()
            }
        })
}