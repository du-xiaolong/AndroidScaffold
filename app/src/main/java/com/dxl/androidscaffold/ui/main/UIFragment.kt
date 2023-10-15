package com.dxl.androidscaffold.ui.main

import com.dxl.androidscaffold.databinding.FragmentUiBinding
import com.dxl.scaffold.base.BaseViewModel
import com.dxl.scaffold.base.BaseVmFragment

/**
 *
 * @author duxiaolong
 * @date 2023/10/15
 */
class UIFragment: BaseVmFragment<BaseViewModel, FragmentUiBinding>() {
    companion object {
        fun newInstance() = UIFragment()
    }


}