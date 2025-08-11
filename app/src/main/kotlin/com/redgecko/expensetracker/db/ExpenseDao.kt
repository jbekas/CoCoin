package com.redgecko.expensetracker.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Sample Room DAO using KSP for annotation processing
 * 
 * This demonstrates how to create DAOs that will be processed by KSP
 * instead of KAPT during compilation
 */

@Dao
interface ExpenseDao {
    
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE isExpense = :isExpense ORDER BY date DESC")
    fun getExpensesByType(isExpense: Boolean): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: java.util.Date, endDate: java.util.Date): Flow<List<ExpenseEntity>>
    
    @Query("SELECT SUM(amount) FROM expenses WHERE isExpense = :isExpense")
    fun getTotalAmount(isExpense: Boolean): Flow<Double?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long
    
    @Update
    suspend fun updateExpense(expense: ExpenseEntity)
    
    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
    
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)
}
