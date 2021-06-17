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

import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapplication.R
import com.example.museumapplication.data.Constant
import com.example.museumapplication.databinding.FragmentMapBinding
import com.example.museumapplication.utils.GeneralUtils
import com.example.museumapplication.utils.map.MapUtils
import com.example.museumapplication.utils.permission.PermissionHelper
import com.example.museumapplication.utils.permission.PermissionInterface
import com.example.museumapplication.utils.settings.SettingsUtils
import com.example.museumapplication.utils.virtual_guide.GpsCheckUtils
import com.google.android.material.snackbar.Snackbar
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.MapStyleOptions
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.site.api.model.Coordinate
import com.huawei.hms.site.api.model.Site
import kotlin.system.exitProcess

@RequiresApi(Build.VERSION_CODES.P)
class MapFragment : Fragment(), OnMapReadyCallback, PermissionInterface {

    companion object {
        private const val TAG = "MapFragment"
    }

    private var hMap: HuaweiMap? = null
    private var mMapView: MapView? = null
    private var listView: RecyclerView? = null
    private var siteListAdapter: SiteListAdapter? = null
    private var isMapReady = false
    private var mPermissionHelper: PermissionHelper? = null


    private lateinit var binding: FragmentMapBinding
    private lateinit var viewModel: MapViewModel

    /**
     * Map Fragment - Owns Map View Model
     */

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        // Every time map is started view model information get cleared
        viewModel.mapUtils.resetInfo()

        // To keep the screen online
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        viewModel.setMuseumRangeString()

        mMapView = binding.root.findViewById(R.id.mapView)
        listView = binding.root.findViewById(R.id.siteList)

        //Request Permissions
        val isSuccess = requestPermissions(this, this)
        if (!isSuccess) {
            return null
        }
        if (!GpsCheckUtils.isGpsEnabled(requireContext()))
            GeneralUtils.showWarnDialog(getString(R.string.gps_not_enabled), requireContext(), null)

        initializeMapObservers()
        initializeUiObservers()


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.map_menu, menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.map_menu -> {
                if(viewModel.listWeight.value == 1f)
                    viewModel.changeMapSize(null)
                viewModel.buttonsIsEnabled.postValue(false)
                viewModel.mapUtils.searchMuseums(viewModel.currentLocation.value!!, viewModel.museumRange *1000)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializeMapObservers() {
        //General animate camera observation. Many animate functions in the project use this observation
        viewModel.animateCameraLatLng.observe(viewLifecycleOwner, {
            animateCamera(it, Constant.MAP_ZOOM)
        })

        //If the site list is initialized, it is set as an adapter to the recycler view
        //Map size is changed to the half
        viewModel.initializeSiteList.observe(viewLifecycleOwner, {
            if (it == true) {
                setRecyclerViewAdapter(binding.root)
                viewModel.changeMapSize(null)
            }
        })

        //Animate the User marker to the next position
        viewModel.currentLocation.observe(viewLifecycleOwner, {
            if (isMapReady) {
                Log.d("Current Location", it.toString())
                if (viewModel.currentPositionMarker.value == null) {
                    handleFirstLocation(it)
                } else {
                    viewModel.animateMarker(LatLng(it.latitude, it.longitude))
                }
                updateRecycleView(it)
            }
        })
    }

    private fun initializeUiObservers() {
        //Creates new markers on the Sites whenever the list has a change
        viewModel.siteList.observe(viewLifecycleOwner, {
            if (isMapReady) {
                setRecyclerViewAdapter(binding.root)
                for (site: Site in it) {
                    val marker = drawMarker(viewModel.mapUtils.createMuseumMarkerOptions(site))
                    viewModel.activeMarkers.value!!.add(marker)
                    viewModel.activeMarkerData[marker.id] = site
                }
            }
        })
        //Focus to current location button clicked observation
        //It animates the map to the current position
        viewModel.focusToCurrentLocation.observe(viewLifecycleOwner, {
            if (it == true) {
                animateCamera(LatLng(viewModel.currentLocation.value!!.latitude, viewModel.currentLocation.value!!.longitude), Constant.MAP_ZOOM)
                viewModel.focusToCurrentLocation.value = false
            }
        })
        //Scan museum warning
        viewModel.scanMuseumWarning.observe(viewLifecycleOwner, {
            if (it) {
                Snackbar.make(binding.root, getString(R.string.please_scan_museum_warning), Snackbar.LENGTH_LONG).show()
                viewModel.scanMuseumWarning.value = false
            }
        })
    }

    /**
     * Creates user marker and focus on it
     * If there is a site to focus at the start of the application, focus on it
     */
    private fun handleFirstLocation(location: Location) {
        if (requireActivity().intent.extras == null)
            moveCamera(LatLng(location.latitude, location.longitude), Constant.MAP_ZOOM)
        else {
            if (requireActivity().intent.getStringExtra("MuseumName") != null) {
                val museumName = requireActivity().intent.getStringExtra("MuseumName")
                val siteToFocus = viewModel.mapUtils.findSiteByName(museumName!!)
                if (siteToFocus != null)
                    moveCamera(LatLng(siteToFocus.location.lat, siteToFocus.location.lng), Constant.MAP_ZOOM)
                else moveCamera(LatLng(location.latitude, location.longitude), Constant.MAP_ZOOM)
            } else {
                val coordinate = Coordinate(requireActivity().intent.getDoubleExtra("favoriteLocationLat", 0.0),
                        requireActivity().intent.getDoubleExtra("favoriteLocationLng", 0.0))
                moveCamera(LatLng(coordinate.lat, coordinate.lng), Constant.MAP_ZOOM)
            }
        }
        viewModel.currentPositionMarker.value = drawMarker(viewModel.mapUtils.getUserMarkerOptions(location, requireActivity())!!)
    }


    /**
     * initialize map
     */
    private fun initMap() {
        val mapViewBundle: Bundle? = null

        mMapView!!.onCreate(mapViewBundle)
        mMapView!!.getMapAsync(this)
    }

    /**
     * Map Configurations on Map ready
     */
    override fun onMapReady(map: HuaweiMap) {
        Log.d(TAG, "onMapReady: ")
        hMap = map
        isMapReady = true

        hMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),
                SettingsUtils.mapStyleDark(requireActivity())))

        hMap!!.setMaxZoomPreference(Constant.MAX_ZOOM)
        hMap!!.setMinZoomPreference(Constant.MIN_ZOOM)
        hMap!!.isIndoorEnabled = true
        viewModel.retrieveSiteList()
        viewModel.startLocationTrack()

        hMap!!.setOnInfoWindowClickListener {
            if (it.id != viewModel.currentPositionMarker.value!!.id)
                MapUtils.showMuseumInfo(viewModel.activeMarkerData[it.id]!!, binding.root, this)
        }
    }


    override fun permissionsRequestCode(): Int {
        return viewModel.permissionsRequestCode()
    }


    override fun permissions(): Array<String?>? {
        return viewModel.permissions()
    }

    override fun requestPermissionsSuccess() {
        viewModel.isGetPermission = true
        Log.i(TAG, "requestPermissionsSuccess")
        initMap()
    }

    private fun requestPermissions(fragment: Fragment, permissionInterface: PermissionInterface): Boolean {
        mPermissionHelper = PermissionHelper(fragment, permissionInterface)
        mPermissionHelper!!.requestPermissions()
        return true
    }


    override fun requestPermissionsFail() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.app_name)
        builder.setMessage(getString(R.string.permissionsAlert))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setNeutralButton(getString(R.string.exit)) { _, _ ->
            requireActivity().finish()
            exitProcess(0)
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        mPermissionHelper!!.requestPermissionsResult(requestCode, grantResults)
    }


    /**
     * move camera function
     */
    private fun moveCamera(latLng: LatLng?, zoom: Float) {
        hMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    /**
     * animate camera function
     */
    private fun animateCamera(latLng: LatLng?, zoom: Float) {
        hMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    /**
     * drawMarker funcion
     */
    private fun drawMarker(markerOptions: MarkerOptions): Marker {
        return hMap!!.addMarker(markerOptions)
    }


    /**
     * Recycler View Adapter set function
     */
    private fun setRecyclerViewAdapter(view: View) {
        viewModel.siteList.value!!.sortWith { site1: Site, site2: Site -> (site1.distance - site2.distance).toInt() }
        siteListAdapter = SiteListAdapter(viewModel.siteList.value!!, requireContext(), view, viewModel, this)
        listView!!.adapter = siteListAdapter
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        listView!!.layoutManager = linearLayoutManager
        val dividerItemDecoration = DividerItemDecoration(listView!!.context,
                linearLayoutManager.orientation)
        listView!!.addItemDecoration(dividerItemDecoration)
    }

    /**
     * Update distances of every museum in the list according to user's position
     */
    private fun updateRecycleView(currentLocation: Location?) {
        if (siteListAdapter != null) siteListAdapter!!.calculateAndUpdateDistance(currentLocation)
    }

}


