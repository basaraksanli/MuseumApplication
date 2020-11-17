package com.example.museumapplication.utils

import android.bluetooth.BluetoothAdapter

object BluetoothCheckUtils {
    /**
     * Check Blue is enabled
     *
     * @return true：Bluetooth device is Enabled
     */
    fun isBlueEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter.isEnabled
    }

    /**
     * Force Bluetooth device on
     *
     * @return true：Forced to open Bluetooth device successfully
     */
    fun turnOnBluetooth(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.enable() ?: false
    }
}