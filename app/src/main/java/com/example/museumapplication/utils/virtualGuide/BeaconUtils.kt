package com.example.museumapplication.utils.virtualGuide

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.museumapplication.R
import com.example.museumapplication.data.Artifact
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.data.Visit
import com.example.museumapplication.ui.home.beacon.VirtualGuideViewModel
import com.example.museumapplication.utils.services.CloudDBManager
import com.huawei.agconnect.cloud.database.Text
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hms.common.ApiException
import com.huawei.hms.nearby.Nearby
import com.huawei.hms.nearby.StatusCode
import com.huawei.hms.nearby.discovery.Distance
import com.huawei.hms.nearby.message.*
import com.yashovardhan99.timeit.Stopwatch
import java.util.*


class BeaconUtils (val context: Context, val viewModel: VirtualGuideViewModel){
    var messageEngine: MessageEngine? = null
    var mMessageHandler: MessageHandler? = null
    private var visitTime = Stopwatch()
    private var visitObject: Visit? =null
    private val timeout = Stopwatch()

    companion object {
        private val downloadedArtifacts: MutableList<Artifact> = ArrayList()
        private val artifactDistances = HashMap<Int, Distance>()
    }

    fun startScanning(activity: Activity) {
        messageEngine = Nearby.getMessageEngine(activity)
        messageEngine!!.registerStatusCallback(
                object : StatusCallback() {
                    override fun onPermissionChanged(isPermissionGranted: Boolean) {
                        super.onPermissionChanged(isPermissionGranted)
                        Log.i("Beacon", "onPermissionChanged:$isPermissionGranted")
                    }
                })
        mMessageHandler = object : MessageHandler() {
            override fun onFound(message: Message) {
                super.onFound(message)
                try {
                    doOnFound(message)
                } catch (e: AGConnectCloudDBException) {
                    e.printStackTrace()
                }
            }
            override fun onDistanceChanged(message: Message, distance: Distance) {
                super.onDistanceChanged(message, distance)
                doOnDistanceChanged(message, distance)
            }
            override fun onLost(message: Message) {
                super.onLost(message)
                doOnLost(message)
            }
        }
        val msgPicker = MessagePicker.Builder().includeAllTypes().build()
        val policy = Policy.Builder().setTtlSeconds(Policy.POLICY_TTL_SECONDS_INFINITE).build()
        val getOption = GetOption.Builder().setPicker(msgPicker).setPolicy(policy).build()
        Nearby.getMessageEngine(activity)[mMessageHandler]
        val task = messageEngine!!.get(mMessageHandler, getOption)
        task.addOnSuccessListener { Toast.makeText(context, "SUCCESS", Toast.LENGTH_SHORT).show() }.addOnFailureListener { e: Exception? ->
            Log.e("Beacon", "Login failed:", e)
            if (e is ApiException) {
                when (e.statusCode) {
                    StatusCode.STATUS_MESSAGE_AUTH_FAILED -> {
                        Toast.makeText(context, "configuration_error", Toast.LENGTH_SHORT).show()
                    }
                    StatusCode.STATUS_MESSAGE_APP_UNREGISTERED -> {
                        Toast.makeText(context, "permission_error", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(context, "start_get_beacon_message_failed", Toast.LENGTH_SHORT)
                                .show()
                    }
                }
            } else {
                Toast.makeText(context, "start_get_beacon_message_failed", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun doOnLost(message: Message?) {
        if (message == null) {
            return
        }
        val messageContent = String(message.content)
        val id = messageContent.toInt()
        artifactDistances.remove(id)
        downloadedArtifacts.removeAt(id)
    }

    fun ungetMessageEngine() {
        if (messageEngine != null && mMessageHandler != null) {
            Log.i("Beacon", "unget")
            messageEngine!!.unget(mMessageHandler)
        }
    }

    @Throws(AGConnectCloudDBException::class)
    fun doOnFound(message: Message?) {
        if (message == null) {
            return
        }
        val type = message.type ?: return
        val messageContent = String(message.content)
        Log.wtf("Beacon", "New Message:$messageContent type:$type")
        if (type.equals("No", ignoreCase = true)) downloadArtifact(messageContent)
    }

    @Throws(AGConnectCloudDBException::class)
    private fun downloadArtifact(messageContent: String) {
        val artifact = CloudDBManager.instance.queryArtifactByID(messageContent.toInt())
        downloadedArtifacts.add(artifact!!)
    }

    fun doOnDistanceChanged(message: Message?, distance: Distance) {
        if (message == null) {
            return
        }
        val type = message.type ?: return
        val messageContent = String(message.content)
        Log.wtf("Beacon", "New Message:" + messageContent + " type:" + type + "Distance: " + distance.meters)
        if (type.equals("No", ignoreCase = true)) operateOnDistanceChanged(messageContent, distance)
    }

    private fun operateOnDistanceChanged(messageContent: String, distance: Distance) {
        val id = messageContent.toInt()
        artifactDistances[id] = distance
        updateUI()
    }

    private fun updateUI() {
        val closestIndex = findClosest()

        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val exhibitRange = sp.getInt("exhibitRange", 2)

        if (closestIndex!!.value.meters < exhibitRange) {
            val closestInfo = findArtifactInformation(closestIndex.key)
            if (closestInfo != null) {
                startStopwatch(closestInfo)
                if(viewModel.currentArtifact.value != closestInfo && visitTime.isStarted && viewModel.currentArtifact.value !=null) {
                    visitTime.stop()
                    visitObject!!.visitTime = (visitTime.elapsedTime / 1000).toInt()
                    if(timeout.isStarted)
                        timeout.stop()
                    if(visitObject!!.visitTime> 20)
                        CloudDBManager.instance.upsertVisit(visitObject!!)
                }
                viewModel.currentArtifact.value = closestInfo

                viewModel.currentArtifact.value = closestInfo
                viewModel.currentMuseum.value = CloudDBManager.instance.getMuseum(closestInfo.museumID)!!.museumName

                if (UserLoggedIn.instance.getArtifactFavorite( closestInfo.artifactID ) != null)
                    viewModel.isArtifactFavored.postValue(true)
                else
                    viewModel.isArtifactFavored.postValue(false)

            }
        } else {
            val oldArtifact = viewModel.currentArtifact.value
            timeout.setOnTickListener {
                if (viewModel.currentArtifact.value == oldArtifact) {
                    timeout.stop()
                }
                if(it.elapsedTime == 10000L) {
                    visitTime.stop()
                    visitObject!!.visitTime = (visitTime.elapsedTime / 1000).toInt()
                    timeout.stop()
                    if(visitObject!!.visitTime> 20)
                        CloudDBManager.instance.upsertVisit(visitObject!!)
                }
            }
            viewModel.currentArtifact.value = null
            viewModel.isArtifactFavored.postValue(false)
        }
    }

    private fun startStopwatch(closestInfo: Artifact){
        if(!visitTime.isStarted) {
            visitTime.start()
            visitObject = Visit()
            visitObject!!.userID = UserLoggedIn.instance.uID
            visitObject!!.artifactID = closestInfo.artifactID
            visitObject!!.date = Calendar.getInstance().time
            visitObject!!.museumID = closestInfo.museumID

        }
    }

    private fun findArtifactInformation(ID: Int): Artifact? {
        for (o in downloadedArtifacts) {
            if (o.artifactID == ID) return o
        }
        return null
    }

    private fun findClosest(): Map.Entry<Int, Distance>? {
        var closest: Map.Entry<Int, Distance>? = null
        for (entry in artifactDistances.entries) {
            if (closest == null || entry.value < closest.value) {
                closest = entry
            }
        }
        return closest
    }

}