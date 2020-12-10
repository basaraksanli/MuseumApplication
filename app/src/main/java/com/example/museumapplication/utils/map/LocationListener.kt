package com.example.museumapplication.utils.map

import android.location.Location

interface LocationListener {
    fun onLocationChange(location : Location?)
}