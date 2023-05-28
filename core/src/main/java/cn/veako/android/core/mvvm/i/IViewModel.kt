package cn.veako.android.core.mvvm.i

import androidx.lifecycle.DefaultLifecycleObserver
import kotlinx.coroutines.CoroutineScope

/**
 * ViewModel 层，让 vm 可以感知 v 的生命周期
 */
interface IViewModel : DefaultLifecycleObserver