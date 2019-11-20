package com.nativess.xmllayouttopdflibrary;

import android.util.Log;

class LogUtil {

    private static final String LOG_TAG = "dave_log";

    //region Logging Methods
    static void d(String msg) {
        log(Log.DEBUG, msg);
    }

    static void i(String msg) {
        log(Log.INFO, msg);
    }

    static void e(String msg) {
        log(Log.ERROR, msg);
    }
    //endregion

    private static void log(int priority, String msg) {
        //Cant add Crashlytics to a library.
        //Crashlytics.log(priority, tag, msg);
        Log.println(priority, LOG_TAG, msg);
    }
}

