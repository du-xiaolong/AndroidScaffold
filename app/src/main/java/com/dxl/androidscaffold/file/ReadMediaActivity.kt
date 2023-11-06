package com.dxl.androidscaffold.file

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dxl.androidscaffold.R
import com.dxl.androidscaffold.databinding.ActivityReadMediaBinding
import com.dxl.androidscaffold.databinding.ItemMediaBinding
import com.dxl.scaffold.base.BaseViewModel
import com.dxl.scaffold.base.BaseVmActivity
import com.dxl.scaffold.utils.dp
import com.dxl.scaffold.utils.lllog
import com.dxl.scaffold.utils.loadImage
import com.dxl.scaffold.utils.toast
import com.permissionx.guolindev.PermissionX

/**
 *
 * @author duxiaolong
 * @date 2023/11/6
 */
class ReadMediaActivity : BaseVmActivity<BaseViewModel, ActivityReadMediaBinding>() {

    private val albumAdapter by lazy {
        object : BaseQuickAdapter<MediaUtils.Album, BaseViewHolder>(0) {
            override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                val textView = TextView(this@ReadMediaActivity).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(10.dp)
                }
                return BaseViewHolder(textView)
            }

            override fun convert(holder: BaseViewHolder, item: MediaUtils.Album) {
                val textView = holder.itemView as TextView
                textView.text = item.displayName
                //获取主题色
                val primary = ContextCompat.getColor(this@ReadMediaActivity, R.color.primary)
                if (checkedAlbum == item) {
                    textView.setBackgroundColor(primary)
                    textView.setTextColor(Color.WHITE)
                } else {
                    textView.setBackgroundColor(Color.WHITE)
                    textView.setTextColor(primary)
                }
            }

        }.apply {
            setOnItemClickListener { adapter, view, position ->
                checkedAlbum = getItem(position)
                notifyDataSetChanged()
                loadMedias()
            }
        }
    }

    private val mediaAdapter by lazy {
        object : BaseQuickAdapter<MediaUtils.Media, BaseDataBindingHolder<ItemMediaBinding>>(R.layout.item_media) {
            override fun convert(holder: BaseDataBindingHolder<ItemMediaBinding>, item: MediaUtils.Media) {
                holder.dataBinding?.apply {
                    when{
                        item.mimeType.startsWith("image/") -> iv.loadImage(activity = this@ReadMediaActivity, uri = item.uri)
                        item.mimeType.startsWith("video/") -> iv.setImageResource(R.drawable.bg_video)
                        item.mimeType.startsWith("audio/") -> iv.setImageResource(R.drawable.bg_audio)
                        else -> iv.setImageResource(R.drawable.bg_file)
                    }
                    tvName.text = item.name
                }
            }

        }
    }



    private fun loadMedias() {
        checkedAlbum?.id?.let {
            val medias = MediaUtils.readMedias(this, checkedMediaType, it)
            mediaAdapter.setList(medias)
        }
    }

    override fun init(savedInstanceState: Bundle?) {
//        requestPermission()
        initView()
    }

    private var checkedMediaType: Int = MediaUtils.MediaType.ALL
    private var checkedAlbum: MediaUtils.Album? = null

    private fun initView() {
        vb.rvAlbums.adapter = albumAdapter
        vb.rvMedias.adapter = mediaAdapter
        vb.rgType.setOnCheckedChangeListener { _, checkedId ->
            checkedMediaType = when (checkedId) {
                R.id.rb_all -> MediaUtils.MediaType.ALL
                R.id.rb_image -> MediaUtils.MediaType.IMAGE
                R.id.rb_video -> MediaUtils.MediaType.VIDEO
                R.id.rb_audio -> MediaUtils.MediaType.AUDIO
                else -> MediaUtils.MediaType.ALL
            }
            loadAlbums()
        }
        vb.rgType.check(vb.rbAll.id)
    }

    private fun loadAlbums() {
        val albums = MediaUtils.queryAllAlbums(this, checkedMediaType)
        checkedAlbum = albums.firstOrNull()
        albumAdapter.setList(albums)
        loadMedias()
    }

    private fun requestPermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        PermissionX.init(this).permissions(permissions).request { allGranted, _, _ ->
            if (allGranted) {
                "已获取全部权限".toast()
            } else {
                "未获取权限，只能读取本应用创建的媒体".toast()
            }
        }
    }


    private fun readMedia() {
        val types = arrayOf("全部", "图片", "视频", "音频")
        AlertDialog.Builder(this)
            .setItems(types) { _, which ->
                val mediaType = when (which) {
                    0 -> MediaUtils.MediaType.ALL
                    1 -> MediaUtils.MediaType.IMAGE
                    2 -> MediaUtils.MediaType.VIDEO
                    3 -> MediaUtils.MediaType.AUDIO
                    else -> MediaUtils.MediaType.ALL
                }
                val albums = MediaUtils.queryAllAlbums(this, mediaType)
                AlertDialog.Builder(this)
                    .setItems(
                        albums.map { it.displayName }.toTypedArray()
                    ) { _, _which ->
                        val album = albums[_which]
                        val uris = MediaUtils.readMedias(this, mediaType, album.id)
                        lllog(uris)
                    }.show()

            }.show()
    }


    @SuppressLint("Range")
    private fun readAlbums() {
        val types = arrayOf("全部", "图片", "视频", "音频")
        AlertDialog.Builder(this)
            .setItems(types) { _, which ->
                val mediaType = when (which) {
                    0 -> MediaUtils.MediaType.ALL
                    1 -> MediaUtils.MediaType.IMAGE
                    2 -> MediaUtils.MediaType.VIDEO
                    3 -> MediaUtils.MediaType.AUDIO
                    else -> MediaUtils.MediaType.ALL
                }
                val albums = MediaUtils.queryAllAlbums(this, mediaType)
            }.show()
    }




}