package com.dxl.scaffold.base

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.dxl.scaffold.R
import com.dxl.scaffold.defaultConfig
import com.dxl.scaffold.utils.inflateBindingWithGeneric
import com.dxl.scaffold.utils.lllog
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import java.lang.reflect.ParameterizedType

/**
 * activity基类
 * @author duxiaolong
 */
abstract class BaseVmActivity<VM : BaseViewModel, VB : ViewBinding> :
    AppCompatActivity() {

    lateinit var viewModel: VM
    lateinit var vb: VB

    private var loadingDialog: LoadingPopupView? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lllog(this.javaClass.simpleName, "页面")
        beforeInit()
        if (statusColor != 0) {
            window.statusBarColor = statusColor
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //获取泛型的类
        val type =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VM>
        viewModel = ViewModelProvider(this)[type]

        vb = inflateBindingWithGeneric(layoutInflater)
        setContentView(vb.root)

        observe()
        init(savedInstanceState)
    }

    @ColorInt
    open val statusColor: Int = 0


    open fun beforeInit() {
        //子类实现
    }

    open fun init(savedInstanceState: Bundle?) {
        //子类实现
    }

    open fun observe() {
        viewModel.dialogStatus.observe(this) {
            if (it)
                showLoading()
            else
                dismissLoading()
        }

        viewModel.progressLiveData.observe(this) {
            showLoading(it ?: defaultConfig.hint.loading)
        }
    }


    fun showLoading(text: String = defaultConfig.hint.loading, dismissOnBack: Boolean = false) {
        if (loadingDialog == null) {
            loadingDialog = XPopup.Builder(this).dismissOnBackPressed(dismissOnBack)
                .dismissOnTouchOutside(false)
                .asLoading(text, R.layout.layout_loading_dialog, LoadingPopupView.Style.Spinner)
        }
        loadingDialog?.setTitle(text)?.show()
    }

    fun dismissLoading() {
        loadingDialog?.smartDismiss()
    }


}