package com.dxl.scaffold.utils

import android.os.Parcelable
import com.tencent.mmkv.MMKV

/**
 *
 * @author duxiaolong
 */
object KVUtil {

    val mmkv: MMKV
        get() = MMKV.defaultMMKV()

    fun getString(key: String, default: String? = null): String? = mmkv.getString(key, default)
    fun putString(key: String, value: String?) = mmkv.putString(key, value)


    fun getInt(key: String, default: Int = 0): Int = mmkv.getInt(key, default)
    fun putInt(key: String, value: Int) = mmkv.putInt(key, value)

    fun getBoolean(key: String, default: Boolean = false) = mmkv.getBoolean(key, default)
    fun putBoolean(key: String, value: Boolean) = mmkv.putBoolean(key, value)

    fun getLong(key: String, default: Long = 0L): Long = mmkv.getLong(key, default)
    fun putLong(key: String, value: Long) = mmkv.putLong(key, value)

    fun getDouble(key: String, default: Double = 0.0): Double = mmkv.decodeDouble(key, default)
    fun putDouble(key: String, value: Double) = mmkv.encode(key, value)

    fun getFloat(key: String, default: Float = 0f): Float = mmkv.getFloat(key, default)
    fun putFloat(key: String, value: Float) = mmkv.putFloat(key, value)

    inline fun <reified T : Parcelable> putParcelable(key: String, value: T?) {
        if (value == null) {
            mmkv.remove(key)
            return
        }
        mmkv.encode(key, value)
    }

    inline fun <reified T : Parcelable> getParcelable(key: String): T? {
        return mmkv.decodeParcelable(key, T::class.java)
    }

    inline fun <reified T> getList(key: String): List<T> = getString(key).parseList()
    fun<T> putList(key: String, list: List<T>) = putString(key, list.toJsonString())
    
    

}