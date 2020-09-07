package com.example.museumapplication.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.museumapplication.R;
import com.example.museumapplication.data.Artifact;
import com.example.museumapplication.utils.Services.CloudDBHelper;
import com.huawei.agconnect.cloud.database.Text;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.nearby.Nearby;
import com.huawei.hms.nearby.StatusCode;
import com.huawei.hms.nearby.discovery.Distance;
import com.huawei.hms.nearby.message.GetOption;
import com.huawei.hms.nearby.message.Message;
import com.huawei.hms.nearby.message.MessageEngine;
import com.huawei.hms.nearby.message.MessageHandler;
import com.huawei.hms.nearby.message.MessagePicker;
import com.huawei.hms.nearby.message.Policy;
import com.huawei.hms.nearby.message.StatusCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BeaconUtils {
    private static BeaconUtils instance = new BeaconUtils();
    public MessageEngine messageEngine;
    public MessageHandler mMessageHandler;
    private static List<Artifact> downloadedArtifacts = new ArrayList<>();
    private static HashMap<Integer, Distance> artifactDistances = new HashMap<>();

    public BeaconUtils() {
    }

    public static BeaconUtils getInstance() {
        return instance;
    }

    public void startScanning(Context activityContext, View root) {
        messageEngine = Nearby.getMessageEngine(activityContext);
        messageEngine.registerStatusCallback(
                new StatusCallback() {
                    @Override
                    public void onPermissionChanged(boolean isPermissionGranted) {
                        super.onPermissionChanged(isPermissionGranted);
                        Log.i("Beacon", "onPermissionChanged:" + isPermissionGranted);
                    }
                });


        mMessageHandler = new MessageHandler() {
            @Override
            public void onFound(Message message) {
                super.onFound(message);
                try {
                    doOnFound(message);
                } catch (AGConnectCloudDBException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDistanceChanged(Message message, Distance distance) {
                super.onDistanceChanged(message, distance);
                doOnDistanceChanged(message, distance, root);
            }
        };

        MessagePicker msgPicker = new MessagePicker.Builder().includeAllTypes().build();
        Policy policy = new Policy.Builder().setTtlSeconds(Policy.POLICY_TTL_SECONDS_INFINITE).build();
        GetOption getOption = new GetOption.Builder().setPicker(msgPicker).setPolicy(policy).build();

        Nearby.getMessageEngine(activityContext).get(mMessageHandler);

        Task<Void> task = messageEngine.get(mMessageHandler, getOption);
        task.addOnSuccessListener(aVoid -> {
            Toast.makeText(activityContext, "SUCCESS", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(
                e -> {
                    Log.e("Beacon", "Login failed:", e);
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        int errorStatusCode = apiException.getStatusCode();
                        if (errorStatusCode == StatusCode.STATUS_MESSAGE_AUTH_FAILED) {
                            Toast.makeText(activityContext, "configuration_error", Toast.LENGTH_SHORT).show();
                        } else if (errorStatusCode == StatusCode.STATUS_MESSAGE_APP_UNREGISTERED) {
                            Toast.makeText(activityContext, "permission_error", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activityContext, "start_get_beacon_message_failed", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } else {
                        Toast.makeText(activityContext, "start_get_beacon_message_failed", Toast.LENGTH_SHORT)
                                .show();
                    }
                });

    }

    public void ungetMessageEngine() {
        if (messageEngine != null && mMessageHandler != null) {
            Log.i("Beacon", "unget");
            messageEngine.unget(mMessageHandler);
        }
    }


    public void doOnFound(Message message) throws AGConnectCloudDBException {
        if (message == null) {
            return;
        }
        String type = message.getType();
        if (type == null) {
            return;
        }
        String messageContent = new String(message.getContent());
        Log.wtf("Beacon", "New Message:" + messageContent + " type:" + type);
        if(type.equalsIgnoreCase("No"))
            downloadArtifact(messageContent);
    }

    private void downloadArtifact(String messageContent) throws AGConnectCloudDBException {
        Artifact artifact = CloudDBHelper.getInstance().queryArtifactByID(Integer.parseInt(messageContent));
        downloadedArtifacts.add(artifact);
    }
    public void doOnDistanceChanged(Message message, Distance distance, View root){
        if (message == null) {
            return;
        }
        String type = message.getType();
        if (type == null) {
            return;
        }
        String messageContent = new String(message.getContent());
        Log.d("Beacon", "New Message:" + messageContent + " type:" + type + "Distance: "+ distance);
        if(type.equalsIgnoreCase("No"))
            operateOnDistanceChanged(messageContent, distance, root);
    }

    private void operateOnDistanceChanged(String messageContent, Distance distance, View root) {
        int id = Integer.parseInt(messageContent);
        artifactDistances.put(id,distance);
        updateUI(root);
    }
    private void updateUI(View root){
        Map.Entry<Integer, Distance> closestIndex = findClosest();
        TextView artifactName = root.findViewById(R.id.artifactNameTextView);
        TextView description = root.findViewById(R.id.descriptionTextView);
        ImageView imageView = root.findViewById(R.id.imageViewBeacon);
        if(closestIndex.getValue().getMeters() < 2)
        {
            Artifact closestInfo =findArtifactInformation(closestIndex.getKey());
            if(closestInfo!=null)
            {
                artifactName.setText(closestInfo.getArtifactName());
                description.setText(closestInfo.getArtifactDescription().toString());
                imageView.setImageBitmap(base64toBitmap(closestInfo.getArtifactImage()));
            }
        }
        else{
            artifactName.setText(R.string.no_nearby_artifact);
            description.setText(R.string.no_nearby_artifact);
            imageView.setImageResource(R.drawable.noimage);
        }
    }
    private Bitmap base64toBitmap(Text imageCode){
        byte[] decodedString = Base64.decode(imageCode.toString(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
    private Artifact findArtifactInformation(int ID){
        for(Artifact o : downloadedArtifacts)
        {
            if(o.getArtifactID()==ID)
                return o;
        }
        return null;
    }
    private Map.Entry<Integer, Distance> findClosest(){
        Map.Entry<Integer, Distance> closest = null;

        for (Map.Entry<Integer, Distance> entry : artifactDistances.entrySet())
        {
            if (closest == null || entry.getValue().compareTo(closest.getValue()) < 0)
            {
                closest = entry;
            }
        }
        return closest;
    }
}
