package cn.veako.android.core.mvvm.vm

import cn.veako.android.core.mvvm.i.IModel

/**
 * Model 层的基类
 */
abstract class BaseModel : IModel {
    override fun onCleared() {}
}