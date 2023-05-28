package cn.veako.android.core.binding.viewadapter.image

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Base64
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import cn.veako.android.core.base.GlobalConfig
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@SuppressLint("CheckResult")
@BindingAdapter(value = ["url", "base64", "placeholderRes", "errorRes"], requireAll = false)
fun setImageUri(
    imageView: ImageView,
    url: String?,
    base64: String?,        //base64，目前主要用法是显示验证码图片
    placeholder: Drawable?,
    error: Drawable?
) {
    if (!TextUtils.isEmpty(url)) {
        //使用Glide框架加载图片
        val request = Glide.with(imageView.context)
            .load(url)
        val options = RequestOptions()

        if (placeholder != null) {
            options.placeholder(placeholder)
        } else {
            val placeholderRes = GlobalConfig.ImageView.placeholderRes
            placeholderRes?.let { options.placeholder(placeholderRes) }
        }
        if (error != null) {
            options.error(placeholder)
        } else {
            val errorRes = GlobalConfig.ImageView.errorRes
            errorRes?.let { options.error(errorRes) }
        }
        request.apply(options).into(imageView)
    } else if (!TextUtils.isEmpty(base64)) {
        val bytes: ByteArray = Base64.decode(base64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        imageView.setImageBitmap(bitmap)
    } else {
        imageView.setImageResource(0)
    }
}