package com.dxl.androidscaffold.file

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull

/**
 *
 * @author duxiaolong
 * @date 2023/11/6
 */
object MediaUtils {


    object MediaType {
        const val IMAGE = 0x01
        const val VIDEO = 0x01 shl 1
        const val AUDIO = 0x01 shl 2
        const val ALL = 0xFF

        /**
         *
         * @param mediaType Int eg: MediaType.ALL
         * @return String  1,2,3
         */
        fun getMediaTypeArgs(mediaType: Int): String {
            val types = mutableListOf<Int>()
            if (mediaType and MediaType.IMAGE == MediaType.IMAGE) types.add(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
            if (mediaType and MediaType.VIDEO == MediaType.VIDEO) types.add(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
            if (mediaType and MediaType.AUDIO == MediaType.AUDIO) types.add(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO)
            return types.joinToString(",")
        }
    }

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

        override fun hashCode(): Int {
            return id.hashCode()
        }
    }


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

        override fun hashCode(): Int {
            return id.hashCode()
        }


    }

    /**
     * 查询所有相册
     * @return List<Album>
     */
    @SuppressLint("Range")
    fun queryAllAlbums(context: Context, mediaType: Int): List<Album> {
        val albums = mutableListOf<Album>()
        val uri = MediaStore.Files.getContentUri("external")
        val types = mutableListOf<Int>()
        if (mediaType and MediaType.IMAGE == MediaType.IMAGE) types.add(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
        if (mediaType and MediaType.VIDEO == MediaType.VIDEO) types.add(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
        if (mediaType and MediaType.AUDIO == MediaType.AUDIO) types.add(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO)

        context.contentResolver.query(
            uri,
            null,
            MediaStore.Files.FileColumns.MEDIA_TYPE + " in (" + types.joinToString(",") + ")",
            null, MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC "
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
            return albums
        }
        return albums
    }

    fun readMedias(context: Context, mediaType: Int, bucketId: Long): List<Media> {
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
                MediaType.getMediaTypeArgs(
                    mediaType
                )
            } )",
            null, MediaStore.Files.FileColumns.DATE_MODIFIED
        )?.use { cursor: Cursor ->
            cursor.moveToFirst()
            do {
                val mimeType =
                    cursor.getStringOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)) ?: "*/*"
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
                    cursor.getStringOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME))?:""
                val dateModified =
                    cursor.getLongOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)) ?:0L
                val mediaBucketId = cursor.getLongOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_ID))?:0L
                val bucketDisplayName =
                    cursor.getStringOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME))?:""
                val size = cursor.getLongOrNull(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)) ?: 0L
                medias.add(
                    Media(id, displayName,  ContentUris.withAppendedId(
                        contentUri, id
                    ), mimeType, size, dateModified, mediaBucketId, bucketDisplayName)
                )

            } while (cursor.moveToNext())
        }
        return medias
    }

}