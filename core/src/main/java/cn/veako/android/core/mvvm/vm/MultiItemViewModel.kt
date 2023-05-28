package cn.veako.android.core.mvvm.vm

/**
 * 列表多布局的 item 数据基类，[mItemType] 表示布局的类型，
 */
open class MultiItemViewModel<VM : BaseViewModel<out BaseModel>>(
    viewModel: VM,
    var mItemType: Any? = null,
    position: Int? = null
) :
    ItemViewModel<VM>(viewModel, position) {
    init {
        mItemType = javaClass
    }
}