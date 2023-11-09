package com.dxl.scaffold.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.IntDef
import androidx.annotation.RequiresPermission
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.dxl.scaffold.utils.MediaUtils.MediaType.Companion.ALL
import com.dxl.scaffold.utils.MediaUtils.MediaType.Companion.AUDIO
import com.dxl.scaffold.utils.MediaUtils.MediaType.Companion.IMAGE
import com.dxl.scaffold.utils.MediaUtils.MediaType.Companion.VIDEO
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import kotlin.coroutines.resume

/**
 *
 * @author duxiaolong
 * @date 2023/11/9
 */
object MediaUtils {


    @Retention(AnnotationRetention.SOURCE)
    @IntDef(ALL, IMAGE, VIDEO, AUDIO)
    annotation class MediaType {
        companion object {
            const val IMAGE = 0x01
            const val VIDEO = 0x01 shl 1
            const val AUDIO = 0x01 shl 2
            const val ALL = 0xFF
        }
    }

    /**
     *
     * @param mediaType Int eg: MediaType.ALL
     * @return String  1,2,3
     */
    private fun getMediaTypeArgs(@MediaType mediaType: Int): String {
        val types = mutableListOf<Int>()
        if (mediaType and IMAGE == IMAGE) types.add(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
        if (mediaType and VIDEO == VIDEO) types.add(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
        if (mediaType and AUDIO == AUDIO) types.add(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO)
        return types.joinToString(",")
    }

    /**
     * 媒体资源
     * @property id Long
     * @property name String
     * @property uri Uri
     * @property mimeType String
     * @property size Long
     * @property dateModified Long
     * @property bucketId Long
     * @property bucketName String
     * @constructor
     */
    data class Media(
        val id: Long,
        val name: String,
        val uri: Uri,
        val mimeType: String,
        val size: Long,
        val dateModified: Long,
        val bucketId: Long,
        val bucketName: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Media
            if (id != other.id) return false
            return true
        }

        override fun hashCode() = id.hashCode()
    }


    /**
     * 相册
     * @property id Long
     * @property displayName String
     * @property count Int
     * @constructor
     */
    data class Album(
        val id: Long,
        val displayName: String,
        var count: Int,
    ) {
        companion object {
            const val ALL = -1
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Album
            if (id != other.id) return false
            return true
        }

        override fun hashCode() = id.hashCode()
    }



    private suspend fun requestPermission(
        activity: FragmentActivity,
        permissions: List<String>
    ): Boolean {
        return suspendCancellableCoroutine {
            PermissionX.init(activity)
                .permissions(permissions)
                .request { allGranted, _, _ ->
                    it.resume(allGranted)
                }
        }
    }


    /**
     *
     * @param context Context
     * @param mediaType Int
     * @return List<Album>
     */
    private fun queryAllAlbumsSync(context: Context, @MediaType mediaType: Int): List<Album> {
        val albums = mutableListOf<Album>()
        val uri = MediaStore.Files.getContentUri("external")
        context.contentResolver.query(
            uri,
            null,
            MediaStore.Files.FileColumns.MEDIA_TYPE + " in (" + getMediaTypeArgs(mediaType) + ")",
            null,
            MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC "
        )?.use { cursor ->
            cursor.moveToFirst()
            do {
                //相册ID
                val bucketId =
                    cursor.getLongOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_ID))
                        ?: continue
                //相册名称
                val bucketName =
                    cursor.getStringOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME))
                        ?: continue
                val find = albums.find { it.id == bucketId }
                if (find == null) {
                    albums.add(Album(bucketId, bucketName, 1))
                } else {
                    find.count++
                }
            } while (cursor.moveToNext())
        }
        return albums
    }

    /**
     * 查询所有相册
     * @param activity FragmentActivity
     * @param mediaType Int
     * @return List<Album>
     */
    @SuppressLint("Range")
    fun queryAllAlbums(
        activity: FragmentActivity,
        @MediaType mediaType: Int,
        albumResult: (Result<List<Album>>) -> Unit
    ) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        activity.lifecycleScope.launch {
            val permissionGranted = requestPermission(activity, permissions)
            if (permissionGranted) {
                val albums = withContext(Dispatchers.IO) {
                    queryAllAlbumsSync(activity, mediaType)
                }
                albumResult(Result.success(albums))
            } else {
                albumResult(Result.failure(IllegalStateException("请到设置中开启相应的权限")))
            }
        }
    }

    private fun readMediasSync(
        context: Context,
        @MediaType mediaType: Int,
        bucketId: Long
    ): List<Media> {
        val medias = mutableListOf<Media>()
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.BUCKET_ID,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DISPLAY_NAME
            ),
            "( ${MediaStore.Files.FileColumns.BUCKET_ID} = $bucketId or $bucketId = ${Album.ALL} ) and ${MediaStore.Files.FileColumns.MEDIA_TYPE} in ( ${
                getMediaTypeArgs(mediaType)
            } )",
            null, MediaStore.Files.FileColumns.DATE_MODIFIED
        )?.use { cursor: Cursor ->
            cursor.moveToFirst()
            do {
                val mimeType =
                    cursor.getStringOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE))
                        ?: "*/*"
                val contentUri = if (mimeType.startsWith("image/")) {
                    //图片
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if (mimeType.startsWith("video/")) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if (mimeType.startsWith("audio/")) {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Files.getContentUri("external")
                }
                val id =
                    cursor.getLongOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
                        ?: continue
                val displayName =
                    cursor.getStringOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME))
                        ?: ""
                val dateModified =
                    cursor.getLongOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED))
                        ?: 0L
                val mediaBucketId =
                    cursor.getLongOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_ID))
                        ?: 0L
                val bucketDisplayName =
                    cursor.getStringOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME))
                        ?: ""
                val size =
                    cursor.getLongOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                        ?: 0L
                medias.add(
                    Media(
                        id,
                        displayName,
                        ContentUris.withAppendedId(contentUri, id),
                        mimeType,
                        size,
                        dateModified,
                        mediaBucketId,
                        bucketDisplayName
                    )
                )

            } while (cursor.moveToNext())
        }
        return medias
    }

    /**
     *
     * @param context Context
     * @param mediaType Int
     * @param bucketId Long 相册ID， -1代表全部
     * @return List<Media>
     */
    fun readMedias(
        activity: FragmentActivity,
        @MediaType mediaType: Int,
        bucketId: Long,
        mediaResult: (Result<List<Media>>) -> Unit
    ) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        activity.lifecycleScope.launch {
            val permissionGranted = requestPermission(activity, permissions)
            if (permissionGranted) {
                val albums = withContext(Dispatchers.IO) {
                    readMediasSync(activity, mediaType, bucketId)
                }
                mediaResult.invoke(Result.success(albums))
            } else {
                mediaResult.invoke(Result.failure(IllegalStateException("请到设置中开启相应的权限")))
            }
        }

    }

    /**
     * 从路径中获取文件名
     * @param url String
     * @return String
     */
    fun getExtensionFromUrl(url: String): String {
        return MimeTypeMap.getFileExtensionFromUrl(url)
    }

    /**
     *
     * @param url String
     * @return String
     */
    fun getMimeTypeFromUrl(url: String): String {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtensionFromUrl(url))
            ?: "*/*"
    }

    fun getSaveDirectory(mimeType: String): String {
        return when {
            //可保存的目录为DCIM/ 和 Pictures/
            mimeType.startsWith("image/") -> Environment.DIRECTORY_PICTURES
            //可保存的目录为 DCIM/、Movies/ 和 Pictures/
            mimeType.startsWith("video/") -> Environment.DIRECTORY_MOVIES
            //可保存的目录为Alarms/、Audiobooks/、Music/、Notifications/、Podcasts/ 和 Ringtones/
            mimeType.startsWith("audio/") -> Environment.DIRECTORY_MUSIC
            else -> Environment.DIRECTORY_DOWNLOADS

        }
    }

    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE], conditional = true)
    private fun InputStream.saveFileToPublicSync(
        activity: FragmentActivity,
        fileName: String,
    ): Uri {
        val mimeType = getMimeTypeFromUrl(fileName)
        val directory = getSaveDirectory(mimeType)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uri = when {
                mimeType.startsWith("image/") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                mimeType.startsWith("video/") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                mimeType.startsWith("audio/") -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
            }
            val insertUri = activity.contentResolver.insert(uri, ContentValues().apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
                put(MediaStore.Files.FileColumns.MIME_TYPE, mimeType)
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, directory)
                put(MediaStore.Files.FileColumns.IS_PENDING, 1)
                val currentTimeMillis = System.currentTimeMillis()
                put(MediaStore.Files.FileColumns.DATE_MODIFIED, currentTimeMillis)
                put(MediaStore.Files.FileColumns.DATE_ADDED, currentTimeMillis)
            }) ?: throw IllegalArgumentException("insertUri = null")

            val outputStream = activity.contentResolver.openOutputStream(insertUri)
                ?: throw IllegalArgumentException("outputStream = null")

            outputStream.use {
                val bytesCount = copyTo(it)
                if (bytesCount > 0) {
                    activity.contentResolver.update(uri, ContentValues().apply {
                        put(MediaStore.Files.FileColumns.IS_PENDING, 0)
                    }, null, null)
                } else {
                    throw IllegalArgumentException("bytesCount = 0")
                }
            }
            return insertUri
        }

        //需要写入文件的权限
        //获取下载目录
        val saveDirectory =
            Environment.getExternalStoragePublicDirectory(getSaveDirectory(mimeType))
        var targetFile = File(saveDirectory, fileName)
        var index = 0
        while (targetFile.exists()) {
            index++
            val newFileName =
                fileName.substringBeforeLast(".") + "($index)." + fileName.substringAfterLast(".")
            targetFile = File(saveDirectory, newFileName)
        }
        targetFile.outputStream().use { outputStream ->
            val length = copyTo(outputStream)
            if (length > 0) {
                MediaScannerConnection.scanFile(
                    activity,
                    arrayOf(targetFile.absolutePath),
                    null,
                    null
                )
            }
            return Uri.fromFile(targetFile)
        }

    }


    fun InputStream.saveFileToPublic(
        activity: FragmentActivity,
        fileName: String,
       saveResult: (Result<Uri>) -> Unit
    ) {
        activity.lifecycleScope.launch {
            val permissionGranted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    true else requestPermission(
                    activity,
                    listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )
            if (permissionGranted) {
                kotlin.runCatching {
                    withContext(Dispatchers.IO) {
                        saveFileToPublicSync(activity, fileName)
                    }
                }.onFailure {
                    saveResult(Result.failure(it))
                }.onSuccess {
                    saveResult(Result.success(it))
                }
            } else {
                saveResult(Result.failure(Exception("请到设置中开启对应的权限")))
            }
        }
    }

}