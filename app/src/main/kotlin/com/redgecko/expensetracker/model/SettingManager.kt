package com.redgecko.expensetracker.model

import android.content.Context
import android.content.SharedPreferences

/**
 * Simplified SettingManager for basic app settings
 */
class SettingManager private constructor() {
    
    companion object {
        private var instance: SettingManager? = null
        
        fun getInstance(): SettingManager {
            if (instance == null) {
                instance = SettingManager()
            }
            return instance!!
        }
    }
    
    // Basic settings
    var loggenOn: Boolean = false
    var userName: String? = null
    var userEmail: String? = null
    var isMonthLimit: Boolean = false
    var monthLimit: Int = 0
    var monthWarning: Int = 0
    var isColorRemind: Boolean = false
    var remindColor: Int = 0xFFE57373.toInt()
    var isForbidden: Boolean = false
    var accountBookName: String = "Default"
    var password: String = ""
    var showPicture: Boolean = true
    var isHollow: Boolean = false
}
