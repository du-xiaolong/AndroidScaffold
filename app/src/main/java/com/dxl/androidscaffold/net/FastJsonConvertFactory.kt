package com.dxl.androidscaffold.net

import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.support.config.FastJsonConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * @author du_xi
 * @date   2023/5/18
 */
class FastJsonConvertFactory : Converter.Factory() {
    private val fastJsonConfig = FastJsonConfig()

    companion object {
        fun create() = FastJsonConvertFactory()
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        return Converter<ResponseBody, Any> { responseBody ->
            val string = responseBody.string()
            Log.d("接口响应", string)
            JSON.parseObject(
                string,
                type,
                fastJsonConfig.parserConfig,
                fastJsonConfig.parseProcess,
                JSON.DEFAULT_PARSER_FEATURE,
                *fastJsonConfig.features
            )
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        return Converter<Any, RequestBody> {
            JSON.toJSONBytesWithFastJsonConfig(
                fastJsonConfig.charset,
                it,
                fastJsonConfig.serializeConfig,
                fastJsonConfig.serializeFilters,
                fastJsonConfig.dateFormat,
                JSON.DEFAULT_GENERATE_FEATURE,
                *fastJsonConfig.serializerFeatures
            ).toRequestBody("application/json; charset=UTF-8".toMediaTypeOrNull())
        }
    }


}