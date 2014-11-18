package com.hillsalex.metatext.util;

import android.util.Log;

/**
 * Created by alex on 11/18/2014.
 */
public class Logger {
    public static final boolean PERF_ENABLED = true;
    public static final String PERF_TAG = "metatext.PERF/";

    public static void LogPerf(String tag, String message, double mills){
        Log.d(PERF_TAG+tag,message + " took " + mills/1000l + " seconds.");
    }
}
