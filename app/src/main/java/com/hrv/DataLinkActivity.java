package com.hrv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hrv.computation.MathHelper;
import com.hrv.controller.BluetoothLeService;
import com.hrv.controller.HRVAppInstance;
import com.hrv.models.SessionTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by manishautomatic on 26/09/16.
 */

public class DataLinkActivity extends AppCompatActivity implements View.OnClickListener{


    private BluetoothDevice currentBleDevice;
    private BluetoothGattService heartRateService;
    private BluetoothGattCharacteristic heartRateCharacteristic;
    private BluetoothLeService bluetoothService;
    private final String HEART_RATE_SERVICE_CONST="0000180d-0000-1000-8000-00805f9b34fb";
    private final UUID HEART_RATE_SERVICE_UUID = UUID.fromString(HEART_RATE_SERVICE_CONST);
    private TextView mTxtVwReading;
    private TextView mtxtVwComputedRR;
    private Button mBtnStart, mBtnStop;
    private long startTime=0, endTime=0, currentTime=0;
    private MathHelper mathHelper= new MathHelper();
    private TextView mTxtVwRMS;
    private TextView mTxtVwLnRms;
    private ProgressDialog pDialog;
    private boolean isSessionLive=false;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.layout_data_link);

        currentBleDevice= HRVAppInstance.getAppInstance().getCurrentBLEDevice();
        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        mTxtVwReading=(TextView)findViewById(R.id.txtvwReading);
        mtxtVwComputedRR=(TextView)findViewById(R.id.txtvwComputedHeartRate);
        mBtnStart=(Button)findViewById(R.id.btnStart);
        mBtnStop=(Button)findViewById(R.id.btnStop);
        mTxtVwRMS=(TextView)findViewById(R.id.txtvwRMS);
        mTxtVwLnRms=(TextView)findViewById(R.id.txtvwLnRMS);
        pDialog = new ProgressDialog(DataLinkActivity.this);
        pDialog.setTitle("HRV-DEMO");
        pDialog.setMessage("processing session data, please wait...");
        pDialog.setCancelable(false);
        mBtnStop.setEnabled(false);
        mBtnStart.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
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


               int heartRate = intent.getIntExtra("HEART_RATE",0);
               int rRvalue = intent.getIntExtra("RR_VALUE",0);

                updateDataonUI(heartRate,rRvalue);

            }
        }
    };




    private void updateDataonUI(int hRate, int rrValue){
        mTxtVwReading.setText("HEART-RATE: "+Integer.toString(hRate)+" \n"
                               +"R.R VALUE: "+Integer.toString(rrValue) );

        if(isSessionLive){
           // startTime=System.currentTimeMillis();
            computeHRfromRR();
        }
    }


    private void computeHRfromRR(){

        try{
            if(isSessionLive) {
                long timeElapsedInMilis = System.currentTimeMillis() - startTime;
                //long timeInSeconds = timeElapsedInMilis / 1000;
                // ArrayList<Integer> rrReadings = HRVAppInstance.getAppInstance().getRR_READINGS();
                ArrayList<Integer> rrReadings = new ArrayList<>();
                rrReadings.clear();
                rrReadings.addAll(HRVAppInstance.getAppInstance().getRR_READINGS());
                long sum = 0;
                for (int d : rrReadings)
                    sum += 1/d;
                long heartRateMeasured = (sum/(rrReadings.size()*1000))*60;
                Double SDNN = mathHelper.computeSDNN(rrReadings);
                Double rmsValue = mathHelper.computeRMS(rrReadings);
                Double lnRMSSD = Math.log(rmsValue);
                DecimalFormat df = new DecimalFormat("#.##");
                String strSDNN = df.format(SDNN);
                String strRMSSD = df.format(rmsValue);
                String strLnRMSSD = df.format(lnRMSSD);

                mtxtVwComputedRR.setText("SDNN:  ->  "+strSDNN);
                mTxtVwRMS.setText("RMS:  ->  "+strRMSSD);
                mTxtVwLnRms.setText("LnRMS:  ->  "+strLnRMSSD);
            }
        }catch(Exception e){
        e.printStackTrace();
        }






    }


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

    @Override
    public void onClick(View view) {
        if(view==mBtnStart){
            isSessionLive=true;
            startTime=System.currentTimeMillis();
            mBtnStart.setEnabled(false);
            mBtnStop.setEnabled(true);
        }if(view==mBtnStop){
            isSessionLive=false;
            mBtnStart.setEnabled(true);
            mBtnStop.setEnabled(false);
            saveSessionData();
        }
    }



    private void saveSessionData(){
    new ProcessSessionAsync().execute();

    }


    private class ProcessSessionAsync extends AsyncTask<Void,Void,Void> {

        private boolean processSuccess=false;

        @Override
        public void onPreExecute(){
            pDialog.show();
            isSessionLive=false;

        }

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                long timeElapsedInMilis = System.currentTimeMillis() - startTime;
                //long timeInSeconds = timeElapsedInMilis / 1000;
                // ArrayList<Integer> rrReadings = HRVAppInstance.getAppInstance().getRR_READINGS();
                ArrayList<Integer> rrReadings = new ArrayList<>();
                rrReadings.clear();
                rrReadings.addAll(HRVAppInstance.getAppInstance().getRR_READINGS());
                String rrDump="";
                long sum = 0;
                for (int d : rrReadings) {
                    sum += 1 / d;
                    rrDump=rrDump+"-"+Integer.toString(d);
                }

                // rrReadings.s
                // long heartRateMeasured = (long) sum / (rrReadings.size());
                long heartRateMeasured = (sum/(rrReadings.size()*1000))*60;
                Double sDNN = mathHelper.computeSDNN(rrReadings);
                Double rmsValue = mathHelper.computeRMS(rrReadings);
                Double lnRMS =Math.log(rmsValue);
                SessionTemplate currentSession = new SessionTemplate();
                currentSession.setLnRMS(lnRMS);
                currentSession.setSdNN(sDNN);
                currentSession.setRms(rmsValue);
                currentSession.setRrValuesDump(rrDump);
                currentSession.setStartTime(startTime);;
                currentSession.setTimeElapsed(System.currentTimeMillis()-startTime);
                currentSession.save();


                processSuccess=true;
            }catch (Exception e){
                processSuccess=false;
            }

            return null;
        }

        @Override
        public void onPostExecute(Void v){
            if(processSuccess){
               // isSessionLive=false;
                pDialog.dismiss();
                Toast.makeText(DataLinkActivity.this, "Session saved successfully", Toast.LENGTH_SHORT).show();
            }else{

            }
        }
    }
}
