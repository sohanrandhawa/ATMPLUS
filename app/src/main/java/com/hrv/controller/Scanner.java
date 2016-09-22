package com.hrv.controller;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;

import com.hrv.callback.BleScanStatusToggleCallback;

/**
 * Created by manishautomatic on 22/09/16.
 */

public class Scanner extends Thread {
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothAdapter.LeScanCallback mLeScanCallback;
    private int SCAN_PERIOD;
    private BleScanStatusToggleCallback statusToggleCallback;

    private volatile boolean isScanning = false;

   public Scanner(BluetoothAdapter adapter,
                  BluetoothAdapter.LeScanCallback callback,
                  int scan_period,
                  BleScanStatusToggleCallback callback_) {
        bluetoothAdapter = adapter;
        mLeScanCallback = callback;
       SCAN_PERIOD=scan_period;
       statusToggleCallback=callback_;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void startScanning() {
        synchronized (this) {
            isScanning = true;
            start();
        }
    }

    public void stopScanning() {
        synchronized (this) {
            isScanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (this) {
                    if (!isScanning)
                        break;





                        bluetoothAdapter.startLeScan(mLeScanCallback);



                }

                sleep(SCAN_PERIOD);

                synchronized (this) {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }
        } catch (InterruptedException ignore) {
        } finally {
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

}
