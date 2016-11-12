package com.hrv;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.hrv.computation.MathHelper;
import com.hrv.controller.BluetoothLeService;
import com.hrv.controller.HRVAppInstance;
import com.hrv.models.SessionTemplate;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by manishautomatic on 26/09/16.
 */

public class DataLinkActivity extends AppCompatActivity implements View.OnClickListener{


    private BluetoothDevice currentBleDevice;
    private BluetoothGattService heartRateService;
    private BluetoothLeService bluetoothService;
    private final String HEART_RATE_SERVICE_CONST="0000180d-0000-1000-8000-00805f9b34fb";
    private final UUID HEART_RATE_SERVICE_UUID = UUID.fromString(HEART_RATE_SERVICE_CONST);
    private TextView mTxtVwReading;
    private TextView mtxtVwComputedRR;
    private Button mBtnToggleSessionState;
    private long startTime=0, endTime=0, currentTime=0;
    private final MathHelper mathHelper= new MathHelper();
    private TextView mTxtVwRMS;
    private TextView mTxtVwLnRms;
    private ProgressDialog pDialog;
    private TextView mTxtVwHRV;
    private boolean isSessionLive=false;
    private TextView mTxtVwElapsedTime;
    private TextView mTxtVwHeartRate;
    private GraphView mGraphView;
    private int lastRRValue=0;
    private final LineGraphSeries<DataPoint> series = new LineGraphSeries<>();


    private LineChart mLineChart;
    // object representing the current ongoing session.
    private SessionTemplate currentSession ;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        //disable screen locking, till this activity is visible to the user.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.layout_data_link);

        currentBleDevice= HRVAppInstance.getAppInstance().getCurrentBLEDevice();
        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        mTxtVwReading=(TextView)findViewById(R.id.txtvwReading);
        mtxtVwComputedRR=(TextView)findViewById(R.id.txtvwComputedHeartRate);
        mBtnToggleSessionState=(Button)findViewById(R.id.btnToggleSession);
        //mBtnStop=(Button)findViewById(R.id.btnStop);
        mTxtVwRMS=(TextView)findViewById(R.id.txtvwRMS);
        mTxtVwHRV=(TextView)findViewById(R.id.txtvwHRV);
        mTxtVwElapsedTime=(TextView)findViewById(R.id.txtvwElapsedTime);
        mTxtVwHeartRate=(TextView)findViewById(R.id.txtvwHeartRate);
        mGraphView = (GraphView) findViewById(R.id.graph);
        mTxtVwLnRms=(TextView)findViewById(R.id.txtvwLnRMS);
       // mLineChart=(LineChart)findViewById(R.id.mpGraph);
        pDialog = new ProgressDialog(DataLinkActivity.this);
        pDialog.setTitle("HRV-DEMO");
        pDialog.setMessage("processing session data, please wait...");
        pDialog.setCancelable(false);

        mBtnToggleSessionState.setOnClickListener(this);

        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);



        initGraph();

    }


    private void initGraph(){

        //clear existing samples..


        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setYAxisBoundsManual(true);
        mGraphView.getViewport().setMinX(5000);
        mGraphView.getViewport().setMaxX(100000);
        mGraphView.getViewport().setMinY((double)40);
        mGraphView.getViewport().setMaxY((double) 90);
        mGraphView.getViewport().setDrawBorder(true);
        mGraphView.getGridLabelRenderer().setNumHorizontalLabels(10);
        mGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        mGraphView.getGridLabelRenderer().setNumVerticalLabels(5);
        mGraphView.getGridLabelRenderer().setVerticalLabelsVAlign(GridLabelRenderer.VerticalLabelsVAlign.ABOVE);
        mGraphView.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        mGraphView.getGridLabelRenderer().reloadStyles();
        series.setBackgroundColor(Color.argb(20,247,54,141));
        series.setColor(Color.argb(255,217,79,20));
        series.setDrawBackground(true);
        series.setThickness(6);
        //series.appendData(new DataPoint(0,0),false,100);
        mGraphView.addSeries(series);
    }


    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                // connection established

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

                // gatt connection lost
                showConnectionLostPromt();


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
        if(rrValue==0)return;// putting a check here to prevent infite value for Y axis of the chart
        mTxtVwReading.setText("H.R. > "+Integer.toString(hRate)+"\n"
                               +"R.R. > "+Integer.toString(rrValue) );
                lastRRValue=lastRRValue+rrValue;
               // prepareSamples(hRate,rrValue);
        //calculate heart rate value form the current RR value sample.
        double currentHR = ((double)1/rrValue)*60*1000;

         series.appendData(new DataPoint(lastRRValue,currentHR ), true, 5000);
        mGraphView.removeSeries(series);
      //  mGraphView.getViewport().setMinX(series.getHighestValueX());
       // mGraphView.getViewport().setMaxX(series.getHighestValueX()+100000);
        //mGraphVie


        if(hRate>100 ){
            mGraphView.getViewport().setMinY((double)(hRate-75));
            //mGraphView.getViewport().setMaxY(Collections.max(hrSamplesForGraph));
            mGraphView.getViewport().setMaxY((double)(hRate+50));
        }
        if(hRate>60 && hRate<=100){
            mGraphView.getViewport().setMinY((double)(hRate-40));
            //mGraphView.getViewport().setMaxY(Collections.max(hrSamplesForGraph));
            mGraphView.getViewport().setMaxY((double)(hRate+30));
        }if(hRate<=60){
            mGraphView.getViewport().setMinY((double) (hRate - 15));
            //mGraphView.getViewport().setMaxY(Collections.max(hrSamplesForGraph));
            mGraphView.getViewport().setMaxY((double) (hRate + 35));
        }

        /*

        if(lastRRValue<50000){
            mGraphView.getViewport().setMinX(5000);
            mGraphView.getViewport().setMaxX(100000);
        }if(lastRRValue>50000 && lastRRValue<500000){
            mGraphView.getViewport().setMinX(50000);
            mGraphView.getViewport().setMaxX(1000000);
        }if(lastRRValue>500000 && lastRRValue<5000000){
            mGraphView.getViewport().setMinX(500000);
            mGraphView.getViewport().setMaxX(10000000);
        }if(lastRRValue>5000000 && lastRRValue<50000000){
            mGraphView.getViewport().setMinX(5000000);
            mGraphView.getViewport().setMaxX(100000000);
        }
        */
            mGraphView.getViewport().setScalableY(true);
            mGraphView.addSeries(series);

        //mGraphView.scrollTo(0,0);
        //mGraphView.scr

        if (isSessionLive){
            computeHRfromRR();
        }
    }





    private void computeHRfromRR(){
        try {
            if (isSessionLive) {
                long timeElapsedInMilis = System.currentTimeMillis() - startTime;
                //long timeInSeconds = timeElapsedInMilis / 1000;
                // ArrayList<Integer> rrReadings = HRVAppInstance.getAppInstance().getRR_READINGS();
                ArrayList<Integer> rrReadings = new ArrayList<>();
                rrReadings.clear();
                rrReadings.addAll(HRVAppInstance.getAppInstance().getRR_READINGS());
                long sum = 0;
                for (Integer d : rrReadings)
                    sum +=  d;
                double meanRR = (double)sum/rrReadings.size();
                double meanRR_secs = meanRR/1000;
             final   double meanHR = (double) 60/meanRR_secs;

                //  Double heartRateMeasured = (double) 60/((sum / (rrReadings.size()) / 1000));
             final   Double SDNN = mathHelper.computeSDNN(rrReadings);
            final    Double rmsValue = mathHelper.computeRMS(rrReadings);
              final  Double lnRMSSD = Math.log(rmsValue);
              final  Double hrv = mathHelper.computeHRV(lnRMSSD);
                DecimalFormat df = new DecimalFormat("#.##");
                String strSDNN = df.format(SDNN);
                String strRMSSD = df.format(rmsValue);
                String strLnRMSSD = df.format(lnRMSSD);
                String strHRV = df.format(hrv);
                String strHRate = df.format(meanHR);

                mtxtVwComputedRR.setText("SDNN:  ->  " + strSDNN);
                mTxtVwRMS.setText("RMS:  ->  " + strRMSSD);
                mTxtVwLnRms.setText("LnRMS:  ->  " + strLnRMSSD);
                mTxtVwHRV.setText("HRV: ->" + strHRV);
                mTxtVwElapsedTime.setText("Duration: "+millisToMinutes(timeElapsedInMilis));
                mTxtVwHeartRate.setText("Heart Rate:  "+strHRate);

                // update the current session object, with the latest set of data.

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentSession.setLnRMS(lnRMSSD);
                        currentSession.setSdNN(SDNN);
                        currentSession.setRms(rmsValue);
                        String rrDump="";
                        if(currentSession.getRrValuesDump().equalsIgnoreCase("")){
                            ArrayList<Integer> tempRRData = new ArrayList<>();
                            tempRRData.clear();
                            tempRRData.addAll(HRVAppInstance.getAppInstance().getCURRENT_RR_PACKET());
                            long sum=0;
                            for (int d : tempRRData) {
                                sum += d;
                                rrDump=rrDump+"-"+Integer.toString(d);
                            }
                            currentSession.setRrValuesDump(rrDump);
                        }else{
                            currentSession.setRrValuesDump(
                                    currentSession.getRrValuesDump()
                                            +"-" +rrDump);
                        }
                        //  currentSession.setRrValuesDump(rrDump);
                        currentSession.setStartTime(startTime);
                        currentSession.setAvgHeartRate(meanHR);
                        currentSession.setHrvValue(hrv);
                        currentSession.setTimeElapsed(System.currentTimeMillis() - startTime);
                        currentSession.save();
                    }
                });



            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/**
    private void computeHRfromRR() {

        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (isSessionLive) {
                                long timeElapsedInMilis = System.currentTimeMillis() - startTime;
                                //long timeInSeconds = timeElapsedInMilis / 1000;
                                // ArrayList<Integer> rrReadings = HRVAppInstance.getAppInstance().getRR_READINGS();
                                ArrayList<Integer> rrReadings = new ArrayList<>();
                                rrReadings.clear();
                                rrReadings.addAll(HRVAppInstance.getAppInstance().getRR_READINGS());
                                long sum = 0;
                                for (Integer d : rrReadings)
                                    sum +=  d;
                                double meanRR = (double)sum/rrReadings.size();
                                double meanRR_secs = meanRR/1000;
                                double meanHR = (double) 60/meanRR_secs;

                                //  Double heartRateMeasured = (double) 60/((sum / (rrReadings.size()) / 1000));
                                Double SDNN = mathHelper.computeSDNN(rrReadings);
                                Double rmsValue = mathHelper.computeRMS(rrReadings);
                                Double lnRMSSD = Math.log(rmsValue);
                                Double hrv = mathHelper.computeHRV(lnRMSSD);
                                DecimalFormat df = new DecimalFormat("#.##");
                                String strSDNN = df.format(SDNN);
                                String strRMSSD = df.format(rmsValue);
                                String strLnRMSSD = df.format(lnRMSSD);
                                String strHRV = df.format(hrv);
                                String strHRate = df.format(meanHR);

                                mtxtVwComputedRR.setText("SDNN:  ->  " + strSDNN);
                                mTxtVwRMS.setText("RMS:  ->  " + strRMSSD);
                                mTxtVwLnRms.setText("LnRMS:  ->  " + strLnRMSSD);
                                mTxtVwHRV.setText("HRV: ->" + strHRV);
                                mTxtVwElapsedTime.setText("Session duration: "+millisToMinutes(timeElapsedInMilis));
                                mTxtVwHeartRate.setText("Heart Rate:  "+strHRate);

                                // update the current session object, with the latest set of data.
                                currentSession.setLnRMS(lnRMSSD);
                                currentSession.setSdNN(SDNN);
                                currentSession.setRms(rmsValue);
                                String rrDump="";
                                if(currentSession.getRrValuesDump().equalsIgnoreCase("")){
                                    ArrayList<Integer> tempRRData = new ArrayList<>();
                                    tempRRData.clear();
                                    tempRRData.addAll(HRVAppInstance.getAppInstance().getCURRENT_RR_PACKET());
                                    for (int d : tempRRData) {
                                        sum += d;
                                        rrDump=rrDump+"-"+Integer.toString(d);
                                    }
                                    currentSession.setRrValuesDump(rrDump);
                                }else{
                                    currentSession.setRrValuesDump(
                                            currentSession.getRrValuesDump()
                                                    +"-" +rrDump);
                                }
                                //  currentSession.setRrValuesDump(rrDump);
                                currentSession.setStartTime(startTime);
                                currentSession.setAvgHeartRate(meanHR);
                                currentSession.setHrvValue(hrv);
                                currentSession.setTimeElapsed(System.currentTimeMillis() - startTime);
                                currentSession.save();

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );


    }
**/

    private void initHeartRateReading(){
        try{
            List<BluetoothGattService> servicesList = bluetoothService.getSupportedGattServices();
            if(servicesList!=null){
                for(BluetoothGattService currentService: servicesList){
                    if(currentService.getUuid().equals(HEART_RATE_SERVICE_UUID)){
                        heartRateService=currentService;
                        try{
                            BluetoothGattCharacteristic heartRateCharacteristic = currentService
                                    .getCharacteristics().get(0);

                            // configure the service to listen to this characteristic
                            bluetoothService.enableCharacteristicsUpdate(heartRateCharacteristic);
                        }catch (Exception e){

                        }

                    }
                }
            }
        }catch(Exception e){

        }


    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothService = ((BluetoothLeService.LocalBinder) service).getService();
            if (bluetoothService.initialize()) {
                Log.e("", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            bluetoothService.setupConnection();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    private String millisToMinutes(long millis){
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(tz);
        return df.format(new Date(millis));

    }


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
        if(view==mBtnToggleSessionState){
            if(!isSessionLive){
                isSessionLive=true;
                startTime=System.currentTimeMillis();
                mBtnToggleSessionState.setText("STOP");
                mBtnToggleSessionState.setBackgroundColor(Color.RED);
                currentSession=new SessionTemplate();// initialize a new session object
            }else{
                confirmSessionClosure();

            }

            //startTime=System.currentTimeMillis();
          //  mBtnStart.setEnabled(false);

        }
        /**
        if(view==mBtnStop){
            isSessionLive=false;
            //mBtnStart.setEnabled(true);

            saveSessionData();
        }
         **/
    }



    private void saveSessionData(){


    }







    // confirm if user wants to close current session.

    private void confirmSessionClosure(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to stop this session ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        isSessionLive=false;
                        mBtnToggleSessionState.setText("START");
                        mBtnToggleSessionState.setBackgroundColor(ContextCompat
                                              .getColor(DataLinkActivity.this,
                                                        R.color.green));

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }



    // alert user that connection to the bluetooth device has been lost.

    private void showConnectionLostPromt(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Connection lost to sensor")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onBackPressed(){
        finish();
    }
}
