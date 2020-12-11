package com.example.museumapplication.utils.virtualGuide

import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi

object GpsCheckUtils {
    /**
     * Is Gps Enabled
     *
     * @param context Context
     * @return true:Gps is enabled
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    fun isGpsEnabled(context: Context): Boolean {
        val `object` = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return false
        return `object`.isLocationEnabled
    }
}