package com.example.museumapplication.utils.resultListeners

import android.location.Location


/**
 * Callback interface for location listener
 */
interface LocationListener {
    fun onLocationChange(location : Location?)
}