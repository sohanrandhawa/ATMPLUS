package com.hrv;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    private Button mBtnConnectDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private final int REQUEST_ENABLE_BT=11;
    private Set<BluetoothDevice> detectedDevices ;
    private final long SCAN_TIMEOUT_INTERVAL =10000;//10 secs
    private  BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback sCallback;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        checkBLESupport();
        checkBluetoothStatus();
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
            // bluetooth is disabled
        }else{
            // since bluetooth is enabled
            // we disable the connect-bluetooth button
            // and trigger the device scan mechanism
            mBtnConnectDevice.setEnabled(false);
            connectToDevice();
        }
    }


    // initialise the UI elements

    private void initUI(){
        mBtnConnectDevice=(Button)findViewById(R.id.btnConnectDevice);
        mBtnConnectDevice.setOnClickListener(this);
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
                connectToDevice();
            } else {
                showBluetoothDisableDialog();
            }
        }

    }


    private void connectToDevice() {
         Set<BluetoothDevice> pairedDevices;
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        List<String> s = new ArrayList<String>();
        for(BluetoothDevice bt : pairedDevices)
            s.add(bt.getName());
        pairedDevices.size();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupLolipopListner();
        } else {
            mBluetoothAdapter.startLeScan(leScanCallback);
        }
        s.size();
    }

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



    private class TimerAsync extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(SCAN_TIMEOUT_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void v){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                stopLoliPopScan();
            } else {
                //mBluetoothAdapter.startLeScan(leScanCallback);
                mBluetoothAdapter.stopLeScan(leScanCallback);
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private void stopLoliPopScan(){
            bluetoothLeScanner.stopScan(sCallback);
        }
    }
}
