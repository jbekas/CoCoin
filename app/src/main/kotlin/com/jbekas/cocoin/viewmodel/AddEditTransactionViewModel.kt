package com.jbekas.cocoin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddEditTransactionViewModel : ViewModel() {
    val amount = MutableLiveData<String>("0")
}