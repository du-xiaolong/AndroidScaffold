package com.dxl.scaffold.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_DCIM
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.DIRECTORY_MUSIC
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.core.content.FileProvider
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.dxl.scaffold.base.BaseApp
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Response
import rxhttp.toDownloadFlow
import rxhttp.wrapper.callback.UriFactory
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.param.RxHttp
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author duxiaolong
 */

/**
 * 文件保存目录（全局）
 */
fun fileDir(type: String): File {
    val dir =
        BaseApp.applicationContext.getExternalFilesDir(type)
            ?: File(BaseApp.applicationContext.filesDir, type)
    if (!dir.exists()) dir.mkdirs()
    return dir
}

/**
 * 缓存目录（全局）
 */
fun cacheDir(type: String): File {
    val cacheDir =
        BaseApp.applicationContext.externalCacheDir ?: BaseApp.applicationContext.cacheDir
    val dir = File(cacheDir, type)
    if (!dir.exists()) dir.mkdirs()
    return dir
}

//文件扩展名
val File.extension: String
    get() = MimeTypeMap.getFileExtensionFromUrl(absolutePath)

//获取文件的mineType
val File.mimeType: String
    get() = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"

//保存文件到公共目录
fun File.saveToPublic() {
    inputStream().saveToPublic(name)
}


/**
 * 保存文件到公共目录
 * 注意：Android Q以下的版本需要申请写入文件权限，否则无法保存成功，Android Q及以上版本无需申请
 */
fun InputStream.saveToPublic(fileName: String) {
    val realMimeType = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(fileName.substringAfterLast(".")) ?: "*/*"
    val directory = when {
        fileName.isVideo -> Environment.DIRECTORY_MOVIES
        fileName.isAudio -> DIRECTORY_MUSIC
        fileName.isImage -> Environment.DIRECTORY_PICTURES
        else -> DIRECTORY_DOWNLOADS
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            val current = System.currentTimeMillis()
            put(MediaStore.Images.Media.DATE_ADDED, current)
            put(MediaStore.Images.Media.DATE_MODIFIED, current)
            put(MediaStore.Images.Media.MIME_TYPE, realMimeType)
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.RELATIVE_PATH, directory)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = when {
            fileName.isVideo -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            fileName.isAudio -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            fileName.isImage -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
        }
        val contentResolver = BaseApp.applicationContext.contentResolver
        val insert =
            contentResolver.insert(uri, contentValues)
                .requireNotNull { "插入失败，insertUri = null" }
        contentResolver.openOutputStream(insert).use { outputStream ->
            val length = copyTo(outputStream.requireNotNull { "保存失败！outputStream = null" })
            require(length > 0) { "保存失败！length = 0" }
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            contentResolver.update(insert, contentValues, null, null)
        }
    } else {
        val publicDirectory =
            Environment.getExternalStoragePublicDirectory(directory)
        var targetFile = File(publicDirectory, fileName)
        var currentIndex = 0
        //如果已经有这个文件名了，就在文件名后边添加（1），依次类推
        while (targetFile.exists()) {
            currentIndex++
            targetFile =
                File(
                    publicDirectory,
                    "${fileName.substringBeforeLast(".")}($currentIndex).${
                        fileName.substringAfterLast(".")
                    }"
                )
        }

        targetFile.outputStream().use {
            val length = copyTo(it)
            require(length > 0) { "保存失败！length = 0" }
            MediaScannerConnection.scanFile(
                BaseApp.applicationContext,
                arrayOf(targetFile.absolutePath),
                null,
                null
            )
        }
    }

}

/**
 * 根据URI获取真实路径
 */
fun getPathByUri(context: Context, uri: Uri?): String? {
    uri ?: return null
    // 以 file:// 开头的使用第三方应用打开 (open with third-party applications starting with file://)
    if (ContentResolver.SCHEME_FILE.equals(uri.scheme, ignoreCase = true))
        return getDataColumn(context, uri)

    // DocumentProvider
    if (DocumentsContract.isDocumentUri(context, uri)) {
        // LocalStorageProvider
        if (isLocalStorageDocument(uri)) {
            // The path is the id
            return DocumentsContract.getDocumentId(uri);
        }
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        .toString() + File.separator + split[1]
                } else {
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + split[1]
                }
            } else if ("home".equals(type, ignoreCase = true)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        .toString() + File.separator + "documents" + File.separator + split[1]
                } else {
                    @Suppress("DEPRECATION")
                    return Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "documents" + File.separator + split[1]
                }
            } else {
                @Suppress("DEPRECATION")
                val sdcardPath =
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "documents" + File.separator + split[1]
                return if (sdcardPath.startsWith("file://")) {
                    sdcardPath.replace("file://", "")
                } else {
                    sdcardPath
                }
            }
        }
        // DownloadsProvider
        else if (isDownloadsDocument(uri)) {
            val id = DocumentsContract.getDocumentId(uri)
            if (id != null && id.startsWith("raw:")) {
                return id.substring(4)
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                val contentUriPrefixesToTry = arrayOf(
                    "content://downloads/public_downloads",
                    "content://downloads/my_downloads",
                    "content://downloads/all_downloads"
                )
                for (contentUriPrefix in contentUriPrefixesToTry) {
                    val contentUri =
                        ContentUris.withAppendedId(Uri.parse(contentUriPrefix), id.toLong())
                    try {
                        val path = getDataColumn(context, contentUri)
                        if (!path.isNullOrBlank()) return path
                    } catch (e: Exception) {
                        Log.e("fileKt", e.toString())
                    }
                }
            } else {
                //testPath(uri)
                return getDataColumn(context, uri)
            }
        }
        // MediaProvider
        else if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val contentUri: Uri? = when (split[0]) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                "download" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI
                } else null

                else -> null
            }
            val selectionArgs = arrayOf(split[1])
            return getDataColumn(context, contentUri, "_id=?", selectionArgs)
        }

        //GoogleDriveProvider
        else if (isGoogleDriveUri(uri)) {
            return getGoogleDriveFilePath(uri, context)
        } else {
            //others
        }
    }
    // MediaStore (and general)
    else if ("content".equals(uri.scheme, ignoreCase = true)) {
        // Return the remote address
        if (isGooglePhotosUri(uri)) {
            return uri.lastPathSegment
        }
        // Google drive legacy provider
        else if (isGoogleDriveUri(uri)) {
            return getGoogleDriveFilePath(uri, context)
        }
        // Huawei
        else if (isHuaWeiUri(uri)) {
            val uriPath = getDataColumn(context, uri) ?: uri.toString()
            //content://com.huawei.hidisk.fileprovider/root/storage/emulated/0/Android/data/com.xxx.xxx/
            if (uriPath.startsWith("/root")) {
                return uriPath.replace("/root".toRegex(), "")
            }
        } else {
            //others
        }
        return getDataColumn(context, uri)
    } else {
        //others
    }
    return getDataColumn(context, uri)
}

/**
 * 文件大小格式化显示，比如3.5MB
 */
val File?.formatSize: String
    get() = (this?.length() ?: 0L).formatSize

/**
 * 文件大小格式化显示，比如3.5MB
 */
val Long.formatSize: String
    get() = ConvertUtils.byte2FitMemorySize(this, 1)

/**
 * 文件是否为视频
 */
val File?.isVideo: Boolean
    get() = this?.name.isVideo

/**
 * 文件是否为图片
 */
val File?.isImage: Boolean
    get() = this?.name.isImage

/**
 * 文件是否为音频
 */
val File?.isAudio: Boolean
    get() = this?.name.isAudio

/**
 * 根据文件名、路径、url判断是否是声音
 */
val String?.isAudio: Boolean
    get() {
        if (isNullOrBlank()) return false
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(substringAfterLast("."))
            ?.contains("audio") == true
    }

/**
 * 根据文件名、路径、url判断是否是图片
 */
val String?.isImage: Boolean
    get() {
        if (isNullOrBlank()) return false
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(substringAfterLast("."))
            ?.contains("image") == true
    }

/**
 * 根据文件名、路径、url判断是否是视频
 */
val String?.isVideo: Boolean
    get() {
        if (isNullOrBlank()) return false
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(substringAfterLast("."))
            ?.contains("video") == true
    }

/**
 * 保存uri到指定的目录
 */
@SuppressLint("Range")
fun copyUriToDirectory(context: Context, uri: Uri?, descDir: File?): Boolean {
    uri ?: return false
    descDir ?: return false
    if (descDir.exists()) {
        descDir.mkdirs()
    }
    var file = File(uri.toString())
    if (file.exists()) {
        return FileUtils.move(file, File(descDir, file.name))
    }
    if (uri.scheme == ContentResolver.SCHEME_FILE) {
        file = uri.path?.let { File(it) } ?: return false
        return FileUtils.move(file, File(descDir, file.name))
    }
    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        return runCatching {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            var result = false
            if (cursor != null && cursor.moveToFirst()) {
                val displayName =
                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                result = FileIOUtils.writeFileFromIS(
                    File(descDir, displayName),
                    contentResolver.openInputStream(uri)
                )
                cursor.close()
            }
            result
        }.getOrDefault(false)
    }
    return false

}


/**
 * 根据uri获取文件名，如果获取失败返回 ""
 */
val Uri?.fileName: String
    get() {
        this ?: return ""
        return BaseApp.applicationContext.contentResolver.query(
            this,
            arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor?.moveToFirst() == true)
                cursor.getStringOrNull(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)) else null
        } ?: ""
    }

/**
 * 根据uri获取文件大小，失败返回0
 */
val Uri?.fileSize: Long
    get() {
        this ?: return 0L
        return BaseApp.applicationContext.contentResolver.query(
            this,
            arrayOf(MediaStore.MediaColumns.SIZE),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor?.moveToFirst() == true)
                cursor.getLongOrNull(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)) else null
        } ?: 0L
    }


/**
 * 保存bitmap
 *
 * @param file
 */
fun Bitmap.saveToFile(file: File) {
    kotlin.runCatching {
        val bos = BufferedOutputStream(FileOutputStream(file))
        compress(Bitmap.CompressFormat.JPEG, 100, bos)
        bos.flush()
        bos.close()
    }
}

/**
 * 对图片进行压缩
 */
suspend fun File.compressImage(): File {
    if (this.isImage.not()) throw IllegalArgumentException("不是图片文件")
    return suspendCancellableCoroutine<File> {
        Luban.with(BaseApp.applicationContext).load(this).ignoreBy(100)
            .setCompressListener(object : OnNewCompressListener {
                override fun onStart() {
                    //开始
                }

                override fun onSuccess(source: String?, compressFile: File?) {
                    if (compressFile?.exists() == true) {
                        it.resume(compressFile)
                    } else {
                        it.resumeWithException(IllegalStateException("图片压缩失败！"))
                    }
                }

                override fun onError(source: String?, e: Throwable?) {
                    it.resumeWithException(IllegalStateException("图片压缩失败！\n${e.format()}"))
                }

            }).launch()
    }
}

/**
 * 对图片进行压缩
 */
suspend fun Uri.compressImage(): Uri {
    if (fileName.isImage.not()) return this
    return suspendCancellableCoroutine {
        Luban.with(BaseApp.applicationContext).load(this).ignoreBy(0)
            .setCompressListener(object : OnNewCompressListener {
                override fun onStart() {
                    //onStart
                }

                override fun onSuccess(source: String?, compressFile: File?) {
                    if (compressFile?.exists() == true) {
                        it.resume(Uri.fromFile(compressFile))
                    } else {
                        it.resumeWithException(IllegalStateException("图片压缩失败！"))
                    }
                }

                override fun onError(source: String?, e: Throwable?) {
                    it.resumeWithException(IllegalStateException("图片压缩失败！\n${e.format()}"))
                }

            }).launch()
    }
}

fun LifecycleOwner.downloadFile(
    context: Context,
    url: String?,
    descPath: File? = fileDir(DIRECTORY_DOWNLOADS),
    fileName: String? = null,
    isOpen: Boolean = false,
    showLoading: ((isShow: Boolean, message: String?) -> Unit)? = null
) {
    if (url.isNullOrEmpty()) {
        "下载失败,链接不存在".toast()
        return
    }
    if (descPath == null) {
        "下载失败,目标路径不存在".toast()
        return
    }
    if (!descPath.exists()) {
        descPath.mkdirs()
    }
    val name = if (fileName.isNullOrBlank()) url.substringAfterLast("/") else fileName.trim()
    val descFile = File(descPath, name)
    lifecycleScope.launch {
        RxHttp.get(url)
            .readTimeout(30 * 60 * 1000L)
            .writeTimeout(30 * 60 * 1000L)
            .connectTimeout(5 * 60 * 1000L)
            .toDownloadFlow(descFile.absolutePath)
            .onProgress {
                showLoading?.invoke(true, "正在下载(${it.progress}%)")
            }
            .catch {
                llloge(it)
                //下载失败。提示重新上传
                showLoading?.invoke(false, null)
                "下载失败：${it.format()}".toast()
            }.collect {
                //下载成功
                showLoading?.invoke(false, null)
                if (isOpen) {
                    context.openFile(descFile)
                } else {
                    "下载成功".toast()
                }
            }
    }
}

/**
 * 调用外部应用打开文件
 */
fun Context.openFile(file: File) {
    val intent = Intent().apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        action = Intent.ACTION_VIEW
        setDataAndType(
            FileProvider.getUriForFile(
                this@openFile,
                "$packageName.fileProvider",
                file
            ), file.extension.getMineTypeFromExtension()
        )
    }
    kotlin.runCatching {
        startActivity(intent)
    }.onFailure {
        "未找到对应的文件打开方式".toast()
    }
}

fun String?.getMineTypeFromExtension() = MimeTypeMap.getSingleton().getMimeTypeFromExtension(this)


/******************************************************* private **************************************************/


/**
 * Android Q以下版本，查询媒体库中当前路径是否存在
 * @return Uri 返回null时说明不存在，可以进行图片插入逻辑
 */
private fun ContentResolver.queryMediaImage28(imagePath: String): Uri? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return null
    val imageFile = File(imagePath)
    if (imageFile.canRead() && imageFile.exists()) {
        // 文件已存在，返回一个file://xxx的uri
        return Uri.fromFile(imageFile)
    }
    // 保存的位置
    val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    // 查询是否已经存在相同图片
    val query = this.query(
        collection,
        arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA),
        "${MediaStore.Images.Media.DATA} == ?",
        arrayOf(imagePath), null
    )
    query?.use {
        if (it.moveToNext()) {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val id = it.getLong(idColumn)
            return ContentUris.withAppendedId(collection, id)
        }
    }
    return null
}


/**
 * BUG : 部分机型进入"文件管理器" 执行到  cursor.getColumnIndexOrThrow(column);出现
 *       Caused by: java.lang.IllegalArgumentException: column '_data' does not exist. Available columns: []
 *
 * Fixed :
 *      https://stackoverflow.com/questions/42508383/illegalargumentexception-column-data-does-not-exist
 *
 */
private fun getDataColumn(
    context: Context,
    uri: Uri?,
    selection: String? = null,
    selectionArgs: Array<String>? = null
): String? {
    @Suppress("DEPRECATION")
    val column = MediaStore.Files.FileColumns.DATA
    val projection = arrayOf(column)
    try {
        context.contentResolver.query(
            uri ?: return null,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { c: Cursor ->
            if (c.moveToFirst()) {
                val columnIndex = c.getColumnIndex(column)
                return c.getString(columnIndex)
            }
        }
    } catch (e: Throwable) {
        Log.e("error", "getDataColumn -> ${e.message}")
    }
    return null
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is local.
 */
private fun isLocalStorageDocument(uri: Uri?): Boolean {
    return ".andoFileProvider".equals(uri?.authority, true)
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
private fun isExternalStorageDocument(uri: Uri?): Boolean {
    return "com.android.externalstorage.documents".equals(uri?.authority, true)
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
private fun isDownloadsDocument(uri: Uri?): Boolean {
    return "com.android.providers.downloads.documents".equals(uri?.authority, true)
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
private fun isMediaDocument(uri: Uri?): Boolean {
    return "com.android.providers.media.documents".equals(uri?.authority, true)
}

private fun isGoogleDriveUri(uri: Uri?): Boolean {
    return "com.google.android.apps.docs.storage.legacy" == uri?.authority || "com.google.android.apps.docs.storage" == uri?.authority
}

private fun getGoogleDriveFilePath(uri: Uri, context: Context): String? {
    context.contentResolver.query(uri, null, null, null, null)?.use { c: Cursor ->
        /*
         Get the column indexes of the data in the Cursor,
         move to the first row in the Cursor, get the data, and display it.
         */
        val nameIndex: Int = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        //val sizeIndex: Int = c.getColumnIndex(OpenableColumns.SIZE)
        if (!c.moveToFirst()) {
            return uri.toString()
        }
        val name: String = c.getString(nameIndex)
        //val size = c.getLong(sizeIndex).toString()
        val file = File(context.cacheDir, name)

        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream?.available() ?: 0
            val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers)?.also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
        return file.path
    }
    return uri.toString()
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
private fun isGooglePhotosUri(uri: Uri?): Boolean {
    return "com.google.android.apps.photos.content".equals(uri?.authority, true)
}

/**
 * content://com.huawei.hidisk.fileprovider/root/storage/emulated/0/Android/data/com.xxx.xxx/
 *
 * @param uri
 * @return
 */
private fun isHuaWeiUri(uri: Uri?): Boolean {
    return "com.huawei.hidisk.fileprovider".equals(uri?.authority, true)
}


/**
 *
 * @receiver FragmentActivity
 * @param url String
 * @param fileName String?  文件名，如果为空，先去下载请求头里的文件名，如果没有的话，取链接的文件名
 * @param publicDirectory String?  公共目录，比如 Environment.DIRECTORY_DOWNLOADS + "/子目录"
 * @param onProgress Function1<[@kotlin.ParameterName] Progress, Unit>?
 * @param onResult Function1<Result<String>, Unit>
 */
fun FragmentActivity.downloadToPublic(
    url: String,
    fileName: String? = null,
    publicDirectory: String? = null,
    onProgress: ((progress: Progress) -> Unit)? = null,
    onResult: (Result<String>) -> Unit
) {
    fun getFileName(response: Response, url: String, fileName: String?): String {
        if (!fileName.isNullOrEmpty()) return fileName
        val responseFileName = response.header("Content-Disposition")
            ?.split(";")
            ?.firstOrNull { it.trim().startsWith("filename=") }
            ?.substringAfter("filename=")
            ?.replace("\"", "")
        if (!responseFileName.isNullOrEmpty()) return responseFileName
        // 如果请求不到，使用链接文件名
        return URLUtil.guessFileName(url, null, null) ?: "file"
    }

    val osFactory = object : UriFactory(this) {
        override fun insert(response: Response): Uri {
            val downloadFileName = getFileName(response, url, fileName)
            var mimeType = response.body?.contentType()?.toString()
            if (mimeType.isNullOrEmpty()) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(downloadFileName)
                ) ?: "*/*"
            }
            val directory = if (publicDirectory.isNullOrEmpty()) {
                if (mimeType.contains("image"))
                    DIRECTORY_DCIM
                else if (mimeType.contains("video"))
                    DIRECTORY_DCIM
                else if (mimeType.contains("audio"))
                    DIRECTORY_MUSIC
                else DIRECTORY_DOWNLOADS

            } else publicDirectory
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues().run {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, downloadFileName) //文件名
                    //取contentType响应头作为文件类型

                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, directory)

                    val uri: Uri = if (mimeType.contains("image")) {
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if (mimeType.contains("video")) {
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if (mimeType.contains("audio")) {
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    } else {
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI
                    }
                    context.contentResolver.insert(uri, this)
                }
                    ?: throw NullPointerException("Uri insert fail, Please change the file name")
            } else {
                val dir =
                    Environment.getExternalStoragePublicDirectory(directory)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val file = File(
                    dir,
                    downloadFileName
                )
                Uri.fromFile(file)
            }
        }
    }

    fun privateDownload() {
        lifecycleScope.launch {
            RxHttp.get(url)
                .toDownloadFlow(osFactory)
                .onProgress {
                    onProgress?.invoke(it)
                }.catch {
                    onResult.invoke(Result.failure(it))
                }.collect {
                    onResult.invoke(Result.success(it.toString()))
                }
        }

    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        PermissionX.init(this).permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    privateDownload()
                } else {
                    onResult.invoke(Result.failure(IllegalStateException("请到设置中开启存储权限")))
                }
            }
    } else {
        privateDownload()
    }
}


/**
 * 获取下载链接的真实文件名
 */
suspend fun requestDownloadFileName(downloadUrl: String): String? = withContext(Dispatchers.IO) {
    kotlin.runCatching {
        val url = URL(downloadUrl)
        val httpURLConnection = url.openConnection() as HttpURLConnection
        httpURLConnection.addRequestProperty(
            "Cookie",
            CookieManager.getInstance().getCookie(downloadUrl)
        )
        httpURLConnection.connect()
        val headerField = httpURLConnection.getHeaderField("Content-Disposition")
        val fileName = headerField.split(";").firstOrNull { it.trim().startsWith("filename=") }
            ?.substringAfter("filename=")?.replace("\"", "")
        httpURLConnection.disconnect()
        URLDecoder.decode(fileName, "UTF-8")
    }.getOrNull()
}



