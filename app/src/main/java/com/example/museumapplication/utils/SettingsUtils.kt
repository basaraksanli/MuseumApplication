package com.example.museumapplication.utils

import android.app.Activity
import androidx.preference.PreferenceManager
import com.example.museumapplication.R

class SettingsUtils {
    companion object{

        fun setTheme(activity: Activity){
            val sp = PreferenceManager.getDefaultSharedPreferences(activity)

            val darkMode = sp.getBoolean("darkMode", true)
            if(darkMode)
                activity.setTheme(R.style.AppThemeDarkNoActionBar)
            else
                activity.setTheme(R.style.AppThemeNoActionBar)
        }
        fun loadSettings(activity: Activity){
            val sp = PreferenceManager.getDefaultSharedPreferences(activity)

            val darkmode = sp.getBoolean("darkMode", false)
            val mapnightmode = sp.getBoolean("mapDarkMode", true)
            val museumRange = sp.getInt("museumRange", 50)
            val exhibitRange = sp.getInt("exhibitRange", 2)


        }
        fun mapStyleDark (activity: Activity): Int {
            val sp = PreferenceManager.getDefaultSharedPreferences(activity)
            return if(sp.getBoolean("mapDarkMode", true))
                R.raw.mapstyle_dark
            else
                R.raw.mapstyle
        }
    }

}