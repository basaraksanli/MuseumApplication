package com.example.museumapplication.utils.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.opengl.Visibility
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.museumapplication.R
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.ui.home.map.MapViewModel
import com.example.museumapplication.utils.services.AwarenessServiceManager
import com.facebook.FacebookSdk
import com.google.gson.Gson
import com.huawei.hms.kit.awareness.Awareness
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest
import com.huawei.hms.kit.awareness.barrier.LocationBarrier
import com.huawei.hms.kit.awareness.barrier.TimeBarrier
import com.huawei.hms.maps.model.BitmapDescriptorFactory
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sign

class MapUtils(private val context: Context, private val viewModel: MapViewModel) {


    internal interface LatLngInterpolator {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng?
        class Linear : LatLngInterpolator {

            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng? {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude

                // Take the shortest path across the 180th meridian.
                if (abs(lngDelta) > 180) {
                    lngDelta -= sign(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }

    private val museumMarkerBitmap: Bitmap
        get() {
            val bitmapDraw = (ContextCompat.getDrawable(context, R.drawable.museum_icon) as BitmapDrawable?)!!
            val bigMarker = bitmapDraw.bitmap
            return Bitmap.createScaledBitmap(bigMarker, 190, 190, false)
        }

    fun searchMuseums(location: Location, radius: Int) {
        viewModel.progressBarVisibility.value = View.VISIBLE
        for (marker: Marker in viewModel.activeMarkers.value!!)
            marker.remove()
        viewModel.activeMarkers.value!!.clear()
        viewModel.siteList.value!!.clear()
        val searchService = SearchServiceFactory.create(context, context.getString(R.string.api_key))
        var count = 0
        val request = NearbySearchRequest()
        request.location = Coordinate(location.latitude, location.longitude)
        request.radius = radius
        request.query = "Museum"
        request.poiType = LocationType.MUSEUM
        request.language = "en"
        request.pageSize = 20
        for (i in 1..19) {
            request.pageIndex = i
            val resultListener: SearchResultListener<NearbySearchResponse?> = object : SearchResultListener<NearbySearchResponse?> {
                override fun onSearchResult(results: NearbySearchResponse?) {
                    if (results == null || results.totalCount <= 0) {
                        return
                    }
                    val sites = results.sites
                    if (sites == null || sites.size == 0) {
                        return
                    }
                    deleteAllBarriers(context)
                    for (site in sites) {
                        if (site.name.contains("Museum") || site.name.contains("Müzesi")) {
                            if (!checkDuplicateSite(site)) {
                                viewModel.siteList.value!!.add(site)
                                addBarrierToAwarenessKit(site, 5000.0, 1000L)
                                Log.d("Sites", site.name)
                            }
                        }
                    }
                    count++
                    if (count == 19) {
                        viewModel.siteList.postValue(viewModel.siteList.value)
                        viewModel.initializeSiteList.postValue(true)
                        saveSiteListToDevice()
                        viewModel.progressBarVisibility.value = View.GONE
                        viewModel.buttonsIsEnabled.postValue(true)
                    }
                }

                override fun onSearchError(searchStatus: SearchStatus) {
                    count++
                    Log.i("TAG", "Error : " + searchStatus.errorCode + " " + searchStatus.errorMessage)
                    if (count == 19) {
                        viewModel.siteList.postValue(viewModel.siteList.value)
                        viewModel.initializeSiteList.postValue(true)
                        saveSiteListToDevice()
                        viewModel.progressBarVisibility.value = View.GONE
                        viewModel.buttonsIsEnabled.postValue(true)
                    }
                }
            }
            searchService.nearbySearch(request, resultListener)
        }
    }

    fun saveSiteListToDevice() {
        val mPrefs = context.getSharedPreferences("SiteData", Context.MODE_PRIVATE)
        val prefsEditor = mPrefs.edit()
        prefsEditor.clear()
        val gson = Gson()
        val siteListJson = gson.toJson(viewModel.siteList.value)
        prefsEditor.putString("siteList", siteListJson)
        prefsEditor.apply()
    }

    fun createMuseumMarkerOptions(site: Site): MarkerOptions {
        val location = site.location
        val position = LatLng(location.lat, location.lng)
        return MarkerOptions().position(position)
                .clusterable(true)
                .title(site.name)
                .icon(BitmapDescriptorFactory.fromBitmap(museumMarkerBitmap))
                .anchorMarker(0.5f, 0.907f)
    }


    fun checkDuplicateSite(toCompare: Site): Boolean {
        for (site in viewModel.siteList.value!!) {
            if (site.name == toCompare.name) return true
        }
        return false
    }

    fun resetInfo() {
        viewModel.progressBarVisibility.value = View.VISIBLE
        viewModel.buttonsIsEnabled.value = true
        viewModel.currentPositionMarker.value = null
        viewModel.listWeight.value = 0f
        viewModel.activeMarkers.value!!.clear()
        viewModel.listWeight.postValue(0f)
        viewModel.siteList.value!!.clear()
    }

    fun addBarrierToAwarenessKit(site: Site, radius: Double, duration: Long) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val stayBarrier = LocationBarrier.stay(site.location.lat, site.location.lng, radius, duration)
        val timeBarrier = TimeBarrier.inTimeCategory(TimeBarrier.TIME_CATEGORY_NIGHT)
        val combinedBarrier = AwarenessBarrier.and(stayBarrier, AwarenessBarrier.not(timeBarrier))
        val pendingIntent: PendingIntent
        val intent = Intent(context, AwarenessServiceManager::class.java)
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //In Android 8.0 or later, only foreground services can be started when the app is running in the background.
            PendingIntent.getForegroundService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        addBarrier(site.name, combinedBarrier, pendingIntent)
    }

    private fun addBarrier(label: String, barrier: AwarenessBarrier, pendingIntent: PendingIntent) {
        val request = BarrierUpdateRequest.Builder()
                .addBarrier(label, barrier, pendingIntent)
                .build()
        Awareness.getBarrierClient(context.applicationContext).updateBarriers(request)
                .addOnSuccessListener {
                    Log.i("AddBarrier", "add barrier success")
                    Toast.makeText(FacebookSdk.getApplicationContext(), "add barrier success", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e: Exception ->
                    Log.e("AddBarrier", "add barrier failed", e)
                    Toast.makeText(FacebookSdk.getApplicationContext(), "add barrier failed$e", Toast.LENGTH_SHORT).show()
                }
    }

    private fun deleteAllBarriers(context: Context) {
        val request = BarrierUpdateRequest.Builder()
                .deleteAll()
                .build()
        Awareness.getBarrierClient(context.applicationContext).updateBarriers(request)
                .addOnSuccessListener { Log.i("DeleteAllBarriers", "delete all barriers success") }
                .addOnFailureListener { e: Exception? -> Log.e("DeleteAllBarriers", "delete all barriers failed ", e) }
    }

    fun findSiteByName(name: String): Site? {
        for (i in viewModel.siteList.value!!.indices) {
            if (viewModel.siteList.value!![i].name == name) return viewModel.siteList.value!![i]
        }
        return null
    }

    private fun dp(value: Float, fragment: Activity): Int {
        return if (value == 0f) {
            0
        } else ceil(fragment.resources.displayMetrics.density * value.toDouble()).toInt()
    }

    private fun createUserBitmap(profilePicture: Bitmap?, activity: Activity): Bitmap? {
        var result: Bitmap? = null
        try {
            result = Bitmap.createBitmap(dp(62f, activity), dp(76f, activity), Bitmap.Config.ARGB_8888)
            result.eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(result)
            @SuppressLint("UseCompatLoadingForDrawables") val drawable = activity.getDrawable(R.drawable.pin)
            drawable?.setBounds(0, 0, dp(62f, activity), dp(76f, activity))
            drawable?.draw(canvas)
            val roundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val bitmapRect = RectF()
            canvas.save()

            //Bitmap bitmap = BitmapFactory.decodeFile(path.toString()); /*generate bitmap here if your image comes from any url*/
            if (profilePicture != null) {
                val shader = BitmapShader(profilePicture, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                val matrix = Matrix()
                val scale = dp(52f, activity) / profilePicture.width.toFloat()
                matrix.postTranslate(dp(5f, activity).toFloat(), dp(5f, activity).toFloat())
                matrix.postScale(scale, scale)
                roundPaint.shader = shader
                shader.setLocalMatrix(matrix)
                bitmapRect[dp(5f, activity).toFloat(), dp(5f, activity).toFloat(), dp(52 + 5.toFloat(), activity).toFloat()] = dp(52 + 5.toFloat(), activity).toFloat()
                canvas.drawRoundRect(bitmapRect, dp(26f, activity).toFloat(), dp(26f, activity).toFloat(), roundPaint)
            }
            canvas.restore()
            try {
                canvas.setBitmap(null)
            } catch (ignored: Exception) {
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return result
    }


    fun getUserMarkerOptions(location: Location, activity: Activity): MarkerOptions? {
        val options = MarkerOptions().position(LatLng(location.latitude, location.longitude))
        val color = Paint()
        color.textSize = 35f
        color.color = Color.BLACK

        val profilePicture: Bitmap? = if (UserLoggedIn.instance.profilePicture == null)
            BitmapFactory.decodeResource(activity.resources, R.drawable.avatar)
        else
            UserLoggedIn.instance.profilePicture


        val bitmap = viewModel.mapUtils.createUserBitmap(profilePicture, activity)
        options.title(UserLoggedIn.instance.name)
        options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        options.anchorMarker(0.5f, 0.907f)

        return options
    }


}