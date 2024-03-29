package cn.veako.android.core.mvvm.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import cn.veako.android.core.base.GlobalConfig
import cn.veako.android.core.mvvm.i.IAppBar
import cn.veako.android.core.mvvm.i.IAppBarProcessor
import cn.veako.android.core.mvvm.vm.AppBarBaseViewModel
import cn.veako.android.core.mvvm.vm.BaseModel
import cn.veako.android.core.util.Utils

abstract class AppBarDataBindingBaseFragment<V : ViewDataBinding, VM : AppBarBaseViewModel<out BaseModel, out IAppBarProcessor>,
        AppBarV : ViewDataBinding, AppBarP : IAppBarProcessor>(
    @LayoutRes layoutId: Int,
    varViewModelId: Int? = null,
    private val varAppBarProcessorId: Int, // 既然使用了 dataBinding，那么必须有个处理者去关联 xml
    @LayoutRes private val appBarLayoutId: Int = GlobalConfig.AppBar.gAppBarLayoutId, // 可以全局设置，也可以单独设置
    sharedViewModel: Boolean = false
) : DataBindingBaseFragment<V, VM>(layoutId, varViewModelId, sharedViewModel), IAppBar<AppBarP> {

    protected lateinit var mAppBarBinding: AppBarV
    protected lateinit var mAppBarProcessor: AppBarP


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = initBinding(inflater, container)

        val pair: Pair<AppBarV, LinearLayout> =
            IAppBar.inflateRootLayout(requireActivity(), mBinding.root, appBarLayoutId)
        mAppBarBinding = pair.first
        return pair.second
    }

    override fun initViewModel() {
        super.initViewModel()
        // 让 vm 层也可以访问到标题栏处理者
        mAppBarProcessor = initAppBarProcessor()
        mAppBarBinding.setVariable(varAppBarProcessorId, mAppBarProcessor)
        mAppBarBinding.lifecycleOwner = this

        mViewModel.setProcessor(mAppBarProcessor)
    }

    override fun onDestroyView() {
        mAppBarBinding.unbind()

        super.onDestroyView()

        Utils.releaseBinding(this.javaClass, AppBarDataBindingBaseFragment::class.java, this, "mAppBarBinding")
    }
}