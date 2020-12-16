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
     * Virtual Guide Permission Request Code
     */
    const val  PERMISSION_REQUEST_CODE_MAP = 8488


    /**
     * TTS SETTINGS
     */
    const val  TTS_VOLUME = 1.0f
    const val  TTS_SPEED = 1.0f



    /**
     *
     *
     * Map Constants
     *
     *
     */

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
     * MAP SIZE ANIMATION DURATION
     */
    const val MAP_SIZE_ANIMATION_DURATION = 100L

    /**
     * Marker movement ANIMATION DURATION
     */
    const val MARKER_MOVEMENT_ANIMATION_DURATION = 500L


    /**
     * Location Callback Interval
     */
    const val LOCATION_INTERVAL = 10000L

}