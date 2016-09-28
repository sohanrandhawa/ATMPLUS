package com.hrv.controller;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

/**
 * Created by manishautomatic on 26/09/16.
 */

public class HRVAppInstance extends Application {


    public static HRVAppInstance instance;
    private ArrayList<Integer> RR_READINGS = new ArrayList<>();

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


    public synchronized ArrayList<Integer> getRR_READINGS() {
        return RR_READINGS;
    }





}
