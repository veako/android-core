package cn.veako.android.core.mvvm.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import cn.veako.android.core.mvvm.i.IAppBar
import cn.veako.android.core.mvvm.vm.BaseModel
import cn.veako.android.core.mvvm.vm.BaseViewModel
import cn.veako.android.core.util.Utils

abstract class AppBarViewBindingBaseFragment<V : ViewBinding, VM : BaseViewModel<out BaseModel>,
        AppBarV : ViewBinding> : ViewBindingBaseFragment<V, VM>() {

    protected lateinit var mAppBarBinding: AppBarV

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = initBinding(inflater, container)
        mAppBarBinding = initAppBarBinding(layoutInflater, null)
        return IAppBar.generateRootLayout(requireActivity(), mAppBarBinding, mBinding.root)
    }

    abstract fun initAppBarBinding(inflater: LayoutInflater, container: ViewGroup?): AppBarV

    override fun onDestroyView() {
        super.onDestroyView()

        Utils.releaseBinding(this.javaClass, AppBarViewBindingBaseFragment::class.java, this, "mAppBarBinding")
    }
}