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
package com.example.museumapplication.data

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

    /**
     * Awareness kit configuration
     */
    const val AWARENESS_BARRIER_RADIUS = 5000.0
    const val AWARENESS_BARRIER_DURATION = 1000L
}