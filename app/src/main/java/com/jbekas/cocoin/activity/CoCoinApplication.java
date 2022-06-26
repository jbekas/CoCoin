package com.jbekas.cocoin.activity;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;

import androidx.multidex.MultiDexApplication;

import com.jbekas.cocoin.BuildConfig;
import com.jbekas.cocoin.util.CoCoinToast;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import timber.log.Timber;

/**
 * Created by 伟平 on 2015/11/2.
 */

public class CoCoinApplication extends MultiDexApplication {

    public static final int VERSION = 120;

    private static Context mContext;

    public static RefWatcher getRefWatcher(Context context) {
        CoCoinApplication application = (CoCoinApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;

    @Override public void onCreate() {
        super.onCreate();

        refWatcher = LeakCanary.install(this);
        CoCoinToast.initialize(this);
        // TODO Remove the following static context
        CoCoinApplication.mContext = getApplicationContext();

        setupTimber();
    }

    public static Context getAppContext() {
//        Timber.e(new Exception("PLEASE FIX, calling CoCoinApplication.getAppContext()"));
        return CoCoinApplication.mContext;
    }

    public static String getAndroidId() {
        return Settings.Secure.getString(
                getAppContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } //else {
            //Timber.plant(new Timber.Tree());
        //}
    }
}
