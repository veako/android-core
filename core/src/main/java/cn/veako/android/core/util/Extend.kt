package cn.veako.android.core.util

import android.os.Looper

fun Any.isInUIThread() = Looper.getMainLooper().thread == Thread.currentThread()