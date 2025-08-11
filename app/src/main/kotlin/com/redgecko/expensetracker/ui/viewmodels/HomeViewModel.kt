package com.redgecko.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redgecko.expensetracker.model.ExpenseRecord
import com.redgecko.expensetracker.repository.ExpenseRepository
import com.redgecko.expensetracker.util.toExpenseRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class HomeUiState(
    val totalExpensesFormatted: String = "$0",
    val thisMonthExpensesFormatted: String = "$0",
    val recentExpenses: List<ExpenseRecord> = emptyList(),
    val lastTransactionFormatted: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun refreshData() {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _homeUiState.value = _homeUiState.value.copy(isLoading = true)
            
            try {
                // Collect data from repository
                expenseRepository.getAllExpenses().collectLatest { entities ->
                    val allRecords = entities.map { it.toExpenseRecord() }
                    val totalExpenses = allRecords.sumOf { it.money.toDouble() }
                    
                    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val thisMonthRecords = allRecords.filter { record ->
                        record.calendar.get(Calendar.MONTH) == currentMonth &&
                        record.calendar.get(Calendar.YEAR) == currentYear
                    }
                    val thisMonthExpenses = thisMonthRecords.sumOf { it.money.toDouble() }
                    
                    val recentExpenses = allRecords.sortedByDescending { it.calendar.timeInMillis }.take(5)
                    val lastTransaction = if (recentExpenses.isNotEmpty()) {
                        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                        "Last: ${dateFormat.format(recentExpenses.first().calendar.time)}"
                    } else {
                        ""
                    }
                    
                    _homeUiState.value = _homeUiState.value.copy(
                        totalExpensesFormatted = formatCurrency(totalExpenses),
                        thisMonthExpensesFormatted = formatCurrency(thisMonthExpenses),
                        recentExpenses = recentExpenses,
                        lastTransactionFormatted = lastTransaction,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _homeUiState.value = _homeUiState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        return when {
            amount >= 10000 -> "$${String.format("%.1fk", amount / 1000)}"
            amount >= 1000 -> "$${String.format("%.1fk", amount / 1000)}"
            else -> "$${String.format("%.0f", amount)}"
        }
    }
}
