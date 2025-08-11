package com.redgecko.expensetracker.util

import com.redgecko.expensetracker.db.ExpenseEntity
import com.redgecko.expensetracker.model.ExpenseRecord
import java.util.Calendar

// Extension functions for converting between Entity and Model
fun ExpenseEntity.toExpenseRecord(): ExpenseRecord {
    return ExpenseRecord().apply {
        id = this@toExpenseRecord.id
        money = this@toExpenseRecord.amount.toFloat()
        tag = this@toExpenseRecord.category.hashCode() // Use category hash as tag
        remark = this@toExpenseRecord.description
        calendar = Calendar.getInstance().apply { time = this@toExpenseRecord.date }
    }
}

fun ExpenseRecord.toExpenseEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = this.id,
        amount = this.money.toDouble(),
        description = this.remark,
        category = "Tag${this.tag}", // Convert tag to category string
        date = this.calendar.time,
        isExpense = true
    )
}
