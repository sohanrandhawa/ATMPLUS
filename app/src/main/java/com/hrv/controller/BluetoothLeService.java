package com.hrv.controller;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by manishautomatic on 26/09/16.
 */

// A service that interacts with the BLE device via the Android BLE API.
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.hrv.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.hrv.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.hrv.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.hrv.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.hrv.EXTRA_DATA";
    private final String HEART_RATE_DESCRIPTOR_STRING ="0000180d-0000-1000-8000-00805f9b34fb";
    private UUID UUID_HEART_RATE_MEASUREMENT= UUID.fromString(HEART_RATE_DESCRIPTOR_STRING);
    private final UUID UUID_HEART_RATE_CHARACTERTISTIC = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");







    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }
                @Override
                // Characteristic notification
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }

            };


    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID_HEART_RATE_CHARACTERTISTIC.equals(characteristic.getUuid())) {
            byte[] data = characteristic.getValue();
            int flag = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            int format = -1;
            int energy = -1;
            int offset = 1;
            int rr_count = 0;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
               // Logger.trace("Heart rate format UINT16.");
                offset = 3;
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                //Logger.trace("Heart rate format UINT8.");
                offset = 2;
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            intent.putExtra("HEART_RATE",heartRate);
            //Logger.trace("Received heart rate: {}", heartRate);
            if ((flag & 0x08) != 0) {
                // calories present
                energy = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                offset += 2;
              //  Logger.trace("Received energy: {}", energy);
            }
            if ((flag & 0x10) != 0) {
                // RR stuff.
                rr_count = ((characteristic.getValue()).length - offset) / 2;
                int mRr_values[]={};

              //  int rrValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                ArrayList<Integer> rrArray = new ArrayList<>();
                for (int i = 0; i < rr_count; i++) {
                   // mRr_values[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                    rrArray.add(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset));
                    offset += 2;
                    //Logger.trace("Received RR: {}", mRr_values[i]);
                }
                // add to master RR list
                HRVAppInstance.getAppInstance().getRR_READINGS().addAll(rrArray);
                intent.putExtra("RR_VALUE",rrArray.get(0));
            }
        }
        sendBroadcast(intent);
    }



    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }


    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }


    public void setupConnection(){
        //mBluetoothGatt.connect();
        mBluetoothGatt= HRVAppInstance.getAppInstance()
                        .getCurrentBLEDevice()
                        .connectGatt(BluetoothLeService.this,false,mGattCallback);
        mBluetoothGatt.connect();
    }


    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }


    // this is where we subscribe for characteristics update notification
    public void enableCharacteristicsUpdate(BluetoothGattCharacteristic characteristic){
            if(characteristic!=null && mBluetoothGatt!=null){


                mBluetoothGatt.setCharacteristicNotification(characteristic,true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);


               /*
                UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
                */
                //mBluetoothGatt.setCharacteristicNotification(characteristic,true);
               // mBluetoothGatt.readCharacteristic(characteristic);
            }
    }
}
