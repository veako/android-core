package cn.veako.android.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import cn.veako.android.core.base.BaseApp

/**
 * 网络工具类
 */
object NetworkUtil {
    fun isConnected(): Boolean {
        val manager = BaseApp.getInstance()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork
        val info = manager.getNetworkCapabilities(network) ?: return false
        return when {
            info.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            info.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            info.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            info.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }
}
