package com.jbekas.cocoin.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jbekas.cocoin.R
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.service.ToastService
import com.jbekas.cocoin.ui.CoCoinTheme
import com.jbekas.cocoin.ui.PasswordTextField
import com.jbekas.cocoin.ui.theme.ButtonWidth
import com.jbekas.cocoin.ui.theme.MarginDouble
import com.jbekas.cocoin.ui.theme.MarginQuad
import com.jbekas.cocoin.ui.theme.MaxTabletWidth
import com.jbekas.cocoin.ui.viewmodels.PinLoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PinActivity : AppCompatActivity() {

    @Inject
    lateinit var toastService: ToastService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CoCoinTheme {
                PinScreen()
            }
        }
    }

    @Composable
    fun PinScreen(
        pinViewModel: PinLoginViewModel = viewModel(),
        modifier: Modifier = Modifier,
    ) {
        val focusRequester = remember { FocusRequester() }

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.width(MaxTabletWidth),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    modifier = Modifier.padding(horizontal = MarginQuad),
                    style = MaterialTheme.typography.h4,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(MarginDouble))
                PasswordTextField(
                    value = pinViewModel.pin,
                    onValueChange = { value -> pinViewModel.onPinChange(value) },
                    label = { Text(text = stringResource(id = R.string.pin)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = pinViewModel.pinError,
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        unfocusedBorderColor = cocoinColors.primary,
//                        textColor = Color.Black
//                    ),
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Spacer(modifier = Modifier.height(MarginQuad))
                Button(
                    onClick = { onPinUnlockClick(pinViewModel) },
                    enabled = pinViewModel.pinButtonEnabled,
                    modifier = Modifier
                        .padding(end = MarginQuad)
                        .width(ButtonWidth)
                        .align(Alignment.End)
                ) {
                    Text(
                        text = stringResource(id = android.R.string.ok)
                    )
                }
            }
        }
        SideEffect {
            focusRequester.requestFocus()
        }
    }

    @Preview
    @Composable
    fun TestComposablePreview() {
        CoCoinTheme {
            PinScreen(
            )
        }
    }

    data class PinState(
        var pin: String,
        var pinButtonEnabled: Boolean,
        var pinError: Boolean,
    )

    interface PinCallbacks {
        fun onPinChange(pin: String)
        fun onPinUnlockClick()
    }

    private fun onPinUnlockClick(pinLoginViewModel: PinLoginViewModel) {
        Timber.d("Validating PIN")

        val intent = Intent()
        if (SettingManager.getInstance().password == pinLoginViewModel.pin) {
            intent.putExtra(MainActivity.LOGIN_SUCCESSFUL, true)
            setResult(RESULT_OK, intent)
            finish()
        } else {
            toastService.showToast("Invalid PIN", R.color.red)
        }
    }
}