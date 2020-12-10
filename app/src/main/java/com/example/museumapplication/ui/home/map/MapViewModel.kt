package com.example.museumapplication.ui.home.map

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.location.Location
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.example.museumapplication.R
import com.example.museumapplication.utils.map.LocationListener
import com.example.museumapplication.utils.map.LocationManager
import com.example.museumapplication.utils.map.MapUtils
import com.example.museumapplication.utils.permission.PermissionHelper
import com.example.museumapplication.utils.permission.PermissionInterface
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.site.api.model.Site

@SuppressLint("UseCompatLoadingForDrawables")
class MapViewModel(application: Application) : AndroidViewModel(application) {


    private val context = getApplication<Application>().applicationContext
    private val REQUEST_CODE = 9001
    val mapUtils = MapUtils(context, this)

    var currentLocation = MutableLiveData<Location>()
    var siteList = MutableLiveData<ArrayList<Site>>(arrayListOf())

    //markers
    var currentPositionMarker =  MutableLiveData<Marker> ()
    var activeMarkers =  MutableLiveData<ArrayList<Marker>>(arrayListOf())

    var scanMuseumWarning = MutableLiveData(false)
    var progressBarVisibility = MutableLiveData(View.VISIBLE)

    var searchMuseumButtonRangeString =  MutableLiveData(context.getString(R.string.search_for_museums))

    var listWeight = MutableLiveData(0f)

    var fabImage = MutableLiveData(context.getDrawable(R.drawable.uparrow))

    var focusToCurrentLocation = MutableLiveData(false)

    var isGetPermission = false
    var mPermissionHelper: PermissionHelper? = null

    var animateCameraLatLng = MutableLiveData<LatLng>()

    var initializeSiteList = MutableLiveData(false)

    var buttonsIsEnabled = MutableLiveData(true)

    private var museumRange = 50

    private val mLocationManager = LocationManager(context)

    companion object{
        @JvmStatic
        @BindingAdapter("android:setArrowImage")
        fun setArrowImage1(view: FloatingActionButton, drawable: Drawable){
            view.setImageDrawable(drawable)
        }

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

        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        museumRange = sp.getInt("museumRange", 50)

        searchMuseumButtonRangeString.value = searchMuseumButtonRangeString.value + " ($museumRange km)"


    }


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

    fun permissions(): Array<String?>? {
        return arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                "android.permission.ACCESS_BACKGROUND_LOCATION",
                "com.huawei.hms.permission.ACTIVITY_RECOGNITION"
        )
    }

    fun requestPermissions(fragment: Fragment, permissionInterface: PermissionInterface): Boolean {
        mPermissionHelper = PermissionHelper(fragment, permissionInterface)
        mPermissionHelper!!.requestPermissions()
        return true
    }

    fun showCurrentLocationFabClicked(view: View){
        focusToCurrentLocation.value = true
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun changeMapSize(view: View?) {
        if (listWeight.value == 1f) {
            val va = ValueAnimator.ofFloat(1f, 0f)
            va.duration = 100
            va.addUpdateListener { animation: ValueAnimator ->
                listWeight.value = (animation.animatedValue as Float)
            }
            va.start()
            fabImage.postValue( context.getDrawable(R.drawable.uparrow))
        } else {
            if (siteList.value!!.size != 0) {
                val va = ValueAnimator.ofFloat(0f, 1f)
                va.duration = 100
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
        valueAnimator.setFloatValues(0F, 1F); // Ignored.
        valueAnimator.duration = 500;
        valueAnimator.start();
    }

    fun retrieveSiteList() {
        val mPrefs = context.getSharedPreferences("SiteData", Context.MODE_PRIVATE)
        val siteListJson = mPrefs.getString("siteList", "")
        val typeSite = object : TypeToken<List<Site?>?>() {}.type
        val gsonBuilder = GsonBuilder()
        val gson = gsonBuilder.create()
        if (siteListJson!!.isNotEmpty()) {
            siteList.value =  gson.fromJson(siteListJson, typeSite)
        }
    }
    fun searchMuseumClick(view: View){
        if(listWeight.value == 1f)
            changeMapSize(null)
        buttonsIsEnabled.postValue(false)
        mapUtils.searchMuseums(currentLocation.value!!, museumRange *1000)
    }


}