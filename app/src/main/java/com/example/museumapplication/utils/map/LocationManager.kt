package com.example.museumapplication.utils.map

import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.home.map.MapFragment
import com.huawei.hms.location.*
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.site.api.model.Coordinate


class LocationManager(val context: Context) {

    private val fusedLocationProviderClient: FusedLocationProviderClient? = LocationServices.getFusedLocationProviderClient(context);
    private var mLocationRequest: LocationRequest? = null

    var currentLocation: Location? = null

    private var firstTime = true

    fun startLocationTrack(locationListener: LocationListener) {

        mLocationRequest = LocationRequest()
        // Set the location update interval (in milliseconds).
        // Set the location update interval (in milliseconds).
        mLocationRequest!!.interval = 10000
        // Set the weight.
        // Set the weight.
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val mLocationCallback: LocationCallback
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult != null) {
                    currentLocation = locationResult.lastLocation
                    locationListener.onLocationChange(currentLocation)
                }
            }
        }
        fusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
                .addOnSuccessListener {
                }
                .addOnFailureListener {
                }
    }


}