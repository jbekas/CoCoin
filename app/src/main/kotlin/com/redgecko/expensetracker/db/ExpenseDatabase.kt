package com.redgecko.expensetracker.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room database configuration using KSP and Hilt
 */

@Database(
    entities = [
        ExpenseEntity::class,
        // Add more entity classes here as needed
        // CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    
    // Add your DAO abstract methods here
    abstract fun expenseDao(): ExpenseDao
    // abstract fun categoryDao(): CategoryDao
}
