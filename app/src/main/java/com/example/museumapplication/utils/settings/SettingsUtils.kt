package com.example.museumapplication.utils.settings

import android.app.Activity
import androidx.preference.PreferenceManager
import com.example.museumapplication.R

class SettingsUtils {
    companion object{

        fun setTheme(activity: Activity){
            val sp = PreferenceManager.getDefaultSharedPreferences(activity)

            val darkMode = sp.getBoolean(activity.applicationContext.getString(R.string.darkModePreferences), true)
            if(darkMode)
                activity.setTheme(R.style.AppThemeDarkNoActionBar)
            else
                activity.setTheme(R.style.AppThemeNoActionBar)
        }

        fun mapStyleDark (activity: Activity): Int {
            val sp = PreferenceManager.getDefaultSharedPreferences(activity)
            return if(sp.getBoolean(activity.applicationContext.getString(R.string.mapDarkPreferences), true))
                R.raw.mapstyle_dark
            else
                R.raw.mapstyle
        }
    }
}