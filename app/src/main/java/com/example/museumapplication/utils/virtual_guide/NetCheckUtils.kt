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
package com.example.museumapplication.utils.virtual_guide

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log

object NetCheckUtils {
    private const val TAG = "NetCheckUtils"

    /**
     * Check Is Network Available
     *
     * @param context Context
     * @return true:Network is available
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val mobileConnection = isMobileConnection(context)
        val wifiConnection = isWifiConnection(context)
        if (!mobileConnection && !wifiConnection) {
            Log.i(TAG, "No network available")
            return false
        }
        return true
    }

    /**
     * Is Mobile Connection
     *
     * @param context Context
     * @return true:Mobile is connection
     */
    private fun isMobileConnection(context: Context): Boolean {
        val `object` = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val networkInfo = `object`.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        return networkInfo != null && networkInfo.isConnected
    }

    /**
     * Is WIFI Connection
     *
     * @param context Context
     * @return true:wifi is connection
     */
    private fun isWifiConnection(context: Context): Boolean {
        val `object` = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val networkInfo = `object`.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return networkInfo != null && networkInfo.isConnected
    }
}