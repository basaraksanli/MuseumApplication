package com.example.museumapplication.data

import android.content.Context
import android.graphics.Bitmap
import com.example.museumapplication.utils.TTSUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class UserLoggedIn {
    var uID: String? = null
    private var providerUID: String? = null
    var name: String? = null
        private set
    var email: String? = null
        private set
    var photoUrl: String? = null
    var profilePicture: Bitmap? = null
    var favoriteMuseumList =  arrayListOf<FavoriteMuseum>()
    var favoriteArtifactList = arrayListOf<FavoriteArtifact>()
    val ttsUtils = TTSUtils()



    fun setUser(user: User) {
        uID = user.uid
        providerUID = user.providerUID
        name = user.displayName
        email = user.email
        photoUrl = user.photoURL
    }

    companion object {
        //Singleton
        val instance = UserLoggedIn()
    }

    fun retrieveFavoriteMuseumList(context: Context) {
        val mPrefs = context.getSharedPreferences("FavoriteMuseum", Context.MODE_PRIVATE)
        val favoriteMuseumListJson = mPrefs.getString("museumList", "")
        val typeFavoriteMuseum = object : TypeToken<List<FavoriteMuseum?>?>() {}.type
        val gsonBuilder = GsonBuilder()
        val gson = gsonBuilder.create()
        if (favoriteMuseumListJson!!.isNotEmpty()) {
            favoriteMuseumList = gson.fromJson(favoriteMuseumListJson, typeFavoriteMuseum)
        }
    }

    fun retrieveFavoriteArtifactList(context: Context) {
        val mPrefs = context.getSharedPreferences("FavoriteArtifact", Context.MODE_PRIVATE)
        val favoriteArtifactListJson = mPrefs.getString("artifactList", "")
        val typeFavoriteArtifact = object : TypeToken<List<FavoriteArtifact?>?>() {}.type
        val gsonBuilder = GsonBuilder()
        val gson = gsonBuilder.create()
        if (favoriteArtifactListJson!!.isNotEmpty()) {
            favoriteArtifactList = gson.fromJson(favoriteArtifactListJson, typeFavoriteArtifact)
        }
    }

    fun saveFavoriteMuseumListToDevice(context: Context) {
        val mPrefs = context.getSharedPreferences("FavoriteMuseum", Context.MODE_PRIVATE)
        val prefsEditor = mPrefs.edit()
        prefsEditor.clear()
        val gson = Gson()
        val museumListJson = gson.toJson(favoriteMuseumList)
        prefsEditor.putString("museumList", museumListJson)
        prefsEditor.apply()
    }
    fun getMuseumFavorite(context: Context, museumName: String): FavoriteMuseum? {
        for (museum : FavoriteMuseum in favoriteMuseumList)
            if(museumName == museum.museumName)
                return museum
        return null
    }



    fun saveFavoriteArtifactListToDevice(context: Context) {
        val mPrefs = context.getSharedPreferences("FavoriteArtifact", Context.MODE_PRIVATE)
        val prefsEditor = mPrefs.edit()
        prefsEditor.clear()
        val gson = Gson()
        val artifactListJson = gson.toJson(favoriteArtifactList)
        prefsEditor.putString("artifactList", artifactListJson)
        prefsEditor.apply()
    }
    fun getArtifactFavorite(context: Context, artifactID: Int): FavoriteArtifact? {
        for (artifact : FavoriteArtifact in favoriteArtifactList)
            if(artifactID == artifact.artifactID)
                return artifact
        return null
    }
}