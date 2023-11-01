package com.dxl.androidscaffold.file

import android.os.Bundle
import com.dxl.androidscaffold.databinding.ActivityFileBinding
import com.dxl.scaffold.base.BaseViewModel
import com.dxl.scaffold.base.BaseVmActivity
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader

/**
 *
 * @author duxiaolong
 * @date 2023/11/1
 */
class FileActivity : BaseVmActivity<BaseViewModel, ActivityFileBinding>() {

    override fun init(savedInstanceState: Bundle?) {
        //写入文件到内部目录
        vb.btnSaveToFileDir.setOnClickListener {
            FileOutputStream(File(filesDir, "test.txt")).use {
                it.write("hello world".toByteArray())
            }
            vb.tvInfo.text = "写入完成"

        }
        //从内部目录读取文件
        vb.btnReadFileDir.setOnClickListener {
            val file = File(filesDir, "test.txt")
            if (!file.exists()) {
                vb.tvInfo.text = "文件不存在"
            } else {
                FileReader(file).use {
                    vb.tvInfo.text = it.readText()
                }
            }
        }

        //复制文件到缓存目录
        val source = File(filesDir, "test.txt")
        val dest = File(cacheDir, "test.txt")
        vb.btnCopyToCache.setOnClickListener {
            if (!source.exists()) {
                vb.tvInfo.text = "源文件不存在"
            } else {
                source.copyTo(dest)
                vb.tvInfo.text = "复制完成"
            }
        }
    }


}