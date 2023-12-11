package com.dxl.androidscaffold.baidu

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.dxl.scaffold.utils.lllog
import com.permissionx.guolindev.PermissionX

/**
 * 百度定位
 * @author duxiaolong
 * @date 2023/10/19
 */
class BaiduLocation private constructor(private val builder: Builder) {

    /**
     * 使用builder构造
     */
    class Builder(val activity: FragmentActivity) {
        //单次定位
        var onceLocation: Boolean = true
        //收到定位结果
        var onReceiveLocation: ((location: BDLocation) -> Unit)? = null
        //定位失败
        var onLocationError: ((LocationException) -> Unit)? = null
        //加载进度
        var onLoading: ((isShow: Boolean) -> Unit)? = null

        fun setLoading(loading: (isShow: Boolean) -> Unit): Builder {
            this.onLoading = loading
            return this
        }

        //设置单次定位
        fun setOnceLocation(onceLocation: Boolean): Builder {
            this.onceLocation = onceLocation
            return this
        }

        //收到位置更新
        fun setOnReceiveLocation(onReceiveLocation: (location: BDLocation) -> Unit): Builder {
            this.onReceiveLocation = onReceiveLocation
            return this
        }

        //定位错误回调
        fun setLocationError(error: (LocationException) -> Unit): Builder {
            this.onLocationError = error
            return this
        }

        //开始定位
        fun start(): BaiduLocation {
            val baiduLocation = BaiduLocation(this)
            baiduLocation.startLocation()
            return baiduLocation
        }
    }

    private var locationClient: LocationClient? = null


    fun startLocation() {
        //先请求权限
        requestLocationPermission {
            locationClient?.stop()
            builder.onLoading?.invoke(true)
            //注册生命周期
            registerLifecycle()
            //同意隐私协议
            LocationClient.setAgreePrivacy(true)

            locationClient = kotlin.runCatching { LocationClient(builder.activity) }.getOrNull()

            locationClient?.locOption = getLocationClientOption()
            locationClient?.registerLocationListener(object : BDAbstractLocationListener() {
                override fun onReceiveLocation(location: BDLocation?) {
                    lllog("收到定位：$location")
                    builder.onLoading?.invoke(false)
                    location?:return
                    val locType = location.locType
                    if (locType == 61 || locType == 161) {
                        //定位成功
                        builder.onReceiveLocation?.invoke(location)
                    } else {
                        builder.onLocationError?.invoke(LocationException(location))
                    }
                }
            })
            locationClient?.start()
        }

    }

    fun stopLocation(){
        locationClient?.stop()
    }

    private fun registerLifecycle() {
        builder.activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                locationClient?.stop()
            }
        })
    }


    private fun getLocationClientOption(): LocationClientOption {

        val option = LocationClientOption()
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy  //高精度
        option.setLocationPurpose(LocationClientOption.BDLocationPurpose.Transport)

        //可选，设置返回经纬度坐标类型，默认gcj02
        //gcj02：国测局坐标；
        //bd09ll：百度经纬度坐标；
        //bd09：百度墨卡托坐标；
        option.setCoorType("bd09ll")

        //可选，首次定位时可以选择定位的返回是准确性优先还是速度优先，默认为速度优先
        //可以搭配setOnceLocation(Boolean isOnceLocation)单次定位接口使用，当设置为单次定位时，setFirstLocType接口中设置的类型即为单次定位使用的类型
        //FirstLocType.SPEED_IN_FIRST_LOC:速度优先，首次定位时会降低定位准确性，提升定位速度；
        //FirstLocType.ACCURACY_IN_FIRST_LOC:准确性优先，首次定位时会降低速度，提升定位准确性；
        if (builder.onceLocation) {
            //如果单次定位，可以选择精确度高的
            option.setFirstLocType(LocationClientOption.FirstLocType.ACCURACY_IN_FIRST_LOC)
        } else {
            option.setFirstLocType(LocationClientOption.FirstLocType.SPEED_IN_FIRST_LOC)
        }
        option.setOnceLocation(builder.onceLocation)

        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效
        if (builder.onceLocation) {
            option.setScanSpan(0)
        } else {
            option.setScanSpan(2000)
        }

        option.setIsNeedLocationPoiList(true)


        //可选，设置是否使用卫星定位，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.isOpenGnss = true

        //可选，设置是否当卫星定位有效时按照1S/1次频率输出卫星定位结果，默认false
        option.isLocationNotify = true

        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
        option.setIgnoreKillProcess(false)


        //可选，设置是否收集Crash信息，默认收集，即参数为false
        option.SetIgnoreCacheException(false)

        //可选，设置是否需要过滤卫星定位仿真结果，默认需要，即参数为false
        option.setEnableSimulateGnss(false)

        //可选，设置是否需要最新版本的地址信息。默认需要，即参数为true
        option.setNeedNewVersionRgc(true)

        return option

    }

    private fun requestLocationPermission(onGranted: () -> Unit) {
        PermissionX.init(builder.activity).permissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
            .explainReasonBeforeRequest()
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "签到需要获取【精确位置】权限",
                    "允许",
                    "拒绝"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "您需要去设置中手动开启【精确位置】权限",
                    "去设置",
                    "取消"
                )
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    //全部允许
                    onGranted.invoke()
                } else {
                    val message =
                        if (deniedList.contains(Manifest.permission.ACCESS_FINE_LOCATION) && grantedList.contains(
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        ) {
                            "未开启【精确位置】权限，请到设置中手动开启定位权限，并打开【精确位置】"
                        } else {
                            "未开启【位置】权限，请到设置中手动开启定位权限，并打开【精确位置】"
                        }

                    AlertDialog.Builder(builder.activity).setTitle("提示").setMessage(message)
                        .setPositiveButton("去设置"
                        ) { _, _ -> toSettings(builder.activity) }
                        .setNegativeButton("取消"
                        ) { dialog, _ -> dialog?.dismiss() }.show()
                }
            }
    }

    private fun toSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            val uri = Uri.fromParts("package", context.packageName, null)
            data = uri
        }
        context.startActivity(intent)
    }


    class LocationException(code: Int, message: String) : Exception() {
        constructor(location: BDLocation) : this(location.locType, location.locTypeDescription)

    }

}