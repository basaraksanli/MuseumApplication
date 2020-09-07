package com.example.museumapplication.utils;

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class GpsCheckUtils {
    private GpsCheckUtils() {}

    /**
     * Is Gps Enabled
     *
     * @param context Context
     * @return true:Gps is enabled
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public static boolean isGpsEnabled(Context context) {
        Object object = context.getSystemService(Context.LOCATION_SERVICE);
        if (!(object instanceof LocationManager)) {
            return false;
        }
        LocationManager locationManager = (LocationManager) object;
        return locationManager.isLocationEnabled();
    }
}
