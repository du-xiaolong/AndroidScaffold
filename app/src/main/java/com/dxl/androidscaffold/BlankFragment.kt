package com.dxl.androidscaffold

import com.dxl.androidscaffold.databinding.FragmentBlankBinding
import com.dxl.scaffold.base.BaseViewModel
import com.dxl.scaffold.base.BaseVmFragment

/**
 *
 * @author duxiaolong
 * @date 2023/10/15
 */
class BlankFragment : BaseVmFragment<BaseViewModel, FragmentBlankBinding>() {
    companion object {
        fun newInstance() = BlankFragment()
    }
}