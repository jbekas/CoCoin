package com.jbekas.cocoin.util;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.github.johnpersano.supertoasts.SuperToast;
import com.jbekas.cocoin.R;
import com.jbekas.cocoin.activity.CoCoinApplication;

import timber.log.Timber;

/**
 * Created by Weiping on 2015/11/30.
 */
public class CoCoinToast {
    // TODO Temporary until Dagger or Hilt is installed
    private static Context mContext;

    public static void initialize(Context context) {
        mContext = context.getApplicationContext();
    }

    private CoCoinToast() {
    }

    public static void showToast(@StringRes int stringResId, int color) {
        Toast.makeText(mContext, mContext.getResources().getString(stringResId), Toast.LENGTH_SHORT).show();
/*
        SuperToast.cancelAllSuperToasts();
        SuperToast superToast = new SuperToast(mContext);
        superToast.setAnimations(CoCoinUtil.TOAST_ANIMATION);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setTextColor(Color.parseColor("#ffffff"));
        superToast.setTextSize(SuperToast.TextSize.SMALL);
        superToast.setText(mContext.getResources().getString(text));
        superToast.setBackground(color);
        superToast.getTextView().setTypeface(CoCoinUtil.typefaceLatoLight);
        superToast.show();
*/
    }

    public static void showToast(String text, int color) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
/*
        SuperToast.cancelAllSuperToasts();
        SuperToast superToast = new SuperToast(mContext);
        superToast.setAnimations(CoCoinUtil.TOAST_ANIMATION);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setTextColor(Color.parseColor("#ffffff"));
        superToast.setTextSize(SuperToast.TextSize.SMALL);
        superToast.setText(text);
        superToast.setBackground(color);
        superToast.getTextView().setTypeface(CoCoinUtil.typefaceLatoLight);
        superToast.show();
*/
    }
}
