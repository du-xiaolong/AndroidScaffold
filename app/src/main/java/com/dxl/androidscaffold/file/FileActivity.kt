package com.dxl.androidscaffold.file

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import com.dxl.androidscaffold.R
import com.dxl.androidscaffold.databinding.ActivityFileBinding
import com.dxl.scaffold.base.BaseViewModel
import com.dxl.scaffold.base.BaseVmActivity
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.RequestCallback
import java.io.File

/**
 *
 * @author duxiaolong
 * @date 2023/11/1
 */
class FileActivity : BaseVmActivity<BaseViewModel, ActivityFileBinding>(), View.OnClickListener {

    override fun init(savedInstanceState: Bundle?) {
        //写入文件到内部目录
        vb.btnSaveToFileDir.setOnClickListener {
            File(filesDir, "test.txt").outputStream().use {
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
                file.inputStream().reader().use {
                    vb.tvInfo.text = it.readText()
                }
            }
        }


        vb.btnCopyToCache.setOnClickListener {
            //复制文件到缓存目录
            val source = File(filesDir, "test.txt")
            val dest = File(cacheDir, "test.txt")
            if (!source.exists()) {
                vb.tvInfo.text = "源文件不存在"
            } else {
                if (dest.exists()) {
                    dest.delete()
                }
                source.copyTo(dest)
                vb.tvInfo.text = "复制完成"
            }
        }

        vb.btnReadExternal.setOnClickListener {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.txt")
            file.outputStream().use {
                it.write("Hello world".toByteArray())
                vb.tvExternalInfo.text = "写入成功"
            }
        }

        vb.btnCheckExternal.setOnClickListener {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                vb.tvExternalInfo.text = "外部存储可用"
            } else {
                vb.tvExternalInfo.text = "外部存储不可用"
            }
        }

        vb.btnSaveImage.setOnClickListener(this)
        vb.btnSaveVideo.setOnClickListener(this)
        vb.btnSaveAudio.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_save_image -> saveImageToPublic()
            R.id.btn_save_video -> saveVideoToPublic()
            R.id.btn_save_audio -> saveAudioToPublic()
        }
    }

    private fun saveAudioToPublic() {
        val categories = arrayOf(
            Environment.DIRECTORY_ALARMS,
            Environment.DIRECTORY_MUSIC,
            Environment.DIRECTORY_RINGTONES,
            Environment.DIRECTORY_NOTIFICATIONS,
            Environment.DIRECTORY_PODCASTS,
        )
        AlertDialog.Builder(this)
            .setItems(
                categories
            ) { _, which ->
                val category = categories[which]
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val insertUri = contentResolver.insert(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        ContentValues().apply {
                            val currentTimeMillis = System.currentTimeMillis()
                            put(MediaStore.Audio.AudioColumns.DATE_ADDED, currentTimeMillis)
                            put(MediaStore.Audio.AudioColumns.DATE_TAKEN, currentTimeMillis)
                            put(MediaStore.Audio.AudioColumns.MIME_TYPE, "audio/mp3")
                            put(MediaStore.Audio.AudioColumns.DISPLAY_NAME, "audio.mp3")
                            put(MediaStore.Audio.AudioColumns.RELATIVE_PATH, category)
                            put(MediaStore.Audio.AudioColumns.IS_PENDING, 1)
                        }) ?: return@setItems
                    contentResolver.openOutputStream(insertUri)?.use { outputStream ->
                        val copyResult = assets.open("audio.mp3").copyTo(outputStream)
                        if (copyResult > 0) {
                            contentResolver.update(insertUri, ContentValues().apply {
                                put(MediaStore.Audio.AudioColumns.IS_PENDING, 0)
                            }, null, null)
                            vb.tvMediaInfo.text = "保存音频到${category}成功"
                        }
                    }
                } else {
                    //需要写入文件的权限
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        val directory = Environment.getExternalStoragePublicDirectory(category)
                        var targetFile = File(directory, "audio.mp3")
                        var index = 0
                        while (targetFile.exists()) {
                            index++
                            targetFile = File(directory, "audio($index).mp3")
                        }
                        targetFile.outputStream().use { outputStream ->
                            val length = assets.open("audio.mp3").copyTo(outputStream)
                            if (length > 0) {
                                MediaScannerConnection.scanFile(
                                    this,
                                    arrayOf(targetFile.absolutePath),
                                    null,
                                    null
                                )
                                vb.tvMediaInfo.text = "保存音频到${category}成功"
                            }
                        }
                    }
                }


            }.show()
    }

    private fun saveImageToPublic() {
        val categories = arrayOf(Environment.DIRECTORY_DCIM, Environment.DIRECTORY_PICTURES)
        AlertDialog.Builder(this)
            .setItems(
                categories
            ) { _, which ->
                val category = categories[which]
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val insertUri = contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        ContentValues().apply {
                            val currentTimeMillis = System.currentTimeMillis()
                            put(MediaStore.Images.ImageColumns.DATE_ADDED, currentTimeMillis)
                            put(MediaStore.Images.ImageColumns.DATE_TAKEN, currentTimeMillis)
                            put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpg")
                            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, "image.jpg")
                            put(MediaStore.Images.ImageColumns.RELATIVE_PATH, category)
                            put(MediaStore.Images.ImageColumns.IS_PENDING, 1)
                        }) ?: return@setItems
                    contentResolver.openOutputStream(insertUri)?.use { outputStream ->
                        val length = assets.open("image.jpg").copyTo(outputStream)
                        if (length > 0) {
                            contentResolver.update(insertUri, ContentValues().apply {
                                put(MediaStore.Images.ImageColumns.IS_PENDING, 0)
                            }, null, null)
                            vb.tvMediaInfo.text = "保存图片到${category}成功"
                        }
                    }
                } else {
                    //需要写入文件的权限
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        val directory = Environment.getExternalStoragePublicDirectory(category)
                        var targetFile = File(directory, "image.jpg")
                        var index = 0
                        while (targetFile.exists()) {
                            index++
                            targetFile = File(directory, "image($index).jpg")
                        }
                        targetFile.outputStream().use { outputStream ->
                            val length = assets.open("image.jpg").copyTo(outputStream)
                            if (length > 0) {
                                MediaScannerConnection.scanFile(
                                    this,
                                    arrayOf(targetFile.absolutePath),
                                    null,
                                    null
                                )
                                vb.tvMediaInfo.text = "保存图片到${category}成功"
                            }
                        }
                    }

                }

            }.show()
    }

    private fun requestPermission(vararg permissions: String, onGranted: () -> Unit) {
        PermissionX.init(this)
            .permissions(*permissions)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    onGranted.invoke()
                }
            }
    }


    private fun saveVideoToPublic() {
        val categories = arrayOf(
            Environment.DIRECTORY_DCIM,
            Environment.DIRECTORY_PICTURES,
            Environment.DIRECTORY_MOVIES
        )
        AlertDialog.Builder(this)
            .setItems(
                categories
            ) { _, which ->
                val category = categories[which]
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val insertUri = contentResolver.insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        ContentValues().apply {
                            val currentTimeMillis = System.currentTimeMillis()
                            put(MediaStore.Video.VideoColumns.DATE_ADDED, currentTimeMillis)
                            put(MediaStore.Video.VideoColumns.DATE_TAKEN, currentTimeMillis)
                            put(MediaStore.Video.VideoColumns.MIME_TYPE, "video/mp4")
                            put(MediaStore.Video.VideoColumns.DISPLAY_NAME, "video.mp4")
                            put(MediaStore.Video.VideoColumns.RELATIVE_PATH, category)
                            put(MediaStore.Video.VideoColumns.IS_PENDING, 1)
                        }) ?: return@setItems
                    contentResolver.openOutputStream(insertUri)?.use { outputStream ->
                        val copyResult = assets.open("video.mp4").copyTo(outputStream)
                        if (copyResult > 0) {
                            contentResolver.update(insertUri, ContentValues().apply {
                                put(MediaStore.Video.VideoColumns.IS_PENDING, 0)
                            }, null, null)

                            vb.tvMediaInfo.text = "保存视频到${category}成功"
                        }
                    }
                }else{
                    //需要写入文件的权限
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        val directory = Environment.getExternalStoragePublicDirectory(category)
                        var targetFile = File(directory, "video.mp4")
                        var index = 0
                        while (targetFile.exists()) {
                            index++
                            targetFile = File(directory, "video($index).jpg")
                        }
                        targetFile.outputStream().use { outputStream ->
                            val length = assets.open("video.mp4").copyTo(outputStream)
                            if (length > 0) {
                                MediaScannerConnection.scanFile(
                                    this,
                                    arrayOf(targetFile.absolutePath),
                                    null,
                                    null
                                )
                                vb.tvMediaInfo.text = "保存视频到${category}成功"
                            }
                        }
                    }
                }

            }.show()

    }


}

