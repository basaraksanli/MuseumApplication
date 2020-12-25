package com.example.museumapplication.data

import android.content.Context
import android.graphics.Bitmap
import com.example.museumapplication.utils.virtual_guide.TTSUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

/**
 * Singleton for Logged in user
 * This class is shared all around the appliaction
 * Contains Shared preferences functions
 */
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


    companion object {
        //Singleton
        val instance = UserLoggedIn()
    }
    /**
     * Sets user
     */
    fun setUser(user: User) {
        resetUser()
        uID = user.uid
        providerUID = user.providerID
        name = user.displayName
        email = user.email
        photoUrl = user.photoURL
    }
    fun resetUser(){
        profilePicture = null
        favoriteArtifactList = arrayListOf()
        favoriteMuseumList = arrayListOf()
    }

    /**
     * retrieves the favorite museum list from the shared preferences
     */
    fun retrieveFavoriteMuseumList(context: Context) {
        val mPrefs = context.getSharedPreferences("$uID museum", Context.MODE_PRIVATE)
        val favoriteMuseumListJson = mPrefs.getString("museumList", "")
        val typeFavoriteMuseum = object : TypeToken<List<FavoriteMuseum?>?>() {}.type
        val gsonBuilder = GsonBuilder()
        val gson = gsonBuilder.create()
        if (favoriteMuseumListJson!!.isNotEmpty()) {
            favoriteMuseumList = gson.fromJson(favoriteMuseumListJson, typeFavoriteMuseum)
        }
    }

    /**
     * retrieves the favorite artifact list from the shared preferences
     */
    fun retrieveFavoriteArtifactList(context: Context) {
        val mPrefs = context.getSharedPreferences("$uID artifact", Context.MODE_PRIVATE)
        val favoriteArtifactListJson = mPrefs.getString("artifactList", "")
        val typeFavoriteArtifact = object : TypeToken<List<FavoriteArtifact?>?>() {}.type
        val gsonBuilder = GsonBuilder()
        val gson = gsonBuilder.create()
        if (favoriteArtifactListJson!!.isNotEmpty()) {
            favoriteArtifactList = gson.fromJson(favoriteArtifactListJson, typeFavoriteArtifact)
        }
    }

    /**
     * saves the favored museums to the shared preferences
     */
    fun saveFavoriteMuseumListToDevice(context: Context) {
        val mPrefs = context.getSharedPreferences("$uID museum", Context.MODE_PRIVATE)
        val prefsEditor = mPrefs.edit()
        prefsEditor.clear()
        val gson = Gson()
        val museumListJson = gson.toJson(favoriteMuseumList)
        prefsEditor.putString("museumList", museumListJson)
        prefsEditor.apply()
    }

    /**
     * returns the favored museum by name
     */
    fun getMuseumFavoriteByName(museumName: String): FavoriteMuseum? {
        for (museum : FavoriteMuseum in favoriteMuseumList)
            if(museumName == museum.museumName)
                return museum
        return null
    }


    /**
     * Saves the favored artifact list to the shared preferences
     */
    fun saveFavoriteArtifactListToDevice(context: Context) {
        val mPrefs = context.getSharedPreferences("$uID artifact", Context.MODE_PRIVATE)
        val prefsEditor = mPrefs.edit()
        prefsEditor.clear()
        val gson = Gson()
        val artifactListJson = gson.toJson(favoriteArtifactList)
        prefsEditor.putString("artifactList", artifactListJson)
        prefsEditor.apply()
    }

    /**
     * get the artifact by name
     */
    fun getArtifactFavoriteByName(artifactID: Int): FavoriteArtifact? {
        for (artifact : FavoriteArtifact in favoriteArtifactList)
            if(artifactID == artifact.artifactID)
                return artifact
        return null
    }
}