package com.example.museumapplication.ui.home.favorite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.museumapplication.data.UserLoggedIn

class FavoritePageSharedModelView(application: Application) : AndroidViewModel(application) {

    var museumList = MutableLiveData(UserLoggedIn.instance.favoriteMuseumList)
    var artifactList = MutableLiveData(UserLoggedIn.instance.favoriteArtifactList)
}