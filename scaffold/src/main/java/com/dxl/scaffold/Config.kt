package com.dxl.scaffold

import com.alibaba.fastjson.JSONException
import com.dxl.scaffold.net.ApiException
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

/**
 * @author duxiaolong
 */

var defaultConfig: Config = Config()

class Config {

    //loading加载布局
    val loadingLayoutRes = R.layout.layout_loading_dialog

    //错误信息展示
    fun getDefaultErrorInfo(exception: Throwable?):String {
        return when {
            exception is ApiException -> exception.msg
            exception is SocketTimeoutException || exception?.message?.contains("timeout") == true || exception is TimeoutException -> "请求超时"
            exception is HttpException && exception.code() == 500 -> "服务器开小差了，稍后再试吧~"
            // 网络请求失败
            exception is ConnectException
                    || exception is UnknownHostException
                    || exception is HttpException
                    || exception is InterruptedIOException -> "当前网络不稳定，请刷新重试～"

            exception is SSLHandshakeException || exception is SSLException -> "证书异常，请刷新重试～"
            // 数据解析错误
            exception is JSONException || exception?.cause is JSONException -> "数据解析异常，请刷新重试～"
            // 其他错误
            else -> exception?.message
        } ?: "请求失败"
    }

    //错误信息格式化显示
    var errorInfo: (Throwable?) -> String = { exception ->
        getDefaultErrorInfo(exception)
    }

    //提示信息
    var hint: Hint = Hint()


    class Hint{
        var loading: String = "加载中"
    }

}

fun Config.errorInfo(errorInfo: (Throwable?) -> String) {
    defaultConfig.errorInfo = {
        val set = errorInfo.invoke(it)
        set.ifEmpty {
            getDefaultErrorInfo(it)
        }
    }
}


fun Config.hint(hint: Config.Hint.() -> Unit) {
    defaultConfig.hint.apply(hint)
}


fun Config(init: Config.() -> Unit) {
    defaultConfig.apply(init)
}

