package com.redgecko.expensetracker.repository

import com.redgecko.expensetracker.db.ExpenseDao
import com.redgecko.expensetracker.db.ExpenseEntity
import com.redgecko.expensetracker.model.ExpenseRecord
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    
    fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return expenseDao.getAllExpenses()
    }
    
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByCategory(category)
    }
    
    fun getExpensesByDateRange(startDate: Date, endDate: Date): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate)
    }
    
    fun getTotalAmount(isExpense: Boolean): Flow<Double?> {
        return expenseDao.getTotalAmount(isExpense)
    }
    
    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }
    
    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }
    
    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }
    
    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }
    

}
