package com.hrv.controller;

import android.bluetooth.BluetoothDevice;

import com.mobiprobe.Mobiprobe;
import com.orm.SugarApp;
import com.orm.SugarContext;

import java.util.ArrayList;

/**
 * Created by manishautomatic on 26/09/16.
 */

public class HRVAppInstance extends SugarApp {


    private static HRVAppInstance instance;
    private final ArrayList<Integer> RR_READINGS = new ArrayList<>();
    private ArrayList<Integer> CURRENT_RR_PACKET = new ArrayList<>();

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
        Mobiprobe.activate(this,"bd49783a");
    }


    public synchronized ArrayList<Integer> getCURRENT_RR_PACKET() {
        return CURRENT_RR_PACKET;
    }

    public synchronized  void setCURRENT_RR_PACKET(ArrayList<Integer> CURRENT_RR_PACKET) {
        this.CURRENT_RR_PACKET = CURRENT_RR_PACKET;
    }

    public static HRVAppInstance getAppInstance(){
        return instance;
    }


    public synchronized ArrayList<Integer> getRR_READINGS() {
        return RR_READINGS;
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }


}
