package com.jbekas.cocoin.service

import android.content.Context
import android.graphics.Color
import android.widget.Toast
import io.github.muddz.styleabletoast.StyleableToast
import javax.inject.Inject

class ToastService @Inject constructor(private val context: Context) {

    fun showToast(
        text: String,
        length: Int,
        textColor: Int? = null,
        textSize: Float? = null,
        backgroundColor: Int? = null,
    ) {
        val toast = StyleableToast
            .Builder(context)
            .length(length)
            .text(text)

        textSize?.let {
            toast.textSize(textSize)
        }
        textColor?.let {
            toast.textColor(it)
        }
        backgroundColor?.let {
            toast.backgroundColor(it)
        }
        toast.show()
    }

    fun showErrorToast(
        text: String,
        length: Int? = null
    ) {
        showToast(
            text = text,
            length = length ?: Toast.LENGTH_SHORT,
            textColor = Color.WHITE,
            backgroundColor = Color.RED
        )
    }

    fun showInfoToast(
        text: String,
        length: Int? = null
    ) {
        showToast(
            text = text,
            length = length ?: Toast.LENGTH_SHORT,
            textColor = Color.WHITE,
            backgroundColor = Color.BLUE
        )
    }

    fun showSuccessToast(
        text: String,
        length: Int? = null
    ) {
        showToast(
            text = text,
            length = length ?: Toast.LENGTH_SHORT,
            textColor = Color.WHITE,
            backgroundColor = Color.GREEN
        )
    }
}