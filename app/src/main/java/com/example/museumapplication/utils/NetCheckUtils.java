package com.example.museumapplication.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetCheckUtils {
    private static final String TAG = "NetCheckUtils";

    private NetCheckUtils() {}

    /**
     * Check Is Network Available
     *
     * @param context Context
     * @return true:Network is available
     */
    public static boolean isNetworkAvailable(Context context) {
        boolean mobileConnection = isMobileConnection(context);
        boolean wifiConnection = isWifiConnection(context);
        if (!mobileConnection && !wifiConnection) {
            Log.i(TAG, "No network available");
            return false;
        }
        return true;
    }

    /**
     * Is Mobile Connection
     *
     * @param context Context
     * @return true:Mobile is connection
     */
    public static boolean isMobileConnection(Context context) {
        Object object = context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!(object instanceof ConnectivityManager)) {
            return false;
        }
        ConnectivityManager manager = (ConnectivityManager) object;
        NetworkInfo networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * Is WIFI Connection
     *
     * @param context Context
     * @return true:wifi is connection
     */
    public static boolean isWifiConnection(Context context) {
        Object object = context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!(object instanceof ConnectivityManager)) {
            return false;
        }
        ConnectivityManager manager = (ConnectivityManager) object;
        NetworkInfo networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
