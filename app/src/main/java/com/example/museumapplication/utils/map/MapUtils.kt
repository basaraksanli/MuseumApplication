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
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.museumapplication.R
import com.example.museumapplication.data.Constant
import com.example.museumapplication.data.FavoriteMuseum
import com.example.museumapplication.data.UserLoggedIn.Companion.instance
import com.example.museumapplication.ui.home.map.MapViewModel
import com.example.museumapplication.utils.services.AwarenessServiceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
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
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sign

class MapUtils(private val context: Context, private val viewModel: MapViewModel) {


    /**
     * Marker movement path calculation for the User Marker Animation
     */
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

    /**
     * Creating Museum Marker Template
     */
    private val museumMarkerBitmap: Bitmap
        get() {
            val bitmapDraw = (ContextCompat.getDrawable(context, R.drawable.museum_icon) as BitmapDrawable?)!!
            val bigMarker = bitmapDraw.bitmap
            return Bitmap.createScaledBitmap(bigMarker, 190, 190, false)
        }

    /**
     * Search Nearby Museums
     * Site Kit - Nearby Search works with pagination logic
     * This function checks for every page of the result(maximum 20 pages)
     * Results of the every page are retrieved async
     * Therefore if the result count 20, search will be assumed finished
     */
    fun searchMuseums(location: Location, radius: Int) {
        viewModel.progressBarVisibility.value = View.VISIBLE
        for (marker: Marker in viewModel.activeMarkers.value!!)
            marker.remove()
        viewModel.activeMarkers.value!!.clear()
        viewModel.activeMarkerData.clear()
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
                    addMuseumsToList(sites)
                    count++
                    processResults(count)
                }

                override fun onSearchError(searchStatus: SearchStatus) {
                    count++
                    Log.i("TAG", "Error : " + searchStatus.errorCode + " " + searchStatus.errorMessage)
                    processResults(count)
                }
            }
            searchService.nearbySearch(request, resultListener)
        }
    }

    /**
     * processes onSearchResult and onSearchError results
     * @param count Int
     */
    private fun processResults(count :Int){
        if (count == 19) {
            viewModel.siteList.postValue(viewModel.siteList.value)
            viewModel.initializeSiteList.postValue(true)
            saveSiteListToDevice()
            viewModel.progressBarVisibility.value = View.GONE
            viewModel.buttonsIsEnabled.postValue(true)
        }
    }

    /**
     * adds museums to the list in the viewModel
     * @param sites MutableList<Site>
     */
    private fun addMuseumsToList(sites : MutableList<Site>){
        for (site in sites) {
            if ((site.name.contains("Museum") || site.name.contains("Müzesi")) && !checkDuplicateSite(site)) {
                    viewModel.siteList.value!!.add(site)
                    addBarrierToAwarenessKit(site, Constant.AWARENESS_BARRIER_RADIUS, Constant.AWARENESS_BARRIER_DURATION)
                    Log.d("Sites", site.name)
            }
        }
    }

    /**
     * Shared preferences save of the current nearby museums
     */
    private fun saveSiteListToDevice() {
        val mPrefs = context.getSharedPreferences("${instance.uID} siteList", Context.MODE_PRIVATE)
        val prefsEditor = mPrefs.edit()
        prefsEditor.clear()
        val gson = Gson()
        val siteListJson = gson.toJson(viewModel.siteList.value)
        prefsEditor.putString("siteList", siteListJson)
        prefsEditor.apply()
    }

    /**
     * Create Museum Marker Options
     */
    fun createMuseumMarkerOptions(site: Site): MarkerOptions {
        val location = site.location
        val position = LatLng(location.lat, location.lng)
        return MarkerOptions().position(position)
                .clusterable(true)
                .title(site.name)
                .icon(BitmapDescriptorFactory.fromBitmap(museumMarkerBitmap))
                .anchorMarker(0.5f, 0.907f)

    }


    /**
     * This function checks the if there is duplicate museums in the list
     */
    private fun checkDuplicateSite(toCompare: Site): Boolean {
        for (site in viewModel.siteList.value!!) {
            if (site.name == toCompare.name) return true
        }
        return false
    }

    /**
     * reset all the information in the view model
     */
    fun resetInfo() {
        viewModel.progressBarVisibility.value = View.VISIBLE
        viewModel.buttonsIsEnabled.value = true
        viewModel.currentPositionMarker.value = null
        viewModel.listWeight.value = 0f
        viewModel.activeMarkers.value!!.clear()
        viewModel.activeMarkerData.clear()
        viewModel.listWeight.postValue(0f)
        viewModel.siteList.value!!.clear()
    }

    /**
     * For every museum nearby, Awareness barriers are added in order to notify the user when he is close
     */
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
        updateBarrier(site.name, combinedBarrier, pendingIntent)
    }

    /**
     * Update the barriers
     */
    private fun updateBarrier(label: String, barrier: AwarenessBarrier, pendingIntent: PendingIntent) {
        val request = BarrierUpdateRequest.Builder()
                .addBarrier(label, barrier, pendingIntent)
                .build()
        Awareness.getBarrierClient(context.applicationContext).updateBarriers(request)
                .addOnSuccessListener {
                    Log.i("AddBarrier", "add barrier success")
                }
                .addOnFailureListener { e: Exception ->
                    Log.e("AddBarrier", "add barrier failed", e)
                }
    }

    /**
     * Delete all barriers from the app
     */
    private fun deleteAllBarriers(context: Context) {
        val request = BarrierUpdateRequest.Builder()
                .deleteAll()
                .build()
        Awareness.getBarrierClient(context.applicationContext).updateBarriers(request)
                .addOnSuccessListener { Log.i("DeleteAllBarriers", "delete all barriers success") }
                .addOnFailureListener { e: Exception? -> Log.e("DeleteAllBarriers", "delete all barriers failed ", e) }
    }

    /**
     * Finds museums by name
     */
    fun findSiteByName(name: String): Site? {
        for (i in viewModel.siteList.value!!.indices) {
            if (viewModel.siteList.value!![i].name == name) return viewModel.siteList.value!![i]
        }
        return null
    }

    /**
     * This is a function to calculate and draw circle like marker with an image inside
     */
    private fun dp(value: Float, fragment: Activity): Int {
        return if (value == 0f) {
            0
        } else ceil(fragment.resources.displayMetrics.density * value.toDouble()).toInt()
    }

    /**
     * Create circle like marker with profile picture inside
     */
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
                Log.d("MapUtils", "Ignored exception")
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return result
    }


    /**
     * Creates User Marker Options
     */
    fun getUserMarkerOptions(location: Location, activity: Activity): MarkerOptions? {
        val options = MarkerOptions().position(LatLng(location.latitude, location.longitude))
        val color = Paint()
        color.textSize = 35f
        color.color = Color.BLACK

        val profilePicture: Bitmap? = if (instance.profilePicture == null)
            BitmapFactory.decodeResource(activity.resources, R.drawable.avatar)
        else
            instance.profilePicture


        val bitmap = viewModel.mapUtils.createUserBitmap(profilePicture, activity)
        options.title(instance.name)
        options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        options.anchorMarker(0.5f, 0.907f)

        return options
    }

    companion object {
        private val df2 = DecimalFormat("#.##")

        @SuppressLint("QueryPermissionsNeeded")
        fun showMuseumInfo(data: Site, root: View, fragment: Fragment) {

            val sp = PreferenceManager.getDefaultSharedPreferences(fragment.requireContext())
            val darkMode = sp.getBoolean("darkMode", false)

            val bottomSheetDialog = BottomSheetDialog(
                    fragment.requireContext(), R.style.BottomSheetDialogTheme)
            val bottomSheetView = LayoutInflater.from(fragment.requireContext().applicationContext)
                    .inflate(R.layout.layout_bottom_sheet_museum, root.findViewById(R.id.bottom_sheet_container)
                    )

            val container = bottomSheetView.findViewById<View>(R.id.bottom_sheet_container)
            val addressText = bottomSheetView.findViewById<TextView>(R.id.bottom_sheet_museum_address)
            val museumNameText = bottomSheetView.findViewById<TextView>(R.id.bottom_sheet_museum_name)
            val museumDistanceText = bottomSheetView.findViewById<TextView>(R.id.bottom_sheet_museum_distance)
            val museumTelephoneText = bottomSheetView.findViewById<TextView>(R.id.bottom_sheet_museum_telephone)
            val webPageText = bottomSheetView.findViewById<TextView>(R.id.bottom_sheet_web_page)


            if (!darkMode) {
                container.background.setTint(Color.WHITE)
                addressText.setTextColor(Color.BLACK)
                museumNameText.setTextColor(Color.BLACK)
                museumDistanceText.setTextColor(Color.BLACK)
                museumTelephoneText.setTextColor(Color.BLACK)
                webPageText.setTextColor(Color.BLACK)
            }
            museumNameText.text = data.name
            addressText.text = data.formatAddress
            museumDistanceText.text = distanceToString(data)
            museumTelephoneText.text = data.poi.phone
            webPageText.text = data.poi.websiteUrl

            directToCall(data.poi.phone, bottomSheetView, fragment, museumTelephoneText)
            directWebPage(data.poi.websiteUrl, bottomSheetView, fragment, webPageText)

            bottomSheetView.findViewById<View>(R.id.navigateButton).setOnClickListener {
                directNavigationApp(data.location.lat, data.location.lng, fragment)
            }
            bottomSheetView.findViewById<View>(R.id.favorite_button).setOnClickListener {
                val temp = instance.getMuseumFavoriteByName(data.name)
                if (temp == null) {
                    instance.favoriteMuseumList.add(FavoriteMuseum(data.name, data.formatAddress, data.poi.phone, data.poi.websiteUrl, data.location))
                    instance.saveFavoriteMuseumListToDevice(fragment.requireContext())
                    (bottomSheetView.findViewById<View>(R.id.starImage) as ImageView).setColorFilter(fragment.requireContext().resources.getColor(R.color.color_gold), PorterDuff.Mode.SRC_IN)
                } else {
                    instance.favoriteMuseumList.remove(temp)
                    instance.saveFavoriteMuseumListToDevice(fragment.requireContext())
                    (bottomSheetView.findViewById<View>(R.id.starImage) as ImageView).setColorFilter(fragment.requireContext().resources.getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN)
                }
            }
            if (instance.getMuseumFavoriteByName(data.name) != null) (bottomSheetView.findViewById<View>(R.id.starImage) as ImageView).setColorFilter(fragment.requireContext().resources.getColor(R.color.color_gold), PorterDuff.Mode.SRC_IN)

            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
        }

        /**
         * @param data Site
         * @return String?
         */
        private fun distanceToString(data: Site): String? {
            val distance = data.distance
            return if (distance > 1000) df2.format(distance / 1000) + " km" else df2.format(distance) + " m"
        }

        /**
         * Directs to User Yandex navigation or Google Navigation if the app is installed on the device
         * @param lat
         * @param lng
         * @param fragment -- for intent control
         *
         */
        private fun directNavigationApp(lat: Double, lng: Double, fragment: Fragment) {
            val uriYandex = "yandexnavi://build_route_on_map?lat_to=${lat}&lon_to=${lng}"
            val intentYandex = Intent(Intent.ACTION_VIEW, Uri.parse(uriYandex))
            intentYandex.setPackage("ru.yandex.yandexnavi")

            val uriGoogle = Uri.parse("google.navigation:q=${lat},${lng}&mode=w")
            val intentGoogle = Intent(Intent.ACTION_VIEW, uriGoogle)
            intentGoogle.setPackage("com.google.android.apps.maps")

            val chooserIntent = Intent.createChooser(intentYandex, "choose map")
            val arr = arrayOfNulls<Intent>(1)
            arr[0] = intentGoogle
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arr)

            val activities = fragment.activity?.packageManager?.queryIntentActivities(chooserIntent, 0)
            if (activities?.size!! > 0) {
                fragment.startActivity(chooserIntent)
            }
        }

        /**
         * directs user to the poi web page
         * @param webSiteUrl String
         * @param bottomSheetView
         * @param fragment for intent controls
         * @param webPageText TextView
         */
        private fun directWebPage(webSiteUrl: String?, bottomSheetView: View, fragment: Fragment, webPageText: TextView) {
            if (webSiteUrl != null) {
                webPageText.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                bottomSheetView.findViewById<View>(R.id.bottom_sheet_web_page).setOnClickListener {
                    val url: String
                    if (!webSiteUrl.startsWith("http://") && !webSiteUrl.startsWith("https://")) {
                        url = "http://$webSiteUrl"
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        fragment.requireContext().startActivity(browserIntent)
                    } else {
                        url = webSiteUrl
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        fragment.requireContext().startActivity(browserIntent)
                    }
                }
            }
        }

        /**
         * directs user to call
         * @param phone
         * @param bottomSheetView
         * @param fragment
         * @param museumTelephoneText TextView
         */
        private fun directToCall(phone: String?, bottomSheetView: View, fragment: Fragment, museumTelephoneText: TextView) {
            if (phone != null) {
                museumTelephoneText.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                bottomSheetView.findViewById<View>(R.id.bottom_sheet_museum_telephone).setOnClickListener {
                    if (fragment.requireContext().checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        val intent = Intent(Intent.ACTION_CALL)
                        intent.data = Uri.parse("tel:" + phone)
                        fragment.requireContext().startActivity(intent)
                    } else {
                        val perm = arrayOf(Manifest.permission.CALL_PHONE)
                        fragment.requestPermissions(perm, 10103)
                    }
                }
            }
        }

    }


}