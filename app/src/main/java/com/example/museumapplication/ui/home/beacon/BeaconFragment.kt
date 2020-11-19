package com.example.museumapplication.ui.home.beacon

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.graphics.PorterDuff
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.example.museumapplication.R
import com.example.museumapplication.data.Artifact
import com.example.museumapplication.data.FavoriteArtifact
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.utils.*
import com.example.museumapplication.utils.permission.PermissionHelper
import com.example.museumapplication.utils.permission.PermissionInterface
import com.example.museumapplication.utils.services.CloudDBHelper
import com.google.gson.Gson
import java.util.concurrent.Executors

class BeaconFragment : Fragment(), PermissionInterface {
    var mContext: Context? = null
    private var isGetPermission = false
    private var mPermissionHelper: PermissionHelper? = null


    private val stateChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.P)
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("BeaconFragment", "Start StatusMonitoring.onReceive")
            val action = intent.action
            when (action) {
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    showWarnDialog("Bluetooth Warn")
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)
                    if (blueState == BluetoothAdapter.STATE_OFF) {
                        showWarnDialog("Bluetooth Warn")
                    }
                }
                ConnectivityManager.CONNECTIVITY_ACTION -> {
                    operateConnectivityAction(context)
                }
                LocationManager.PROVIDERS_CHANGED_ACTION -> {
                    val `object` = context.getSystemService(Context.LOCATION_SERVICE)
                    if (`object` !is LocationManager) {
                        showWarnDialog("Gps Warn")
                        return
                    }
                    if (!`object`.isLocationEnabled) {
                        showWarnDialog("Gps Warn")
                    }
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                }
                else -> {
                }
            }
        }
    }

    override fun permissionsRequestCode(): Int {
        return REQUEST_CODE
    }

    override fun permissions(): Array<String?>? {
        return arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun requestPermissionsSuccess() {
        isGetPermission = true
        Log.i("BeaconFragment", "requestPermissionsSuccess")
    }

    override fun requestPermissionsFail() {
        //TODO onFail
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        mPermissionHelper!!.requestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        val executorService = Executors.newFixedThreadPool(1)
        executorService.submit {
            while (!isGetPermission) {
                try {
                    Thread.sleep(THREAD_SLEEP_TIME.toLong())
                } catch (e: InterruptedException) {
                    Log.i("BeaconFragment", "Thread sleep error", e)
                }
            }
            registerStatusReceiver()
            view?.let { BeaconUtils.instance.startScanning(requireActivity(), it) }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val beaconViewModel = ViewModelProviders.of(this).get(BeaconViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_beacon, container, false)
        val isSuccess = requestPermissions(this, this)
        if (!isSuccess) {
            return null
        }
        Log.i("BeaconFragment", "requestPermissions success")
        if (!NetCheckUtils.isNetworkAvailable(requireContext())) {
            showWarnDialog("NETWORK_ERROR")
            return null
        }
        if (!BluetoothCheckUtils.isBlueEnabled()) {
            showWarnDialog("BLUETOOTH_ERROR")
            return null
        }
        if (!GpsCheckUtils.isGpsEnabled(requireContext())) {
            showWarnDialog("GPS_ERROR")
            return null
        }
        mContext = context
        val executorService = Executors.newFixedThreadPool(1)
        executorService.submit {
            while (!isGetPermission) {
                try {
                    Thread.sleep(THREAD_SLEEP_TIME.toLong())
                } catch (e: InterruptedException) {
                    Log.i("BeaconFragment", "Thread sleep error", e)
                }
            }
            registerStatusReceiver()
            BeaconUtils.instance.startScanning(requireActivity(), root)

        }
        val playButton = root.findViewById<ImageButton>(R.id.playButton)
        val stopButton = root.findViewById<ImageButton>(R.id.stopButton)
        val descriptionTextView = root.findViewById<TextView>(R.id.descriptionTextView)
        val starArtifactButton = root.findViewById<ImageView>(R.id.starArtifact)
        val favoriteCount = root.findViewById<TextView>(R.id.favoriteCount)

        val ttsUtils = UserLoggedIn.instance.ttsUtils
        playButton.setOnClickListener { if (descriptionTextView.text != "No nearby artifact" && descriptionTextView.text != "") ttsUtils.startTTSreading(descriptionTextView.text as String) }
        stopButton.setOnClickListener { ttsUtils.stopTTSreading() }



        starArtifactButton.setOnClickListener {
            val toBeFavored = BeaconUtils.instance.currentArtifact
            if (toBeFavored != null) {
                val temp = UserLoggedIn.instance.getArtifactFavorite(requireContext(), toBeFavored.artifactID)
                if (temp == null) {
                    favoriteCount.text = (favoriteCount.text.toString().toInt() +1).toString()
                    BeaconUtils.instance.increaseArtifactFavCount(toBeFavored.artifactID)
                    UserLoggedIn.instance.favoriteArtifactList.add(
                            FavoriteArtifact(
                                    toBeFavored.artifactID,
                                    toBeFavored.artifactName,
                                    toBeFavored.artifactDescription.toString(),
                                    toBeFavored.artifactImage.toString(),
                                    (CloudDBHelper.instance.getMuseum(toBeFavored.museumID))!!.museumName,
                                    toBeFavored.category))
                    UserLoggedIn.instance.saveFavoriteArtifactListToDevice(requireContext())
                    (root.findViewById<View>(R.id.starArtifact) as ImageView).setColorFilter(requireContext().resources.getColor(R.color.color_gold), PorterDuff.Mode.SRC_IN)
                    CloudDBHelper.instance.increaseFavoredCountArtifact(toBeFavored.artifactID)
                } else {
                    favoriteCount.text = (favoriteCount.text.toString().toInt() -1).toString()
                    BeaconUtils.instance.decreaseArtifactFavCount(toBeFavored.artifactID)

                    UserLoggedIn.instance.favoriteArtifactList.remove(temp)
                    UserLoggedIn.instance.saveFavoriteArtifactListToDevice(requireContext())
                    (root.findViewById<View>(R.id.starArtifact) as ImageView).setColorFilter(requireContext().resources.getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN)
                    CloudDBHelper.instance.decreaseFavoredCountArtifact(toBeFavored.artifactID)
                }
            }
        }


        requireActivity().application.onTerminate()
        return root

    }


    private fun requestPermissions(fragment: Fragment, permissionInterface: PermissionInterface): Boolean {
        mPermissionHelper = PermissionHelper(fragment, permissionInterface)
        mPermissionHelper!!.requestPermissions()
        return true
    }

    private fun registerStatusReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        mContext!!.registerReceiver(stateChangeReceiver, intentFilter)
    }

    private fun showWarnDialog(content: String) {
        val onClickListener = DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> Process.killProcess(Process.myPid()) }
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Warning")
        builder.setMessage(content)
        builder.setNegativeButton("Confirm", onClickListener)
        builder.show()
    }

    private fun operateConnectivityAction(context: Context) {
        val `object` = context.getSystemService(Context.CONNECTIVITY_SERVICE)
        if (`object` !is ConnectivityManager) {
            showWarnDialog("Network Warn")
            return
        }
        val activeNetworkInfo = `object`.activeNetworkInfo
        if (activeNetworkInfo == null) {
            showWarnDialog("Network Warn")
            return
        }
        val networkType = activeNetworkInfo.type
        when (networkType) {
            ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI -> {
            }
            else -> {
                showWarnDialog("Network Warn")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BeaconUtils.instance.ungetMessageEngine()
    }

    companion object {
        private const val REQUEST_CODE = 8488
        private const val THREAD_SLEEP_TIME = 500
    }
}