package com.example.museumapplication.utils;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;


public class JsonUtils {
    private static final String TAG = "JsonUtils";

    private static Gson sGsonInstance;

    private static Gson getGsonInstance() {
        if (sGsonInstance == null) {
            synchronized (JsonUtils.class) {
                if (sGsonInstance == null) {
                    sGsonInstance = getGsonBuilder().create();
                }
            }
        }
        return sGsonInstance;
    }

    private static GsonBuilder getGsonBuilder() {
        return new GsonBuilder()
                .enableComplexMapKeySerialization()
                .serializeSpecialFloatingPointValues()
                .disableHtmlEscaping()
                .setLenient();
    }

    /**
     * JSON to Object
     *
     * @param json        json String
     * @param objectClass objectClass
     * @return Object
     */
    public static Object json2Object(String json, Class objectClass) {
        if (TextUtils.isEmpty(json) || objectClass == null) {
            return null;
        }
        try {
            return getGsonInstance().fromJson(json, objectClass);
        } catch (JsonParseException e) {
            return null;
        }
    }

    /**
     * Object to Json
     *
     * @param object object
     * @return Json String
     */
    public static String object2Json(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return getGsonInstance().toJson(object);
        } catch (JsonParseException e) {
            e.printStackTrace();
            Log.e(TAG, "JsonParseException:" + e.getMessage());
            return null;
        }
    }
}