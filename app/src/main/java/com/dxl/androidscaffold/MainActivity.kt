package com.dxl.androidscaffold

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dxl.androidscaffold.databinding.ActivityMainBinding
import com.dxl.androidscaffold.ui.compose.ComposeFragment
import com.dxl.androidscaffold.ui.main.UIFragment
import com.dxl.scaffold.base.BaseViewModel
import com.dxl.scaffold.base.BaseVmActivity

class MainActivity : BaseVmActivity<BaseViewModel, ActivityMainBinding>() {

    override fun init(savedInstanceState: Bundle?) {
        val fragments = listOf(
            UIFragment.newInstance(),
            ComposeFragment.newInstance(),
            BlankFragment.newInstance(),
            BlankFragment.newInstance(),
            BlankFragment.newInstance()
        )
        vb.viewPager.adapter = object :FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
        vb.navView.setOnItemSelectedListener {
            vb.navView.menu.forEachIndexed { index, item ->
                if (it.itemId == item.itemId) {
                    vb.viewPager.setCurrentItem(index, false)
                }
            }
            true
        }
        vb.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                vb.navView.menu.getItem(position).isChecked = true
            }
        })

        vb.viewPager.setCurrentItem(0, false)

    }

}