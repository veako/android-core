package cn.veako.android.core.base

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import cn.veako.android.core.R

/**
 * App 状态监听器，可用于判断应用是在后台还是在前台
 */
object AppStateTracker {
    private var mIsTract = false
    private var mChangeListener: MutableList<AppStateChangeListener> = mutableListOf()
    const val STATE_FOREGROUND = 0
    const val STATE_BACKGROUND = 1
    var currentState = STATE_BACKGROUND
        get() {
            if (!mIsTract) {
                throw RuntimeException(
                    BaseApp.getInstance().getString(R.string.appStateTracker_msg)
                )
            }
            return field
        }

    fun track(appStateChangeListener: AppStateChangeListener) {
        if (!mIsTract) {
            mIsTract = true
            ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleChecker())
        }
        mChangeListener.add(appStateChangeListener)
    }

    interface AppStateChangeListener {
        fun appTurnIntoForeground()
        fun appTurnIntoBackground()
    }

    class LifecycleChecker : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            currentState = STATE_FOREGROUND
            mChangeListener.forEach {
                it.appTurnIntoForeground()
            }
        }

        override fun onPause(owner: LifecycleOwner) {
            currentState = STATE_BACKGROUND
            mChangeListener.forEach {
                it.appTurnIntoBackground()
            }
        }
    }
}