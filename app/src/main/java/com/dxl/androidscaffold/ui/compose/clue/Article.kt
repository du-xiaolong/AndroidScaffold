package com.dxl.androidscaffold.ui.compose.clue


import android.os.Build
import android.text.Html
import com.dxl.androidscaffold.R
import com.google.gson.annotations.SerializedName
import kotlin.math.abs

data class Article(
    @SerializedName("adminAdd")
    val adminAdd: Boolean, // false
    @SerializedName("apkLink")
    val apkLink: String?,
    @SerializedName("audit")
    val audit: Int, // 1
    @SerializedName("author")
    val author: String?, // 鸿洋
    @SerializedName("canEdit")
    val canEdit: Boolean, // false
    @SerializedName("chapterId")
    val chapterId: Int, // 644
    @SerializedName("chapterName")
    val chapterName: String, // 快手
    @SerializedName("collect")
    var collect: Boolean, // false
    @SerializedName("courseId")
    val courseId: Int, // 13
    @SerializedName("desc")
    val desc: String?,
    @SerializedName("descMd")
    val descMd: String?,
    @SerializedName("envelopePic")
    val envelopePic: String?,
    @SerializedName("fresh")
    val fresh: Boolean, // true
    @SerializedName("host")
    val host: String?,
    @SerializedName("id")
    val id: Int, // 27630
    @SerializedName("link")
    val link: String?, // https://mp.weixin.qq.com/s/bD9fsPbjcHKa0r1Nh-ltww
    @SerializedName("niceDate")
    val niceDate: String, // 1小时前
    @SerializedName("niceShareDate")
    val niceShareDate: String?, // 1小时前
    @SerializedName("origin")
    val origin: String?,
    @SerializedName("prefix")
    val prefix: String?,
    @SerializedName("projectLink")
    val projectLink: String?,
    @SerializedName("publishTime")
    val publishTime: Long, // 1701237617000
    @SerializedName("realSuperChapterId")
    val realSuperChapterId: Int, // 604
    @SerializedName("selfVisible")
    val selfVisible: Int, // 0
    @SerializedName("shareDate")
    val shareDate: Long, // 1701237617000
    @SerializedName("shareUser")
    val shareUser: String?,
    @SerializedName("superChapterId")
    val superChapterId: Int, // 605
    @SerializedName("superChapterName")
    val superChapterName: String?, // 大厂对外分享 - 学习路径
    @SerializedName("tags")

    val tags: List<Tag>?,
    @SerializedName("title")
    val title: String, // 快手AGP升级之路：解决包体积增加的痛苦经历
    @SerializedName("type")
    val type: Int, // 0
    @SerializedName("userId")
    val userId: Int, // 2
    @SerializedName("visible")
    val visible: Int, // 1
    @SerializedName("zan")
    val zan: Int, // 0
    var top:Boolean = false
) {
    data class Tag(
        val name: String,
        val url: String?
    )

    @Suppress("DEPRECATION")
    val titleHtml: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(title).toString()
        }


    private val avatarList: List<Int> = listOf(
        R.drawable.avatar_1_raster,
        R.drawable.avatar_2_raster,
        R.drawable.avatar_3_raster,
        R.drawable.avatar_4_raster,
        R.drawable.avatar_5_raster,
        R.drawable.avatar_6_raster,
    )


    val avatarId by lazy {
        avatarList[abs(userId) % avatarList.size]
    }
}




