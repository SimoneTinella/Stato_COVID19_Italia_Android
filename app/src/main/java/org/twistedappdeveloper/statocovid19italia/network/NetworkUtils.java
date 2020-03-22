package org.twistedappdeveloper.statocovid19italia.network;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkUtils {

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
