package cn.veako.android.core

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.veako.android.core.util.LogUtil

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LogUtil.e("asfvxzc")
    }
}