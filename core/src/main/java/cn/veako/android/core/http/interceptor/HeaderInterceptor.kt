package cn.veako.android.core.http.interceptor

import androidx.collection.ArrayMap
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(private val headers: ArrayMap<String, String>) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        if (headers.isNotEmpty()) {
            for ((key, value) in headers) {
                builder.addHeader(key, value)
            }
        }
        //请求信息
        return chain.proceed(builder.build())
    }
}