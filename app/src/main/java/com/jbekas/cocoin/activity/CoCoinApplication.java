package com.jbekas.cocoin.activity;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;

import com.jbekas.cocoin.BuildConfig;
import com.jbekas.cocoin.db.RecordManager;
//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;

import dagger.hilt.android.HiltAndroidApp;
import timber.log.Timber;

/**
 * Created by 伟平 on 2015/11/2.
 */

@HiltAndroidApp
public class CoCoinApplication extends Application {

    public static final int VERSION = 120;

    private static Context mContext;

//    public static RefWatcher getRefWatcher(Context context) {
//        CoCoinApplication application = (CoCoinApplication) context.getApplicationContext();
//        return application.refWatcher;
//    }
//
//    private RefWatcher refWatcher;

    @Override public void onCreate() {
        super.onCreate();

//        refWatcher = LeakCanary.install(this);
        // TODO Remove the following static context
        CoCoinApplication.mContext = getApplicationContext();

        setupTimber();

        RecordManager.getInstance(this);
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
