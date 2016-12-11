package com.hrv;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hrv.computation.MathHelper;
import com.hrv.controller.BluetoothLeService;
import com.hrv.controller.HRVAppInstance;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by manishautomatic on 07/12/16.
 */

public class ActivityPacerSurface extends Activity implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Paint paint, bluePaint;
    private Path path;
    private int SURFACE_VIEW_WIDTH=0;
    private int SURFACE_VIEW_HEIGHT=0;
    private int HORIZONTAL_SPLIT=0;
    private TextView mTxtVwBreathPhaseStatus;
    private final String HEART_RATE_SERVICE_CONST="0000180d-0000-1000-8000-00805f9b34fb";
    private final UUID HEART_RATE_SERVICE_UUID = UUID.fromString(HEART_RATE_SERVICE_CONST);
    private BluetoothGattService heartRateService;
    private Bitmap bitmapBlueBall;

    // logic for animated ball
    private ImageView mImgVwPixel;
    private Animation tweenAnimation;
    private Animation animation1,
            animation2,
            animation4,
            animation5;
    //private int SCREEN_WIDTH=0;
    //private int SCREEN_HEIGHT =0;
    //private int X_AXIS_PHASE_LENGTH=0;
    private RelativeLayout relVwPacerContainer;
    private RelativeLayout mRelLytSurfaceContainer;
    private GraphView mGraphView;
    private BluetoothLeService bluetoothService;
    private MathHelper mathHelper;
    private int lastRRValue=0;
    private final LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
    private boolean isSessionLive=true;
    private TextView mTxtVwRMSValue;


    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_pacer_surface);
        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        mathHelper = new MathHelper();
        initUI();
       // bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

    }


    private void initAnimObjects(){
            // x1-x2 . y1-y2
        animation1 = new TranslateAnimation(0, 10,0, 0);
        animation1.setDuration(1000);
        animation1.setFillAfter(true);
        //PHASE -2 : translate linear-up, from (x1,0)-->(x2,y1)
        animation2 = new TranslateAnimation(10, (float)(3)*(HORIZONTAL_SPLIT), 0, -(SURFACE_VIEW_HEIGHT-60)/2);
        animation2.setDuration(2000);
        // animation2.setStartOffset(1500);
        animation2.setFillAfter(true);
        // mTxtVwPacer.startAnimation(animation2);
        //PHASE -3 : translate horizontal-flat, from (x2,y1)-->(x3,y1)
       // animation3 = new TranslateAnimation((float)(2.5)*(HORIZONTAL_SPLIT-5), (float)(3.5)*(HORIZONTAL_SPLIT-5),-(SURFACE_VIEW_HEIGHT-60)/2, -(SURFACE_VIEW_HEIGHT-60)/2);
        //animation3.setDuration(1000);
        //animation2.setStartOffset(3000);
        //animation3.setFillAfter(true);
        //mTxtVwPacer.startAnimation(animation3);
        //PHASE -4 : translate linear-downward, from (x3,y1)-->(x4,y0)
        animation4 = new TranslateAnimation((float)(3)*(HORIZONTAL_SPLIT), SURFACE_VIEW_WIDTH-10,-(SURFACE_VIEW_HEIGHT-60)/2, 0);
        animation4.setDuration(2000);
        //animation2.setStartOffset(4500);
        animation4.setFillAfter(true);
        //mTxtVwPacer.startAnimation(animation4);
        //PHASE -5 : translate horizontal-flat-end, from (x2,y1)-->(x3,y1)
        animation5 = new TranslateAnimation(SURFACE_VIEW_WIDTH-10, 6*(HORIZONTAL_SPLIT),0, 0);
        animation5.setDuration(1000);
        //animation2.setStartOffset(6000);
        animation5.setFillAfter(false);
        //mTxtVwPacer.startAnimation(animation5);
    }


    private void initUI(){

        bitmapBlueBall= BitmapFactory.decodeResource(getResources(), R.drawable.circle);


        initGraph();
        path = new Path();
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(8);
        paint.setDither(true);                    // set the dither to true
        paint.setStyle(Paint.Style.STROKE);       // set to STOKE
        paint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        paint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        paint.setAntiAlias(true);


        bluePaint = new Paint();
        bluePaint.setColor(Color.BLUE);
        bluePaint.setStrokeWidth(8);
        bluePaint.setDither(true);                    // set the dither to true
        bluePaint.setStyle(Paint.Style.FILL_AND_STROKE);       // set to STOKE
        bluePaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        bluePaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        bluePaint.setAntiAlias(true);





        // init the path object


        mSurfaceView = (SurfaceView)findViewById(R.id.sfvw);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceView.setWillNotDraw(true);
        mSurfaceHolder.addCallback(this);
        mRelLytSurfaceContainer =(RelativeLayout)findViewById(R.id.relLytSurfaceContainer);
        mImgVwPixel=(ImageView)findViewById(R.id.imgVwCircle);
        mTxtVwBreathPhaseStatus=(TextView)findViewById(R.id.txtvwBreathStatus);
        mTxtVwRMSValue =(TextView)findViewById(R.id.txtvwRMSValue);
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



    private void preparePath(){
        path.moveTo(0,SURFACE_VIEW_HEIGHT-60);
        path.lineTo(10,SURFACE_VIEW_HEIGHT-60);
        //RAMP-UP
        path.moveTo(10,SURFACE_VIEW_HEIGHT-60);
        path.lineTo((float)3*HORIZONTAL_SPLIT,(SURFACE_VIEW_HEIGHT-60)/2);
        path.moveTo((float)3*HORIZONTAL_SPLIT,(SURFACE_VIEW_HEIGHT-60)/2);
        //RAMP-DOWN
        path.lineTo((float)(SURFACE_VIEW_WIDTH-10),(SURFACE_VIEW_HEIGHT-60));
        path.moveTo((float)(SURFACE_VIEW_WIDTH-10),(SURFACE_VIEW_HEIGHT-60));
        path.lineTo(SURFACE_VIEW_WIDTH,SURFACE_VIEW_HEIGHT-60);




    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            synchronized(holder) {
                SURFACE_VIEW_WIDTH = mSurfaceView.getMeasuredWidth();
                SURFACE_VIEW_HEIGHT=mSurfaceView.getMeasuredHeight();
                HORIZONTAL_SPLIT=SURFACE_VIEW_WIDTH/6;
                preparePath();

                onDraw(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }


    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        canvas.drawPath(path,paint);
        canvas.drawCircle(10,SURFACE_VIEW_HEIGHT-60,10,paint);

        //bobPath.moveTo(0,SURFACE_VIEW_HEIGHT-60);
        //bobPath.q;


        /*
        for (int i = 0; i < SURFACE_VIEW_WIDTH; i++)
        {


            Log.e("co-ordinates",
                    "("+Integer.toString(i)+","
                            +(((SURFACE_VIEW_HEIGHT) +(SURFACE_VIEW_HEIGHT/2)*(float)Math.sin(i/180.0*Math.PI)))+") ("+Integer.toString(i+10)+","
                            +
                            Float.toString((SURFACE_VIEW_HEIGHT)  +(SURFACE_VIEW_HEIGHT/2)*(float)Math.sin ((i + 10)/180.0*Math.PI))+")");

            canvas.drawLine (i,
                    (SURFACE_VIEW_HEIGHT) +(SURFACE_VIEW_HEIGHT/2)*(float)Math.sin(i/180.0*Math.PI)
                    (((0) +(SURFACE_VIEW_HEIGHT-20)*(float)Math.sin(i/180.0*Math.PI))),
                            i + 10,
                    ((0)  +(SURFACE_VIEW_HEIGHT-20)*(float)Math.sin ((i)/180.0*Math.PI)), paint);
        }
    */

        initAnimObjects();
        setAnimationListners();
        mImgVwPixel.startAnimation(animation1);

    }


    private void setAnimationListners(){
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation2.reset();
                mImgVwPixel.startAnimation(animation2);
                mTxtVwBreathPhaseStatus.setText("--INHALE--");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation4.reset();
                mImgVwPixel.startAnimation(animation4);
                mTxtVwBreathPhaseStatus.setText("--EXHALE--");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation4.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation5.reset();
                mImgVwPixel.startAnimation(animation5);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation5.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation1.reset();
                //mTxtVwPacer.setAnimation(animation1);
                mImgVwPixel.startAnimation(animation1);
                mTxtVwBreathPhaseStatus.setText("--INHALE--");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    // setup the graph object

    private void initGraph(){
        mGraphView=(GraphView)findViewById(R.id.rmsGraph);
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



    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                // connection established

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

                // gatt connection lost
                //showConnectionLostPromt();


            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                // connect to heart rate service for this gatt server.
                initHeartRateReading();

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                try{
                    int heartRate = intent.getIntExtra("HEART_RATE",0);
                    // Bundle extra = getIntent().getBundleExtra("RR_VALUE");
                    //int[] rrSamples = (int[]) extra.getSerializable("RR_VALUE_ARRAY");
                    int rRvalue = intent.getIntExtra("RR_VALUE",0);

                    updateDataonUI(heartRate,rRvalue);
                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        }
    };




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


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }



    private synchronized void  updateDataonUI(int hRate_, int rrValue){


        if(rrValue==0 )return;// putting a check here to prevent infite value for Y axis of the chart

        lastRRValue=lastRRValue+rrValue;
        // prepareSamples(hRate,rrValue);
        //calculate heart rate value form the current RR value sample.
        //double currentRMSSD = ((double)1/rrValue)*60*1000;
        ArrayList<Integer> rrReadings = new ArrayList<>();
        rrReadings.clear();
        rrReadings.addAll(HRVAppInstance.getAppInstance().getRR_READINGS());

        Double rmsValue = mathHelper.computeRMS(rrReadings);

        series.appendData(new DataPoint(lastRRValue,rmsValue ), true, 5000);
        mGraphView.removeSeries(series);
        //  mGraphView.getViewport().setMinX(series.getHighestValueX());
        // mGraphView.getViewport().setMaxX(series.getHighestValueX()+100000);
        //mGraphVie


        if(rmsValue>100 ){
            mGraphView.getViewport().setMinY((double)(rmsValue-75));
            //mGraphView.getViewport().setMaxY(Collections.max(hrSamplesForGraph));
            mGraphView.getViewport().setMaxY((double)(rmsValue+50));
        }
        if(rmsValue>60 && rmsValue<=100){
            mGraphView.getViewport().setMinY((double)(rmsValue-40));
            //mGraphView.getViewport().setMaxY(Collections.max(hrSamplesForGraph));
            mGraphView.getViewport().setMaxY((double)(rmsValue+30));
        }if(rmsValue<=60){
            mGraphView.getViewport().setMinY((double) (rmsValue - 15));
            //mGraphView.getViewport().setMaxY(Collections.max(hrSamplesForGraph));
            mGraphView.getViewport().setMaxY((double) (rmsValue + 35));
        }
        mGraphView.getViewport().setScalableY(true);
        mGraphView.addSeries(series);

        DecimalFormat df = new DecimalFormat("#.##");
       // String strSDNN = df.format(SDNN);
        String strRMSSD = df.format(rmsValue);
        //String strLnRMSSD = df.format(lnRMSSD);
        //String strHRV = df.format(hrv);
        //String strHRate = df.format(meanHR);
        mTxtVwRMSValue.setText("RMS- "+strRMSSD);


    }




    private void computeHRfromRR(){
        try {
            if (isSessionLive) {
               // long timeElapsedInMilis = System.currentTimeMillis() - startTime;
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


                // update the current session object, with the latest set of data.





            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
