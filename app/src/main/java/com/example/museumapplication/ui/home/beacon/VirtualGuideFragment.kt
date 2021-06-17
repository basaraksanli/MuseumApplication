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

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.museumapplication.R
import com.example.museumapplication.databinding.FragmentVirtualGuideBinding
import com.example.museumapplication.ui.home.HomeActivity
import com.example.museumapplication.utils.GeneralUtils
import com.example.museumapplication.utils.permission.PermissionHelper
import com.example.museumapplication.utils.permission.PermissionInterface
import com.example.museumapplication.utils.virtual_guide.BluetoothCheckUtils
import com.example.museumapplication.utils.virtual_guide.GpsCheckUtils
import com.example.museumapplication.utils.virtual_guide.NetCheckUtils
import java.util.concurrent.Executors

class VirtualGuideFragment : Fragment(), PermissionInterface {
    var mContext: Context? = null
    private var isGetPermission = false
    private var mPermissionHelper: PermissionHelper? = null
    private var viewModel: VirtualGuideViewModel? = null
    val TAG = "Virtual Guide Fragment"


    /**
     * Virtual Guide Fragment
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {


        val binding = DataBindingUtil.inflate<FragmentVirtualGuideBinding>(inflater, R.layout.fragment_virtual_guide, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(VirtualGuideViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this


        /**
         * At the beginning permissions are checked
         */
        val isSuccess = requestPermissions(this, this)
        if (!isSuccess) {
            return null
        }
        /**
         * Network Bluetooth and GPS are checked
         */
        Log.i(TAG, "requestPermissions success")
        if (!NetCheckUtils.isNetworkAvailable(requireContext())) {
            GeneralUtils.showWarnDialog(getString(R.string.network_error), requireContext(), viewModel!!.navigateToHome)
        }
        if (!BluetoothCheckUtils.isBlueEnabled()) {
            GeneralUtils.showWarnDialog(getString(R.string.bluetooth_error), requireContext(), viewModel!!.navigateToHome)
        }
        if (!GpsCheckUtils.isGpsEnabled(requireContext())) {
            GeneralUtils.showWarnDialog(getString(R.string.gps_error), requireContext(), viewModel!!.navigateToHome)
        }
        mContext = context

        viewModel!!.navigateToHome.observe(viewLifecycleOwner, Observer {
            if (it) {
                navigateToHomePage()
                viewModel!!.navigateToHome.postValue(false)
            }
        })

        requireActivity().application.onTerminate()
        return binding.root

    }


    /**
     * State Changes are listened in this fragment
     */
    private fun registerStatusReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        mContext!!.registerReceiver(viewModel!!.stateChangeReceiver, intentFilter)
    }

    /**
     * Beacon detection is disabled whenever this fragment is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        viewModel!!.beaconUtils.ungetMessageEngine()
    }

    /**
     * Check permissions and then Register the state listener
     * Start Scanning for Beacons
     */
    override fun onResume() {
        super.onResume()
        val executorService = Executors.newFixedThreadPool(1)
        executorService.submit {
            while (!isGetPermission) {
                try {
                    Thread.sleep(viewModel!!.THREAD_SLEEP_TIME.toLong())
                } catch (e: InterruptedException) {
                    Log.i("BeaconFragment", "Thread sleep error", e)
                }
            }
            registerStatusReceiver()
            viewModel!!.beaconUtils.startScanning(requireActivity())
        }
    }

    override fun permissionsRequestCode(): Int {
        return viewModel!!.REQUEST_CODE
    }

    override fun permissions(): Array<String?> {
        return viewModel!!.permissions()
    }

    override fun requestPermissionsSuccess() {
        isGetPermission = true
        Log.i("BeaconFragment", "requestPermissionsSuccess")
    }

    override fun requestPermissionsFail() {
        Log.e(TAG, "Request Permission Fail")
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        mPermissionHelper!!.requestPermissionsResult(requestCode, grantResults)
    }

    private fun requestPermissions(fragment: Fragment, permissionInterface: PermissionInterface): Boolean {
        mPermissionHelper = PermissionHelper(fragment, permissionInterface)
        mPermissionHelper!!.requestPermissions()
        return true
    }

    private fun navigateToHomePage(){
        val homeActivity = Intent(activity, HomeActivity::class.java)
        requireActivity().startActivity(homeActivity)
    }
}