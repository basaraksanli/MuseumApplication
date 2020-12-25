package com.example.museumapplication.utils.map

import android.content.Context
import android.location.Location
import android.os.Looper
import com.example.museumapplication.data.Constant
import com.example.museumapplication.utils.result_listeners.LocationListener
import com.huawei.hms.location.*


class LocationManager(val context: Context) {

    private val fusedLocationProviderClient: FusedLocationProviderClient? = LocationServices.getFusedLocationProviderClient(context);
    private var mLocationRequest: LocationRequest? = null

    var currentLocation: Location? = null


    /**
     * This function starts listening the location of the device
     */
    fun startLocationTrack(locationListener: LocationListener) {

        mLocationRequest = LocationRequest()
        // Set the location update interval (in milliseconds).
        // Set the location update interval (in milliseconds).
        mLocationRequest!!.interval = Constant.LOCATION_INTERVAL
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