package com.example.museumapplication.ui.home.beacon

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Process
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.museumapplication.R
import com.example.museumapplication.data.Artifact
import com.example.museumapplication.data.FavoriteArtifact
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.utils.services.CloudDBManager
import com.example.museumapplication.utils.virtualGuide.BeaconUtils

class VirtualGuideViewModel(application: Application) : AndroidViewModel(application) {


    val REQUEST_CODE = 8488
    val THREAD_SLEEP_TIME = 500

    val context: Context = application.applicationContext
    val beaconUtils = BeaconUtils(context, this)
    private val ttsUtils = UserLoggedIn.instance.ttsUtils

    var currentArtifact = MutableLiveData<Artifact>(null)
    var currentMuseum = MutableLiveData<String>(null)
    var isArtifactFavored = MutableLiveData(false)

    var navigateToHome = MutableLiveData(false)

    companion object {
        @SuppressLint("UseCompatLoadingForDrawables")
        @BindingAdapter("image")
        @JvmStatic
        fun setImage(view: ImageView, image: String?) {
            if (image == null)
                view.setImageDrawable(view.context.getDrawable(R.drawable.noimage))
            else {
                val byteArray = Base64.decode(image, Base64.DEFAULT)
                view.setImageBitmap(BitmapFactory.decodeByteArray(byteArray,0,byteArray.size))
            }
        }

        @BindingAdapter("currentArtifactId")
        @JvmStatic
        fun favoriteButtonChangeColor(view: ImageView, isArtifactFavored: Boolean) {
            if (isArtifactFavored)
                view.setColorFilter(view.context.resources.getColor(R.color.color_gold), PorterDuff.Mode.SRC_IN)
            else
                view.setColorFilter(view.context.resources.getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN)
        }
    }

    fun startTTS(view: View) {
        if (currentArtifact.value != null)
            ttsUtils.startTTSreading(currentArtifact.value!!.artifactDescription.toString())
    }

    fun stopTTS(view: View) {
        ttsUtils.stopTTSreading()
    }

    fun favoriteButtonClick(view: View) {
        val toBeFavored = currentArtifact.value

        if (toBeFavored != null) {
            val temp = UserLoggedIn.instance.getArtifactFavorite(toBeFavored.artifactID)
            if (temp == null) {
                toBeFavored.favoriteCount += 1
                currentArtifact.postValue(currentArtifact.value)

                UserLoggedIn.instance.favoriteArtifactList.add(
                        FavoriteArtifact(
                                toBeFavored.artifactID,
                                toBeFavored.artifactName,
                                toBeFavored.artifactDescription.toString(),
                                toBeFavored.artifactImage.toString(),
                                (CloudDBManager.instance.getMuseum(toBeFavored.museumID))!!.museumName,
                                toBeFavored.category))

                UserLoggedIn.instance.saveFavoriteArtifactListToDevice(context)

                isArtifactFavored.postValue(true)

                CloudDBManager.instance.increaseFavoredCountArtifact(toBeFavored.artifactID)
            } else {
                toBeFavored.favoriteCount -= 1
                currentArtifact.postValue(currentArtifact.value)

                UserLoggedIn.instance.favoriteArtifactList.remove(temp)
                UserLoggedIn.instance.saveFavoriteArtifactListToDevice(context)

                isArtifactFavored.postValue(false)
                CloudDBManager.instance.decreaseFavoredCountArtifact(toBeFavored.artifactID)
            }
        }
    }

    fun permissions(): Array<String?> {
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

    private fun operateConnectivityAction() {
        val `object` = context.getSystemService(Context.CONNECTIVITY_SERVICE)
        if (`object` !is ConnectivityManager) {
            showWarnDialog("Network Warn", context)
            return
        }
        val activeNetworkInfo = `object`.activeNetworkInfo
        if (activeNetworkInfo == null) {
            showWarnDialog("Network Warn", context)
            return
        }
        when (activeNetworkInfo.type) {
            ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI -> {
            }
            else -> {
                showWarnDialog("Network Warn", context)
            }
        }
    }

    val stateChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.P)
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("BeaconFragment", "Start StatusMonitoring.onReceive")
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    showWarnDialog(context.resources.getString(R.string.bluetooth_error), context)
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)
                    if (blueState == BluetoothAdapter.STATE_OFF) {
                        showWarnDialog(context.resources.getString(R.string.bluetooth_error), context)
                    }
                }
                ConnectivityManager.CONNECTIVITY_ACTION -> {
                    operateConnectivityAction()
                }
                LocationManager.PROVIDERS_CHANGED_ACTION -> {
                    val `object` = context.getSystemService(Context.LOCATION_SERVICE)
                    if (`object` !is LocationManager) {
                        showWarnDialog(context.resources.getString(R.string.gps_error), context)
                        return
                    }
                    if (!`object`.isLocationEnabled) {
                        showWarnDialog(context.resources.getString(R.string.gps_error), context)
                    }
                }
            }
        }
    }
    fun showWarnDialog(content: String , context: Context) {
        DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> Process.killProcess(Process.myPid()) }
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Warning")
        builder.setMessage(content)
        builder.setNegativeButton("Confirm") { _: DialogInterface, _: Int ->
            navigateToHome.postValue(true)
        }
        builder.show()
    }

}