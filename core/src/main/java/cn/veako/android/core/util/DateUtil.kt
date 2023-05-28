package cn.veako.android.core.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*
import kotlin.math.abs


object DateUtil {
    private val locale: Locale = Locale.CHINA
    var YYYY = "yyyy"
    var YYYY_MM = "yyyy-MM"
    var HH_MM_SS = "HH:mm:ss"
    val parsePatterns = arrayOf(
        "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
        "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
        "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM",
        "yyyy-MM-dd HH-mm-ss"
    )

    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    val nowDate: Date
        get() = Date()

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    val date: String
        get() = dateTimeNow(parsePatterns[0])
    val time: String
        get() = dateTimeNow(parsePatterns[1])

    @JvmOverloads
    fun dateTimeNow(format: String = parsePatterns[1]): String {
        return parseDateToStr(format, Date())
    }

    fun dateTime(date: Date): String {
        return parseDateToStr(parsePatterns[0], date)
    }

    fun parseDateToStr(format: String, date: Date): String {
        return SimpleDateFormat(format, locale).format(date)
    }

    fun dateTime(format: String, ts: String): Date {
        return try {
            SimpleDateFormat(format, locale).parse(ts) as Date
        } catch (e: ParseException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 计算相差天数
     */
    fun differentDaysByMillisecond(date1: Date, date2: Date): Int {
        return abs(((date2.time - date1.time) / (1000 * 3600 * 24)).toInt())
    }

    /**
     * 计算时间差
     *
     * @param endTime   最后时间
     * @param startTime 开始时间
     * @return 时间差（天/小时/分钟）
     */
    fun timeDistance(endTime: Date, startTime: Date): String {
        val nd = (1000 * 24 * 60 * 60).toLong()
        val nh = (1000 * 60 * 60).toLong()
        val nm = (1000 * 60).toLong()
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        val diff = endTime.time - startTime.time
        // 计算差多少天
        val day = diff / nd
        // 计算差多少小时
        val hour = diff % nd / nh
        // 计算差多少分钟
        val min = diff % nd % nh / nm
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day.toString() + "天" + hour + "小时" + min + "分钟"
    }

    /**
     * 增加 LocalDateTime ==> Date
     */
    fun toDate(temporalAccessor: LocalDateTime): Date {
        val zdt = temporalAccessor.atZone(ZoneId.systemDefault())
        return Date.from(zdt.toInstant())
    }

    /**
     * 增加 LocalDate ==> Date
     */
    fun toDate(temporalAccessor: LocalDate?): Date {
        val localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0))
        val zdt = localDateTime.atZone(ZoneId.systemDefault())
        return Date.from(zdt.toInstant())
    }
}