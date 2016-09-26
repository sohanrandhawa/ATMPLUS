package com.hrv;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.hrv.controller.BluetoothLeService;
import com.hrv.controller.HRVAppInstance;

import java.util.List;
import java.util.UUID;

/**
 * Created by manishautomatic on 26/09/16.
 */

public class DataLinkActivity extends Activity {


    private BluetoothDevice currentBleDevice;
    private BluetoothGattService heartRateService;
    private BluetoothGattCharacteristic heartRateCharacteristic;
    private BluetoothLeService bluetoothService;
    private final String HEART_RATE_SERVICE_CONST="0000180d-0000-1000-8000-00805f9b34fb";
    private final UUID HEART_RATE_SERVICE_UUID = UUID.fromString(HEART_RATE_SERVICE_CONST);

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.layout_data_link);

        currentBleDevice= HRVAppInstance.getAppInstance().getCurrentBLEDevice();
        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }



    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                // connection established

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

                // gatt connection lost


            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                // connect to heart rate service for this gatt server.
                    initHeartRateReading();

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(BleService.EXTRA_SERVICE_UUID), intent.getStringExtra(BleService.EXTRA_TEXT));

            }
        }
    };



    private void initHeartRateReading(){

        List<BluetoothGattService> servicesList = bluetoothService.getSupportedGattServices();
        if(servicesList!=null){
            for(BluetoothGattService currentService: servicesList){
                if(currentService.getUuid().equals(HEART_RATE_SERVICE_UUID)){
                    heartRateService=currentService;
                    heartRateCharacteristic=currentService
                                            .getCharacteristics().get(0);

                    // configure the service to listen to this characteristic
                    bluetoothService.enableCharacteristicsUpdate(heartRateCharacteristic);
                }
            }
        }

    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothService.initialize()) {
                Log.e("", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            bluetoothService.setupConnection();;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };



    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothService != null) {
            bluetoothService.setupConnection();
            //Log.d("", "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        bluetoothService = null;
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
