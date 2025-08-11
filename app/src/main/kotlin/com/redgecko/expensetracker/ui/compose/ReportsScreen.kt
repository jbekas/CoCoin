package com.redgecko.expensetracker.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReportsScreen(
    onNavigateToMonthlyReport: () -> Unit = {},
    onNavigateToDailyReport: () -> Unit = {},
    onNavigateToCustomReport: () -> Unit = {},
    onNavigateToTagReport: () -> Unit = {},
    onNavigateToListReport: () -> Unit = {},
    onNavigateToExpenseReport: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Reports",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Button(
            onClick = onNavigateToExpenseReport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Expense Report")
        }
        
        Button(
            onClick = onNavigateToMonthlyReport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Monthly Report")
        }
        
        Button(
            onClick = onNavigateToDailyReport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Daily Report")
        }
        
        Button(
            onClick = onNavigateToCustomReport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Custom Report")
        }
        
        Button(
            onClick = onNavigateToTagReport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tag Report")
        }
    }
}
