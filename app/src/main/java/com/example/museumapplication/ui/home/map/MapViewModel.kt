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
package com.example.museumapplication.ui.home.map

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.example.museumapplication.R
import com.example.museumapplication.data.Constant
import com.example.museumapplication.data.User
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.utils.map.LocationManager
import com.example.museumapplication.utils.map.MapUtils
import com.example.museumapplication.utils.result_listeners.LocationListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.site.api.model.Site

@RequiresApi(Build.VERSION_CODES.P)
@SuppressLint("UseCompatLoadingForDrawables")
class MapViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Initialization
     */
    private val context = getApplication<Application>().applicationContext
    private val REQUEST_CODE = Constant.PERMISSION_REQUEST_CODE_MAP
    val mapUtils = MapUtils(context, this)
    var isGetPermission = false
    var currentLocation = MutableLiveData<Location>()
    var siteList = MutableLiveData<ArrayList<Site>>(arrayListOf())
    private val mLocationManager = LocationManager(context)

    /**
     * UI adjustments
     */
    var initializeSiteList = MutableLiveData(false)
    var searchMuseumButtonRangeString =  MutableLiveData(context.getString(R.string.search_for_museums))
    var buttonsIsEnabled = MutableLiveData(true)
    var scanMuseumWarning = MutableLiveData(false)
    var progressBarVisibility = MutableLiveData(View.VISIBLE)
    var listWeight = MutableLiveData(0f)
    var fabImage = MutableLiveData(context.getDrawable(R.drawable.uparrow))
    var museumRange = 50
    var museumResultCount = MutableLiveData(0)
    var museumResultRange = MutableLiveData(0)
    /**
     * Camera variables
     */
    var focusToCurrentLocation = MutableLiveData(false)
    var animateCameraLatLng = MutableLiveData<LatLng>()

    /**
     * Markers
     */
    var currentPositionMarker =  MutableLiveData<Marker> ()
    var activeMarkers =  MutableLiveData<ArrayList<Marker>>(arrayListOf())
    var activeMarkerData = HashMap<String, Site>()



    companion object{
        /**
         * Expand Nearby Site List Fab button image assignment
         */
        @JvmStatic
        @BindingAdapter("android:setArrowImage")
        fun setArrowImage1(view: FloatingActionButton, drawable: Drawable){
            view.setImageDrawable(drawable)
        }

        /**
         * UI map and site list separation adjustment
         */
        @JvmStatic
        @BindingAdapter("android:setWeight")
        fun setWeight1(view: LinearLayout, weight: Float){
            val layoutParams = view.layoutParams as? LinearLayout.LayoutParams
            layoutParams?.let {
                it.weight = weight
                view.layoutParams = it
            }
        }
        @JvmStatic
        @BindingAdapter("app:setEnabled")
        fun setEnabled1(view: LinearLayout, isEnabled: Boolean){
            view.isEnabled= isEnabled
        }
    }

    init {
        mapUtils.resetInfo()
    }


    /**
     * Location Kit Location Track initialization
     */
    fun startLocationTrack(){
        mLocationManager.startLocationTrack(object : LocationListener {
            override fun onLocationChange(location: Location?) {
                if (progressBarVisibility.value == View.VISIBLE)
                    progressBarVisibility.value = View.GONE
                currentLocation.postValue(location)
            }
        })
    }


    fun permissionsRequestCode(): Int {
        return REQUEST_CODE
    }

    fun permissions(): Array<String?> {
        return arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                "android.permission.ACCESS_BACKGROUND_LOCATION",
                "com.huawei.hms.permission.ACTIVITY_RECOGNITION",
        )
    }


    /**
     * Focus current location on map. it is observed in fragment
     */
    fun focusCurrentLocationFabClicked(view: View){
        focusToCurrentLocation.value = true
    }

    /**
     * Change Map size adjustments. Map can be full sized, or half sized with site list
     * Value animator is used for smooth movement
     * Map weight is always 1, Site list weight differ 0 to 1
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    fun changeMapSize(view: View?) {
        if (listWeight.value == 1f) {
            val va = ValueAnimator.ofFloat(1f, 0f)
            va.duration = Constant.MAP_SIZE_ANIMATION_DURATION
            va.addUpdateListener { animation: ValueAnimator ->
                listWeight.value = (animation.animatedValue as Float)
            }
            va.start()
            fabImage.postValue( context.getDrawable(R.drawable.uparrow))
        } else {
            if (siteList.value!!.size != 0) {
                val va = ValueAnimator.ofFloat(0f, 1f)
                va.duration = Constant.MAP_SIZE_ANIMATION_DURATION
                va.addUpdateListener { animation: ValueAnimator ->
                    listWeight.value = (animation.animatedValue as Float)
                }
                va.start()
                fabImage.postValue(context.getDrawable(R.drawable.downarrow))
            } else {
                scanMuseumWarning.value = true
            }
        }
    }

    /**
     * Marker movements according to device location
     * value animator is used for smooth movement
     */
    fun animateMarker(finalLocation: LatLng){
        val latLngInterpolator: MapUtils.LatLngInterpolator = MapUtils.LatLngInterpolator.Linear()
        val startPosition = currentPositionMarker.value!!.position

        val valueAnimator = ValueAnimator()
        valueAnimator.addUpdateListener {
            val v = it.animatedFraction
            val newPosition: LatLng? = latLngInterpolator
                    .interpolate(v, startPosition, finalLocation)
            if(currentPositionMarker.value!=null)
                currentPositionMarker.value!!.position = newPosition
        }
        valueAnimator.setFloatValues(0F, 1F) // Ignored.
        valueAnimator.duration = Constant.MARKER_MOVEMENT_ANIMATION_DURATION
        valueAnimator.start()
    }

    /**
     * This function gets the site list from the last session
     * Whenever the user searches for the museums, these locations are recorded to shared preferences
     */
    fun retrieveSiteList() {
        var mPrefs = context.getSharedPreferences("${UserLoggedIn.instance.uID} siteList", Context.MODE_PRIVATE)
        val siteListJson = mPrefs.getString("siteList", "")
        val typeSite = object : TypeToken<List<Site?>?>() {}.type
        val gsonBuilder = GsonBuilder()
        val gson = gsonBuilder.create()
        if (siteListJson!!.isNotEmpty()) {
            siteList.value =  gson.fromJson(siteListJson, typeSite)
        }
        museumResultCount.postValue(siteList.value!!.size)

        mPrefs = context.getSharedPreferences("${UserLoggedIn.instance.uID} museumRange", Context.MODE_PRIVATE)
        museumResultRange.postValue(mPrefs.getInt("museumRange" , 0))
    }

    fun setMuseumRangeString(){
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        museumRange = sp.getInt("museumRange", 50)
        searchMuseumButtonRangeString.value = context.getString(R.string.search_for_museums) + " ($museumRange km)"
    }


}