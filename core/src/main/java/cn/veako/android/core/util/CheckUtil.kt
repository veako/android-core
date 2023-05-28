package cn.veako.android.core.util

import cn.veako.android.core.R
import cn.veako.android.core.base.BaseApp

internal object CheckUtil {
    fun checkStartAndFinishEvent(event: Any?) {
        if (event == null) {
            throw RuntimeException(
                BaseApp.getInstance().getString(R.string.tip_start_activity_finish)
            )
        }
    }

    fun checkStartForResultEvent(event: Any?) {
        if (event == null) {
            throw RuntimeException(
                BaseApp.getInstance().getString(R.string.start_activity_for_result_tips)
            )
        }
    }

    fun checkLoadSirEvent(event: Any?) {
        if (event == null) {
            throw RuntimeException(BaseApp.getInstance().getString(R.string.load_sir_tips))
        }
    }

    fun checkLoadingDialogEvent(event: Any?) {
        if (event == null) {
            throw RuntimeException(BaseApp.getInstance().getString(R.string.loadingDialogTips))
        }
    }
}