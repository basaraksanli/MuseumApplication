package com.example.museumapplication.data

import com.huawei.hms.site.api.model.Coordinate

data class FavoriteMuseum(
        val museumName : String,
        val museumAddress: String?,
        val museumPhone:String?,
        val museumPage:String?,
        val museumLocation :Coordinate
)