package org.twistedappdeveloper.statocovid19italia.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class Utils {
    public static final String CURSORE_KEY = "cursore";
    public static final String DATACONTEXT_KEY = "contesto";
    public static final String TREND_KEY = "trendKey";


    public static boolean isDeviceOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) {
            return false;
        }
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
