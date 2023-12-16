package com.dxl.androidscaffold.ui.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment

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
                Text(text = "1")
            }
        }
    }
}