package com.example.museumapplication.utils

import android.content.Context
import android.content.DialogInterface
import android.os.Process
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData

class GeneralUtils {

    companion object{
        /**
         * Warning Dialog Builder for the issues
         */
        fun showWarnDialog(content: String , context: Context, navigation : MutableLiveData<Boolean>?) {
            DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> Process.killProcess(Process.myPid()) }
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Warning")
            builder.setMessage(content)
            builder.setNegativeButton("Confirm") { _: DialogInterface, _: Int ->
                navigation?.postValue(true)
            }
            builder.show()
        }
    }
}