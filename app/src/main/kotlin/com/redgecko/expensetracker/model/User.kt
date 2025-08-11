package com.redgecko.expensetracker.model

import cn.bmob.v3.BmobUser

/**
 * Simplified User class extending BmobUser
 */
class User : BmobUser() {
    
    // Basic user properties
    var androidId: String? = null
    var logoObjectId: String = ""
    var isMonthLimit: Boolean = false
    var monthLimit: Int = 0
    var monthWarning: Int = 0
    var isColorRemind: Boolean = false
    var remindColor: Int = 0xFFE57373.toInt()
    var isForbidden: Boolean = false
    var accountBookName: String = "Default"
    var accountBookPassword: String = ""
    var showPicture: Boolean = true
    var isHollow: Boolean = false
}
