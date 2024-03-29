package cn.veako.android.core.mvvm.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import cn.veako.android.core.mvvm.i.IAppBar
import cn.veako.android.core.mvvm.vm.BaseModel
import cn.veako.android.core.mvvm.vm.BaseViewModel

abstract class AppBarViewBindingBaseActivity<V : ViewBinding, VM : BaseViewModel<out BaseModel>,
        AppBarV : ViewBinding> : ViewBindingBaseActivity<V, VM>() {

    protected lateinit var mAppBarBinding: AppBarV
    override fun initContentView(contentView: View) {
        mAppBarBinding = initAppBarBinding(layoutInflater, null)
        super.initContentView(IAppBar.generateRootLayout(this, mAppBarBinding, contentView))
    }

    abstract fun initAppBarBinding(inflater: LayoutInflater, container: ViewGroup?): AppBarV
}