package com.jbekas.cocoin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.jbekas.cocoin.ui.CoCoinTheme
import com.jbekas.cocoin.ui.theme.MarginDouble
import com.jbekas.cocoin.ui.theme.MaxTabletWidth

class ReportsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        (activity!! as AppCompatActivity).supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(false)
            it.setDisplayShowHomeEnabled(false)
            it.setHomeButtonEnabled(false)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                CoCoinTheme {
                    ReportSelection()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.buttonSecond.setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
//        }
    }

    @Composable
    fun ReportSelection(
        modifier: Modifier = Modifier,
    ) {
        val focusRequester = remember { FocusRequester() }

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.width(MaxTabletWidth).padding(top = 48.dp)
            ) {
                Spacer(modifier = Modifier.height(MarginDouble))

                Button(
                    onClick = { onTodayReportClicked() },
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(id = R.string.date_range_view)
                    )
                }

                Spacer(modifier = Modifier.height(MarginDouble))

                Button(
                    onClick = { onTodayReportClicked() },
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(id = R.string.custom_view)
                    )
                }

                Spacer(modifier = Modifier.height(MarginDouble))

                Button(
                    onClick = { onTodayReportClicked() },
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(id = R.string.tags_view)
                    )
                }

                Spacer(modifier = Modifier.height(MarginDouble))

                Button(
                    onClick = { onTodayReportClicked() },
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(id = R.string.months_view)
                    )
                }

                Spacer(modifier = Modifier.height(MarginDouble))

                Button(
                    onClick = { onTodayReportClicked() },
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(id = R.string.list_view)
                    )
                }

                Spacer(modifier = Modifier.height(MarginDouble))

                Button(
                    onClick = { onTodayReportClicked() },
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(id = R.string.expense_report)
                    )
                }
            }
        }
    }

    @Preview
    @Composable
    fun TestReportSelectionPreview() {
        CoCoinTheme {
            ReportSelection(
            )
        }
    }

    fun onTodayReportClicked() {
        val navController = findNavController(this)
        navController.navigate(R.id.DateRangeReportFragment)
    }
}