package com.redgecko.expensetracker.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import kotlin.random.Random
import java.util.Calendar
// Removed fragment imports for now to get basic build working
import com.redgecko.expensetracker.ui.compose.SettingsScreen
import com.redgecko.expensetracker.ui.compose.AddEditExpenseScreen
import com.redgecko.expensetracker.ui.compose.ReportsScreen
import com.redgecko.expensetracker.ui.compose.ExpenseReportScreen
import cn.bmob.v3.Bmob
import cn.bmob.v3.BmobUser
import com.redgecko.expensetracker.R
import com.redgecko.expensetracker.model.ExpenseTracker
import com.redgecko.expensetracker.model.SettingManager
import com.redgecko.expensetracker.model.User
import com.redgecko.expensetracker.model.RecordManager
import com.redgecko.expensetracker.ui.compose.HomeScreen
import com.redgecko.expensetracker.ui.compose.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
// import com.redgecko.expensetracker.util.ToastUtil // Removed for now

import timber.log.Timber

@AndroidEntryPoint
class ComposeMainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Bmob
        Bmob.initialize(this, ExpenseTracker.APPLICATION_ID)
        
        // Initialize RecordManager - CRITICAL for reports to work
        RecordManager.getInstance(com.redgecko.expensetracker.ExpenseTrackerApplication.getAppContext())
        
        // Check user login status
        val user = BmobUser.getCurrentUser(this, User::class.java)
        if (user != null) {
            SettingManager.getInstance().loggenOn = true
            SettingManager.getInstance().userName = user.username
            SettingManager.getInstance().userEmail = user.email
        } else {
            SettingManager.getInstance().loggenOn = false
        }
        
        setContent {
            ExpenseTrackerTheme {
                ExpenseTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp() {
    val navController = rememberNavController()
    var homeRefreshTrigger by remember { mutableStateOf(0) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            ExpenseTrackerBottomNavigation(
                navController = navController
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToAddExpense = {
                        navController.navigate("add_expense")
                    },
                    onNavigateToReports = {
                        navController.navigate("reports")
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onExpenseClick = { expense ->
                        // Navigate to edit expense - could pass expense ID in the future
                        navController.navigate("add_expense")
                    },
                    refreshTrigger = homeRefreshTrigger
                )
            }
            
            composable("add_expense") {
                AddEditExpenseScreen(
                    onNavigateBack = {
                        // Trigger home refresh and navigate back
                        homeRefreshTrigger += 1
                        navController.popBackStack()
                    }
                )
            }
            
            composable("reports") {
                ReportsScreen(
                    onNavigateToMonthlyReport = {
                        navController.navigate("monthly_report")
                    },
                    onNavigateToDailyReport = {
                        navController.navigate("daily_report")
                    },
                    onNavigateToCustomReport = {
                        navController.navigate("custom_report")
                    },
                    onNavigateToTagReport = {
                        navController.navigate("tag_report")
                    },
                    onNavigateToListReport = {
                        // Keep as placeholder for now
                    },
                    onNavigateToExpenseReport = {
                        navController.navigate("expense_report")
                    }
                )
            }
            
            composable("monthly_report") {
                // TODO: Implement MonthlyReportScreen
                Text("Monthly Report - Coming Soon", modifier = Modifier.padding(16.dp))
            }
            
            composable("daily_report") {
                // TODO: Implement DailyReportScreen
                Text("Daily Report - Coming Soon", modifier = Modifier.padding(16.dp))
            }
            
            composable("custom_report") {
                // TODO: Implement CustomReportScreen
                Text("Custom Report - Coming Soon", modifier = Modifier.padding(16.dp))
            }
            
            composable("tag_report") {
                // TODO: Implement TagReportScreen
                Text("Tag Report - Coming Soon", modifier = Modifier.padding(16.dp))
            }
            
            composable("expense_report") {
                ExpenseReportScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun ExpenseTrackerBottomNavigation(
    navController: androidx.navigation.NavController
) {
    val items = listOf(
        BottomNavItem("home", stringResource(R.string.home_label), Icons.Default.Home),
        BottomNavItem("add_expense", stringResource(R.string.home_add_expense), Icons.Default.Add),
        BottomNavItem("reports", stringResource(R.string.reports_label), Icons.Default.Info),
        BottomNavItem("settings", stringResource(R.string.settings_fragment_label), Icons.Default.Settings)
    )
    
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        
        items.forEach { item ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
