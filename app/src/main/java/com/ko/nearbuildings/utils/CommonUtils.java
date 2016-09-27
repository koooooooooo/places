package com.ko.nearbuildings.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CommonUtils {

    /**
     * <p>This is method check Internet connection</p>
     *
     * @param ctx is the Application context
     * @return true if exist connection with Internet network, false if not exist connection with Internet network
     */
    public static boolean isHasInternetConnection(Context ctx) {
        final ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

}
