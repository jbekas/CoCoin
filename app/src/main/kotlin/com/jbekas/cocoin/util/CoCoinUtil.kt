package com.jbekas.cocoin.util

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.daimajia.androidanimations.library.BaseViewAnimator
import com.jbekas.cocoin.BuildConfig
import com.jbekas.cocoin.R
import com.jbekas.cocoin.activity.CoCoinApplication
import com.jbekas.cocoin.db.DB
import com.jbekas.cocoin.db.DBHelper
import com.jbekas.cocoin.db.RecordManager
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.model.Tag
import com.nineoldandroids.animation.ObjectAnimator
import com.rengwuxian.materialedittext.MaterialEditText
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

// Todo floating labels in english
class CoCoinUtil(context: Context) {
    val appContext: Context

    @JvmField
    var editRecordPosition = -1
    var LOGO_PATH = "/sdcard/logo/"
    var LOGO_NAME = "logo.jpg"

    @JvmField
    var MY_BLUE = 0

    @JvmField
    var backupCoCoinRecord: CoCoinRecord? = null
    var PASSWORD = "1234"
    var WEEKDAY_SHORT_START_ON_MONDAY = intArrayOf(
        0,
        R.string.monday_short,
        R.string.tuesday_short,
        R.string.wednesday_short,
        R.string.thursday_short,
        R.string.friday_short,
        R.string.saturday_short,
        R.string.sunday_short
    )
    var WEEKDAY_SHORT_START_ON_SUNDAY = intArrayOf(
        0,
        R.string.sunday_short,
        R.string.monday_short,
        R.string.tuesday_short,
        R.string.wednesday_short,
        R.string.thursday_short,
        R.string.friday_short,
        R.string.saturday_short
    )
    var WEEKDAY_START_ON_MONDAY = intArrayOf(
        0,
        R.string.monday,
        R.string.tuesday,
        R.string.wednesday,
        R.string.thursday,
        R.string.friday,
        R.string.saturday,
        R.string.sunday
    )
    var WEEKDAY_START_ON_SUNDAY = intArrayOf(
        0,
        R.string.sunday,
        R.string.monday,
        R.string.tuesday,
        R.string.wednesday,
        R.string.thursday,
        R.string.friday,
        R.string.saturday
    )

    @JvmField
    var FLOATINGLABELS = arrayOf(
        "",
        "",
        "十",
        "百",
        "千",
        "万",
        "十万",
        "百万",
        "千万",
        "亿",
        "十亿")

    @JvmField
    var BUTTONS = arrayOf(
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "D",
        "0",
        "Y"
    )
    var TODAY_VIEW_EMPTY_TIP = intArrayOf(
        R.string.today_empty,
        R.string.yesterday_empty,
        R.string.this_week_empty,
        R.string.last_week_empty,
        R.string.this_month_empty,
        R.string.last_month_empty,
        R.string.this_year_empty,
        R.string.last_year_empty
    )

    @JvmField
    var MONTHS_SHORT = intArrayOf(0,
        R.string.january_short,
        R.string.february_short,
        R.string.march_short,
        R.string.april_short,
        R.string.may_short,
        R.string.june_short,
        R.string.july_short,
        R.string.august_short,
        R.string.september_short,
        R.string.october_short,
        R.string.november_short,
        R.string.december_short
    )
    var MONTHS = intArrayOf(
        0,
        R.string.january,
        R.string.february,
        R.string.march,
        R.string.april,
        R.string.may,
        R.string.june,
        R.string.july,
        R.string.august,
        R.string.september,
        R.string.october,
        R.string.november,
        R.string.december
    )
    var TODAY_VIEW_TITLE = intArrayOf(
        R.string.today_view_today,
        R.string.today_view_yesterday,
        R.string.today_view_this_week,
        R.string.today_view_last_week,
        R.string.today_view_this_month,
        R.string.today_view_last_month,
        R.string.today_view_this_year,
        R.string.today_view_last_year
    )
    var TAG_ICON = intArrayOf(
        R.drawable.sum_pie_icon,
        R.drawable.sum_histogram_icon,
        R.drawable.meal_icon,
        R.drawable.closet_icon,
        R.drawable.home_icon,
        R.drawable.traffic_icon,
        R.drawable.vehicle_maintenance_icon,
        R.drawable.book_icon,
        R.drawable.hobby_icon,
        R.drawable.internet_icon,
        R.drawable.friend_icon,
        R.drawable.education_icon,
        R.drawable.entertainment_icon,
        R.drawable.medical_icon,
        R.drawable.insurance_icon,
        R.drawable.donation_icon,
        R.drawable.sport_icon,
        R.drawable.snack_icon,
        R.drawable.music_icon,
        R.drawable.fund_icon,
        R.drawable.drink_icon,
        R.drawable.fruit_icon,
        R.drawable.film_icon,
        R.drawable.baby_icon,
        R.drawable.partner_icon,
        R.drawable.housing_loan_icon,
        R.drawable.pet_icon,
        R.drawable.telephone_bill_icon,
        R.drawable.travel_icon,
        R.drawable.lunch_icon,
        R.drawable.breakfast_icon,
        R.drawable.midnight_snack_icon
    )
    var TAG_COLOR = intArrayOf(
        R.color.my_blue,
        R.color.sum_header_pie,
        R.color.sum_header_histogram,
        R.color.meal_header,
        R.color.closet_header,
        R.color.home_header,
        R.color.traffic_header,
        R.color.vehicle_maintenance_header,
        R.color.book_header,
        R.color.hobby_header,
        R.color.internet_header,
        R.color.friend_header,
        R.color.education_header,
        R.color.entertainment_header,
        R.color.medical_header,
        R.color.insurance_header,
        R.color.donation_header,
        R.color.sport_header,
        R.color.snack_header,
        R.color.music_header,
        R.color.fund_header,
        R.color.drink_header,
        R.color.fruit_header,
        R.color.film_header,
        R.color.baby_header,
        R.color.partner_header,
        R.color.housing_loan_header,
        R.color.pet_header,
        R.color.telephone_bill_header,
        R.color.travel_header,
        R.color.lunch_header,
        R.color.breakfast_header,
        R.color.midnight_snack_header
    )
    var TAG_SNACK = intArrayOf(
        R.drawable.snackbar_shape_undo,
        R.drawable.snackbar_shape_sum_pie,
        R.drawable.snackbar_shape_sum_histogram,
        R.drawable.snackbar_shape_meal,
        R.drawable.snackbar_shape_closet,
        R.drawable.snackbar_shape_home,
        R.drawable.snackbar_shape_traffic,
        R.drawable.snackbar_shape_vehicle_maintenance,
        R.drawable.snackbar_shape_book,
        R.drawable.snackbar_shape_hobby,
        R.drawable.snackbar_shape_internet,
        R.drawable.snackbar_shape_friend,
        R.drawable.snackbar_shape_education,
        R.drawable.snackbar_shape_entertainment,
        R.drawable.snackbar_shape_medical,
        R.drawable.snackbar_shape_insurance,
        R.drawable.snackbar_shape_donation,
        R.drawable.snackbar_shape_sport,
        R.drawable.snackbar_shape_snack,
        R.drawable.snackbar_shape_music,
        R.drawable.snackbar_shape_fund,
        R.drawable.snackbar_shape_drink,
        R.drawable.snackbar_shape_fruit,
        R.drawable.snackbar_shape_film,
        R.drawable.snackbar_shape_baby,
        R.drawable.snackbar_shape_partner,
        R.drawable.snackbar_shape_housing_loan,
        R.drawable.snackbar_shape_pet,
        R.drawable.snackbar_shape_telephone_bill,
        R.drawable.snackbar_shape_travel,
        R.drawable.snackbar_shape_lunch,
        R.drawable.snackbar_shape_breakfast,
        R.drawable.snackbar_shape_midnight_snack
    )
    var TAG_NAME = intArrayOf(
        R.string.tag_sum_pie,
        R.string.tag_sum_histogram,
        R.string.tag_meal,
        R.string.tag_closet,
        R.string.tag_home,
        R.string.tag_traffic,
        R.string.tag_vehicle_maintenance,
        R.string.tag_book,
        R.string.tag_hobby,
        R.string.tag_internet,
        R.string.tag_friend,
        R.string.tag_education,
        R.string.tag_entertainment,
        R.string.tag_medical,
        R.string.tag_insurance,
        R.string.tag_donation,
        R.string.tag_sport,
        R.string.tag_snack,
        R.string.tag_music,
        R.string.tag_fund,
        R.string.tag_drink,
        R.string.tag_fruit,
        R.string.tag_film,
        R.string.tag_baby,
        R.string.tag_partner,
        R.string.tag_housing_loan,
        R.string.tag_pet,
        R.string.tag_telephone_bill,
        R.string.tag_travel,
        R.string.tag_lunch,
        R.string.tag_breakfast,
        R.string.tag_midnight_snack
    )
    var TAG_DRAWABLE = intArrayOf(
        R.drawable.transparent //            R.drawable.sum_header_pie,
        //            R.drawable.sum_header_histogram,
        //            R.drawable.meal_header,
        //            R.drawable.closet_header,
        //            R.drawable.home_header,
        //            R.drawable.traffic_header,
        //            R.drawable.vehicle_maintenance_header,
        //            R.drawable.book_header,
        //            R.drawable.hobby_header,
        //            R.drawable.internet_header,
        //            R.drawable.friend_header,
        //            R.drawable.education_header,
        //            R.drawable.entertainment_header,
        //            R.drawable.medical_header,
        //            R.drawable.insurance_header,
        //            R.drawable.donation_header,
        //            R.drawable.sport_header,
        //            R.drawable.snack_header,
        //            R.drawable.sum_header_pie,
        //            R.drawable.sum_header_pie,
        //            R.drawable.sum_header_pie,
        //            R.drawable.sum_header_pie,
        //            R.drawable.sum_header_pie,
        //            R.drawable.sum_header_pie,
        //            R.drawable.sum_header_pie,
        //            R.drawable.sum_header_pie,
        //            R.drawable.sum_header_pie,
        //            R.drawable.sum_header_pie,
        //            R.drawable.sum_header_pie,
        //            R.drawable.sum_header_pie
    )
    var TAG_HEADER_URL = arrayOf(
        "http://file.bmob.cn/M02/5A/B5/oYYBAFajMcmAWxDmAAAAX54YFR4292.png",
        "http://file.bmob.cn/M02/5A/B6/oYYBAFajMguAMEjaAACp5TYCw2c161.jpg",
        "http://file.bmob.cn/M02/5A/B7/oYYBAFajMjeAUJ6bAACp5TYCw2c909.jpg",
        "http://file.bmob.cn/M02/5A/B9/oYYBAFajMsCASQSTAAST9c5hnKk541.jpg",
        "http://file.bmob.cn/M02/5A/B9/oYYBAFajMt6ACKvpAAFCadMFDZ0001.jpg",
        "http://file.bmob.cn/M02/5A/BA/oYYBAFajMxaAcCHVAAFMn10yiSc776.jpg",
        "http://file.bmob.cn/M02/5A/BB/oYYBAFajM0WAeSXuAAPMSTPknmg198.jpg",
        "http://file.bmob.cn/M02/5A/BB/oYYBAFajM2uAF2dAAAERt2oghvQ131.jpg",
        "http://file.bmob.cn/M02/5A/BD/oYYBAFajM9mAfvhbAAEAaJskdeI618.jpg",
        "http://file.bmob.cn/M02/5A/BE/oYYBAFajM_GAY_sdAAIiUMzp8aA126.jpg",
        "http://file.bmob.cn/M02/5A/BF/oYYBAFajNBCAfPCYAAIXneirgI8186.jpg",
        "http://file.bmob.cn/M02/5A/C0/oYYBAFajNEuAdQRXAAEeDe0Ytew460.jpg",
        "http://file.bmob.cn/M02/5A/C1/oYYBAFajNGeAHKBYAACR7VAH6tY388.jpg",
        "http://file.bmob.cn/M02/5A/C4/oYYBAFajNOOANYPbAAIMih4I730340.jpg",
        "http://file.bmob.cn/M02/5A/C4/oYYBAFajNP6AREhrAAFRYvfSI1o564.jpg",
        "http://file.bmob.cn/M02/5A/C5/oYYBAFajNRyASQs4AABNrzBmPsU695.jpg",
        "http://file.bmob.cn/M02/5A/C5/oYYBAFajNTCAZJWYAACFnWVbblw284.jpg",
        "http://file.bmob.cn/M02/5A/C6/oYYBAFajNUaACmmCAACd5x8slZY981.jpg",
        "http://file.bmob.cn/M02/5A/C6/oYYBAFajNWKALgknAAHaJgXlKLI169.jpg",
        "http://file.bmob.cn/M02/5A/C7/oYYBAFajNZyAVA0mAAX6uqgz5os812.png",
        "http://file.bmob.cn/M02/5A/CC/oYYBAFajNoeAWF96AAYHTWlvWA8779.png",
        "http://file.bmob.cn/M02/5A/D0/oYYBAFajN4uAcFDhAAbKaer4urk522.png",
        "http://file.bmob.cn/M02/5A/D3/oYYBAFajODSAe5iEAAgDnDRtjG0045.png",
        "http://file.bmob.cn/M02/5A/D6/oYYBAFajOh6AH4ZKAANQpB8MUBQ569.png",
        "http://file.bmob.cn/M02/5A/D6/oYYBAFajOmSAZHFRAAbpe8mI4v0254.png",
        "http://file.bmob.cn/M02/5A/D7/oYYBAFajOpWAKl8gAAZ8d8Z0BAM967.png",
        "http://file.bmob.cn/M02/5A/D7/oYYBAFajOr-Aapj2AAaQOo6Zvzs039.png",
        "http://file.bmob.cn/M02/5A/D8/oYYBAFajOv2ANQlZAAg-CYhjjLQ254.png",
        "http://file.bmob.cn/M02/5A/D9/oYYBAFajOyyAaEqvAAavKIaVqfs654.png",
        "http://file.bmob.cn/M02/5A/DA/oYYBAFajO3WAMt9LAAdYtJz1cCE193.png",
        "http://file.bmob.cn/M02/5A/E1/oYYBAFajPwqAAVQaAAd1j6hRHZw363.png",
        "http://file.bmob.cn/M02/5A/E9/oYYBAFajQruAdJYeAAd3DzoZwNk311.png",
        "http://file.bmob.cn/M02/5A/F4/oYYBAFajRpiAAczeAAgXftIUsqk135.png"
    )
    var DRAWER_TOP_URL = intArrayOf(
        R.drawable.material_design_0,
        R.drawable.material_design_1,
        R.drawable.material_design_2,
        R.drawable.material_design_3,
        R.drawable.material_design_4
    )

    fun getTypeface(): Typeface? {
        return ResourcesCompat.getFont(appContext, R.font.lato)
    }

    fun getLanguage(): String {
        return Locale.getDefault().language
    }

    fun getInMoney(money: Int): String {
        return if ("zh" == Locale.getDefault().language) "¥$money" else "$$money "
    }

    fun getInRecords(records: Int): String {
        return "$records's"
    }

    fun getSpendString(money: Int): String {
        return "Spent $$money "
    }

    fun getSpendString(money: Double): String {
        return "Spent $" + money.toInt() + " "
    }

    fun getPercentString(percent: Double): String {
        return " (" + String.format("%.2f", percent) + "% for period)"
    }

    fun getPurePercentString(percent: Double): String {
        return if ("zh" == Locale.getDefault().language) " " + String.format("%.2f",
            percent) + "%" else " " + String.format("%.2f", percent) + "%"
    }

    fun getTodayViewTitle(fragmentPosition: Int): String {
        return appContext.getString(TODAY_VIEW_TITLE[fragmentPosition])
    }

    @JvmField
    var WEEK_START_WITH_SUNDAY = false

    fun getAxisDateName(type: Int, position: Int): String {
        return when (type) {
            Calendar.HOUR_OF_DAY -> position.toString() + ""
            Calendar.DAY_OF_WEEK -> if (WEEK_START_WITH_SUNDAY) appContext.resources
                .getString(WEEKDAY_SHORT_START_ON_SUNDAY[position + 1]) else appContext.resources
                .getString(WEEKDAY_SHORT_START_ON_MONDAY[position + 1])
            Calendar.DAY_OF_MONTH -> (position + 1).toString() + ""
            Calendar.MONTH -> appContext.resources
                .getString(MONTHS_SHORT[position + 1])
            else -> ""
        }
    }

    fun getTodayViewEmptyTip(fragmentPosition: Int): Int {
        return TODAY_VIEW_EMPTY_TIP[fragmentPosition]
    }

    fun getMonthShort(i: Int): String {
        return appContext.resources.getString(MONTHS_SHORT[i])
    }

    fun getMonth(i: Int): String {
        return appContext.resources.getString(MONTHS[i])
    }

    fun getWeekDay(position: Int): String {
        return if (WEEK_START_WITH_SUNDAY) appContext.resources
            .getString(WEEKDAY_START_ON_SUNDAY[position + 1]) else appContext.resources
            .getString(WEEKDAY_START_ON_MONDAY[position + 1])
    }

    fun getTodayLeftRange(today: Calendar): Calendar {
        val calendar = today.clone() as Calendar
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.MINUTE, 0)
        return calendar
    }

    fun getTodayRightRange(today: Calendar): Calendar {
        val calendar = today.clone() as Calendar
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.MINUTE, 0)
        return calendar
    }

    fun getYesterdayLeftRange(today: Calendar): Calendar {
        val calendar = today.clone() as Calendar
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.add(Calendar.MINUTE, 0)
        return calendar
    }

    fun getYesterdayRightRange(today: Calendar): Calendar {
        val calendar = today.clone() as Calendar
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.add(Calendar.MINUTE, 0)
        return calendar
    }

    fun getThisWeekLeftRange(today: Calendar): Calendar {
        val nowDayOfWeek = today[Calendar.DAY_OF_WEEK]
        val calendar = today.clone() as Calendar
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.MINUTE, 0)
        if (WEEK_START_WITH_SUNDAY) {
            val diff = intArrayOf(0, 0, -1, -2, -3, -4, -5, -6)
            calendar.add(Calendar.DATE, diff[nowDayOfWeek])
        } else {
            val diff = intArrayOf(0, -6, 0, -1, -2, -3, -4, -5)
            calendar.add(Calendar.DATE, diff[nowDayOfWeek])
        }
        return calendar
    }

    fun getThisWeekRightRange(today: Calendar): Calendar {
        val calendar = getThisWeekLeftRange(today).clone() as Calendar
        calendar.add(Calendar.DATE, 7)
        return calendar
    }

    fun getLastWeekLeftRange(today: Calendar): Calendar {
        val nowDayOfWeek = today[Calendar.DAY_OF_WEEK]
        val calendar = today.clone() as Calendar
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.MINUTE, 0)
        if (WEEK_START_WITH_SUNDAY) {
            val diff = intArrayOf(0, 0, -1, -2, -3, -4, -5, -6)
            calendar.add(Calendar.DATE, diff[nowDayOfWeek] - 7)
        } else {
            val diff = intArrayOf(0, -6, 0, -1, -2, -3, -4, -5)
            calendar.add(Calendar.DATE, diff[nowDayOfWeek] - 7)
        }
        return calendar
    }

    fun getLastWeekRightRange(today: Calendar): Calendar {
        val calendar = getLastWeekLeftRange(today).clone() as Calendar
        calendar.add(Calendar.DATE, 7)
        return calendar
    }

    fun getNextWeekLeftRange(today: Calendar): Calendar {
        val nowDayOfWeek = today[Calendar.DAY_OF_WEEK]
        val calendar = today.clone() as Calendar
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.MINUTE, 0)
        if (WEEK_START_WITH_SUNDAY) {
            val diff = intArrayOf(0, 0, -1, -2, -3, -4, -5, -6)
            calendar.add(Calendar.DATE, diff[nowDayOfWeek] + 7)
        } else {
            val diff = intArrayOf(0, -6, 0, -1, -2, -3, -4, -5)
            calendar.add(Calendar.DATE, diff[nowDayOfWeek] + 7)
        }
        return calendar
    }

    fun getNextWeekRightRange(today: Calendar): Calendar {
        val calendar = getNextWeekLeftRange(today).clone() as Calendar
        calendar.add(Calendar.DATE, 7)
        return calendar
    }

    fun getNextWeekRightShownRange(today: Calendar): Calendar {
        val calendar = getNextWeekLeftRange(today).clone() as Calendar
        calendar.add(Calendar.DATE, 6)
        return calendar
    }

    fun getThisWeekRightShownRange(today: Calendar): Calendar {
        val calendar = getThisWeekLeftRange(today).clone() as Calendar
        calendar.add(Calendar.DATE, 6)
        return calendar
    }

    fun getLastWeekRightShownRange(today: Calendar): Calendar {
        val calendar = getLastWeekLeftRange(today).clone() as Calendar
        calendar.add(Calendar.DATE, 6)
        return calendar
    }

    // Moved to misc.kt - move other Calendar functions there as extensions.
/*
    fun GetThisMonthLeftRange(today: Calendar): Calendar {
        val calendar = today.clone() as Calendar
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.MINUTE, 0)
        return calendar
    }
*/

    fun GetThisMonthRightRange(today: Calendar): Calendar {
        val calendar = today.clone() as Calendar
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar.add(Calendar.MONTH, 1)
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.MINUTE, 0)
        return calendar
    }

    fun getLastMonthLeftRange(today: Calendar): Calendar {
        val calendar = today.clone() as Calendar
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.MONTH, -1)
        calendar.add(Calendar.MINUTE, 0)
        return calendar
    }

    fun getLastMonthRightRange(today: Calendar): Calendar {
        val calendar = today.clone() as Calendar
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.MINUTE, 0)
        return calendar
    }

    fun getThisYearLeftRange(today: Calendar): Calendar {
        val calendar = today.clone() as Calendar
        calendar[Calendar.MONTH] = 0
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.MINUTE, 0)
        return calendar
    }

    fun getThisYearRightRange(today: Calendar): Calendar {
        val calendar = today.clone() as Calendar
        calendar[Calendar.MONTH] = 0
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar.add(Calendar.YEAR, 1)
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.MINUTE, 0)
        return calendar
    }

    fun getLastYearLeftRange(today: Calendar): Calendar {
        val calendar = today.clone() as Calendar
        calendar[Calendar.MONTH] = 0
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.YEAR, -1)
        calendar.add(Calendar.MINUTE, 0)
        return calendar
    }

    fun getLastYearRightRange(today: Calendar): Calendar {
        val calendar = today.clone() as Calendar
        calendar[Calendar.MONTH] = 0
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.add(Calendar.MINUTE, 0)
        return calendar
    }

    fun clickButtonDelete(position: Int): Boolean {
        return position == 9
    }

    fun clickButtonCommit(position: Int): Boolean {
        return position == 11
    }

    fun clickButtonIsZero(position: Int): Boolean {
        return position == 10
    }

    fun toDollars(money: Double, currency: String?): Double {
        return 1.0 * money
    }

    fun isStringRelation(s1: String?, s2: String?): Boolean {
        return true
    }

    var relativeSizeSpan: RelativeSizeSpan? = null
    var redForegroundSpan: ForegroundColorSpan? = null
    var greenForegroundSpan: ForegroundColorSpan? = null
    var whiteForegroundSpan: ForegroundColorSpan? = null
    private var lastColor0: String? = null
    private var lastColor1: String? = null
    private var lastColor2: String? = null
    private var random: Random? = null
    private val Colors = arrayOf("#F44336",
        "#E91E63",
        "#9C27B0",
        "#673AB7",
        "#3F51B5",
        "#2196F3",
        "#03A9F4",
        "#00BCD4",
        "#009688",
        "#4CAF50",
        "#8BC34A",
        "#CDDC39",
        "#FFEB3B",
        "#FFC107",
        "#FF9800",
        "#FF5722",
        "#795548",
        "#9E9E9E",
        "#607D8B")

    fun getRandomColor(): Int {
        val random = Random()
        var p = random.nextInt(Colors.size)
        while (Colors[p] == lastColor0 || Colors[p] == lastColor1 || Colors[p] == lastColor2) {
            p = random.nextInt(Colors.size)
        }
        lastColor0 = lastColor1
        lastColor1 = lastColor2
        lastColor2 = Colors[p]
        return Color.parseColor(Colors[p])
    }

    fun getTagColorResource(tag: Int): Int {
        val index = if (TAG_COLOR.size <= (tag + 2)) {
            Timber.e("Could not get Tag Color Resource for tag($tag)")
            0
        } else {
            tag + 2
        }
        return TAG_COLOR[index]
    }

    fun getTagColor(tag: Int): Int {
        return if (TAG_COLOR.size > (tag + 3)) {
            ContextCompat.getColor(appContext, TAG_COLOR[tag + 3])
        } else {
            Timber.e("Unable to get color for tagId(${tag + 3})")
            ContextCompat.getColor(appContext, R.color.my_blue);
        }
    }

    fun getTagDrawable(tagId: Int): Drawable? {
        return ContextCompat.getDrawable(
            appContext, TAG_DRAWABLE[tagId + 3])
    }

    fun getTagUrl(tagId: Int): String {
        return TAG_HEADER_URL[tagId + 3]
    }

    fun getSnackBarBackground(tagId: Int): Int {
        return TAG_SNACK[tagId + 3]
    }

    fun getTagIcon(tagId: Int): Int {
        return if (TAG_ICON.size > (tagId + 2)) {
            TAG_ICON[tagId + 2]
        } else {
            Timber.e("Unable to get tag icon for tagId(${tagId + 2})")
            R.drawable.sum_pie_icon
        }
    }

    fun getTagIconDrawable(tagId: Int): Drawable? {
        return ContextCompat.getDrawable(
            appContext, TAG_ICON[tagId + 2])
    }

    fun getTagName(tagId: Int): String {
        return if (TAG_NAME.size > (tagId + 2)) {
            appContext.resources.getString(TAG_NAME[tagId + 2])
        } else {
            Timber.e("Unable to get tag name for tagId(${tagId + 2})")
            "N/A"
        }
    }

    fun getTagById(tagId: Int): Tag? {
        return RecordManager.TAGS.firstOrNull { tagId == it.id }
    }

    fun <K, V : Comparable<V>?> sortTreeMapByValues(map: Map<K, V>): Map<K, V> {
        val valueComparator = Comparator<K> { k1, k2 ->
            val compare = map[k1]!!.compareTo(map[k2]!!)
            if (compare == 0) 1 else compare
        }
        val sortedByValues = TreeMap<K, V>(valueComparator)
        sortedByValues.putAll(map)
        return sortedByValues
    }

    private val EMPTY_STATE = intArrayOf()
    fun clearState(drawable: Drawable?) {
        if (drawable != null) {
            drawable.state = EMPTY_STATE
        }
    }

    fun isNumber(c: Char): Boolean {
        return '0' <= c && c <= '9'
    }

    val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = appContext
                .resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = appContext
                    .resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    fun getDeeperColor(color: Int): Int {
        val alpha = Color.alpha(color)
        val red = (Color.red(color) * 0.8).toInt()
        val green = (Color.green(color) * 0.8).toInt()
        val blue = (Color.blue(color) * 0.8).toInt()
        return Color.argb(alpha, red, green, blue)
    }

    fun getAlphaColor(color: Int): Int {
        val alpha = 6
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    fun getKeyBoard(editText: MaterialEditText, context: Context) {
        editText.requestFocus()
        val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun getKeyBoard(editText: EditText) {
        editText.requestFocus()
        val keyboard = appContext
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun dpToPx(dp: Int): Int {
        val displayMetrics =
            appContext.resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun getToolBarHeight(context: Context): Int {
        val styledAttributes =
            context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        val mActionBarSize = styledAttributes.getDimension(0, 0f).toInt()
        styledAttributes.recycle()
        return mActionBarSize
    }

    fun getDrawerTopUrl(): HashMap<String, Int> {
        val drawerTopUrls = HashMap<String, Int>()
        drawerTopUrls["0"] = DRAWER_TOP_URL[0]
        drawerTopUrls["1"] = DRAWER_TOP_URL[1]
        drawerTopUrls["2"] = DRAWER_TOP_URL[2]
        drawerTopUrls["3"] = DRAWER_TOP_URL[3]
        drawerTopUrls["4"] = DRAWER_TOP_URL[4]
        return drawerTopUrls
    }

    val transparentUrls: HashMap<String, Int>
        get() {
            val transparentUrls = HashMap<String, Int>()
            transparentUrls["0"] = R.drawable.transparent
            transparentUrls["1"] = R.drawable.transparent
            return transparentUrls
        }

    fun isPointInsideView(x: Float, y: Float, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewX = location[0]
        val viewY = location[1]

        //point is inside view bounds
        return if (x > viewX && x < viewX + view.width &&
            y > viewY && y < viewY + view.height
        ) {
            true
        } else {
            false
        }
    }

    fun getScreenWidth(activity: Activity): Int {
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }

    fun getScreenHeight(context: Context): Int {
        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.y
    }

    fun getScreenSize(context: Context): Point {
        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }

    @JvmField
    var INPUT_MIN_EXPENSE = 0.0

    @JvmField
    var INPUT_MAX_EXPENSE = 99999.0

    fun getCurrentVersion(): String {
        return "CoCoin V" + CoCoinApplication.VERSION / 100 + "." + CoCoinApplication.VERSION % 100 / 10 + "." + CoCoinApplication.VERSION % 10
    }

    fun getCalendarString(context: Context, calendar: Calendar): String {
        if ("en" == Locale.getDefault().language) {
            return context.resources.getString(MONTHS_SHORT[calendar[Calendar.MONTH] + 1]) + calendar[Calendar.DAY_OF_MONTH] + " " + calendar[Calendar.YEAR]
        }
        return if ("zh" == Locale.getDefault().language) {
            context.resources.getString(MONTHS_SHORT[calendar[Calendar.MONTH] + 1]) + calendar[Calendar.DAY_OF_MONTH] + " " + calendar[Calendar.YEAR]
        } else (calendar[Calendar.MONTH] + 1).toString() + "-" + calendar[Calendar.DAY_OF_MONTH] + " " + calendar[Calendar.YEAR]
    }

    fun getCalendarStringRecordCheckDialog(context: Context, calendar: Calendar): String {
        return context.resources.getString(MONTHS_SHORT[calendar[Calendar.MONTH] + 1]) + " " + calendar[Calendar.DAY_OF_MONTH] + " " + calendar[Calendar.YEAR]
    }

    fun getCalendarStringDayExpenseSort(context: Context, year: Int, month: Int, day: Int): String {
        return context.resources.getString(MONTHS_SHORT[month]) + " " + day + " " + year
    }

    fun getCalendarString(context: Context, string: String?): String {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            calendar.time = sdf.parse(string)
        } catch (p: ParseException) {
        }
        if ("en" == Locale.getDefault().language) {
            return context.resources.getString(MONTHS_SHORT[calendar[Calendar.MONTH] + 1]) + calendar[Calendar.DAY_OF_MONTH] + " " + calendar[Calendar.YEAR]
        }
        return if ("zh" == Locale.getDefault().language) {
            context.resources.getString(MONTHS_SHORT[calendar[Calendar.MONTH] + 1]) + calendar[Calendar.DAY_OF_MONTH] + " " + calendar[Calendar.YEAR]
        } else (calendar[Calendar.MONTH] + 1).toString() + "-" + calendar[Calendar.DAY_OF_MONTH] + " " + calendar[Calendar.YEAR]
    }

    fun getRecordDatabasePath(context: Context): String {
        var databasePath = ""
        databasePath = if (Build.VERSION.SDK_INT >= 17) {
            context.applicationInfo.dataDir + "/databases/"
        } else {
            "/data/data/" + context.packageName + "/databases/"
        }
        databasePath += DB.DB_NAME_STRING
        if (BuildConfig.DEBUG) Log.d("CoCoin", "Get record database path $databasePath")
        return databasePath
    }

    // if the uploaded file's size and name is the same, the BmobProFile.upload will not upload in fact
    fun deleteBmobUploadCach(context: Context?) {
        val dbHelper = DBHelper(context, "bmob", null, 1)
        val sqliteDatabase = dbHelper.writableDatabase
        sqliteDatabase.delete("upload", "_id>?", arrayOf("0"))
        //        String databasePath = "";
//        if (android.os.Build.VERSION.SDK_INT >= 17) {
//            databasePath = context.getApplicationInfo().dataDir + "/databases/";
//        } else {
//            databasePath = "/data/data/" + context.getPackageName() + "/databases/";
//        }
//        databasePath += "bmob";
//        File file = new File(databasePath);
//        if (file.exists()) file.delete();
    }

    // the tagId is clothes, food, house and traffic
    fun IsCFHT(tagId: Int): Int {
        if (tagId == 2) {
            return 0
        } else if (tagId == -3 || tagId == -2 || tagId == -1 || tagId == 0 || tagId == 15 || tagId == 19 || tagId == 20) {
            return 1
        } else if (tagId == 3 || tagId == 24) {
            return 2
        } else if (tagId == 4 || tagId == 5) {
            return 3
        }
        return -1
    }

    fun textCounter(s: String): Int {
        var counter = 0
        for (c in s.toCharArray()) {
            if (c.code < 128) {
                counter++
            } else {
                counter += 2
            }
        }
        return counter
    }

    fun copyToClipboard(content: String, context: Context) {
        val cmb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cmb.text = content.trim { it <= ' ' }
    }

    class MyShakeAnimator : BaseViewAnimator {
        private var amplitude: Int

        constructor() {
            amplitude = 25
        }

        constructor(amplitude: Int) {
            this.amplitude = amplitude
        }

        public override fun prepare(target: View) {
            val amplitude1 = (amplitude * 0.4).toInt()
            val amplitude2 = (amplitude * 0.2).toInt()
            animatorAgent.playTogether(
                ObjectAnimator.ofFloat(target,
                    "translationX",
                    0f,
                    amplitude.toFloat(),
                    -amplitude.toFloat(),
                    amplitude.toFloat(),
                    -amplitude.toFloat(),
                    amplitude.toFloat(),
                    -amplitude.toFloat(),
                    amplitude.toFloat(),
                    -amplitude.toFloat(),
                    amplitude.toFloat(),
                    -amplitude.toFloat(),
                    amplitude1.toFloat(),
                    -amplitude1.toFloat(),
                    amplitude2.toFloat(),
                    -amplitude2.toFloat(),
                    0f)
            )
        }
    }

    init {
        appContext = context.applicationContext
        relativeSizeSpan = RelativeSizeSpan(2f)
        redForegroundSpan = ForegroundColorSpan(Color.parseColor("#ff5252"))
        greenForegroundSpan = ForegroundColorSpan(Color.parseColor("#4ca550"))
        whiteForegroundSpan = ForegroundColorSpan(Color.parseColor("#ffffff"))
        lastColor0 = ""
        lastColor1 = ""
        lastColor2 = ""
        random = Random()
        MY_BLUE = ContextCompat.getColor(appContext, R.color.my_blue)
    }
}