package com.dxl.androidscaffold

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dxl.scaffold.Config
import com.dxl.scaffold.errorInfo
import com.dxl.scaffold.hint
import com.dxl.scaffold.net.ApiException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Config {

        }
    }
}