package com.jbekas.cocoin.util

import java.util.*

fun Calendar.getThisMonthLeftRange(): Calendar {
    val calendar = this.clone() as Calendar
    calendar[Calendar.DAY_OF_MONTH] = 1
    calendar[Calendar.HOUR_OF_DAY] = 0
    calendar[Calendar.MINUTE] = 0
    calendar[Calendar.SECOND] = 0
    calendar[Calendar.MILLISECOND] = 0
    calendar.add(Calendar.MINUTE, 0)
    return calendar
}
