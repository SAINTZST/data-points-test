package com.neversitup.utilities;

import android.util.Log;

/**
 * Created by saintzst on 7/17/2018 AD
 */
public class L {

    public static final String TAG = "dataPointLog";

    public static final void d(String message) {
        if (message != null) {
            Log.d(TAG, message);
        } else {
            Log.e(TAG, "MESSAGE WAS NULL");
        }
    }
}
