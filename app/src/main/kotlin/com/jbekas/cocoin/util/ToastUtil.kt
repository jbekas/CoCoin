package com.jbekas.cocoin.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.github.johnpersano.supertoasts.SuperActivityToast
import com.github.johnpersano.supertoasts.SuperToast

object ToastUtil {
    @JvmField
    var TOAST_ANIMATION = SuperToast.Animations.FLYIN

    // TODO Seriously?
    private var lastToast = ""

//    @JvmStatic
//    fun showToast(
//        context: Context,
//        text: String,
//        textColor: Int?,
//        color: Int?,
//    ) {
//        if (context is Activity) {
//            showActivityToast(
//                activity = context,
//                text = text,
//                textColor = textColor ?: Color.parseColor("#ffffff"),
//                color = color ?: SuperToast.Background.BLUE)
//        } else {
//            showContextToast(
//                context = context,
//                text = text,
//                textColor = Color.parseColor("#ffffff"),
//                color = color ?: SuperToast.Background.BLUE
//            )
//        }
//    }

/*
    @JvmStatic
    fun showToast(
        context: Context,
        @StringRes textId: Int,
        textColor: Int?,
        color: Int?,
    ) {
        val text = context.resources.getString(textId)
        showToast(
            context = context,
            text = text,
            textColor = textColor ?: Color.parseColor("#ffffff"),
            color = color ?: SuperToast.Background.BLUE
        )
    }
*/

//    private fun showContextToast(
//        context: Context,
//        text: String,
//        textColor: Int,
//        color: Int,
//    ) {
//        if (lastToast == text) {
//            SuperToast.cancelAllSuperToasts()
//        } else {
//            lastToast = text
//        }
//        val superToast = SuperToast(context)
//        superToast.animations = SuperToast.Animations.FLYIN
//        superToast.duration = SuperToast.Duration.VERY_SHORT
//        superToast.textColor = textColor
//        superToast.setTextSize(SuperToast.TextSize.SMALL)
//        superToast.text = text
//        superToast.background = color
//        superToast.show()
//    }

    @JvmStatic
    fun showToast(
        activity: Activity,
        @StringRes textId: Int,
        textColor: Int = Color.parseColor("#ffffff"),
        color: Int = SuperToast.Background.BLUE,
    ) {
        showToast(
            activity = activity,
            text = activity.resources.getString(textId),
            textColor = textColor,
            color = color
        )
    }
        @JvmStatic
    fun showToast(
        activity: Activity,
        text: String,
        textColor: Int = Color.parseColor("#ffffff"),
        color: Int = SuperToast.Background.BLUE,
    ) {
        if (lastToast == text) {
            SuperToast.cancelAllSuperToasts()
        } else {
            lastToast = text
        }
        val superToast = SuperActivityToast.create(
            activity,
            text,
            SuperToast.Duration.VERY_SHORT
        )
        superToast.animations = SuperToast.Animations.FLYIN
        superToast.textColor = textColor
        superToast.setTextSize(SuperToast.TextSize.SMALL)
        superToast.background = color
        superToast.show()
    }
}