package com.hrv.controller;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

/**
 * Created by manishautomatic on 26/09/16.
 */

public class HRVAppInstance extends Application {


    public static HRVAppInstance instance;

    public BluetoothDevice getCurrentBLEDevice() {
        return currentBLEDevice;
    }

    public void setCurrentBLEDevice(BluetoothDevice currentBLEDevice) {
        this.currentBLEDevice = currentBLEDevice;
    }

    private BluetoothDevice currentBLEDevice;

    @Override
    public void onCreate(){
        super.onCreate();
        instance = this;
    }



    public static HRVAppInstance getAppInstance(){
        return instance;
    }



}
