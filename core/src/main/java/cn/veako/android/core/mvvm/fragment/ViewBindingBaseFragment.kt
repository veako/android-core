package cn.veako.android.core.mvvm.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import cn.veako.android.core.bus.LiveDataBus
import cn.veako.android.core.mvvm.i.IActivityResult
import cn.veako.android.core.mvvm.i.ILoading
import cn.veako.android.core.mvvm.i.ILoadingDialog
import cn.veako.android.core.mvvm.i.IView
import cn.veako.android.core.mvvm.vm.BaseModel
import cn.veako.android.core.mvvm.vm.BaseViewModel
import cn.veako.android.core.util.Utils
import cn.veako.android.core.widget.CustomLayoutDialog
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir

abstract class ViewBindingBaseFragment<V : ViewBinding, VM : BaseViewModel<out BaseModel>>(
    private val sharedViewModel: Boolean = false
) : Fragment(),
    IView<V, VM>, ILoadingDialog, ILoading, IActivityResult {

    protected lateinit var mBinding: V
    protected lateinit var mViewModel: VM

    private lateinit var mStartActivityForResult: ActivityResultLauncher<Intent>

    private val mLoadingDialog by lazy(mode = LazyThreadSafetyMode.NONE) { CustomLayoutDialog(requireActivity(), loadingDialogLayout()) }

    private lateinit var mLoadService: LoadService<*>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = initBinding(inflater, container)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initParam()
        initUiChangeLiveData()
        initViewObservable()
        initLoadSir()
        initData()
    }

    @CallSuper
    override fun initViewModel() {
        mViewModel = if (sharedViewModel) {
            initViewModel(requireActivity())
        } else {
            initViewModel(this)
        }
        // 让 vm 可以感知 v 的生命周期
        lifecycle.addObserver(mViewModel)
    }

    @CallSuper
    override fun initUiChangeLiveData() {
        if (isViewModelNeedStartAndFinish()) {
            mViewModel.mUiChangeLiveData.initStartAndFinishEvent()

            // vm 可以结束界面
            LiveDataBus.observe(
                this,
                mViewModel.mUiChangeLiveData.finishEvent!!,
                Observer { activity?.finish() },
                true
            )
            // vm 可以启动界面
            LiveDataBus.observe<Class<out Activity>>(
                this,
                mViewModel.mUiChangeLiveData.startActivityEvent!!,
                {
                    startActivity(it)
                },
                true
            )
            LiveDataBus.observe<Pair<Class<out Activity>, ArrayMap<String, *>>>(this,
                mViewModel.mUiChangeLiveData.startActivityWithMapEvent!!,
                {
                    startActivity(it?.first, it?.second)
                },
                true
            )
            // vm 可以启动界面，并携带 Bundle，接收方可调用 getBundle 获取
            LiveDataBus.observe<Pair<Class<out Activity>, Bundle?>>(this,
                mViewModel.mUiChangeLiveData.startActivityEventWithBundle!!,
                {
                    startActivity(it?.first, bundle = it?.second)
                },
                true
            )
        }
        if (isViewModelNeedStartForResult()) {
            mViewModel.mUiChangeLiveData.initStartActivityForResultEvent()

            // vm 可以启动界面
            LiveDataBus.observe<Class<out Activity>>(
                this,
                mViewModel.mUiChangeLiveData.startActivityForResultEvent!!,
                {
                    startActivityForResult(it)
                },
                true
            )
            // vm 可以启动界面，并携带 Bundle，接收方可调用 getBundle 获取
            LiveDataBus.observe<Pair<Class<out Activity>, Bundle?>>(
                this,
                mViewModel.mUiChangeLiveData.startActivityForResultEventWithBundle!!,
                {
                    startActivityForResult(it?.first, bundle = it?.second)
                },
                true
            )
            LiveDataBus.observe<Pair<Class<out Activity>, ArrayMap<String, *>>>(
                this,
                mViewModel.mUiChangeLiveData.startActivityForResultEventWithMap!!,
                {
                    startActivityForResult(it?.first, it?.second)
                },
                true
            )
        }

        if (isNeedLoadingDialog()) {
            mViewModel.mUiChangeLiveData.initLoadingDialogEvent()

            // 显示对话框
            mViewModel.mUiChangeLiveData.showLoadingDialogEvent?.observe(viewLifecycleOwner) {
                showLoadingDialog(mLoadingDialog, it)
            }
            // 隐藏对话框
            mViewModel.mUiChangeLiveData.dismissLoadingDialogEvent?.observe(viewLifecycleOwner) {
                dismissLoadingDialog(mLoadingDialog)
            }
        }
    }

    @CallSuper
    override fun initLoadSir() {
        // 只有目标不为空的情况才有实例化的必要
        if (getLoadSirTarget() != null) {
            mLoadService = LoadSir.getDefault().register(
                getLoadSirTarget()
            ) { onLoadSirReload() }

            mViewModel.mUiChangeLiveData.initLoadSirEvent()
            mViewModel.mUiChangeLiveData.loadSirEvent?.observe(viewLifecycleOwner) {
                if (it == null) {
                    mLoadService.showSuccess()
                    onLoadSirSuccess()
                } else {
                    mLoadService.showCallback(it)
                    onLoadSirShowed(it)
                }
            }
        }
    }

    fun startActivity(
        clz: Class<out Activity>?,
        map: ArrayMap<String, *>? = null,
        bundle: Bundle? = null
    ) {
        startActivity(Utils.getIntentByMapOrBundle(activity, clz, map, bundle))
    }

    fun startActivityForResult(
        clz: Class<out Activity>?,
        map: ArrayMap<String, *>? = null,
        bundle: Bundle? = null
    ) {
        initStartActivityForResult()
        mStartActivityForResult.launch(Utils.getIntentByMapOrBundle(activity, clz, map, bundle))
    }

    private fun initStartActivityForResult() {
        if (!this::mStartActivityForResult.isInitialized) {
            mStartActivityForResult =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    val data = it.data ?: Intent()
                    when (it.resultCode) {
                        Activity.RESULT_OK -> {
                            onActivityResultOk(data)
                            if (this::mViewModel.isInitialized) {
                                mViewModel.onActivityResultOk(data)
                            }
                        }
                        Activity.RESULT_CANCELED -> {
                            onActivityResultCanceled(data)
                            if (this::mViewModel.isInitialized) {
                                mViewModel.onActivityResultCanceled(data)
                            }
                        }
                        else -> {
                            onActivityResult(it.resultCode, data)
                            if (this::mViewModel.isInitialized) {
                                mViewModel.onActivityResult(it.resultCode, data)
                            }
                        }
                    }
                }
        }
    }

    /**
     * 通过 setArguments 传递 bundle，在这里可以获取
     */
    override fun getBundle(): Bundle? {
        return arguments
    }

    /**
     * <pre>
     *     // 一开始我们这么写
     *     mViewModel.liveData.observe(this, Observer { })
     *
     *     // 用这个方法可以这么写
     *     observe(mViewModel.liveData) { }
     *
     *     // 或者这么写
     *     observe(mViewModel.liveData, this::onChanged)
     *     private fun onChanged(s: String) { }
     * </pre>
     */
    fun <T> observe(liveData: LiveData<T>, onChanged: ((t: T) -> Unit)) =
        liveData.observe(this) { onChanged(it) }

    /**
     * 如果加载中对话框可手动取消，并且开启了取消耗时任务的功能，则在取消对话框后调用取消耗时任务
     */
    @CallSuper
    override fun onCancelLoadingDialog() = mViewModel.cancelConsumingTask()

    override fun onDestroyView() {
        super.onDestroyView()
        Utils.releaseBinding(this.javaClass, ViewBindingBaseFragment::class.java, this, "mBinding")
    }

    override fun onDestroy() {
        super.onDestroy()

        // 界面销毁时移除 vm 的生命周期感知
        if (this::mViewModel.isInitialized) {
            lifecycle.removeObserver(mViewModel)
        }
        removeLiveDataBus(this)
    }
}