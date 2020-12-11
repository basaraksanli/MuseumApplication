package com.example.museumapplication.utils.resultListeners

import android.location.Location

interface LocationListener {
    fun onLocationChange(location : Location?)
}