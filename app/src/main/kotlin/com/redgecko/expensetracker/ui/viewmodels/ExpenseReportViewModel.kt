package com.redgecko.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redgecko.expensetracker.model.ExpenseRecord
import com.redgecko.expensetracker.repository.ExpenseRepository
import com.redgecko.expensetracker.util.ExpenseUtil
import com.redgecko.expensetracker.util.toExpenseRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import javax.inject.Inject

data class ExpenseReportUiState(
    val dateRangeText: String = "This Month",
    val totalExpenses: String = "$0",
    val transactionCount: Int = 0,
    val averageExpense: String = "$0",
    val comparisonWithPrevious: String = "",
    val topCategories: List<Pair<String, Double>> = emptyList(),
    val dailySpending: Map<String, Double> = emptyMap(),
    val peakSpendingDay: String = "",
    val recentTransactions: List<ExpenseRecord> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ExpenseReportViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseReportUiState())
    val uiState: StateFlow<ExpenseReportUiState> = _uiState.asStateFlow()
    
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency = Currency.getInstance("USD")
    }
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    private var startDate: Calendar = getStartOfCurrentMonth()
    private var endDate: Calendar = getEndOfCurrentMonth()

    init {
        loadReportData()
    }

    fun updateDateRange(start: Calendar, end: Calendar) {
        startDate = start.clone() as Calendar
        endDate = end.clone() as Calendar
        loadReportData()
    }

    private fun loadReportData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Collect data from repository
                expenseRepository.getAllExpenses().collectLatest { entities ->
                    val allRecords = entities.map { it.toExpenseRecord() }
                    val filteredRecords = filterRecordsByDateRange(allRecords, startDate, endDate)
                    
                    // Generate mock data if no real data exists
                    val recordsToAnalyze = if (filteredRecords.isEmpty()) {
                        generateMockData()
                    } else {
                        filteredRecords
                    }
                    
                    val dateRangeText = formatDateRange(startDate, endDate)
                    val totalExpenses = calculateTotalExpenses(recordsToAnalyze)
                    val transactionCount = recordsToAnalyze.size
                    val averageExpense = calculateAverageExpense(recordsToAnalyze)
                    val comparisonText = calculateComparison(recordsToAnalyze, startDate, endDate)
                    val topCategories = calculateTopCategories(recordsToAnalyze)
                    val dailySpending = calculateDailySpending(recordsToAnalyze)
                    val peakSpendingDay = findPeakSpendingDay(dailySpending)
                    val recentTransactions = recordsToAnalyze.sortedByDescending { it.calendar.timeInMillis }.take(10)
                    
                    _uiState.value = _uiState.value.copy(
                        dateRangeText = dateRangeText,
                        totalExpenses = formatCurrency(totalExpenses),
                        transactionCount = transactionCount,
                        averageExpense = formatCurrency(averageExpense),
                        comparisonWithPrevious = comparisonText,
                        topCategories = topCategories,
                        dailySpending = dailySpending,
                        peakSpendingDay = peakSpendingDay,
                        recentTransactions = recentTransactions,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    private fun filterRecordsByDateRange(records: List<ExpenseRecord>, start: Calendar, end: Calendar): List<ExpenseRecord> {
        return records.filter { record ->
            val recordTime = record.calendar.timeInMillis
            recordTime >= start.timeInMillis && recordTime <= end.timeInMillis
        }
    }

    private fun generateMockData(): List<ExpenseRecord> {
        // Generate some mock data for demonstration
        val mockRecords = ArrayList<ExpenseRecord>()
        val calendar = Calendar.getInstance()
        
        for (i in 0..14) {
            calendar.add(Calendar.DAY_OF_MONTH, -i)
            val mockRecord = ExpenseRecord()
            mockRecord.calendar = calendar.clone() as Calendar
            mockRecord.money = (Math.random() * 100 + 10).toFloat()
            mockRecord.tag = (Math.random() * 5).toInt()
            mockRecord.remark = "Mock expense $i"
            mockRecords.add(mockRecord)
        }
        
        return mockRecords
    }

    private fun formatDateRange(start: Calendar, end: Calendar): String {
        return if (isCurrentMonth(start, end)) {
            "This Month"
        } else if (isCurrentWeek(start, end)) {
            "This Week"
        } else {
            "${dateFormat.format(start.time)} - ${dateFormat.format(end.time)}"
        }
    }

    private fun isCurrentMonth(start: Calendar, end: Calendar): Boolean {
        val now = Calendar.getInstance()
        return start.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
               start.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
               end.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
               end.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    }

    private fun isCurrentWeek(start: Calendar, end: Calendar): Boolean {
        val now = Calendar.getInstance()
        val weekStart = now.clone() as Calendar
        weekStart.set(Calendar.DAY_OF_WEEK, weekStart.firstDayOfWeek)
        val weekEnd = weekStart.clone() as Calendar
        weekEnd.add(Calendar.DAY_OF_WEEK, 6)
        
        return start.timeInMillis == weekStart.timeInMillis && end.timeInMillis == weekEnd.timeInMillis
    }

    private fun calculateTotalExpenses(records: List<ExpenseRecord>): Double {
        return records.sumOf { it.money.toDouble() }
    }

    private fun calculateAverageExpense(records: List<ExpenseRecord>): Double {
        return if (records.isNotEmpty()) records.sumOf { it.money.toDouble() } / records.size else 0.0
    }

    private fun calculateComparison(records: List<ExpenseRecord>, start: Calendar, end: Calendar): String {
        // Calculate comparison with previous period
        val currentPeriodDays = ((end.timeInMillis - start.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        val previousStart = start.clone() as Calendar
        previousStart.add(Calendar.DAY_OF_MONTH, -currentPeriodDays)
        val previousEnd = start.clone() as Calendar
        previousEnd.add(Calendar.DAY_OF_MONTH, -1)
        
        // For now, return a placeholder since we need to handle async data collection
        // In a real implementation, you'd collect from repository again
        return "Comparison data loading..."
    }

    private fun calculateTopCategories(records: List<ExpenseRecord>): List<Pair<String, Double>> {
        val categoryTotals = mutableMapOf<Int, Double>()
        
        records.forEach { record ->
            val currentTotal = categoryTotals[record.tag] ?: 0.0
            categoryTotals[record.tag] = currentTotal + record.money.toDouble()
        }
        
        return categoryTotals.entries
            .map { entry -> Pair("Tag ${entry.key}", entry.value) }
            .sortedByDescending { it.second }
            .take(5)
    }

    private fun calculateDailySpending(records: List<ExpenseRecord>): Map<String, Double> {
        val dailyTotals = mutableMapOf<String, Double>()
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        
        records.forEach { record ->
            val dateKey = dateFormat.format(record.calendar.time)
            val currentTotal = dailyTotals[dateKey] ?: 0.0
            dailyTotals[dateKey] = currentTotal + record.money.toDouble()
        }
        
        return dailyTotals
    }

    private fun findPeakSpendingDay(dailySpending: Map<String, Double>): String {
        return dailySpending.maxByOrNull { it.value }?.key ?: "No data"
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount)
    }

    private fun getStartOfCurrentMonth(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    private fun getEndOfCurrentMonth(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar
    }
}
