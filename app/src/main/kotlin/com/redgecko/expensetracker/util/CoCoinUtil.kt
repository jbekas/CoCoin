package com.redgecko.expensetracker.util

import java.text.SimpleDateFormat
import java.util.*

object ExpenseUtil {
    
    private val monthNames = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    private val monthNamesShort = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    fun GetMonthShort(month: Int): String {
        return if (month in 1..12) monthNamesShort[month - 1] else "Jan"
    }

    fun GetMonthName(month: Int): String {
        return if (month in 1..12) monthNames[month - 1] else "January"
    }

    fun ToDollas(amount: Double, currency: String): Double {
        // Simple currency conversion - in a real app, you'd use actual exchange rates
        return when (currency.uppercase()) {
            "USD" -> amount
            "EUR" -> amount * 1.1
            "GBP" -> amount * 1.3
            "JPY" -> amount / 110.0
            "CNY" -> amount / 6.5
            else -> amount
        }
    }

    fun ToDollas(amount: Float, currency: String): Double {
        return ToDollas(amount.toDouble(), currency)
    }

    fun formatCurrency(amount: Float, currency: String): String {
        return when (currency.uppercase()) {
            "USD" -> "$${String.format("%.2f", amount)}"
            "EUR" -> "€${String.format("%.2f", amount)}"
            "GBP" -> "£${String.format("%.2f", amount)}"
            "JPY" -> "¥${String.format("%.0f", amount)}"
            "CNY" -> "¥${String.format("%.2f", amount)}"
            else -> "${String.format("%.2f", amount)} $currency"
        }
    }

    fun formatDate(calendar: Calendar, pattern: String = "yyyy-MM-dd"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    fun parseDate(dateString: String, pattern: String = "yyyy-MM-dd"): Calendar? {
        return try {
            val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
            val date = dateFormat.parse(dateString)
            val calendar = Calendar.getInstance()
            date?.let { calendar.time = it }
            calendar
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentMonth(): Int {
        return Calendar.getInstance().get(Calendar.MONTH) + 1
    }

    fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun isToday(calendar: Calendar): Boolean {
        val today = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
               calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
               calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
    }

    fun isThisMonth(calendar: Calendar): Boolean {
        val today = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
               calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)
    }

    fun isThisYear(calendar: Calendar): Boolean {
        val today = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
    }
}
