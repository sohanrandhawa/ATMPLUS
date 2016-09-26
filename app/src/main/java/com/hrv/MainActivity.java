package com.hrv;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.hrv.adapters.DeviceSelectorAdapter;
import com.hrv.controller.Constants;
import com.hrv.controller.HRVAppInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    private Button mBtnConnectDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private final int REQUEST_ENABLE_BT=11;
    private Set<BluetoothDevice> detectedDevices ;
    private final long SCAN_TIMEOUT_INTERVAL =10000;//10 secs
    private final static int SCAN_DURATION = 500;
    private static BluetoothLeScanner bluetoothLeScanner;
    private static ScanCallback sCallback;

    private Scanner scanner;
    private  ArrayList<BluetoothDevice> leDevices=new ArrayList<>();
    private ListView mLstVwDevices;
    private ProgressDialog pDialog;
    private final String HEART_RATE_DESCRIPTOR_STRING ="0000180d-0000-1000-8000-00805f9b34fb";
    private UUID[] deviceUuidArray= {UUID.fromString(HEART_RATE_DESCRIPTOR_STRING)};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        leDevices.clear();

        initUI();
        checkBLESupport();

    }

    private void checkBLESupport() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please enable bluetooth to use HRV App")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            builder.setCancelable(false);
            AlertDialog alert = builder.create();

            alert.show();

        }

    }


    private void checkBluetoothStatus() {

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // bluetooth is disabled, turn it on...
            final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }else{
            // since bluetooth is enabled
            // we disable the connect-bluetooth button
            // and trigger the device scan mechanism
            initScanner();
            mBtnConnectDevice.setEnabled(false);

        }
    }



    @Override
    public void onResume(){
        super.onResume();
        checkBluetoothStatus();
    }

    // initialise the UI elements

    private void initUI(){

        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Scanning for devices, please wait..");
        mBtnConnectDevice=(Button)findViewById(R.id.btnConnectDevice);
        mBtnConnectDevice.setOnClickListener(this);
        mLstVwDevices=(ListView)findViewById(R.id.lstvwDevices);

        mLstVwDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Intent intent = new Intent(MainActivity.this, DataLinkActivity.class);

                HRVAppInstance.getAppInstance().setCurrentBLEDevice(leDevices.get(i));
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.session_history) {
            startActivity(new Intent(MainActivity.this,ActivitySessionsHistory.class
            ));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {

        if(view==mBtnConnectDevice){
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        System.out.println(resultCode);
        if(requestCode==REQUEST_ENABLE_BT){

            if (resultCode == Activity.RESULT_OK) {
                initScanner();
            } else {
                showBluetoothDisableDialog();
            }
        }

    }


    private void initScanner(){

        if (scanner == null) {
            scanner = new Scanner(mBluetoothAdapter, mLeScanCallback);
            scanner.startScanning();
            pDialog.show();
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(!leDevices.contains(device))
                                leDevices.add(device);
                           // leDeviceListAdapter.addDevice(device, rssi);
                            //leDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupLolipopListner(){
         bluetoothLeScanner =  mBluetoothAdapter.getBluetoothLeScanner();

         sCallback =new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                detectedDevices.add(device);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
        bluetoothLeScanner.startScan(sCallback);
    }


    private void showBluetoothDisableDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please enable bluetooth to use HRV App")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void searchForBluetoothDevices(){

    }


    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            // your implementation here
                detectedDevices.add(device);
            device.toString();
        }
    };



    private  class Scanner extends Thread {
        private final BluetoothAdapter bluetoothAdapter;
        private final BluetoothAdapter.LeScanCallback mLeScanCallback;

        private volatile boolean isScanning = false;

        Scanner(BluetoothAdapter adapter, BluetoothAdapter.LeScanCallback callback) {
            bluetoothAdapter = adapter;
            mLeScanCallback = callback;
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


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startBleScanForLolipop(bluetoothAdapter);
                        }else{
                            bluetoothAdapter.startLeScan(deviceUuidArray,mLeScanCallback);
                        }

                    }

                    sleep(SCAN_DURATION);
                    synchronized (this) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            stopBleScanForLolipop(bluetoothAdapter);
                        }else{
                            bluetoothAdapter.stopLeScan(mLeScanCallback);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateListAdapter();
                            }
                        });


                    }
                }
            } catch (InterruptedException ignore) {
            } finally {
                bluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private  void startBleScanForLolipop(BluetoothAdapter bluetoothAdapter ){
        bluetoothLeScanner =  bluetoothAdapter.getBluetoothLeScanner();
        ScanFilter filter = new ScanFilter.Builder()
                            .setServiceUuid(new ParcelUuid(UUID.fromString(HEART_RATE_DESCRIPTOR_STRING)))
                            .build();
List<ScanFilter> filterList = new  ArrayList<ScanFilter>();
        sCallback =new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                if(!leDevices.contains(device))
                    leDevices.add(device);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
        bluetoothLeScanner.startScan(filterList,null,sCallback);
        //bluetoothLeScanner.startScan(sCallback);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void stopBleScanForLolipop(BluetoothAdapter bAdapter){
        bAdapter.getBluetoothLeScanner().stopScan(sCallback);
    }


    private  void updateListAdapter(){
        pDialog.cancel();
       // scanner.stopScanning();
        DeviceSelectorAdapter devicesAdapter = new DeviceSelectorAdapter(MainActivity.this, leDevices);
        mLstVwDevices.setAdapter(devicesAdapter);
        Log.e("set","called");



    }
}
