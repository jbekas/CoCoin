package com.redgecko.expensetracker.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.*

class RecordManager private constructor(context: Context) {
    
    companion object {
        private var instance: RecordManager? = null
        
        fun getInstance(context: Context): RecordManager {
            if (instance == null) {
                instance = RecordManager(context)
            }
            return instance!!
        }
        
        // Static properties for selected records
        var SELECTED_SUM: Double = 0.0
        var SELECTED_RECORDS: List<ExpenseRecord> = emptyList()
        var SUM: Int = 0
        var RECORDS: List<ExpenseRecord> = emptyList()
        var TAGS: List<Tag> = emptyList()
        var TAG_NAMES: Map<Int, String> = emptyMap()
        
        const val SAVE_TAG_ERROR_DATABASE_ERROR = -1
        const val SAVE_TAG_ERROR_DUPLICATED_NAME = -2
        const val DELETE_TAG_ERROR_DATABASE_ERROR = -1
        const val DELETE_TAG_ERROR_TAG_REFERENCE = -2
        
        // Static access methods
        fun getAllRecords(): List<ExpenseRecord> = RECORDS
        fun getAllTags(): List<Tag> = TAGS
        fun getTagNames(): Map<Int, String> = TAG_NAMES
        fun getTotalSum(): Int = SUM
        fun getTagName(tagId: Int): String = TAG_NAMES[tagId] ?: "Unknown"
        fun getTagColor(tagId: Int): Int = TAGS.find { it.id == tagId }?.color ?: 0xFF90A4AE.toInt()
    }

    init {
        // Initialize with default tags if first time
        val preferences = context.getSharedPreferences("Values", Context.MODE_PRIVATE)
        if (preferences.getBoolean("FIRST_TIME", true)) {
            createDefaultTags()
            preferences.edit().putBoolean("FIRST_TIME", false).apply()
        }
    }

    private fun createDefaultTags() {
        // Create default expense categories
        val defaultTags = listOf(
            Tag(0, "Food", 0xFFE57373.toInt()),
            Tag(1, "Transport", 0xFF81C784.toInt()),
            Tag(2, "Shopping", 0xFF64B5F6.toInt()),
            Tag(3, "Entertainment", 0xFFFFB74D.toInt()),
            Tag(4, "Health", 0xFFBA68C8.toInt()),
            Tag(5, "Education", 0xFF4DB6AC.toInt()),
            Tag(6, "Other", 0xFF90A4AE.toInt())
        )
        
        TAGS = defaultTags
        TAG_NAMES = defaultTags.associate { it.id to it.name }
    }

    fun addRecord(record: ExpenseRecord): Boolean {
        return try {
            RECORDS = RECORDS + record
            updateSum()
            true
        } catch (e: Exception) {
            Log.e("RecordManager", "Error adding record", e)
            false
        }
    }

    fun updateRecord(record: ExpenseRecord): Boolean {
        return try {
            val index = RECORDS.indexOfFirst { it.id == record.id }
            if (index != -1) {
                RECORDS = RECORDS.toMutableList().apply { 
                    this[index] = record 
                }
                updateSum()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("RecordManager", "Error updating record", e)
            false
        }
    }

    fun deleteRecord(recordId: Long): Boolean {
        return try {
            RECORDS = RECORDS.filter { it.id != recordId }
            updateSum()
            true
        } catch (e: Exception) {
            Log.e("RecordManager", "Error deleting record", e)
            false
        }
    }

    fun getRecordsForDate(date: Calendar): List<ExpenseRecord> {
        return RECORDS.filter { record ->
            val recordDate = record.calendar
            recordDate.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
            recordDate.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
            recordDate.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
        }
    }

    fun getRecordsForMonth(year: Int, month: Int): List<ExpenseRecord> {
        return RECORDS.filter { record ->
            val recordDate = record.calendar
            recordDate.get(Calendar.YEAR) == year &&
            recordDate.get(Calendar.MONTH) == month
        }
    }

    private fun updateSum() {
        SUM = RECORDS.sumOf { it.money.toInt() }
    }


}


