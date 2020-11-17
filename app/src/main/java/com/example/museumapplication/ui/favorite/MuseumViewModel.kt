package com.example.museumapplication.ui.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.museumapplication.data.FavoriteMuseum

class MuseumViewModel : ViewModel() {
    private val _favoriteMuseumList : MutableLiveData<MutableList<FavoriteMuseum>> = MutableLiveData()
    val favoriteMuseumList: LiveData<MutableList<FavoriteMuseum>> get() = _favoriteMuseumList
}