package cn.veako.android.core.http

import android.annotation.SuppressLint
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * @Author ikun
 * @Date 2023-02-22 20:05
 * 帮我写代码，我教你打篮球
 * @Description:
 */
class SSLFactory private constructor() {
    val trustManager: X509TrustManager = createTrustManager()
    val sslSocketFactory: SSLSocketFactory = createSSLSocketFactory()
    val hostnameVerifier: HostnameVerifier = HostnameVerifier { _, _ -> true }


    // 内部类单例
    private object Holder {
        val INSTANCE = SSLFactory()
    }

    companion object {
        val instance = Holder.INSTANCE
    }

    private fun createTrustManager(): X509TrustManager =
        @SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
        }

    private fun createSSLSocketFactory(): SSLSocketFactory {
        lateinit var sslFactory: SSLSocketFactory
        try {
            val sc: SSLContext = SSLContext.getInstance("TLS")
            sc.init(null, arrayOf(createTrustManager()), SecureRandom())
            sslFactory = sc.socketFactory;
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sslFactory
    }
}