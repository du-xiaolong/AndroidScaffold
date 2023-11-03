package com.dxl.androidscaffold.ui.main

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.dxl.androidscaffold.R
import com.dxl.androidscaffold.databinding.FragmentUiBinding
import com.dxl.androidscaffold.databinding.ItemUiFunctionBinding
import com.dxl.androidscaffold.file.FileActivity
import com.dxl.androidscaffold.ui.waterMark.WaterMarkDemoActivity
import com.dxl.scaffold.base.BaseViewModel
import com.dxl.scaffold.base.BaseVmFragment
import com.dxl.scaffold.utils.startActivity

/**
 *
 * @author duxiaolong
 * @date 2023/10/15
 */
class UIFragment : BaseVmFragment<BaseViewModel, FragmentUiBinding>() {
    companion object {
        fun newInstance() = UIFragment()
    }


    override fun initView() {
        val funcs = listOf(
            "水印",
            "文件"
        )
        vb.rvFuncs.adapter = object :
            BaseQuickAdapter<String, BaseDataBindingHolder<ItemUiFunctionBinding>>(
                R.layout.item_ui_function,
                funcs.toMutableList()
            ) {
            override fun convert(
                holder: BaseDataBindingHolder<ItemUiFunctionBinding>,
                item: String
            ) {
                holder.dataBinding?.btnFunc?.text = item
            }

        }.apply {
            setOnItemClickListener { _, _, position ->
                itemClick(getItem(position))
            }
        }
    }

    private fun itemClick(item: String) {
        when(item) {
            "水印" -> {
                startActivity<WaterMarkDemoActivity>()
            }
            "文件" -> {
                startActivity<FileActivity>()
            }
        }
    }


}