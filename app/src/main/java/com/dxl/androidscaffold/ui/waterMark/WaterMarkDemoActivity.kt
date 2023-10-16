package com.dxl.androidscaffold.ui.waterMark

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import com.dxl.androidscaffold.databinding.ActivityWaterMarkBinding
import com.dxl.androidscaffold.ui.waterMark.WaterMarkDrawable.Companion.setWaterMark
import com.dxl.androidscaffold.ui.waterMark.WaterMarkDrawable.Companion.setWatermark
import com.dxl.scaffold.base.BaseViewModel
import com.dxl.scaffold.base.BaseVmActivity
import com.dxl.scaffold.utils.dp

/**
 *
 * @author duxiaolong
 * @date 2023/10/15
 */
class WaterMarkDemoActivity : BaseVmActivity<BaseViewModel, ActivityWaterMarkBinding>() {

    override fun init(savedInstanceState: Bundle?) {
        vb.sbFontSize.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                reset()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        vb.sbSpace.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                reset()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        vb.rgArea.setOnCheckedChangeListener { buttonView, isChecked ->
            reset()
        }

        vb.rgColor.setOnCheckedChangeListener { buttonView, isChecked ->
            reset()
        }

        vb.rgType.setOnCheckedChangeListener { buttonView, isChecked ->
            reset()
        }

        vb.rbColorGreen.setOnCheckedChangeListener { buttonView, isChecked ->
            reset()
        }

        reset()
    }


    @SuppressLint("NewApi")
    private fun reset() {
        val fontSize = 9 + vb.sbFontSize.progress * 30 / 100
        val space = vb.sbSpace.progress * 4
        val text = vb.etWaterMark.text.toString().trim()
        val color = if (vb.rbColorGreen.isChecked) Color.GREEN else Color.RED
        val type =
            if (vb.rbTypeBottom.isChecked) WaterMarkDrawable.WatermarkType.BACKGROUND else WaterMarkDrawable.WatermarkType.FOREGROUND
        if (vb.rbAreaActivity.isChecked) {
            vb.v.background = null
            vb.v.foreground = null
            setWaterMark(
                text,
                watermarkColor = color,
                textSize = fontSize.dp,
                type = type,
                space = space
            )
        }else {
            vb.root.background = null
            vb.root.foreground = null
            vb.v.setWatermark(
                text,
                watermarkColor = color,
                textSize = fontSize.dp,
                type = type,
                space = space
            )
        }
    }


}