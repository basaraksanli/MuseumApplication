package com.example.museumapplication.utils.virtualGuide

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log

object NetCheckUtils {
    private const val TAG = "NetCheckUtils"

    /**
     * Check Is Network Available
     *
     * @param context Context
     * @return true:Network is available
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val mobileConnection = isMobileConnection(context)
        val wifiConnection = isWifiConnection(context)
        if (!mobileConnection && !wifiConnection) {
            Log.i(TAG, "No network available")
            return false
        }
        return true
    }

    /**
     * Is Mobile Connection
     *
     * @param context Context
     * @return true:Mobile is connection
     */
    private fun isMobileConnection(context: Context): Boolean {
        val `object` = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val networkInfo = `object`.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        return networkInfo != null && networkInfo.isConnected
    }

    /**
     * Is WIFI Connection
     *
     * @param context Context
     * @return true:wifi is connection
     */
    private fun isWifiConnection(context: Context): Boolean {
        val `object` = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val networkInfo = `object`.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return networkInfo != null && networkInfo.isConnected
    }
}