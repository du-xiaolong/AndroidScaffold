package com.dxl.androidscaffold.ui.waterMarkerCamera

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.View.MeasureSpec.UNSPECIFIED
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.baidu.location.BDLocation
import com.blankj.utilcode.util.ScreenUtils
import com.dxl.androidscaffold.baidu.BaiduLocation
import com.dxl.androidscaffold.databinding.ActivityWaterMarkerCameraBinding
import com.dxl.androidscaffold.databinding.ViewWaterMarkerBinding
import com.dxl.androidscaffold.ui.waterMark.WaterMarkDrawable.Companion.setWaterMark
import com.dxl.scaffold.utils.lllog
import com.dxl.scaffold.utils.llloge
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

/**
 * 水印相机
 * @author duxiaolong
 * @date 2023/12/10 10:29
 */
class WaterMarkerCameraActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityWaterMarkerCameraBinding

    private var cameraUri: Uri? = null

    private var currentLocation: BDLocation? = null

    private fun createCameraUri(): Uri? {
        return if (viewBinding.chkSaveOrigin.isChecked) {
            contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(
                        MediaStore.Images.ImageColumns.DISPLAY_NAME,
                        "${System.currentTimeMillis()}.jpg"
                    )
                    put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
                })
        } else {
            FileProvider.getUriForFile(
                this,
                "com.dxl.androidscaffold.fileProvider",
                File(externalCacheDir, "${System.currentTimeMillis()}.jpg")
            )
        }
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionResult ->
            if (permissionResult) {
                cameraUri = createCameraUri()
                takeCameraLauncher.launch(cameraUri)
            } else {
                Toast.makeText(this, "请到设置中开启相机权限", Toast.LENGTH_SHORT).show()
            }
        }

    private val takeCameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { takePictureResult ->
            if (takePictureResult) {

                if (viewBinding.chkCompress.isChecked) {
                    //压缩
                    Luban.with(this).setTargetDir(externalCacheDir!!.absolutePath)
                        .load(cameraUri)
                        .setCompressListener(object : OnNewCompressListener {
                            override fun onStart() {
                            }

                            override fun onSuccess(source: String?, compressFile: File?) {
                                addWaterMarker(compressFile?.inputStream())
                            }

                            override fun onError(source: String?, e: Throwable?) {
                                llloge(e)
                            }

                        }).launch()
                }else{
                    contentResolver.openInputStream(cameraUri!!).use { inputStream: InputStream? ->
                        addWaterMarker(inputStream)
                    }
                }

            }

        }


    @SuppressLint("SetTextI18n")
    private fun addWaterMarker(inputStream: InputStream?) {

        val sourceBitmap = BitmapFactory.decodeStream(inputStream)
        val createBitmap = Bitmap.createBitmap(
            sourceBitmap.width,
            sourceBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(createBitmap)
        canvas.drawBitmap(sourceBitmap, 0f, 0f, null)
        val waterMarkerBinding = ViewWaterMarkerBinding.inflate(layoutInflater)

        //时间
        val date = Date()
        waterMarkerBinding.tvTime.text =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        //具体时间
        val weeks =
            arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        val week = weeks[Calendar.getInstance()[Calendar.DAY_OF_WEEK] - 1]
        waterMarkerBinding.tvTimeDetail.text =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(date) + " " + week
        //地理位置
        waterMarkerBinding.tvAddress.text =
            currentLocation.address()
        waterMarkerBinding.tvLocation.text =
            "经度：${currentLocation?.longitude ?: 0.0}  纬度：${currentLocation?.latitude ?: 0.0}"
        //类型
        waterMarkerBinding.tvType.text = "打卡"

        val scale = sourceBitmap.width / 3000f

        val waterMarkerView = waterMarkerBinding.root
        waterMarkerView.measure(
            View.MeasureSpec.makeMeasureSpec(
                (sourceBitmap.width / scale).toInt(),
                View.MeasureSpec.EXACTLY
            ),
            View.MeasureSpec.makeMeasureSpec(
                (sourceBitmap.height / scale).toInt(),
                View.MeasureSpec.AT_MOST
            )
        )
        waterMarkerView.layout(
            0,
            0,
            waterMarkerView.measuredWidth,
            waterMarkerView.measuredHeight
        )
        canvas.save()

        val matrix = Matrix()
        matrix.postScale(sourceBitmap.width * 1f / 3000, sourceBitmap.width * 1f / 3000)

        matrix.postTranslate(
            0f,
            (sourceBitmap.height - waterMarkerView.measuredHeight * scale)
        )

        canvas.setMatrix(matrix)

        waterMarkerView.draw(canvas)

        canvas.restore()

        viewBinding.ivPreview.setImageBitmap(createBitmap)

        if (viewBinding.chkSaveWater.isChecked) {
            saveBitmapToPublic(createBitmap)
        }

    }

    private fun saveBitmapToPublic(bitmap: Bitmap) {
        val insert = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    "${System.currentTimeMillis()}.jpg"
                )
                put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
            })

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, contentResolver.openOutputStream(insert!!)!!)

    }

    private fun BDLocation?.address(): String {
        val poi = this?.poiList?.firstOrNull()
        if (poi != null) {
            return poi.addr + poi.name
        }
        return this?.addrStr ?: "未获取到位置"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            ActivityWaterMarkerCameraBinding.inflate(layoutInflater).also { viewBinding = it }.root
        )
        viewBinding.btnCamera.setOnClickListener {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        startLocation()
    }

    private fun startLocation() {
        BaiduLocation.Builder(this)
            .setOnceLocation(false)
            .setOnReceiveLocation {
                currentLocation = it
            }.start()
    }

}