package cn.veako.android.core.util

import android.text.Html
import android.text.Spanned

object StringUtil {
    fun string2Html(string: String): Spanned {
        return Html.fromHtml(string, Html.FROM_HTML_MODE_COMPACT)
    }
}