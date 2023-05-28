package cn.veako.android.core.http

import cn.veako.android.core.mvvm.i.IBaseResponse
import cn.veako.android.core.util.LogUtil
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

object HttpHandler {
    /**
     * 处理请求结果
     *
     * [entity] 实体
     * [onSuccess] 状态码对了就回调
     * [onResult] 状态码对了，且实体不是 null 才回调
     * [onFailed] 有错误发生，可能是服务端错误，可能是数据错误，详见 code 错误码和 msg 错误信息
     */
    fun <T> handleResult(
        entity: IBaseResponse<T?>?,
        onSuccess: (() -> Unit)? = null,
        onResult: ((t: T) -> Unit)? = null,
        onFailed: ((code: Int, msg: String?) -> Unit)? = null
    ) {
        if (entity == null) {
            onFailed?.invoke(entityNullable, msgEntityNullable)
            if (LogUtil.isLog()) LogUtil.i("HttpResult", msgEntityNullable)
            return
        }
        val code = entity.code()
        val msg = entity.msg()
        val data = entity.data()
        // 请求成功
        if (entity.isSuccess()) {
            onSuccess?.invoke()
            if (data != null) {
                onResult?.invoke(data)
            }
        } else {
            onFailed?.invoke(code, msg)
        }
    }

    /**
     * 处理异常
     */
    fun handleException(
        e: Exception,
        onFailed: ((code: Int, msg: String?) -> Unit)? = null
    ) {
        if (LogUtil.isLog()) {
            LogUtil.i("HttpHandleException", e)
        }
        when (e) {
            is SocketTimeoutException -> {
                onFailed?.invoke(
                    socketTimeoutException,
                    msgSocketTimeoutException
                )
            }

            is HttpException -> {
                onFailed?.invoke(notHttpException, msgNotHttpException)
            }

            else -> {
                onFailed?.invoke(notHttpException, e.message)
            }
        }
    }
}