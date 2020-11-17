package com.example.museumapplication.utils;

import android.os.Bundle;

import com.huawei.hms.mlsdk.tts.MLTtsCallback;
import com.huawei.hms.mlsdk.tts.MLTtsConfig;
import com.huawei.hms.mlsdk.tts.MLTtsConstants;
import com.huawei.hms.mlsdk.tts.MLTtsEngine;
import com.huawei.hms.mlsdk.tts.MLTtsError;
import com.huawei.hms.mlsdk.tts.MLTtsWarn;

public class TTSUtils {
    MLTtsConfig mlTtsConfig;
    MLTtsEngine mlTtsEngine;

    public TTSUtils() {
        mlTtsConfig = new MLTtsConfig()
                .setLanguage(MLTtsConstants.TTS_EN_US)
                .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_EN)
                .setSpeed(1.0f)
                .setVolume(1.0f);
        mlTtsEngine = new MLTtsEngine(mlTtsConfig);
        mlTtsEngine.updateConfig(mlTtsConfig);

        MLTtsCallback callback = new MLTtsCallback() {
            @Override
            public void onError(String s, MLTtsError mlTtsError) {

            }

            @Override
            public void onWarn(String s, MLTtsWarn mlTtsWarn) {

            }

            @Override
            public void onRangeStart(String s, int i, int i1) {

            }

            @Override
            public void onEvent(String s, int i, Bundle bundle) {
                switch (i) {
                    case MLTtsConstants.EVENT_PLAY_START:
                        // Called when playback starts.
                        break;
                    case MLTtsConstants.EVENT_PLAY_STOP:
                        // Called when playback stops.
                        boolean isInterrupted = bundle.getBoolean(MLTtsConstants.EVENT_PLAY_STOP_INTERRUPTED);
                        break;
                    case MLTtsConstants.EVENT_PLAY_RESUME:
                        // Called when playback resumes.
                        break;
                    case MLTtsConstants.EVENT_PLAY_PAUSE:
                        // Called when playback pauses.
                        break;

                }
            }
        };
        mlTtsEngine.setTtsCallback(callback);
    }
    public void startTTSreading(String text){
        mlTtsEngine.stop();
        String[] sentences= text.split("\n|\\.(?!\\d)|(?<!\\d)\\.");

        for(String sentence: sentences)
            mlTtsEngine.speak(sentence, MLTtsEngine.QUEUE_APPEND);
    }
    public void stopTTSreading(){
        mlTtsEngine.stop();
    }
    public void destroyTTSreading(){
        mlTtsEngine.shutdown();
    }
}
