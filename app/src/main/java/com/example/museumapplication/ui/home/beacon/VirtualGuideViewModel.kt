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
package com.example.museumapplication.ui.home.beacon

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.museumapplication.R
import com.example.museumapplication.data.Artifact
import com.example.museumapplication.data.Constant
import com.example.museumapplication.data.FavoriteArtifact
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.utils.GeneralUtils
import com.example.museumapplication.utils.services.CloudDBManager
import com.example.museumapplication.utils.virtual_guide.BeaconUtils

/**
 * Virtual Guide Page View Model
 */
class VirtualGuideViewModel(application: Application) : AndroidViewModel(application) {


    val REQUEST_CODE = Constant.PERMISSION_REQUEST_CODE_VIRTUAL_GUIDE
    val THREAD_SLEEP_TIME = Constant.THREAD_SLEEP_TIME

    val context: Context = application.applicationContext
    val beaconUtils = BeaconUtils(context, this)
    private val ttsUtils = UserLoggedIn.instance.ttsUtils

    var currentArtifact = MutableLiveData<Artifact>(null)
    var currentMuseum = MutableLiveData<String>(null)
    var isArtifactFavored = MutableLiveData(false)

    var navigateToHome = MutableLiveData(false)

    companion object {

        /**
         * Set Image of the Exhibit
         */
        @SuppressLint("UseCompatLoadingForDrawables")
        @BindingAdapter("image")
        @JvmStatic
        fun setImage(view: ImageView, image: String?) {
            if (image == null)
                view.setImageDrawable(view.context.getDrawable(R.drawable.noimage))
            else {
                val byteArray = Base64.decode(image, Base64.DEFAULT)
                view.setImageBitmap(BitmapFactory.decodeByteArray(byteArray,0,byteArray.size))
            }
        }

        /**
         * Favorite Button Color Change Control
         */
        @BindingAdapter("currentArtifactId")
        @JvmStatic
        fun favoriteButtonChangeColor(view: ImageView, isArtifactFavored: Boolean) {
            if (isArtifactFavored)
                view.setColorFilter(view.context.resources.getColor(R.color.color_gold), PorterDuff.Mode.SRC_IN)
            else
                view.setColorFilter(view.context.resources.getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN)
        }
    }

    /**
     * Text to Speech Start Reading
     */
    fun startTTS(view: View) {
        if (currentArtifact.value != null)
            ttsUtils.startTTSreading(currentArtifact.value!!.artifactDescription.toString())
    }

    /**
     * Text to Speech Stop Reading
     */
    fun stopTTS(view: View) {
        ttsUtils.stopTTSreading()
    }

    /**
     * This Button is used to favorite the Exhibit, and saves to the shared preferences.
     */
    fun favoriteButtonClick(view: View) {
        val toBeFavored = currentArtifact.value

        if (toBeFavored != null) {
            val temp = UserLoggedIn.instance.getArtifactFavoriteByName(toBeFavored.artifactID)
            if (temp == null) {
                toBeFavored.favoriteCount += 1
                currentArtifact.postValue(currentArtifact.value)

                UserLoggedIn.instance.favoriteArtifactList.add(
                        FavoriteArtifact(
                                toBeFavored.artifactID,
                                toBeFavored.artifactName,
                                toBeFavored.artifactDescription.toString(),
                                toBeFavored.artifactImage.toString(),
                                (CloudDBManager.instance.getMuseum(toBeFavored.museumID))!!.museumName,
                                toBeFavored.category))

                UserLoggedIn.instance.saveFavoriteArtifactListToDevice(context)

                isArtifactFavored.postValue(true)

                CloudDBManager.instance.increaseFavoredCountArtifact(toBeFavored.artifactID)
            } else {
                toBeFavored.favoriteCount -= 1
                currentArtifact.postValue(currentArtifact.value)

                UserLoggedIn.instance.favoriteArtifactList.remove(temp)
                UserLoggedIn.instance.saveFavoriteArtifactListToDevice(context)

                isArtifactFavored.postValue(false)
                CloudDBManager.instance.decreaseFavoredCountArtifact(toBeFavored.artifactID)
            }
        }
    }

    /**
     * Permissions for Virtual Guide
     */
    fun permissions(): Array<String?> {
        return arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
        )
    }

    private fun operateConnectivityAction() {
        val `object` = context.getSystemService(Context.CONNECTIVITY_SERVICE)
        if (`object` !is ConnectivityManager) {
            GeneralUtils.showWarnDialog(context.getString(R.string.networkWarnString), context, navigateToHome)
            return
        }
        val activeNetworkInfo = `object`.activeNetworkInfo
        if (activeNetworkInfo == null) {
            GeneralUtils.showWarnDialog(context.getString(R.string.networkWarnString), context, navigateToHome)
            return
        }
        when (activeNetworkInfo.type) {
            ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI -> { Log.d("Connection", "Connection is established")}
            else -> {
                GeneralUtils.showWarnDialog(context.getString(R.string.networkWarnString), context, navigateToHome)
            }
        }
    }

    /**
     * In the virtual guide page application listens for the state changes of WIFI, BLUETOOTH, GPS and INTERNET
     * If an issue is detected, user is directed back to home page
     */
    val stateChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.P)
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("BeaconFragment", "Start StatusMonitoring.onReceive")
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    GeneralUtils.showWarnDialog(context.resources.getString(R.string.bluetooth_error), context,navigateToHome)
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)
                    if (blueState == BluetoothAdapter.STATE_OFF) {
                        GeneralUtils.showWarnDialog(context.resources.getString(R.string.bluetooth_error), context,navigateToHome)
                    }
                }
                ConnectivityManager.CONNECTIVITY_ACTION -> {
                    operateConnectivityAction()
                }
                LocationManager.PROVIDERS_CHANGED_ACTION -> {
                    val `object` = context.getSystemService(Context.LOCATION_SERVICE)
                    if (`object` !is LocationManager) {
                        GeneralUtils.showWarnDialog(context.resources.getString(R.string.gps_error), context, navigateToHome)
                        return
                    }
                    if (!`object`.isLocationEnabled) {
                        GeneralUtils.showWarnDialog(context.resources.getString(R.string.gps_error), context,navigateToHome)
                    }
                }
            }
        }
    }

}