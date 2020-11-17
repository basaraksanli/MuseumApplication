package com.example.museumapplication.utils

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
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.example.museumapplication.R
import com.example.museumapplication.data.Artifact
import com.example.museumapplication.data.UserLoggedIn
import com.example.museumapplication.data.Visit
import com.example.museumapplication.utils.services.CloudDBHelper
import com.huawei.agconnect.cloud.database.Text
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hms.common.ApiException
import com.huawei.hms.nearby.Nearby
import com.huawei.hms.nearby.StatusCode
import com.huawei.hms.nearby.discovery.Distance
import com.huawei.hms.nearby.message.*
import com.yashovardhan99.timeit.Stopwatch
import java.util.*


class BeaconUtils {
    var messageEngine: MessageEngine? = null
    var mMessageHandler: MessageHandler? = null
    var currentArtifact :Artifact? = null
    private var visitTime = Stopwatch()
    private var visitObject: Visit? =null
    private val timeout = Stopwatch()

    fun startScanning(activityContext: Context?, root: View) {
        messageEngine = Nearby.getMessageEngine(activityContext)
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
                doOnDistanceChanged(message, distance, root, activityContext)
            }

            override fun onLost(message: Message) {
                super.onLost(message)
                doOnLost(message)
            }
        }
        val msgPicker = MessagePicker.Builder().includeAllTypes().build()
        val policy = Policy.Builder().setTtlSeconds(Policy.POLICY_TTL_SECONDS_INFINITE).build()
        val getOption = GetOption.Builder().setPicker(msgPicker).setPolicy(policy).build()
        Nearby.getMessageEngine(activityContext)[mMessageHandler]
        val task = messageEngine!!.get(mMessageHandler, getOption)
        task.addOnSuccessListener { Toast.makeText(activityContext, "SUCCESS", Toast.LENGTH_SHORT).show() }.addOnFailureListener { e: Exception? ->
            Log.e("Beacon", "Login failed:", e)
            if (e is ApiException) {
                when (e.statusCode) {
                    StatusCode.STATUS_MESSAGE_AUTH_FAILED -> {
                        Toast.makeText(activityContext, "configuration_error", Toast.LENGTH_SHORT).show()
                    }
                    StatusCode.STATUS_MESSAGE_APP_UNREGISTERED -> {
                        Toast.makeText(activityContext, "permission_error", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(activityContext, "start_get_beacon_message_failed", Toast.LENGTH_SHORT)
                                .show()
                    }
                }
            } else {
                Toast.makeText(activityContext, "start_get_beacon_message_failed", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun doOnLost(message: Message?) {
        if (message == null) {
            return
        }
        val type = message.type ?: return
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
        val artifact = CloudDBHelper.instance.queryArtifactByID(messageContent.toInt())
        downloadedArtifacts.add(artifact!!)
    }

    fun doOnDistanceChanged(message: Message?, distance: Distance, root: View, activityContext: Context?) {
        if (message == null) {
            return
        }
        val type = message.type ?: return
        val messageContent = String(message.content)
        Log.wtf("Beacon", "New Message:" + messageContent + " type:" + type + "Distance: " + distance.meters)
        if (type.equals("No", ignoreCase = true)) operateOnDistanceChanged(messageContent, distance, root, activityContext)
    }

    private fun operateOnDistanceChanged(messageContent: String, distance: Distance, root: View, activityContext: Context?) {
        val id = messageContent.toInt()
        artifactDistances[id] = distance
        updateUI(root, activityContext)
    }

    private fun updateUI(root: View, activityContext: Context?) {
        val closestIndex = findClosest()
        val artifactName = root.findViewById<TextView>(R.id.artifactNameTextView)
        val description = root.findViewById<TextView>(R.id.descriptionTextView)
        val imageView = root.findViewById<ImageView>(R.id.imageViewBeacon)


        val sp = PreferenceManager.getDefaultSharedPreferences(activityContext)
        val exhibitRange = sp.getInt("exhibitRange", 2)

        if (closestIndex!!.value.meters < exhibitRange) {
            val closestInfo = findArtifactInformation(closestIndex.key)
            if (closestInfo != null) {

                startStopwatch(closestInfo)

                if(currentArtifact != closestInfo && visitTime.isStarted && currentArtifact !=null) {
                    visitTime.stop()
                    visitObject!!.visitTime = (visitTime.elapsedTime / 1000).toInt()
                    if(timeout.isStarted)
                        timeout.stop()
                    if(visitObject!!.visitTime> 20)
                        CloudDBHelper.instance.upsertVisit(visitObject!!)
                }



                currentArtifact = closestInfo
                artifactName.text = closestInfo.artifactName
                description.text = closestInfo.artifactDescription.toString()
                imageView.setImageBitmap(base64toBitmap(closestInfo.artifactImage))

                if (UserLoggedIn.instance.getArtifactFavorite(root.context, closestInfo.artifactID) != null)
                    (root.findViewById<View>(R.id.starArtifact) as ImageView).setColorFilter(root.context.resources.getColor(R.color.color_gold), PorterDuff.Mode.SRC_IN)
                else
                    (root.findViewById<View>(R.id.starArtifact) as ImageView).setColorFilter(root.context.resources.getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN)

            }
        } else {

            val oldArtifact = currentArtifact
            timeout.setOnTickListener {
                if (currentArtifact == oldArtifact) {
                    timeout.stop()
                }
                if(it.elapsedTime == 10000L) {
                    visitTime.stop()
                    visitObject!!.visitTime = (visitTime.elapsedTime / 1000).toInt()
                    timeout.stop()
                    if(visitObject!!.visitTime> 20)
                        CloudDBHelper.instance.upsertVisit(visitObject!!)
                }
            }
            currentArtifact != null
            artifactName.setText(R.string.no_nearby_artifact)
            description.setText(R.string.no_nearby_artifact)
            imageView.setImageResource(R.drawable.noimage)
            (root.findViewById<View>(R.id.starArtifact) as ImageView).setColorFilter(root.context.resources.getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN)
        }
    }

    private fun startStopwatch(closestInfo: Artifact){
        if(!visitTime.isStarted) {
            visitTime.start()
            visitObject = Visit()
            visitObject!!.userID = UserLoggedIn.instance.uID
            visitObject!!.artifactID = closestInfo.artifactID
            visitObject!!.date = Date(System.currentTimeMillis())
        }
    }

    private fun base64toBitmap(imageCode: Text): Bitmap {
        val decodedString = Base64.decode(imageCode.toString(), Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
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

    companion object {
        val instance = BeaconUtils()
        private val downloadedArtifacts: MutableList<Artifact> = ArrayList()
        private val artifactDistances = HashMap<Int, Distance>()
    }
}