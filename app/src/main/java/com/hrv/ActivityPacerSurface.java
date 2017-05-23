package com.hrv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hrv.computation.MathHelper;
import com.hrv.controller.BluetoothLeService;
import com.hrv.controller.HRVAppInstance;
import com.hrv.models.SessionTemplate;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by manishautomatic on 07/12/16.
 */

public class ActivityPacerSurface extends AppCompatActivity implements
        SurfaceHolder.Callback, View.OnClickListener {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Paint paint, bluePaint;
    private Path path;
    private int SURFACE_VIEW_WIDTH=0;
    private int SURFACE_VIEW_HEIGHT=0;
    private int HORIZONTAL_SPLIT=0;

    private final String HEART_RATE_SERVICE_CONST="0000180d-0000-1000-8000-00805f9b34fb";
    private final UUID HEART_RATE_SERVICE_UUID = UUID.fromString(HEART_RATE_SERVICE_CONST);
    private BluetoothGattService heartRateService;
    private Bitmap bitmapBlueBall;
    private Button mBtnTogglePacerMode, mBtnToggleSession;

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
    private int summationRRValue=0;
    private final LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
    //private boolean isSessionLive=false;
    private TextView mTxtVwRMSValue;
    private TextView mTxtVwAnimationPhaseDuration;
    private TextView mTxtVwBreathPhaseStatus, mtxtVwSessionDuration;

    private SeekBar mSkBarPacerSpeed;
    private int BREATH_HALF_PHASE_DURATION=2000;
    private TextView mTxtVwBreathsPerMinute;
    private int RR_SAMPLE_N =0;
    private int RR_SAMPLE_N_1=0;
    private int RR_SAMPLE_N_2=0;
    /*
        defining some variables for guiding the breathing pacer
     */
    private int LAST_RR_MEASURED =0;
    private int RR_SAMPLES_MEASURED = 0;
    private int MAX_SAMPLES_PRIOR_TO_COMPUTATION=10;
    private long breathingPhaseStartTime=0;
    private long INHALATION_PHASE_DURATION=0;
    private long EXHALATION_PHASE_DURATION=0;
    private boolean BREATHING_PHASE_INHALATION=true;
    private int BREATHING_PHASE =0; // this will toggle between  -1 ( exhalation) and 1 (inhalation), each | 0= initialization value
    private long currentBreathCycleDuration;
    private TextView mTxtVwEvaluatedBreathDuration;
    private long trainingBreathCycleDuration=0;
    final Handler timerHandler = new Handler();
    private boolean PACER_MODE_AUTOMATIC=true, IS_SESSION_LIVE=false;
    private long sessionStartTime=0;
    private Runnable updater;
    private SessionTemplate currentSession;
    private ProgressDialog mPrgDlgInitializing;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_pacer_surface);
        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        mathHelper = new MathHelper();
        initUI();
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

    }


    private void initAnimObjects(){
            // x1-x2 . y1-y2
        animation1 = new TranslateAnimation(0, 10,0, 0);
        animation1.setDuration(100);
        animation1.setFillAfter(true);
        //PHASE -2 : translate linear-up, from (x1,0)-->(x2,y1)
        animation2 = new TranslateAnimation(10, (float)(3)*(HORIZONTAL_SPLIT), 0, -(SURFACE_VIEW_HEIGHT-60)/2);
        animation2.setDuration(BREATH_HALF_PHASE_DURATION);
        // animation2.setStartOffset(1500);
        animation2.setFillAfter(true);
         //PHASE -4 : translate linear-downward, from (x3,y1)-->(x4,y0)
        animation4 = new TranslateAnimation((float)(3)*(HORIZONTAL_SPLIT), SURFACE_VIEW_WIDTH-10,-(SURFACE_VIEW_HEIGHT-60)/2, 0);
        animation4.setDuration(BREATH_HALF_PHASE_DURATION);
        //animation2.setStartOffset(4500);
        animation4.setFillAfter(true);
        //mTxtVwPacer.startAnimation(animation4);
        //PHASE -5 : translate horizontal-flat-end, from (x2,y1)-->(x3,y1)
        animation5 = new TranslateAnimation(SURFACE_VIEW_WIDTH-10, 6*(HORIZONTAL_SPLIT),0, 0);
        animation5.setDuration(100);
        //animation2.setStartOffset(6000);
        animation5.setFillAfter(false);
        //mTxtVwPacer.startAnimation(animation5);
    }


    private void initUI(){
        mPrgDlgInitializing=new ProgressDialog(ActivityPacerSurface.this);
        mPrgDlgInitializing.setMessage("Initializing, please wait...");
        mPrgDlgInitializing.setCancelable(false);
        mPrgDlgInitializing.show();
        bitmapBlueBall= BitmapFactory.decodeResource(getResources(), R.drawable.circle);
        mBtnTogglePacerMode=(Button) findViewById(R.id.btnToggleMode);
        mtxtVwSessionDuration=(TextView)findViewById(R.id.txtvwBreathingSessionDuration);
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
        mTxtVwEvaluatedBreathDuration=(TextView)findViewById(R.id.txtvwEvaluatedBreathDuration);
        mTxtVwAnimationPhaseDuration=(TextView)findViewById(R.id.txtvAnimationPhaseDuration);
        mBtnToggleSession=(Button)findViewById(R.id.btnToggleBreathingSession);
        mBtnToggleSession.setOnClickListener(this);
        mTxtVwBreathsPerMinute=(TextView)findViewById(R.id.txtvwBreathsPerMinute);
        mTxtVwBreathsPerMinute.setText("Seekbar (BPM)=15");

        mSkBarPacerSpeed =(SeekBar)findViewById(R.id.skbarPaceController);
        mSkBarPacerSpeed.setProgress(15);
        mSkBarPacerSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i==0)i=1;
                int secondsPerBreath = 60/i;
                int millisPerBreath = secondsPerBreath*1000;
                BREATH_HALF_PHASE_DURATION = millisPerBreath/2;

                mTxtVwBreathsPerMinute.setText("Seekbar (BPM)="+Integer.toString(i));
                mTxtVwAnimationPhaseDuration.setText("Pacer Duration: "+Integer.toString(2*BREATH_HALF_PHASE_DURATION));
                mImgVwPixel.clearAnimation();
                updateAnimationObjects();
                mImgVwPixel.startAnimation(animation2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mBtnTogglePacerMode.setOnClickListener(this);
    }



    private void updateAnimationObjects(){
        animation2.setDuration(BREATH_HALF_PHASE_DURATION);
        animation4.setDuration(BREATH_HALF_PHASE_DURATION);
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
        initAnimObjects();
        setAnimationListners();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImgVwPixel.startAnimation(animation1);
            }
        });


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
                updateAnimationObjects();
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
                int heartRate = intent.getIntExtra("HEART_RATE",0);
                // Bundle extra = getIntent().getBundleExtra("RR_VALUE");
                //int[] rrSamples = (int[]) extra.getSerializable("RR_VALUE_ARRAY");
                int rRvalue = intent.getIntExtra("RR_VALUE",0);

                if(PACER_MODE_AUTOMATIC){
                    computeBreathPaceFromSensor(heartRate,rRvalue);
                    mSkBarPacerSpeed.setEnabled(false);
                }else{
                    mSkBarPacerSpeed.setEnabled(true);
                }

                if(RR_SAMPLES_MEASURED<MAX_SAMPLES_PRIOR_TO_COMPUTATION){

                }else{
                    updateDataonUI(heartRate,rRvalue);
                    mPrgDlgInitializing.cancel();
                }



            }
        }
    };




    private void computeBreathPaceFromSensor(int HEART_RATE, int rRvalue){
        try{
             if(RR_SAMPLES_MEASURED<MAX_SAMPLES_PRIOR_TO_COMPUTATION){
                RR_SAMPLES_MEASURED++;
            }else{
                //
                if(RR_SAMPLE_N==0){
                    breathingPhaseStartTime = System.currentTimeMillis();
                    RR_SAMPLE_N=rRvalue;
                }else{
                    if(RR_SAMPLE_N_1==0){
                        RR_SAMPLE_N_1=rRvalue;
                        // lets set the current mode..
                        if((RR_SAMPLE_N_1-RR_SAMPLE_N)>0){
                            BREATHING_PHASE=1;// EXHALE
                        }else{
                            BREATHING_PHASE=-1;//INHALE
                        }
                    }else{
                        if(RR_SAMPLE_N_2==0){
                            RR_SAMPLE_N_2=rRvalue;
                        }
                        // lets compare if the MODE has changed..

                        if(((rRvalue-RR_SAMPLE_N_2)>0 && BREATHING_PHASE==1)
                                || ((rRvalue-RR_SAMPLE_N_2)<0 && BREATHING_PHASE==-1)){

                            // phase is same, do nothing, just update the values of samples.
                            RR_SAMPLE_N=RR_SAMPLE_N_1;
                            RR_SAMPLE_N_1=RR_SAMPLE_N_2;
                            RR_SAMPLE_N_2=rRvalue;
                        }else{
                            // phase has toggled.
                            if(BREATHING_PHASE==1){
                                EXHALATION_PHASE_DURATION=System.currentTimeMillis()-breathingPhaseStartTime;
                                BREATHING_PHASE=-1;
                            }else{
                                INHALATION_PHASE_DURATION=System.currentTimeMillis()-breathingPhaseStartTime;
                                BREATHING_PHASE=1;
                            }
                            breathingPhaseStartTime = System.currentTimeMillis();

                            // lets up-shift the SAMPLES

                            RR_SAMPLE_N=RR_SAMPLE_N_1;
                            RR_SAMPLE_N_1=RR_SAMPLE_N_2;
                            RR_SAMPLE_N_2=rRvalue;
                            currentBreathCycleDuration=INHALATION_PHASE_DURATION+EXHALATION_PHASE_DURATION;
                            double breathPerMinuteMeasured = (1/(((double)currentBreathCycleDuration)/1000))*60;

                            if(breathPerMinuteMeasured>15){

                            }else{
                                DecimalFormat df = new DecimalFormat("#.##");
                                mTxtVwEvaluatedBreathDuration.setText("Sensor (BPM): "
                                        +df.format(breathPerMinuteMeasured));
                                configureProjectedBreath(breathPerMinuteMeasured);

                            }

                        }
                    }


                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    // this function defines the rules for
    // determining the training breathing rate
    // from the evaluated rate.
    // these are the rules.
    /*
       >15   15
        15	13
        14	12
        13	11
        12	10
        11	9
        10	8
        9	8
        8	8
     */

    private void configureProjectedBreath(double measuredBrPM){
        trainingBreathCycleDuration=0;
        if(measuredBrPM>15){
            trainingBreathCycleDuration=15;
        }else if(measuredBrPM==15){
            trainingBreathCycleDuration=13;
        }else if(measuredBrPM>=14){
            trainingBreathCycleDuration=12;
        }else if(measuredBrPM>=13){
            trainingBreathCycleDuration=11;
        }else if(measuredBrPM>=12){
            trainingBreathCycleDuration=10;
        }else if(measuredBrPM>=11){
            trainingBreathCycleDuration=9;
        }else if(measuredBrPM>=10){
            trainingBreathCycleDuration=8;
        }else if(measuredBrPM>=9){
            trainingBreathCycleDuration=8;
        }else if(measuredBrPM>=8){
            trainingBreathCycleDuration=8;
        }

        updatePacerSpeed();

    }


    private void updatePacerSpeed(){
        //subtracting 200 ms for 100ms each of left pading and right padding.
       // long halfAnimationDuration = ((trainingBreathCycleDuration*1000)-200)/2;
        if(trainingBreathCycleDuration==0)return;
        Double halfAnimationDuration =    (((60/((double)(trainingBreathCycleDuration)))*1000)-200)/2;
       // BREATH_HALF_PHASE_DURATION= Math.round(halfAnimationDuration);
        BREATH_HALF_PHASE_DURATION=halfAnimationDuration.intValue();
        mTxtVwAnimationPhaseDuration.setText("Breath Duration: "+Integer.toString(BREATH_HALF_PHASE_DURATION));
      //  updateAnimationObjects();
       // resetCurrentAnimation();
    }



    private void resetCurrentAnimation(){
        mImgVwPixel.clearAnimation();
        mImgVwPixel.startAnimation(animation2);
    }




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



    private synchronized void  updateDataonUI(int hRate_, final int rrValue){


        if(rrValue==0 )return;// putting a check here to prevent infite value for Y axis of the chart

        summationRRValue=summationRRValue+rrValue;
        // prepareSamples(hRate,rrValue);
        //calculate heart rate value form the current RR value sample.
        //double currentRMSSD = ((double)1/rrValue)*60*1000;
        ArrayList<Integer> rrReadings = new ArrayList<>();
        rrReadings.clear();
        rrReadings.addAll(HRVAppInstance.getAppInstance().getRR_READINGS());

        Double rmsValue = mathHelper.computeRMS(rrReadings);

        rmsValue=(double)Math.round(rmsValue*100);
        rmsValue = rmsValue/100;

        series.appendData(new DataPoint(summationRRValue,rmsValue ), true, 5000);
        mGraphView.removeSeries(series);
        //  mGraphView.getViewport().setMinX(series.getHighestValueX());
        // mGraphView.getViewport().setMaxX(series.getHighestValueX()+100000);
        //mGraphVie


        mGraphView.getViewport().setMinY((double) (rmsValue - 1));
        mGraphView.getViewport().setMaxY((double) (rmsValue + 1));
         mGraphView.getViewport().setScalableY(true);
        mGraphView.addSeries(series);

        DecimalFormat df = new DecimalFormat("#.##");
       // String strSDNN = df.format(SDNN);
        final String strRMSSD = df.format(rmsValue);
        if(strRMSSD.equalsIgnoreCase("NaN"))return;
        //String strLnRMSSD = df.format(lnRMSSD);
        //String strHRV = df.format(hrv);
        //String strHRate = df.format(meanHR);
        mTxtVwRMSValue.setText("RMS- "+strRMSSD);


        if(IS_SESSION_LIVE){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentSession.setRrValuesDump(currentSession.getRrValuesDump()+"--"+Integer.toString(rrValue));
                    currentSession.setRmsValuesDump(currentSession.getRmsValuesDump()+"--"+strRMSSD);
                    currentSession.setTimeElapsed(System.currentTimeMillis()-sessionStartTime);
                    currentSession.save();
                }
            });
        }


    }


    private String millisToMinutes(long millis){
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(tz);
        return df.format(new Date(millis));

    }

    private void confirmSessionClosure(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to stop this session ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        IS_SESSION_LIVE=false;
                        mBtnToggleSession.setText("START");
                        timerHandler.removeCallbacks(updater);

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




    @Override
    public void onClick(View view) {
        if(view==mBtnTogglePacerMode){
            if(PACER_MODE_AUTOMATIC){
                PACER_MODE_AUTOMATIC=false;
                mBtnTogglePacerMode.setText("AUTO");
                mSkBarPacerSpeed.setEnabled(true);
            }else{
                PACER_MODE_AUTOMATIC=true;
                mBtnTogglePacerMode.setText("MANUAL");
                mSkBarPacerSpeed.setEnabled(false);
            }
        }if(view==mBtnToggleSession){
                if(!IS_SESSION_LIVE){
                    IS_SESSION_LIVE=true;
                    sessionStartTime=System.currentTimeMillis();
                    currentSession=new SessionTemplate();
                    currentSession.setSessionType(2);// this is a breathing training session
                    currentSession.setStartTime(sessionStartTime);
                    currentSession.save();
                    mBtnToggleSession.setText("STOP");
                    updater = new Runnable() {
                        @Override
                        public void run() {
                            mtxtVwSessionDuration.setText("Session Duration:"
                                    +millisToMinutes(System.currentTimeMillis()-sessionStartTime));
                            timerHandler.postDelayed(updater,1000);
                        }
                    };
                    timerHandler.post(updater);
                }else{
                    confirmSessionClosure();
                }
        }
    }
}
