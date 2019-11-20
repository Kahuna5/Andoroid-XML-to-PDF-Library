package com.nativess.xmllayouttopdflibrary;

import android.util.Log;

@SuppressWarnings({"WeakerAccess", "unused"})
public class LogUtil {

    private static final String LOG_TAG = "dave_log";

    //region Logging Methods
    public static void v(String msg) {
        log(Log.VERBOSE, msg);
    }

    public static void d(String msg) {
        log(Log.DEBUG, msg);
    }

    public static void i(String msg) {
        log(Log.INFO, msg);
    }

    public static void w(String msg) {
        log(Log.WARN, msg);
    }

    public static void e(String msg) {
        log(Log.ERROR, msg);
    }

    public static void a(String msg) {
        log(Log.ASSERT, msg);
    }
    //endregion

    private static void log(int priority, String msg) {
        //Cant add Crashlytics to a library.
        //Crashlytics.log(priority, tag, msg);
        Log.println(priority, LOG_TAG, msg);
    }
}

