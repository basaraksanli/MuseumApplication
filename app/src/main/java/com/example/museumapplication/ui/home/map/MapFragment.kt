package com.example.museumapplication.ui.home.map

import android.annotation.SuppressLint
import android.graphics.*
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapplication.R
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.databinding.FragmentMapBinding
import com.example.museumapplication.utils.settings.SettingsUtils
import com.example.museumapplication.utils.map.SiteListAdapter
import com.example.museumapplication.utils.map.MapUtils
import com.example.museumapplication.utils.permission.PermissionHelper
import com.example.museumapplication.utils.permission.PermissionInterface
import com.google.android.material.snackbar.Snackbar
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.*
import com.huawei.hms.site.api.model.Coordinate
import com.huawei.hms.site.api.model.Site
import kotlin.system.exitProcess

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

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {



        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        viewModel.mapUtils.resetInfo()

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mMapView = binding.root.findViewById(R.id.mapView)
        listView = binding.root.findViewById(R.id.siteList)


        val isSuccess = requestPermissions(this, this)
        if (!isSuccess) {
            return null
        }

        //Scan museum warning
        viewModel.scanMuseumWarning.observe(viewLifecycleOwner, {
            if (it == true) {
                Snackbar.make(binding.root, getString(R.string.please_scan_museum_warning), Snackbar.LENGTH_LONG).show()
                viewModel.scanMuseumWarning.value = false
            }
        })

        viewModel.focusToCurrentLocation.observe(viewLifecycleOwner, {
            if (it == true) {
                animateCamera(LatLng(viewModel.currentLocation.value!!.latitude, viewModel.currentLocation.value!!.longitude), 15f)
                viewModel.focusToCurrentLocation.value = false
            }
        })

        viewModel.siteList.observe(viewLifecycleOwner, {
            if (isMapReady) {
                setRecyclerViewAdapter(binding.root)
                for (site: Site in it)
                    viewModel.activeMarkers.value!!.add(drawMarker(viewModel.mapUtils.createMuseumMarkerOptions(site)))
            }
        })

        viewModel.animateCameraLatLng.observe(viewLifecycleOwner, {
            animateCamera(it, 15f)
        })

        viewModel.initializeSiteList.observe(viewLifecycleOwner, {
            if (it == true) {
                setRecyclerViewAdapter(binding.root)
                viewModel.changeMapSize(null)
            }
        })

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


        return binding.root
    }

    private fun handleFirstLocation(location: Location){
        if (requireActivity().intent.extras == null)
            moveCamera(LatLng(location.latitude, location.longitude), 15f)
        else {
            if (requireActivity().intent.getStringExtra("MuseumName") != null) {
                val museumName = requireActivity().intent.getStringExtra("MuseumName")
                val siteToFocus = viewModel.mapUtils.findSiteByName(museumName!!)
                if (siteToFocus != null)
                    moveCamera(LatLng(siteToFocus.location.lat, siteToFocus.location.lng), 15f)
                else moveCamera(LatLng(location.latitude, location.longitude), 15f)
            } else {
                val coordinate = Coordinate(requireActivity().intent.getDoubleExtra("favoriteLocationLat", 0.0),
                        requireActivity().intent.getDoubleExtra("favoriteLocationLng", 0.0))
                moveCamera(LatLng(coordinate.lat, coordinate.lng), 15f)
            }
        }
        viewModel.currentPositionMarker.value = drawMarker(viewModel.mapUtils.getUserMarkerOptions(location, requireActivity())!!)
    }



    private fun initMap() {
        val mapViewBundle: Bundle? = null

        mMapView!!.onCreate(mapViewBundle)
        mMapView!!.getMapAsync(this)
    }

    override fun onMapReady(map: HuaweiMap) {
        Log.d(TAG, "onMapReady: ")
        hMap = map
        isMapReady= true

        hMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),
                SettingsUtils.mapStyleDark(requireActivity())))

        hMap!!.setMaxZoomPreference(20.0f)
        hMap!!.setMinZoomPreference(6.0f)
        hMap!!.isIndoorEnabled = true
        viewModel.retrieveSiteList()
        viewModel.startLocationTrack()
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
    fun requestPermissions(fragment: Fragment, permissionInterface: PermissionInterface): Boolean {
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
            exitProcess(0);
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        mPermissionHelper!!.requestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun moveCamera(latLng: LatLng?, zoom: Float) {
        hMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun animateCamera(latLng: LatLng?, zoom: Float) {
        hMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun drawMarker(markerOptions: MarkerOptions): Marker {
        return hMap!!.addMarker(markerOptions)
    }


    private fun setRecyclerViewAdapter(view: View) {
        viewModel.siteList.value!!.sortWith { site1: Site, site2: Site -> (site1.distance - site2.distance).toInt() }
        siteListAdapter = SiteListAdapter(viewModel.siteList.value!!, requireContext(), view, viewModel)
        listView!!.adapter = siteListAdapter
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        listView!!.layoutManager = linearLayoutManager
        val dividerItemDecoration = DividerItemDecoration(listView!!.context,
                linearLayoutManager.orientation)
        listView!!.addItemDecoration(dividerItemDecoration)
    }

    private fun updateRecycleView(currentLocation: Location?) {
        if (siteListAdapter != null) siteListAdapter!!.calculateAndUpdateDistance(currentLocation)
    }

}


