package com.example.museumapplication.utils.result_listeners

import android.location.Location


/**
 * Callback interface for location listener
 */
interface LocationListener {
    fun onLocationChange(location : Location?)
}