package com.dxl.androidscaffold.baidu

import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.baidu.location.BDLocation
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.LogoPosition
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.geocode.GeoCodeResult
import com.baidu.mapapi.search.geocode.GeoCoder
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult
import com.baidu.mapapi.utils.DistanceUtil
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ConvertUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.dxl.androidscaffold.R
import com.dxl.androidscaffold.databinding.ActivityBaiduMapBinding
import com.dxl.androidscaffold.databinding.ItemPoiBinding
import com.dxl.scaffold.base.BaseViewModel
import com.dxl.scaffold.base.BaseVmActivity
import com.dxl.scaffold.utils.BarUtil.immerseStatus
import com.dxl.scaffold.utils.click
import com.dxl.scaffold.utils.dp
import com.dxl.scaffold.utils.finishWithResult
import com.dxl.scaffold.utils.format
import com.dxl.scaffold.utils.toast
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 百度地图选择位置
 * @author duxiaolong
 * @date 2023/12/5
 */
class BaiduMapActivity : BaseVmActivity<BaseViewModel, ActivityBaiduMapBinding>() {

    private val mapView: MapView get() = vb.mapView
    private val map: BaiduMap get() = mapView.map

    //用户移动了地图，不再更新定位
    private var userDragMap: Boolean = false

    private var poiClick: Boolean = false

    //当前定位点
    private var currentLocation: BDLocation? = null

    private var currentPage: Int = 0

    //用户选择的目标点
    private var target: LatLng? = null

    private var checkedPoiInfo: PoiInfo? = null

    private val poiAdapter by lazy {
        object :
            BaseQuickAdapter<PoiInfo, BaseDataBindingHolder<ItemPoiBinding>>(R.layout.item_poi),
            LoadMoreModule {
            override fun convert(holder: BaseDataBindingHolder<ItemPoiBinding>, item: PoiInfo) {
                holder.dataBinding?.apply {
                    tvTitle.text = item.name
                    tvDesc.text = item.address
                    ivChecked.visibility =
                        if (item.uid == checkedPoiInfo?.uid) View.VISIBLE else View.INVISIBLE
                }
            }
        }.apply {
            loadMoreModule.setOnLoadMoreListener {
                searchPoi()
            }
            setOnItemClickListener { _, _, position ->
                val poiInfo = getItem(position)
                if (poiInfo.uid == checkedPoiInfo?.uid) return@setOnItemClickListener
                val location = poiInfo.location
                target = location
                poiClick = true
                userDragMap = false

                val oldCheckedPosition = data.indexOfFirst { it.uid == checkedPoiInfo?.uid }
                checkedPoiInfo = poiInfo

                notifyItemChanged(oldCheckedPosition)
                notifyItemChanged(position)

                map.animateMapStatus(
                    MapStatusUpdateFactory.newMapStatus(
                        MapStatus.Builder().zoom(19f).rotate(0f).target(
                            LatLng(location.latitude, location.longitude)
                        ).build()
                    )
                )
            }
        }
    }

    override fun beforeInit() {
        immerseStatus()
    }

    override fun init(savedInstanceState: Bundle?) {
        initMap()
        initView()
    }

    private fun initMap() {
        addCenterMarker()

        mapView.map.setOnMapLoadedCallback {
            initLocation()
        }

        mapView.init(this,
            onMapStatusChangeStart = { mapStatus, reason ->
                if (reason == BaiduMap.OnMapStatusChangeListener.REASON_GESTURE) {
                    //用户操作了地图
                    userDragMap = true
                    poiClick = false
                    poiAdapter.removeAllHeaderView()
                }
            },
            onMapStatusChangeFinish = { mapStatus ->
                if (currentLocation != null && !poiClick) {
                    currentPage = 0
                    target = mapStatus.target
                    searchPoi()
                }
            })

        vb.btnMyLocation.setOnClickListener {

            userDragMap = false
            poiClick = false
            if (currentLocation != null) {
                map.animateMapStatus(
                    MapStatusUpdateFactory.newMapStatus(
                        MapStatus.Builder().zoom(19f).rotate(0f).target(
                            LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                        ).build()
                    )
                )
            }
        }
    }

    private fun initLocation() {
        BaiduLocation.Builder(this)
            .setOnceLocation(false)
            .setLoading { showLoading ->
                if (showLoading) {
                    showLoading("正在获取位置..")
                } else {
                    dismissLoading()
                }
            }.setLocationError {
                it.format().toast()
            }
            .setOnReceiveLocation { bdLocation: BDLocation ->
                currentLocation = bdLocation
                map.setMyLocationData(
                    MyLocationData.Builder()
                        .accuracy(bdLocation.radius)
                        .direction(bdLocation.direction)
                        .latitude(bdLocation.latitude)
                        .longitude(bdLocation.longitude)
                        .speed(bdLocation.speed)
                        .build()
                )

                if (userDragMap || poiClick) return@setOnReceiveLocation

                val distance = distance(
                    LatLng(bdLocation.latitude, bdLocation.longitude),
                    map.mapStatus.target
                )
                if (distance > 10) {
                    map.animateMapStatus(
                        MapStatusUpdateFactory.newMapStatus(
                            MapStatus.Builder().zoom(19f).rotate(0f).target(
                                LatLng(bdLocation.latitude, bdLocation.longitude)
                            ).build()
                        )
                    )
                }
            }.start()
    }

    private fun initView() {
        vb.rv.adapter = poiAdapter
        vb.btnSend.layoutParams = (vb.btnSend.layoutParams as MarginLayoutParams).apply {
            setMargins(0, BarUtils.getStatusBarHeight() + 10.dp, 10.dp, 0)
        }
        vb.btnSend.click {
            setResult()
        }
    }

    private fun setResult() {
        val poiInfo = checkedPoiInfo
        if (poiInfo == null) {
            "请选择位置".toast()
            return
        }
        finishWithResult(
            "lat" to poiInfo.location.latitude,
            "lng" to poiInfo.location.longitude,
            "title" to poiInfo.name,
            "content" to poiInfo.address,
            "url" to getMapUrl(poiInfo.location.latitude, poiInfo.location.longitude)
        )

    }

    private fun getMapUrl(lat: Double, lng: Double): String? {
        return "https://api.map.baidu.com/staticimage/v2?ak=vQG0IoxEKqbyAiL9XjWQfmhdYbNw0d1x&width=600&height=300&zoom=18&dpiType=ph&center=$lng,$lat&markers=$lng,$lat&markerStyles=l"
    }

    private fun addCenterMarker() {
        mapView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val drawable =
                    ContextCompat.getDrawable(this@BaiduMapActivity, R.drawable.ic_location)
                //获取View中心点在屏幕中的位置
                val location = intArrayOf(0, 0)
                mapView.getLocationOnScreen(location)

                val centerX = location[0] + mapView.width / 2
                val centerY = location[1] + mapView.height / 2

                map.addOverlay(
                    MarkerOptions()
                        .position(map.mapStatus.target)
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                ConvertUtils.drawable2Bitmap(
                                    drawable
                                )
                            )
                        )
                        .isForceDisPlay(true)
                        .clickable(true)
                        .zIndex(100)
                        .draggable(true)
                        .fixedScreenPosition(Point(centerX, centerY))
                ) as? Marker

                mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }

        })
    }

    private fun searchPoi() {
        lifecycleScope.launch {
            requestPoiList(target, currentPage)
                .onStart {
                    vb.flProgress.isVisible = true
                }
                .onCompletion {
                    vb.flProgress.isVisible = false
                }
                .catch {
                    vb.flProgress.isVisible = false
                }.collect { infos ->
                    if (currentPage == 0) {
                        checkedPoiInfo = infos.firstOrNull()
                        poiAdapter.setList(infos)
                    } else {
                        poiAdapter.addData(infos)
                    }
                    if (infos.isEmpty()) {
                        poiAdapter.loadMoreModule.loadMoreEnd(true)
                    } else {
                        currentPage++
                        poiAdapter.loadMoreModule.loadMoreComplete()
                    }
                }
        }

    }


    /**
     * 两个位置的距离
     * @param firstLocation BDLocation?
     * @param secondLocation BDLocation?
     * @return Double
     */
    private fun distance(firstLocation: LatLng?, secondLocation: LatLng?): Double {
        if (firstLocation == null || secondLocation == null) return 0.0
        return DistanceUtil.getDistance(
            LatLng(firstLocation.latitude, firstLocation.longitude),
            LatLng(secondLocation.latitude, secondLocation.longitude)
        )
    }

    fun MapView.init(
        componentActivity: ComponentActivity,
        onMapStatusChangeStart: ((mapStatus: MapStatus?, reason: Int) -> Unit)? = null,
        onMapStatusChangeFinish: ((mapStatus: MapStatus) -> Unit)? = null
    ) {
        componentActivity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                onPause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                onDestroy()
            }
        })
        showZoomControls(true)
        map.mapType = BaiduMap.MAP_TYPE_NORMAL
        map.setCompassEnable(true)
        map.setMapStatus(
            MapStatusUpdateFactory.newMapStatus(
                MapStatus.Builder().zoom(10f).build()
            )
        )
        logoPosition = LogoPosition.logoPostionRightBottom
        map.isMyLocationEnabled = true
        map.setOnMapStatusChangeListener(object : BaiduMap.OnMapStatusChangeListener {
            override fun onMapStatusChangeStart(mapStatus: MapStatus?) {

            }

            override fun onMapStatusChangeStart(mapStatus: MapStatus?, reason: Int) {
                onMapStatusChangeStart?.invoke(mapStatus, reason)

            }

            override fun onMapStatusChange(mapStatus: MapStatus?) {

            }

            override fun onMapStatusChangeFinish(mapStatus: MapStatus) {
                onMapStatusChangeFinish?.invoke(mapStatus)

            }

        })
    }


    /**
     * 获取一个位置附近的poi列表
     * @param target LatLng?
     * @param page Int
     * @return Flow<List<MyPoi>>
     */
    private fun requestPoiList(target: LatLng?, page: Int) =
        flow {
            emit(suspendCancellableCoroutine {
                if (target == null) {
                    it.resumeWithException(IllegalArgumentException("target is null"))
                    return@suspendCancellableCoroutine
                }
                val geoCoder = GeoCoder.newInstance()
                geoCoder.setOnGetGeoCodeResultListener(object : OnGetGeoCoderResultListener {
                    override fun onGetGeoCodeResult(geoCodeResult: GeoCodeResult) {

                    }

                    override fun onGetReverseGeoCodeResult(reverseGeoCodeResult: ReverseGeoCodeResult) {
                        val poiList =
                            reverseGeoCodeResult.poiList
                        it.resume(poiList ?: emptyList<PoiInfo>())
                    }

                })
                geoCoder.reverseGeoCode(
                    ReverseGeoCodeOption().pageNum(1).location(target).newVersion(1).pageNum(page)
                        .pageSize(20)
                        .radius(1000)
                )
            })

        }


}