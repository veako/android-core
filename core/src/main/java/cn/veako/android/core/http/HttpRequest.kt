package cn.veako.android.core.http

import android.widget.*
import androidx.collection.ArrayMap
import cn.veako.android.core.http.interceptor.HeaderInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 *
 * 目的1：没网的时候，尝试读取缓存，避免界面空白，只需要addInterceptor和cache即可（已实现）
 * 目的2：有网的时候，总是读取网络上最新的，或者设置一定的超时时间，比如10秒内有多个同一请求，则都从缓存中获取（正在开发）
 * 目的3：不同的接口，不同的缓存策略（？）
 */
object HttpRequest {

    // 缓存 service
    private val mServiceMap = ArrayMap<String, Any>()

    // 默认的 baseUrl
    lateinit var mDefaultBaseUrl: String

    // 默认的请求头
    private lateinit var mDefaultHeader: ArrayMap<String, String>

    /**
     * 请求超时时间，秒为单位
     */
    private var mDefaultTimeout = 60

    /**
     * 添加默认的请求头
     */
    @JvmStatic
    fun addDefaultHeader(name: String, value: String) {
        if (!this::mDefaultHeader.isInitialized) {
            mDefaultHeader = ArrayMap()
        }
        mDefaultHeader[name] = value
    }


    /**
     * 若有不同的服务器或接口，根据host分类放在同一个Server可调用此方法生成不同的apiServer
     */
    @JvmStatic
    fun <T> getService(cls: Class<T>, host: String, vararg interceptors: Interceptor?): T {
        val name = cls.name
        var obj: Any? = mServiceMap[name]
        if (obj == null) {
            val httpClientBuilder = OkHttpClient.Builder()
            //https
            val mSSLFactory = SSLFactory.instance
            httpClientBuilder
                // 超时时间
                .readTimeout(mDefaultTimeout.toLong(), TimeUnit.SECONDS)
                .connectTimeout(mDefaultTimeout.toLong(), TimeUnit.SECONDS)
                .writeTimeout(mDefaultTimeout.toLong(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .followRedirects(false)
                .sslSocketFactory(mSSLFactory.sslSocketFactory, mSSLFactory.trustManager)
                .hostnameVerifier(mSSLFactory.hostnameVerifier)

            // 添加拦截器
            interceptors.forEach { interceptor ->
                interceptor?.let {
                    httpClientBuilder.addInterceptor(it)
                }
            }

            // Default HeaderInterceptor
            if (this::mDefaultHeader.isInitialized) {
                httpClientBuilder.addInterceptor(HeaderInterceptor(mDefaultHeader))
            }

            val client = httpClientBuilder.build()
            val builder = Retrofit.Builder()
                .client(client)
                // 基础url
                .baseUrl(host)
                // JSON解析
                .addConverterFactory(GsonConverterFactory.create())
            obj = builder.build().create(cls)
            mServiceMap[name] = obj
        }
        @Suppress("UNCHECKED_CAST")
        return obj as T
    }

    /**
     * 设置了 [mDefaultBaseUrl] 后，可通过此方法获取 Service
     */
    @JvmStatic
    fun <T> getService(cls: Class<T>, vararg interceptors: Interceptor?): T {
        if (!this::mDefaultBaseUrl.isInitialized) {
            throw RuntimeException("必须初始化 mBaseUrl")
        }
        return getService(cls, host = mDefaultBaseUrl, interceptors = interceptors)
    }

    /**
     * 同步的请求，当一个界面需要调用多个接口才能呈现出来时，可以在子线程中或者Observable.zip操作多个接口
     */
    @JvmStatic
    fun <T> execute(call: Call<T>): T? {
        try {
            return call.execute().body()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }
}
