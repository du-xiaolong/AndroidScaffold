package com.dxl.androidscaffold.ui.waterMarkerCamera

import android.Manifest
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dxl.androidscaffold.databinding.ActivityWaterMarkerCameraBinding
import com.dxl.androidscaffold.databinding.ViewWaterMarkerBinding
import java.io.InputStream

/**
 * 水印相机
 * @author duxiaolong
 * @date 2023/12/10 10:29
 */
class WaterMarkerCameraActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityWaterMarkerCameraBinding

    private var cameraUri: Uri? = null

    private fun createCameraUri(): Uri? {
        return contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    "${System.currentTimeMillis()}.jpg"
                )
                put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
            })
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
                addWaterMarker()
            }

        }

    private fun addWaterMarker() {
        val cameraUri = cameraUri ?: return
        contentResolver.openInputStream(cameraUri).use { inputStream: InputStream? ->
            val sourceBitmap = BitmapFactory.decodeStream(inputStream)
            val createBitmap = Bitmap.createBitmap(
                sourceBitmap.width,
                sourceBitmap.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(createBitmap)
            canvas.drawBitmap(sourceBitmap, 0f, 0f, null)
            val waterMarkerBinding = ViewWaterMarkerBinding.inflate(layoutInflater)
            val waterMarkerView = waterMarkerBinding.root
            waterMarkerView.measure(
                View.MeasureSpec.makeMeasureSpec(sourceBitmap.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(sourceBitmap.height, View.MeasureSpec.AT_MOST)
            )
            waterMarkerView.layout(0,0,waterMarkerView.measuredWidth,waterMarkerView.measuredHeight)
            canvas.save()

            val matrix = Matrix()
            matrix.setTranslate(0f, sourceBitmap.height.toFloat() - waterMarkerView.measuredHeight)
            canvas.setMatrix(matrix)

            waterMarkerView.draw(canvas)

            canvas.restore()

            viewBinding.ivPreview.setImageBitmap(createBitmap)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            ActivityWaterMarkerCameraBinding.inflate(layoutInflater).also { viewBinding = it }.root
        )
        viewBinding.btnCamera.setOnClickListener {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

}