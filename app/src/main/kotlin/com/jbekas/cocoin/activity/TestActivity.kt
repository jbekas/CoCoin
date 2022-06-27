package com.jbekas.cocoin.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.jbekas.cocoin.R
import com.jbekas.cocoin.ui.CoCoinTheme
import timber.log.Timber

class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        Timber.d("hello world");

        setContent {
            CoCoinTheme {
                TestComposable()
            }
        }
    }

    @Composable
    fun TestComposable() {
        CoCoinTheme {
            Scaffold(modifier = Modifier.windowInsetsPadding(
                WindowInsets.navigationBars.only(WindowInsetsSides.Start + WindowInsetsSides.End)
            )
//                bottomBar = { BottomNavigation() }
            ) { padding ->
                HomeScreen(Modifier.padding(paddingValues = padding))
            }
        }
    }

    @Composable
    fun HomeScreen(modifier: Modifier = Modifier) {
        Column(
            modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 48.dp)
        ) {
            Text(stringResource(R.string.title_activity_account_book))
        }
    }

    @Preview(widthDp = 360, heightDp = 640)
    @Composable
    fun TestComposablePreview() {
        TestComposable()
    }
}