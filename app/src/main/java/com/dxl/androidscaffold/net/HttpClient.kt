package com.dxl.androidscaffold.net

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 *
 * @author duxiaolong
 * @date 2023/11/29
 */
object HttpClient {
    const val WAN_BASE_URL = "https://www.wanandroid.com/"

    inline fun <reified T> getApiService(): T {
        return Retrofit.Builder()
            .client(OkHttpClient())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(FastJsonConvertFactory.create())
            .baseUrl(WAN_BASE_URL)
            .build().create(T::class.java)
    }


}