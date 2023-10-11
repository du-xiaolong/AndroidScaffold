package com.dxl.scaffold.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.blankj.utilcode.util.ResourceUtils
import com.dxl.scaffold.R
import com.dxl.scaffold.databinding.DialogSelectAddressBinding
import com.dxl.scaffold.ui.wheel.adapter.WheelAdapter
import com.dxl.scaffold.ui.wheel.interfaces.IPickerViewData
import com.dxl.scaffold.ui.wheel.view.WheelView
import com.dxl.scaffold.utils.click
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *
 * @author duxiaolong
 */
@SuppressLint("ViewConstructor")
class SelectAddressDialog(
    context: Context,
    private val provinceCode: String?,
    private val cityCode: String?,
    private val areaCode: String?,
    private val onConfirm: (Province?, Province.City?, Province.City.Area?) -> Unit
) : BottomPopupView(context) {

    companion object {
        fun show(
            context: Context,
            provinceCode: String?,
            cityCode: String?,
            areaCode: String?,
            onConfirm: (Province?, Province.City?, Province.City.Area?) -> Unit
        ) {
            XPopup.Builder(context)
                .dismissOnTouchOutside(true)
                .dismissOnBackPressed(true)
                .isDestroyOnDismiss(true)
                .asCustom(
                    SelectAddressDialog(
                        context,
                        provinceCode,
                        cityCode,
                        areaCode,
                        onConfirm
                    )
                )
                .show()
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_select_address
    }

    private lateinit var vb: DialogSelectAddressBinding

    private var currentProvinces: List<Province> = emptyList()
    private var currentCities: List<Province.City> = emptyList()
    private var currentAreas: List<Province.City.Area> = emptyList()


    data class Province(val code: String, val name: String, val cities: List<City>):
        IPickerViewData {
        data class City(val code: String, val name: String, val areas: List<Area>):IPickerViewData {
            data class Area(val code: String, val name: String):IPickerViewData {
                override fun getPickerViewText(): String = name
            }
            override fun getPickerViewText(): String = name
        }

        override fun getPickerViewText(): String = name
    }

    override fun onCreate() {
        super.onCreate()
        vb = DialogSelectAddressBinding.bind(popupImplView)

        setWheels(vb.wvProvince, vb.wvCity, vb.wvCounty)
        setProvinceWheels()

        lifecycleScope.launch(Dispatchers.IO) {
            val json = ResourceUtils.readAssets2String("address.json")
            val addressJson = JSON.parseObject(json)
            val provincesJson = addressJson.getJSONObject("86")
            val provinces = mutableListOf<Province>()
            provincesJson.keys.forEach { provinceCode: String ->
                val provinceName = provincesJson.getString(provinceCode)
                val citiesJson = addressJson.getJSONObject(provinceCode)
                val cities = mutableListOf<Province.City>()
                citiesJson.keys.forEach { cityCode ->
                    val cityName = citiesJson.getString(cityCode)
                    val areasJson = addressJson.getJSONObject(cityCode)?: JSONObject()
                    val areas = mutableListOf<Province.City.Area>()
                    areasJson.keys.forEach { areaCode ->
                        val areaName = areasJson.getString(areaCode)
                        areas.add(Province.City.Area(areaCode, areaName))
                    }
                    cities.add(Province.City(cityCode, cityName, areas))
                }
                provinces.add(Province(provinceCode, provinceName, cities))
            }
            currentProvinces = provinces
            withContext(Dispatchers.Main) {
                setProvinceWheels()
            }
        }

        vb.btnCancel.click {
            dismiss()
        }
        vb.btnConfirm.click {
            onConfirm.invoke(
                currentProvinces.getOrNull(vb.wvProvince.currentItem),
                currentCities.getOrNull(vb.wvCity.currentItem),
                currentAreas.getOrNull(vb.wvCounty.currentItem),
            )
            dismiss()
        }

    }

    private fun setProvinceWheels() {
        val adapter = object : WheelAdapter<Province> {
            override fun getItemsCount() = currentProvinces.size

            override fun getItem(index: Int) = currentProvinces.getOrNull(index)

            override fun indexOf(o: Province?): Int = currentProvinces.indexOf(o)

        }

        vb.wvProvince.adapter = adapter
        vb.wvProvince.setOnItemSelectedListener {
            setCityWheels()
        }
        val defaultIndex = currentProvinces.indexOfFirst { it.code == provinceCode }
        vb.wvProvince.currentItem = if (defaultIndex < 0) 0 else defaultIndex
        setCityWheels()
    }


    private fun setCityWheels() {
        currentCities = currentProvinces.getOrNull(vb.wvProvince.currentItem)?.cities ?: emptyList()
        val adapter = object : WheelAdapter<Province.City> {
            override fun getItemsCount() = currentCities.size

            override fun getItem(index: Int) = currentCities.getOrNull(index)

            override fun indexOf(o: Province.City?): Int = currentCities.indexOf(o)

        }

        vb.wvCity.adapter = adapter
        vb.wvCity.setOnItemSelectedListener {
            setAreaWheels()
        }
        val defaultIndex = currentCities.indexOfFirst { it.code == cityCode }
        vb.wvCity.currentItem = if (defaultIndex < 0) 0 else defaultIndex
        setAreaWheels()
    }

    private fun setAreaWheels() {
        currentAreas = currentCities.getOrNull(vb.wvCity.currentItem)?.areas ?: emptyList()
        val adapter = object : WheelAdapter<Province.City.Area> {
            override fun getItemsCount() = currentAreas.size

            override fun getItem(index: Int) = currentAreas.getOrNull(index)

            override fun indexOf(o: Province.City.Area?): Int = currentAreas.indexOf(o)

        }

        vb.wvCounty.adapter = adapter

        val defaultIndex = currentAreas.indexOfFirst { it.code == areaCode }
        vb.wvCounty.currentItem = if (defaultIndex < 0) 0 else defaultIndex
    }


    private fun setWheels(vararg wheels: WheelView) {
        wheels.forEach { wheel ->
            wheel.setCyclic(false)
            wheel.setItemsVisibleCount(5)
            wheel.setAlphaGradient(false)
            wheel.setDividerColor(Color.TRANSPARENT)
            wheel.setLineSpacingMultiplier(2.5f)
            wheel.setTextColorCenter(Color.BLACK)
            wheel.setTextSize(22f)
            wheel.setTextColorOut(Color.parseColor("#969696"))
            wheel.setIsOptions(true)
        }
    }


}