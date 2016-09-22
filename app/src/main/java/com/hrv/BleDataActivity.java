package com.hrv;

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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.hrv.controller.Constants;
import com.hrv.sensor.BleHeartRateSensor;
import com.hrv.sensor.BleSensor;
import com.hrv.sensor.BleSensors;
import com.hrv.sensor.BleService;

import org.w3c.dom.Text;

import java.util.List;
import java.util.UUID;

/**
 * Created by manishautomatic on 22/09/16.
 */

public class BleDataActivity extends AppCompatActivity {

   // private final static String TAG = DeviceServicesActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView connectionState;
    private TextView dataField;
    private TextView heartRateField;
    private TextView intervalField;
    private Button demoButton;

   // private ExpandableListView gattServicesList;


    private String deviceName;
    private String deviceAddress;
    private BleService bleService;
    private boolean isConnected = false;

    private BleSensor<?> activeSensor;
    private BleSensor<?> heartRateSensor;
    private BluetoothGattService heartRateService;
    private BluetoothGattCharacteristic heartRateCharacteristic;



    // Code to manage Service lifecycle.
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bleService = ((BleService.LocalBinder) service).getService();
            if (!bleService.initialize()) {
                Log.e("", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            bleService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bleService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BleService.ACTION_GATT_CONNECTED.equals(action)) {
                isConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
              //  displayGattServices(bleService.getSupportedGattServices());
                enableHeartRateSensor(bleService.getSupportedGattServices());
            } else if (BleService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BleService.EXTRA_SERVICE_UUID), intent.getStringExtra(BleService.EXTRA_TEXT));

            }
        }
    };




    private void clearUI() {


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ble_data_activity);

        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(Constants.EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(Constants.EXTRAS_DEVICE_ADDRESS);
        heartRateField=(TextView)findViewById(R.id.txtvwDataValue);

        // Sets up UI references.






        final Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bleService != null) {
            final boolean result = bleService.connect(deviceAddress);
            Log.d("", "Connect request result=" + result);
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
        bleService = null;
    }





    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               // connectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String uuid, String data) {
        if (data != null) {
            if (uuid.equals(BleHeartRateSensor.getServiceUUIDString())) {
                heartRateField.setText(data);
            } else {
               // dataField.setText(data);
            }
        }
    }

    private boolean enableHeartRateSensor(List<BluetoothGattService> servicesList) {


        for(BluetoothGattService currentService:servicesList){
            if(currentService.getUuid().equals(
                    UUID.fromString(BleHeartRateSensor.getServiceUUIDString()))) {
                heartRateService = currentService;
                heartRateCharacteristic = currentService.getCharacteristics().get(0);
            }
        }


        final BleSensor<?> sensor = BleSensors.getSensor(heartRateCharacteristic
                .getService()
                .getUuid()
                .toString());

        if (heartRateSensor != null)
            bleService.enableSensor(heartRateSensor, false);

        if (sensor == null) {
            bleService.readCharacteristic(heartRateCharacteristic);
            return true;
        }

        if (sensor == heartRateSensor)
            return true;

        heartRateSensor = sensor;
        bleService.enableSensor(sensor, true);

       // this.setServiceListener(demoClickListener);

        return true;
        /*

        final BluetoothGattCharacteristic characteristic = gattServiceAdapter
                .getHeartRateCharacteristic();
        Log.d(TAG,"characteristic: " + characteristic);
        final BleSensor<?> sensor = BleSensors.getSensor(characteristic
                .getService()
                .getUuid()
                .toString());

        if (heartRateSensor != null)
            bleService.enableSensor(heartRateSensor, false);

        if (sensor == null) {
            bleService.readCharacteristic(characteristic);
            return true;
        }

        if (sensor == heartRateSensor)
            return true;

        heartRateSensor = sensor;
        bleService.enableSensor(sensor, true);

        this.setServiceListener(demoClickListener);

        return true;
        */
        //return true;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)

            return;


    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
