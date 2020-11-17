package com.example.museumapplication.utils

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapplication.R
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.utils.services.AwarenessService
import com.facebook.FacebookSdk
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.huawei.hms.kit.awareness.Awareness
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest
import com.huawei.hms.kit.awareness.barrier.LocationBarrier
import com.huawei.hms.kit.awareness.barrier.TimeBarrier
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.model.BitmapDescriptorFactory
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*
import java.util.*
import kotlin.math.ceil

class MapUtils(private val mapFragmentView: View, private val mapContext: Context, private val siteListView: RecyclerView, private val mapListLayout: LinearLayout, private val sizeButton: FloatingActionButton,  mMap: HuaweiMap?,private val root :View) {
    private fun dp(value: Float, fragment: Activity): Int {
        return if (value == 0f) {
            0
        } else ceil(fragment.resources.displayMetrics.density * value.toDouble()).toInt()
    }

    fun drawUserMarker(location: Location, profilePicture: Bitmap?, activity: Activity, mMap: HuaweiMap) {
        val options = MarkerOptions().position(LatLng(location.latitude, location.longitude))
        val color = Paint()
        color.textSize = 35f
        color.color = Color.BLACK
        val bitmap = createUserBitmap(activity, profilePicture)
        if (profilePicture != null) {
            options.title(UserLoggedIn.instance.name)
            options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
            options.anchorMarker(0.5f, 0.907f)
            if (currentPositionMarker != null) currentPositionMarker!!.remove()
            currentPositionMarker = mMap.addMarker(options)
        }
    }

    private fun createUserBitmap(activity: Activity, profilePicture: Bitmap?): Bitmap? {
        var result: Bitmap? = null
        try {
            result = Bitmap.createBitmap(dp(62f, activity), dp(76f, activity), Bitmap.Config.ARGB_8888)
            result.eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(result)
            @SuppressLint("UseCompatLoadingForDrawables") val drawable = activity.resources.getDrawable(R.drawable.pin)
            drawable.setBounds(0, 0, dp(62f, activity), dp(76f, activity))
            drawable.draw(canvas)
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

    @SuppressLint("UseCompatLoadingForDrawables")
    fun changeMapSize() {
        if (!isBig) {
            val va = ValueAnimator.ofFloat(1f, 0f)
            va.duration = 100
            va.addUpdateListener { animation: ValueAnimator ->
                (mapListLayout.layoutParams as LinearLayout.LayoutParams).weight = (animation.animatedValue as Float)
                mapListLayout.requestLayout()
            }
            va.start()
            sizeButton.setImageDrawable(FacebookSdk.getApplicationContext().getDrawable(R.drawable.uparrow))
            isBig = true
        } else {
            if (siteList.size != 0) {
                val va = ValueAnimator.ofFloat(0f, 1f)
                va.duration = 100
                va.addUpdateListener { animation: ValueAnimator ->
                    (mapListLayout.layoutParams as LinearLayout.LayoutParams).weight = (animation.animatedValue as Float)
                    mapListLayout.requestLayout()
                }
                va.start()
                sizeButton.setImageDrawable(FacebookSdk.getApplicationContext().getDrawable(R.drawable.downarrow))
                isBig = false
            } else {
                Snackbar.make(mapFragmentView, "Please scan for Museums first", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    fun searchMuseums(location: Location, radius: Int) {
        activeMarkers.clear()
        siteList.clear()
        val searchService = SearchServiceFactory.create(mapContext, mapContext.getString(R.string.api_key))
        var count =0
        val request = NearbySearchRequest()
        request.location = Coordinate(location.latitude, location.longitude)
        request.radius = radius
        request.query = "Museum"
        request.poiType = LocationType.MUSEUM
        request.language = "en"
        request.pageSize = 20
        request.query = "Museum"
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
                    deleteAllBarriers(mapContext)
                    for (site in sites) {
                        if (site.name.contains("Museum") || site.name.contains("Müzesi")) {
                            if (!checkDuplicateSite(site)) {
                                siteList.add(site)
                                activeMarkers.add(drawMuseumMarkers(site))
                                addBarrierToAwarenessKit(site, 5000.0, 1000L)
                                Log.d("Sites", site.name)
                            }
                        }
                    }
                    count++
                    if (count == 19) {
                        initializeRecycleView()
                        changeMapSize()
                        saveSiteListToDevice()
                    }
                }

                override fun onSearchError(searchStatus: SearchStatus) {
                    count++
                    Log.i("TAG", "Error : " + searchStatus.errorCode + " " + searchStatus.errorMessage)
                    if (count== 19) {
                        initializeRecycleView()
                        changeMapSize()
                        saveSiteListToDevice()
                    }
                }
            }
            searchService.nearbySearch(request, resultListener)
        }
    }

    fun saveSiteListToDevice() {
        val mPrefs = mapContext.getSharedPreferences("SiteData", Context.MODE_PRIVATE)
        val prefsEditor = mPrefs.edit()
        prefsEditor.clear()
        val gson = Gson()
        val siteListJson = gson.toJson(siteList)
        prefsEditor.putString("siteList", siteListJson)
        prefsEditor.apply()
    }

    fun retrieveSiteList() {
        val mPrefs = mapContext.getSharedPreferences("SiteData", Context.MODE_PRIVATE)
        val siteListJson = mPrefs.getString("siteList", "")
        val typeSite = object : TypeToken<List<Site?>?>() {}.type
        val gsonBuilder = GsonBuilder()
        val gson = gsonBuilder.create()
        if (siteListJson!!.isNotEmpty()) {
            siteList = gson.fromJson(siteListJson, typeSite)
            initializeRecycleView()
        }
        for (site in siteList) activeMarkers.add(drawMuseumMarkers(site))
    }

    fun initializeRecycleView() {
        siteList.sortWith { site1: Site, site2: Site -> (site1.distance - site2.distance).toInt() }
        adapter = SiteListAdapter(siteList, mapContext, root)
        siteListView.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(mapContext)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        siteListView.layoutManager = linearLayoutManager
        val dividerItemDecoration = DividerItemDecoration(siteListView.context,
                linearLayoutManager.orientation)
        siteListView.addItemDecoration(dividerItemDecoration)
    }

    private val museumMarkerBitmap: Bitmap
        get() {
            val bitmapDraw = (ContextCompat.getDrawable(mapContext, R.drawable.museum_icon) as BitmapDrawable?)!!
            val bigMarker = bitmapDraw.bitmap
            return Bitmap.createScaledBitmap(bigMarker, 190, 190, false)
        }

    fun drawMuseumMarkers(site: Site): Marker {
        val location = site.location
        val position = LatLng(location.lat, location.lng)
        val options = MarkerOptions().position(position)
                .clusterable(true)
                .title(site.name)
                .icon(BitmapDescriptorFactory.fromBitmap(museumMarkerBitmap))
                .anchorMarker(0.5f, 0.907f)
        return mMap!!.addMarker(options)
    }

    fun updateRecycleView(currentLocation: Location?) {
        if (adapter != null) adapter!!.calculateAndUpdateDistance(currentLocation)
    }

    fun checkDuplicateSite(toCompare: Site): Boolean {
        for (site in siteList) {
            if (site.name == toCompare.name) return true
        }
        return false
    }

    fun resetInfo() {
        activeMarkers.clear()
        isBig = true
        siteList.clear()
    }

    fun addBarrierToAwarenessKit(site: Site, radius: Double, duration: Long) {
        if (ActivityCompat.checkSelfPermission(mapContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val stayBarrier = LocationBarrier.stay(site.location.lat, site.location.lng, radius, duration)
        val timeBarrier = TimeBarrier.inTimeCategory(TimeBarrier.TIME_CATEGORY_NIGHT)
        val combinedBarrier = AwarenessBarrier.and(stayBarrier, AwarenessBarrier.not(timeBarrier))
        val pendingIntent: PendingIntent
        val intent = Intent(mapContext, AwarenessService::class.java)
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //In Android 8.0 or later, only foreground services can be started when the app is running in the background.
            PendingIntent.getForegroundService(mapContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getService(mapContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        addBarrier(site.name, combinedBarrier, pendingIntent)
    }

    private fun addBarrier(label: String, barrier: AwarenessBarrier, pendingIntent: PendingIntent) {
        val request = BarrierUpdateRequest.Builder()
                .addBarrier(label, barrier, pendingIntent)
                .build()
        Awareness.getBarrierClient(mapContext.applicationContext).updateBarriers(request)
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
        for (i in siteList.indices) {
            if (siteList[i].name == name) return siteList[i]
        }
        return null
    }

    companion object {
        var currentPositionMarker: Marker? = null
        @JvmField
        var activeMarkers: MutableList<Marker> = ArrayList()
        var siteList: MutableList<Site> = ArrayList()
        private var adapter: SiteListAdapter? = null
        var isBig = true
        private var mMap: HuaweiMap? = null
        @JvmStatic
        fun setMap(mMap: HuaweiMap?) {
            Companion.mMap = mMap
        }

        @JvmStatic
        fun moveCamera(latLng: LatLng?, zoom: Float) {
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        }

        @JvmStatic
        fun animateCamera(latLng: LatLng?, zoom: Float) {
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        }
    }
}