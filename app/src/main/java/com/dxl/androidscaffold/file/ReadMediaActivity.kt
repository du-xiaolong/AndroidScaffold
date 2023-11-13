package com.dxl.androidscaffold.file

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.dxl.scaffold.utils.MediaUtils
import com.dxl.scaffold.utils.dp
import com.dxl.scaffold.utils.format
import com.dxl.scaffold.utils.loadImage
import com.dxl.scaffold.utils.toast
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.interfaces.OnQueryAllAlbumListener
import com.luck.picture.lib.interfaces.OnQueryDataResultListener
import com.luck.picture.lib.loader.IBridgeMediaLoader
import java.util.ArrayList

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
        object :
            BaseQuickAdapter<MediaUtils.Media, BaseDataBindingHolder<ItemMediaBinding>>(R.layout.item_media) {
            override fun convert(
                holder: BaseDataBindingHolder<ItemMediaBinding>,
                item: MediaUtils.Media
            ) {
                holder.dataBinding?.apply {
                    when {
                        item.mimeType.startsWith("image/") -> iv.loadImage(
                            activity = this@ReadMediaActivity,
                            uri = item.uri
                        )

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
        checkedAlbum?.id?.let { id ->
            showLoading("加载中")
            MediaUtils.readMedias(this, checkedMediaType, id) { mediasResult ->
                dismissLoading()
                if (mediasResult.isSuccess) {
                    mediaAdapter.setList(mediasResult.getOrNull())
                } else {
                    mediasResult.exceptionOrNull()?.format()?.toast()
                }
            }

        }
    }

    override fun init(savedInstanceState: Bundle?) {
        initView()
    }

    @MediaUtils.MediaType
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
        loadAlbums()
    }

    private fun loadAlbums() {
        showLoading("加载中...")
        Log.d("查询", "开始查询")
        MediaUtils.queryAllAlbums(this, checkedMediaType) { albumResult ->
            dismissLoading()
            Log.d("查询", "loadAlbums: $albumResult")
            if (albumResult.isSuccess) {
                val albumList = albumResult.getOrNull()
                checkedAlbum = albumList?.firstOrNull()
                albumAdapter.setList(albumList)
                loadMedias()
            } else {
                albumResult.exceptionOrNull()?.format()?.toast()
            }
        }

    }

}