package com.example.museumapplication.ui.home.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapplication.R
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.utils.MapUtils
import com.example.museumapplication.utils.MapUtils.Companion.animateCamera
import com.example.museumapplication.utils.MapUtils.Companion.moveCamera
import com.example.museumapplication.utils.MapUtils.Companion.setMap
import com.example.museumapplication.utils.SettingsUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.MapStyleOptions
import com.huawei.hms.site.api.model.Coordinate

class MapFragment : Fragment(), OnMapReadyCallback {
    private var hMap: HuaweiMap? = null
    private var mapUtils: MapUtils? = null
    private var firstTime = true
    private var mMapView: MapView? = null
    private var mLocationPermissionGranted = false
    private var mSavedInstnceState: Bundle? = null
    private var progressBar: ProgressBar? = null
    private var listView: RecyclerView? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mSavedInstnceState = savedInstanceState
        mMapView = root.findViewById(R.id.mapView)
        val mapListLayout = root.findViewById<LinearLayout>(R.id.mapListLayout)
        val fabScreenSize: FloatingActionButton = root.findViewById(R.id.fabScreenSize)
        fabScreenSize.setOnClickListener { mapUtils!!.changeMapSize() }
        listView = root.findViewById(R.id.siteList)
        mapUtils = MapUtils(root, requireContext(), listView!!, mapListLayout, fabScreenSize, hMap, root)
        mapUtils!!.resetInfo()
        val fabLocation: FloatingActionButton = root.findViewById(R.id.fabLocation)
        fabLocation.setOnClickListener { animateCamera(LatLng(currentLocation!!.latitude, currentLocation!!.longitude), 15f) }
        val searchForMuseumButton = root.findViewById<Button>(R.id.searchForMuseumButton)

        val sp = PreferenceManager.getDefaultSharedPreferences(activity)
        val museumRange = sp.getInt("museumRange", 50)
        searchForMuseumButton.append(" ($museumRange km)")
        searchForMuseumButton.setOnClickListener { mapUtils!!.searchMuseums(currentLocation!!, museumRange * 1000) }

        // get mapView by layout view
        progressBar = root.findViewById(R.id.mapProgressBar)
        progressBar!!.visibility = View.VISIBLE
        progressBar!!.bringToFront()
        locationPermissions
        if (mLocationPermissionGranted) {
            initMap(savedInstanceState)
            Log.d("Location Permission:", "Permission granted")
        }
        return root
    }

    override fun onMapReady(map: HuaweiMap) {
        Log.d(TAG, "onMapReady: ")
        hMap = map
        setMap(map)

        hMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), SettingsUtils.mapStyleDark(requireActivity())))

        hMap!!.setMaxZoomPreference(20.0f)
        hMap!!.setMinZoomPreference(6.0f)
        hMap!!.isIndoorEnabled = true
        deviceLocation
        mapUtils!!.retrieveSiteList()
    }//Toast.makeText(getContext(), currentLocation.toString(), Toast.LENGTH_SHORT).show();

    // TODO: Consider calling
    //    ActivityCompat#requestPermissions
    // here to request the missing permissions, and then overriding
    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
    //                                          int[] grantResults)
    // to handle the case where the user grants the permission. See the documentation
    // for ActivityCompat#requestPermissions for more details.
    private val deviceLocation: Unit
        get() {
            val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0f, object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    currentLocation = location
                    //Toast.makeText(getContext(), currentLocation.toString(), Toast.LENGTH_SHORT).show();
                    Log.d("Current Location", currentLocation.toString())
                    if (firstTime) {
                        progressBar!!.visibility = View.GONE
                        if (requireActivity().intent.extras == null) moveCamera(LatLng(location.latitude, location.longitude), 15f) else {
                            if (requireActivity().intent.getStringExtra("MuseumName") != null) {
                                val museumName = requireActivity().intent.getStringExtra("MuseumName")
                                val siteToFocus = mapUtils!!.findSiteByName(museumName!!)
                                if (siteToFocus != null) moveCamera(LatLng(siteToFocus.location.lat, siteToFocus.location.lng), 15f) else moveCamera(LatLng(location.latitude, location.longitude), 15f)
                            }
                            else
                            {
                                val coordinate = Coordinate(requireActivity().intent.getDoubleExtra("favoriteLocationLat", 0.0),requireActivity().intent.getDoubleExtra("favoriteLocationLng", 0.0) )
                                moveCamera(LatLng(coordinate.lat, coordinate.lng), 15f)
                            }
                        }
                        firstTime = false
                    }
                    mapUtils!!.updateRecycleView(currentLocation)


                    if (activity != null)
                        if (UserLoggedIn.instance.profilePicture == null)
                            mapUtils!!.drawUserMarker(location, BitmapFactory.decodeResource(resources, R.drawable.avatar), activity!!, hMap!!)
                        else
                            mapUtils!!.drawUserMarker(location, UserLoggedIn.instance.profilePicture, activity!!, hMap!!)
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            })
        }
    private val locationPermissions: Unit
        get() {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                Log.i(TAG, "sdk < 28 Q")
                if (ActivityCompat.checkSelfPermission(requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(), "com.huawei.hms.permission.ACTIVITY_RECOGNITION") != PackageManager.PERMISSION_GRANTED) {
                    val strings = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, "com.huawei.hms.permission.ACTIVITY_RECOGNITION")
                    requestPermissions(strings, 1)
                } else mLocationPermissionGranted = true
            } else {
                if (ActivityCompat.checkSelfPermission(requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(),
                                "android.permission.ACCESS_BACKGROUND_LOCATION") != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(), "com.huawei.hms.permission.ACTIVITY_RECOGNITION") != PackageManager.PERMISSION_GRANTED) {
                    val strings = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            "android.permission.ACCESS_BACKGROUND_LOCATION",
                            "com.huawei.hms.permission.ACTIVITY_RECOGNITION")
                    requestPermissions(strings, 2)
                } else mLocationPermissionGranted = true
            }
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initMap(mSavedInstnceState)
                Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSION successful")
                mLocationPermissionGranted = true
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSION  failed")
                progressBar!!.visibility = View.GONE
            }
        }
        if (requestCode == 2) {
            if (grantResults.size > 2 && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initMap(mSavedInstnceState)
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION successful")
                mLocationPermissionGranted = true
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION  failed")
                progressBar!!.visibility = View.GONE
            }
        }
    }

    private fun initMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        mMapView!!.onCreate(mapViewBundle)
        mMapView!!.getMapAsync(this)
    }

    companion object {
        private const val TAG = "MapViewDemoActivity"
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        var currentLocation: Location? = null
    }
}