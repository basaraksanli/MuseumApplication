package com.example.museumapplication.utils.virtualGuide;

import android.os.Bundle;

import com.example.museumapplication.data.Constant;
import com.huawei.hms.mlsdk.tts.MLTtsCallback;
import com.huawei.hms.mlsdk.tts.MLTtsConfig;
import com.huawei.hms.mlsdk.tts.MLTtsConstants;
import com.huawei.hms.mlsdk.tts.MLTtsEngine;
import com.huawei.hms.mlsdk.tts.MLTtsError;
import com.huawei.hms.mlsdk.tts.MLTtsWarn;

public class TTSUtils {
    MLTtsConfig mlTtsConfig;
    MLTtsEngine mlTtsEngine;

    /**
     * Text to speech service initialization
     */
    public TTSUtils() {
        mlTtsConfig = new MLTtsConfig()
                .setLanguage(MLTtsConstants.TTS_EN_US)
                .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_EN)
                .setSpeed(Constant.TTS_SPEED)
                .setVolume(Constant.TTS_VOLUME);
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

    /**
     * Starts TTS reading
     * @param text
     */
    public void startTTSreading(String text){
        mlTtsEngine.stop();
        String[] sentences= text.split("\n|\\.(?!\\d)|(?<!\\d)\\.");

        for(String sentence: sentences)
            mlTtsEngine.speak(sentence, MLTtsEngine.QUEUE_APPEND);
    }

    /**
     * Stops TTS reading
     */
    public void stopTTSreading(){
        mlTtsEngine.stop();
    }

    /**
     * Shutdowns TTS
     */
    public void destroyTTSreading(){
        mlTtsEngine.shutdown();
    }
}
