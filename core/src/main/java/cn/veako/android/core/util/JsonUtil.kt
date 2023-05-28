package cn.veako.android.core.util

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object JsonUtil {
    private const val TAG = "Utils"

    private val GSON = Gson()

    fun getGson(): Gson {
        return GSON
    }

    fun <T> toJson(obj: T): String {
        return GSON.toJson(obj)
    }

    /**
     * 取得Json解析的结果
     *
     * @param jsonString Json格式的字符串
     * @param clazz     相应的JavaBean类
     * @return 返回相应的JavaBean类实例，解析失败，返回null
     */
    fun <T> parseJson(jsonString: String, clazz: Class<T>): T? {
        var t: T? = null
        try {
            t = GSON.fromJson(jsonString, clazz) as T
        } catch (e: Exception) {
            LogUtil.e(TAG, "###JSON解析出错，类名：$clazz.name ###，字符串是：$jsonString")
        }
        return t
    }

    /**
     * 取得Json解析的结果
     *
     * @param jsonString Json格式的字符串
     * @param type     相应的TypeToken
     * @return 返回相应的JavaBean类实例，解析失败，返回null
     */
    fun <T> parseJson(jsonString: String, type: TypeToken<T>): T? {
        var t: T? = null
        try {
            t = GSON.fromJson(jsonString, type) as T
        } catch (e: Exception) {
            LogUtil.e(TAG, "###JSON解析出错，type：${type.type} ###，字符串是：$jsonString")
        }
        return t
    }

    /**
     * 取得Json解析的结果
     *
     * @param jsonString Json格式的字符串
     * @param type     相应的Type
     * @return 返回相应的JavaBean类实例，解析失败，返回null
     */
    fun <T> parseJson(jsonString: String, type: Type): T? {
        var t: T? = null
        try {
            t = GSON.fromJson(jsonString, type) as T
        } catch (e: Exception) {
            LogUtil.e(TAG, "###JSON解析出错，type：${type.typeName} ###，字符串是：$jsonString")
        }
        return t
    }

    /**
     * @return JSON数组解析
     */
    fun <T> parseJsonArray(jsonString: String, clazz: Class<T>): List<T> {
        val t: ArrayList<T> = ArrayList()
        try {
            val jsonArray = JsonParser.parseString(jsonString).asJsonArray
            for (jsonElement in jsonArray) {
                t.add(GSON.fromJson(jsonElement, clazz))
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "###JSON数组解析出错，类名：$clazz.name ###，字符串是：$jsonString")
        }
        return t
    }
}
