package cn.veako.android.core.mvvm.vm

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.collection.ArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import cn.veako.android.core.R
import cn.veako.android.core.base.RepositoryManager
import cn.veako.android.core.bus.LiveDataBus
import cn.veako.android.core.bus.SingleLiveEvent
import cn.veako.android.core.http.HttpHandler
import cn.veako.android.core.mvvm.i.IActivityResult
import cn.veako.android.core.mvvm.i.IArgumentsFromBundle
import cn.veako.android.core.mvvm.i.IArgumentsFromIntent
import cn.veako.android.core.mvvm.i.IBaseResponse
import cn.veako.android.core.mvvm.i.IViewModel
import cn.veako.android.core.util.CheckUtil
import cn.veako.android.core.util.Utils
import cn.veako.android.core.util.isInUIThread
import com.kingja.loadsir.callback.Callback
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Call
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

open class BaseViewModel<M : BaseModel>(app: Application) : AndroidViewModel(app), IViewModel,
    IActivityResult, IArgumentsFromBundle, IArgumentsFromIntent {

    constructor(app: Application, model: M) : this(app) {
        isAutoCreateRepo = false
        mModel = model
    }

    /**
     * 可能存在没有仓库的 vm，但我们这里也不要是可 null 的。
     * 如果 vm 没有提供仓库，说明此变量不可用，还去使用的话自然就报错。
     */
    lateinit var mModel: M

    private lateinit var mCompositeDisposable: Any
    private lateinit var mCallList: MutableList<Call<*>>

    internal val mUiChangeLiveData by lazy { UiChangeLiveData() }

    internal var mBundle: Bundle? = null
    internal var mIntent: Intent? = null

    /**
     * 是否自动创建仓库，默认是 true，
     */
    private var isAutoCreateRepo = true

    /**
     * 是否缓存自动创建的仓库，默认是 true
     */
    protected open fun isCacheRepo() = true

    /**
     * 所有网络请求都在 mCoroutineScope 域中启动协程，当页面销毁时会自动取消
     */
    fun <T> launch(
        block: suspend CoroutineScope.() -> IBaseResponse<T?>?,
        onSuccess: (() -> Unit)? = null,
        onResult: ((t: T) -> Unit)? = null,
        onFailed: ((code: Int, msg: String?) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        ) {
        viewModelScope.launch {
            try {
                HttpHandler.handleResult(block(), onSuccess, onResult, onFailed)
            } catch (e: Exception) {
                HttpHandler.handleException(e, onFailed)
            } finally {
                onComplete?.invoke()
            }
        }
    }

    /**  async 函数则是更进一步，用于异步执行耗时任务，
     * 并且需要返回值（如网络请求、数据库读写、文件读写），
     * 在执行完毕通过 await() 函数获取返回值。
     */
    fun <T> async(
        block: suspend () -> T,
    ): Deferred<T> {
        return viewModelScope.async {
            block.invoke()
        }
    }

    /**
     * 发起协程，让协程和 UI 相关
     */
    fun launchUI(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch { block() }
    }

    /**
     * 发起流
     */
    fun <T> launchFlow(block: suspend () -> T): Flow<T> {
        return flow {
            emit(block())
        }
    }

    @CallSuper
    override fun onCreate(owner: LifecycleOwner) {
        if (isAutoCreateRepo) {
            if (!this::mModel.isInitialized) {
                val modelClass: Class<M>?
                val type: Type? = javaClass.genericSuperclass
                modelClass = if (type is ParameterizedType) {
                    @Suppress("UNCHECKED_CAST")
                    type.actualTypeArguments[0] as? Class<M>
                } else null
                if (modelClass != null && modelClass != BaseModel::class.java) {
                    mModel = RepositoryManager.getRepo(modelClass, isCacheRepo())
                }
            }
        }
    }

    @CallSuper  //重写时候必须调用此方法
    override fun onCleared() {
        // 可能 mModel 是未初始化的
        if (this::mModel.isInitialized) {
            mModel.onCleared()
        }

        LiveDataBus.removeObserve(this)
        LiveDataBus.removeStickyObserver(this)
        cancelConsumingTask()
    }

    /**
     * 取消耗时任务，比如在界面销毁时，或者在对话框消失时
     */
    open fun cancelConsumingTask() {
        // ViewModel销毁时会执行，同时取消所有异步任务
        if (this::mCallList.isInitialized) {
            mCallList.forEach { it.cancel() }
            mCallList.clear()
        }
        viewModelScope.cancel()
    }


    /**
     * 不使用 Rx，使用 Retrofit 原生的请求方式
     */
    fun addCall(call: Any) {
        if (!this::mCallList.isInitialized) {
            mCallList = mutableListOf()
        }
        mCallList.add(call as Call<*>)
    }

    // 以下是加载中对话框相关的 =========================================================

    fun showLoadingDialog() {
        showLoadingDialog(getApplication<Application>().getString(R.string.tip_loading))
    }

    fun showLoadingDialog(msg: String) {
        CheckUtil.checkLoadingDialogEvent(mUiChangeLiveData.showLoadingDialogEvent)
        if (isInUIThread()) {
            mUiChangeLiveData.showLoadingDialogEvent?.value = msg
        } else {
            mUiChangeLiveData.showLoadingDialogEvent?.postValue(msg)
        }
    }

    fun dismissLoadingDialog() {
        CheckUtil.checkLoadingDialogEvent(mUiChangeLiveData.dismissLoadingDialogEvent)
        if (isInUIThread()) {
            mUiChangeLiveData.dismissLoadingDialogEvent?.call()
        } else {
            mUiChangeLiveData.dismissLoadingDialogEvent?.postValue(null)
        }
    }

    // 以下是内嵌加载中布局相关的 =========================================================

    fun showLoadSirSuccess() {
        CheckUtil.checkLoadSirEvent(mUiChangeLiveData.loadSirEvent)
        if (isInUIThread()) {
            mUiChangeLiveData.loadSirEvent?.value = null
        } else {
            mUiChangeLiveData.loadSirEvent?.postValue(null)
        }
    }

    fun showLoadSir(clz: Class<out Callback>) {
        CheckUtil.checkLoadSirEvent(mUiChangeLiveData.loadSirEvent)
        if (isInUIThread()) {
            mUiChangeLiveData.loadSirEvent?.value = clz
        } else {
            mUiChangeLiveData.loadSirEvent?.postValue(clz)
        }
    }

    // 以下是界面开启和结束相关的 =========================================================

    @MainThread
    fun setResult(
        resultCode: Int,
        map: ArrayMap<String, *>? = null,
        bundle: Bundle? = null,
    ) {
        setResult(resultCode, Utils.getIntentByMapOrBundle(map = map, bundle = bundle))
    }

    @MainThread
    fun setResult(resultCode: Int, data: Intent? = null) {
        CheckUtil.checkStartAndFinishEvent(mUiChangeLiveData.setResultEvent)
        LiveDataBus.send(mUiChangeLiveData.setResultEvent, Pair(resultCode, data))
    }

    @MainThread
    fun finish(
        resultCode: Int? = null,
        map: ArrayMap<String, *>? = null,
        bundle: Bundle? = null,
    ) {
        finish(resultCode, Utils.getIntentByMapOrBundle(map = map, bundle = bundle))
    }

    @MainThread
    fun finish(resultCode: Int? = null, data: Intent? = null) {
        CheckUtil.checkStartAndFinishEvent(mUiChangeLiveData.finishEvent)
        LiveDataBus.send(mUiChangeLiveData.finishEvent, Pair(resultCode, data))
    }

    fun startActivity(clazz: Class<out Activity>) {
        CheckUtil.checkStartAndFinishEvent(mUiChangeLiveData.startActivityEvent)
        LiveDataBus.send(mUiChangeLiveData.startActivityEvent, clazz)
    }

    fun startActivity(clazz: Class<out Activity>, map: ArrayMap<String, *>) {
        CheckUtil.checkStartAndFinishEvent(mUiChangeLiveData.startActivityWithMapEvent)
        LiveDataBus.send(mUiChangeLiveData.startActivityWithMapEvent, Pair(clazz, map))
    }

    fun startActivity(clazz: Class<out Activity>, bundle: Bundle?) {
        CheckUtil.checkStartAndFinishEvent(mUiChangeLiveData.startActivityEventWithBundle)
        LiveDataBus.send(mUiChangeLiveData.startActivityEventWithBundle, Pair(clazz, bundle))
    }

    fun startActivityForResult(clazz: Class<out Activity>) {
        CheckUtil.checkStartForResultEvent(mUiChangeLiveData.startActivityForResultEvent)
        LiveDataBus.send(mUiChangeLiveData.startActivityForResultEvent, clazz)
    }

    fun startActivityForResult(clazz: Class<out Activity>, bundle: Bundle?) {
        CheckUtil.checkStartForResultEvent(mUiChangeLiveData.startActivityForResultEventWithBundle)
        LiveDataBus.send(mUiChangeLiveData.startActivityForResultEventWithBundle, Pair(clazz, bundle))
    }

    fun startActivityForResult(clazz: Class<out Activity>, map: ArrayMap<String, *>) {
        CheckUtil.checkStartForResultEvent(mUiChangeLiveData.startActivityForResultEventWithMap)
        LiveDataBus.send(mUiChangeLiveData.startActivityForResultEventWithMap, Pair(clazz, map))
    }


    // ===================================================================================

    /**
     * 通用的 Ui 改变变量
     */
    class UiChangeLiveData {
        var showLoadingDialogEvent: SingleLiveEvent<String>? = null
        var dismissLoadingDialogEvent: SingleLiveEvent<Any>? = null

        var startActivityEvent: String? = null
        var startActivityWithMapEvent: String? = null
        var startActivityEventWithBundle: String? = null

        var startActivityForResultEvent: String? = null
        var startActivityForResultEventWithMap: String? = null
        var startActivityForResultEventWithBundle: String? = null

        var finishEvent: String? = null
        var setResultEvent: String? = null

        var loadSirEvent: SingleLiveEvent<Class<out Callback>?>? = null

        fun initLoadSirEvent() {
            loadSirEvent = SingleLiveEvent()
        }

        fun initLoadingDialogEvent() {
            showLoadingDialogEvent = SingleLiveEvent()
            dismissLoadingDialogEvent = SingleLiveEvent()
        }

        fun initStartActivityForResultEvent() {
            startActivityForResultEvent = UUID.randomUUID().toString()
            startActivityForResultEventWithMap = UUID.randomUUID().toString()
            startActivityForResultEventWithBundle = UUID.randomUUID().toString()
        }

        fun initStartAndFinishEvent() {
            startActivityEvent = UUID.randomUUID().toString()
            startActivityWithMapEvent = UUID.randomUUID().toString()
            startActivityEventWithBundle = UUID.randomUUID().toString()
            finishEvent = UUID.randomUUID().toString()
            setResultEvent = UUID.randomUUID().toString()
        }
    }
    override fun getBundle(): Bundle? = mBundle

    override fun getArgumentsIntent(): Intent? = mIntent
}