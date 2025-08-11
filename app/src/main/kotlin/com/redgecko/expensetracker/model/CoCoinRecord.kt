package com.redgecko.expensetracker.model

import java.text.SimpleDateFormat
import java.util.*

data class ExpenseRecord(
    var id: Long = -1L,
    var money: Float = 0f,
    var currency: String = "USD",
    var tag: Int = 0,
    var calendar: Calendar = Calendar.getInstance(),
    var remark: String = "",
    var userId: String = "",
    var localObjectId: String = "",
    var isUploaded: Boolean = false
) {
    
    fun set(expenseRecord: ExpenseRecord) {
        this.id = expenseRecord.id
        this.money = expenseRecord.money
        this.currency = expenseRecord.currency
        this.tag = expenseRecord.tag
        this.calendar = expenseRecord.calendar
        this.remark = expenseRecord.remark
        this.userId = expenseRecord.userId
        this.localObjectId = expenseRecord.localObjectId
        this.isUploaded = expenseRecord.isUploaded
    }

    fun isInTime(c1: Calendar, c2: Calendar): Boolean {
        return (!this.calendar.before(c1)) && this.calendar.before(c2)
    }

    fun isInMoney(money1: Double, money2: Double, currency: String): Boolean {
        // Note: This would need ExpenseUtil.ToDollas implementation
        return money1 <= this.money && money2 > this.money
    }

    fun getCalendarString(): String {
        val hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
        val minute = String.format("%02d", calendar.get(Calendar.MINUTE))
        val month = getMonthShort(calendar.get(Calendar.MONTH) + 1)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)
        
        return "$hour:$minute $month $day $year"
    }

    fun setCalendar(calendarString: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        try {
            val date = sdf.parse(calendarString)
            this.calendar = Calendar.getInstance()
            date?.let { this.calendar.time = it }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getMonthShort(month: Int): String {
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return if (month in 1..12) months[month - 1] else "Jan"
    }

    override fun toString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return "ExpenseRecord(" +
                "id = $id, " +
                "money = $money, " +
                "currency = $currency, " +
                "tag = $tag, " +
                "calendar = ${dateFormat.format(calendar.time)}, " +
                "remark = $remark, " +
                "userId = $userId, " +
                "localObjectId = $localObjectId, " +
                "isUploaded = $isUploaded)"
    }
}
