package cn.veako.android.core.util

import android.widget.Toast
import androidx.annotation.StringRes
import cn.veako.android.core.base.BaseApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 吐司提示类，可设置位置，偏移量，默认是系统自带的位置和偏移量。
 * 可设置自定义布局和消息id。
 */
@OptIn(DelicateCoroutinesApi::class)
object ToastUtil {

    /**
     * 系统自带的短消息提示
     *
     * @param stringResID 消息内容
     */
    fun showShortToast(
            @StringRes
            stringResID: Int) {
        showToast(stringResID, Toast.LENGTH_SHORT)
    }

    /**
     * 系统自带的短消息提示
     *
     * @param msg 消息内容
     */
    fun showShortToast(msg: String) {
        showToast(msg, Toast.LENGTH_SHORT)
    }

    /**
     * 系统自带的长消息提示
     *
     * @param stringResID 消息内容
     */
    fun showLongToast(
            @StringRes
            stringResID: Int) {
        showToast(stringResID, Toast.LENGTH_LONG)
    }

    /**
     * 系统自带的长消息提示
     *
     * @param msg 消息内容
     */
    fun showLongToast(msg: String) {
        showToast(msg, Toast.LENGTH_LONG)
    }

    private fun showToast(
        @StringRes
        stringResID: Int, duration: Int
    ) {
        showToast(BaseApp.getInstance().getString(stringResID), duration)
    }

    private fun showToast(msg: String, duration: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            val toast = Toast.makeText(BaseApp.getInstance(), msg, duration)
            toast.show()
        }
    }
}
