package com.redgecko.expensetracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Sample Room entity using KSP for annotation processing
 * 
 * This demonstrates how to create entities that will be processed by KSP
 * instead of KAPT during compilation
 */

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val amount: Double,
    val description: String,
    val category: String,
    val date: Date,
    val isExpense: Boolean = true,
    
    // Additional fields as needed
    val location: String? = null,
    val tags: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
