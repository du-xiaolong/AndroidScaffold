package com.dxl.androidscaffold.ui.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import com.dxl.scaffold.utils.startActivity

/**
 *
 * @author duxiaolong
 * @date 2023/12/15
 */
class ComposeFragment : Fragment() {
    companion object {
        fun newInstance(): Fragment = ComposeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ComposeMain()
            }
        }
    }

    @Composable
    fun ComposeMain() {
        Button(
            modifier = Modifier.wrapContentSize(),
            onClick = {
                startActivity<ComposeActivity>()
            }) {
            Text("Compose")
        }
    }


    @Preview
    @Composable
    fun ComposeMainPreview() {
        ComposeMain()
    }

}