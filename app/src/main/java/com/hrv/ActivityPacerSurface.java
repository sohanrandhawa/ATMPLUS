package com.hrv;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by manishautomatic on 07/12/16.
 */

public class ActivityPacerSurface extends Activity implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Paint paint;
    private int SURFACE_VIEW_WIDTH=0;
    private int SURFACE_VIEW_HEIGHT=0;
    private int HORIZONTAL_SPLIT=0;
    private TextView mTxtVwBreathPhaseStatus;

    // logic for animated ball
    private ImageView mImgVwPixel;
    private Animation tweenAnimation;
    private Animation animation1,
            animation2,
            animation3,
            animation4,
            animation5;
    //private int SCREEN_WIDTH=0;
    //private int SCREEN_HEIGHT =0;
    //private int X_AXIS_PHASE_LENGTH=0;
    private RelativeLayout relVwPacerContainer;
    private RelativeLayout mRelLytSurfaceContainer;


    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_pacer_surface);
        initUI();

    }


    private void initAnimObjects(){

        animation1 = new TranslateAnimation(0, HORIZONTAL_SPLIT-5,0, 0);
        animation1.setDuration(1000);
        animation1.setFillAfter(true);
        //PHASE -2 : translate linear-up, from (x1,0)-->(x2,y1)
        animation2 = new TranslateAnimation(HORIZONTAL_SPLIT-5, (float)(2.5)*(HORIZONTAL_SPLIT-5), 0, -(SURFACE_VIEW_HEIGHT-60)/2);
        animation2.setDuration(2000);
        // animation2.setStartOffset(1500);
        animation2.setFillAfter(true);
        // mTxtVwPacer.startAnimation(animation2);
        //PHASE -3 : translate horizontal-flat, from (x2,y1)-->(x3,y1)
        animation3 = new TranslateAnimation((float)(2.5)*(HORIZONTAL_SPLIT-5), (float)(3.5)*(HORIZONTAL_SPLIT-5),-(SURFACE_VIEW_HEIGHT-60)/2, -(SURFACE_VIEW_HEIGHT-60)/2);
        animation3.setDuration(1000);
        //animation2.setStartOffset(3000);
        animation3.setFillAfter(true);
        //mTxtVwPacer.startAnimation(animation3);
        //PHASE -4 : translate linear-downward, from (x3,y1)-->(x4,y0)
        animation4 = new TranslateAnimation((float)(3.5)*(HORIZONTAL_SPLIT-5), 5*(HORIZONTAL_SPLIT),-(SURFACE_VIEW_HEIGHT-60)/2, 0);
        animation4.setDuration(2000);
        //animation2.setStartOffset(4500);
        animation4.setFillAfter(true);
        //mTxtVwPacer.startAnimation(animation4);
        //PHASE -5 : translate horizontal-flat-end, from (x2,y1)-->(x3,y1)
        animation5 = new TranslateAnimation(5*(HORIZONTAL_SPLIT), 6*(HORIZONTAL_SPLIT),0, 0);
        animation5.setDuration(1000);
        //animation2.setStartOffset(6000);
        animation5.setFillAfter(false);
        //mTxtVwPacer.startAnimation(animation5);
    }


    private void initUI(){
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(8);
        paint.setDither(true);                    // set the dither to true
        paint.setStyle(Paint.Style.STROKE);       // set to STOKE
        paint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        paint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        paint.setAntiAlias(true);

        // init the path object


        mSurfaceView = (SurfaceView)findViewById(R.id.sfvw);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceView.setWillNotDraw(true);
        mSurfaceHolder.addCallback(this);
        mRelLytSurfaceContainer =(RelativeLayout)findViewById(R.id.relLytSurfaceContainer);
        mImgVwPixel=(ImageView)findViewById(R.id.imgVwCircle);
        mTxtVwBreathPhaseStatus=(TextView)findViewById(R.id.txtvwBreathStatus);


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
        //draw flat
        canvas.drawLine(0, SURFACE_VIEW_HEIGHT-60, HORIZONTAL_SPLIT, SURFACE_VIEW_HEIGHT-60, paint);
        // ramp-up
        canvas.drawLine(HORIZONTAL_SPLIT, SURFACE_VIEW_HEIGHT-60, (float)2.5*HORIZONTAL_SPLIT,(SURFACE_VIEW_HEIGHT-60)/2 , paint);
        // translate-flat
        canvas.drawLine((float)2.5*HORIZONTAL_SPLIT, (SURFACE_VIEW_HEIGHT-60)/2,(float)3.5*HORIZONTAL_SPLIT ,(SURFACE_VIEW_HEIGHT-60)/2 , paint);
        //ramp-down
        canvas.drawLine((float)3.5*HORIZONTAL_SPLIT, (SURFACE_VIEW_HEIGHT-60)/2,(float)5*HORIZONTAL_SPLIT ,(SURFACE_VIEW_HEIGHT-60) , paint);
        // end-flat
        canvas.drawLine((float)5*HORIZONTAL_SPLIT, (SURFACE_VIEW_HEIGHT-60),(float)6*HORIZONTAL_SPLIT ,(SURFACE_VIEW_HEIGHT-60) , paint);

        //bobPath.moveTo(0,SURFACE_VIEW_HEIGHT-60);
        //bobPath.q;
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
                animation3.reset();
                mImgVwPixel.startAnimation(animation3);
                mTxtVwBreathPhaseStatus.setText("--HOLD--");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation3.setAnimationListener(new Animation.AnimationListener() {
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

}
