package com.jbekas.cocoin.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jbekas.cocoin.activity.PinActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val MinPinLength = 4
private const val MaxPinLength = 4
private const val CorrectPin = "1234"

@HiltViewModel
class PinLoginViewModel @Inject constructor(
) : ViewModel(), PinActivity.PinCallbacks {
    var pin by mutableStateOf("")
    var pinButtonEnabled by mutableStateOf(false)
    var pinError by mutableStateOf(false)

    override fun onPinChange(pin: String) {
        this.pin = pin
        this.pinButtonEnabled = pin.length >= 4
    }

    override fun onPinUnlockClick() {
        TODO("Not yet implemented")
    }

}
