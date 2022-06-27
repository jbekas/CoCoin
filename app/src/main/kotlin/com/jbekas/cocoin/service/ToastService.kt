package com.jbekas.cocoin.service

import android.content.Context
import android.graphics.Color
import androidx.annotation.StringRes
import android.widget.Toast
import com.github.johnpersano.supertoasts.SuperToast
import com.jbekas.cocoin.util.CoCoinUtil
import javax.inject.Inject

class ToastService @Inject constructor(private val context: Context) {

    fun showToast(@StringRes stringResId: Int, color: Int) {
        if (true) {
            Toast.makeText(context,
                context.resources.getString(stringResId),
                Toast.LENGTH_SHORT).show()
        } else {
            SuperToast.cancelAllSuperToasts()
            val superToast = SuperToast(context)
            superToast.animations = CoCoinUtil.TOAST_ANIMATION
            superToast.duration = SuperToast.Duration.SHORT
            superToast.textColor = Color.parseColor("#ffffff")
            superToast.setTextSize(SuperToast.TextSize.SMALL)
            superToast.text = context.resources.getString(stringResId)
            superToast.background = color
            superToast.textView.setTypeface(CoCoinUtil.typefaceLatoLight)
            superToast.show()
        }
    }

    fun showToast(text: String?, color: Int) {
        if (true) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        } else {
            SuperToast.cancelAllSuperToasts()
            val superToast = SuperToast(context)
            superToast.animations = CoCoinUtil.TOAST_ANIMATION
            superToast.duration = SuperToast.Duration.SHORT
            superToast.textColor = Color.parseColor("#ffffff")
            superToast.setTextSize(SuperToast.TextSize.SMALL)
            superToast.text = text
            superToast.background = color
            superToast.textView.typeface = CoCoinUtil.typefaceLatoLight
            superToast.show()
        }
    }
}