package com.dxl.androidscaffold.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dxl.androidscaffold.ui.compose.main.MainNavPage
import com.dxl.androidscaffold.ui.compose.theme.WanTheme

/**
 * compose demo
 * 2023-12-16
 */
class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WanTheme {
                MainNavPage()
            }
        }
    }
}