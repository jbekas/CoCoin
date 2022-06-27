package com.jbekas.cocoin.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import com.jbekas.cocoin.R
import timber.log.Timber

//import com.jbekas.cocoin.ui.CoCoinTheme

//@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.dummy_layout);

        Timber.d("hello world");

//        setContent {
//            CoCoinTheme {
//                val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
//
//                val navController = rememberNavController()
//                NavHost(navController = navController, startDestination = Routes.Home.route) {
//                    composable(Routes.Home.route) {
//                        val mainViewModel = hiltViewModel<MainViewModel>()
//                        MainScreen(
//                            widthSize = widthSizeClass,
//                            onExploreItemClicked = {
//                                launchDetailsActivity(context = this@MainActivity, item = it)
//                            },
//                            onDateSelectionClicked = {
//                                navController.navigate(Routes.Calendar.route)
//                            },
//                            mainViewModel = mainViewModel
//                        )
//                    }
//                    composable(Routes.Calendar.route) {
//                        val parentEntry = remember {
//                            navController.getBackStackEntry(Routes.Home.route)
//                        }
//                        val parentViewModel = hiltViewModel<MainViewModel>(
//                            parentEntry
//                        )
//                        CalendarScreen(onBackPressed = {
//                            navController.popBackStack()
//                        }, mainViewModel = parentViewModel)
//                    }
//                }
//            }
//        }
    }
}