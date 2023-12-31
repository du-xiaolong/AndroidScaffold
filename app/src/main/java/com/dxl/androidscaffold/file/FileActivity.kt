package com.dxl.androidscaffold.file

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.database.Cursor
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import com.dxl.androidscaffold.R
import com.dxl.androidscaffold.databinding.ActivityFileBinding
import com.dxl.scaffold.base.BaseViewModel
import com.dxl.scaffold.base.BaseVmActivity
import com.dxl.scaffold.utils.downloadFile
import com.dxl.scaffold.utils.downloadToPublic
import com.dxl.scaffold.utils.lllog
import com.dxl.scaffold.utils.startActivity
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
        vb.btnSaveFile.setOnClickListener(this)
        vb.btnReadImage.setOnClickListener(this)
        vb.btnReadFile.setOnClickListener(this)

        vb.btnDownloadFile.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_save_image -> saveImageToPublic()
            R.id.btn_save_video -> saveVideoToPublic()
            R.id.btn_save_audio -> saveAudioToPublic()
            R.id.btn_save_file -> saveFileToPublic()
            R.id.btn_read_image -> readImageFromPublic()
            R.id.btn_read_file -> readFileFromPublic()
            R.id.btn_download_file -> downloadFile()
        }
    }

    private fun downloadFile() {
        downloadToPublic("https://k.sinaimg.cn/n/sinakd20231207s/261/w640h421/20231207/b62f-df22051593426b8ff7e06d926feec36c.png/w700d1q75cms.jpg", publicDirectory = "${Environment.DIRECTORY_DCIM}/鲁南") {
            vb.tvMediaInfo.text = "下载成功：${it.getOrNull()}"
        }
    }

    @SuppressLint("Range")
    private fun readFileFromPublic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, null, null, null, null)
                ?.use { cursor: Cursor ->
                    if (cursor.moveToFirst()) {
                        do {
                            val displayName =
                                cursor.getString(cursor.getColumnIndex(MediaStore.DownloadColumns.DISPLAY_NAME))
                            lllog(displayName, "查询")
                        } while (cursor.moveToNext())
                    } else {
                        lllog("无数据", "查询")
                    }
                }
        } else {

        }
    }


    private fun readImageFromPublic() {
        startActivity<ReadMediaActivity>()
    }

    private fun saveFileToPublic() {
        //保存文件到公共目录
        val assetFileName = "file.txt"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val insertUri = contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    val currentTimeMillis = System.currentTimeMillis()
                    put(MediaStore.DownloadColumns.DATE_ADDED, currentTimeMillis)
                    put(MediaStore.DownloadColumns.DATE_TAKEN, currentTimeMillis)
                    put(
                        MediaStore.DownloadColumns.MIME_TYPE,
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")
                    )
                    put(MediaStore.DownloadColumns.DISPLAY_NAME, assetFileName)
//                    put(MediaStore.DownloadColumns.RELATIVE_PATH, category)
                    put(MediaStore.DownloadColumns.IS_PENDING, 1)
                }) ?: return
            contentResolver.openOutputStream(insertUri)?.use { outputStream ->
                val copyResult = assets.open(assetFileName).copyTo(outputStream)
                if (copyResult > 0) {
                    contentResolver.update(insertUri, ContentValues().apply {
                        put(MediaStore.DownloadColumns.IS_PENDING, 0)
                    }, null, null)
                    vb.tvMediaInfo.text = "保存文件成功"
                }
            }
        } else {
            //需要写入文件的权限
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                //获取下载目录
                val directory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                var targetFile = File(directory, assetFileName)
                var index = 0
                while (targetFile.exists()) {
                    index++
                    targetFile = File(directory, "file($index).txt")
                }
                targetFile.outputStream().use { outputStream ->
                    val length = assets.open(assetFileName).copyTo(outputStream)
                    if (length > 0) {
                        MediaScannerConnection.scanFile(
                            this,
                            arrayOf(targetFile.absolutePath),
                            null,
                            null
                        )
                        vb.tvMediaInfo.text = "保存文件downloads成功"
                    }
                }
            }
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
                } else {
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

