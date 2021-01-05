/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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