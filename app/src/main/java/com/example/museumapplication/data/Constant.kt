package com.example.museumapplication.data

import android.Manifest

object Constant {
    /**
     * Verification timer to wait before sending another request
     */
    const val VERIFICATION_TIMER = 120

    /**
     * Virtual Guide Thread Sleep
     */
    const val THREAD_SLEEP_TIME = 500

    /**
     * Virtual Guide Permission Request Code
     */
    const val  PERMISSION_REQUEST_CODE_VIRTUAL_GUIDE = 8488

    /**
     * MAP ZOON
     */
    const val MAP_ZOOM = 15f


    /**
     * Max Zoom
     */
    const val MAX_ZOOM = 20.0f

    /**
     * Min ZOOM
     */
    const val MIN_ZOOM = 6.0f

    /**
     * Gps error
     */
    const val GPS_ERROR = "GPS is invalid! Turn on GPS and run this app again."
}