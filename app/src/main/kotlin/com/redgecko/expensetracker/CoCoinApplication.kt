package com.redgecko.expensetracker

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.redgecko.expensetracker.model.RecordManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Simplified ExpenseTrackerApplication for the expense tracker
 */
@HiltAndroidApp
class ExpenseTrackerApplication : MultiDexApplication() {
    
    companion object {
        private var mContext: Context? = null
        
        fun getAppContext(): Context {
            return mContext ?: throw IllegalStateException("Application context not initialized")
        }
        
        fun getAndroidId(): String {
            return android.provider.Settings.Secure.getString(
                getAppContext().contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Store application context
        ExpenseTrackerApplication.mContext = applicationContext
        
        // Setup logging
        setupTimber()
        
        // Initialize RecordManager
        RecordManager.getInstance(this)
    }
    
    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
