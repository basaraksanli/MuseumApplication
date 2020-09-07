package com.example.museumapplication.utils;

import android.bluetooth.BluetoothAdapter;

public class BluetoothCheckUtils {
    private BluetoothCheckUtils() {}

    /**
     * Check Blue is enabled
     *
     * @return true：Bluetooth device is Enabled
     */
    public static boolean isBlueEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }
        if (bluetoothAdapter.isEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Force Bluetooth device on
     *
     * @return true：Forced to open Bluetooth device successfully
     */
    public static boolean turnOnBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null) {
            return bluetoothAdapter.enable();
        }

        return false;
    }
}
