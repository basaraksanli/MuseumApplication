package com.example.museumapplication.data


data class FavoriteArtifact(
        val artifactID : Int,
        val artifactName: String,
        val artifactDescription : String,
        val artifactImage: String?,
        val museumName: String,
        val category: String
)