package com.hrv;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hrv.helpers.DrawView;

/**
 * Created by manishautomatic on 05/12/16.
 */

public class ActivityBreathingPacer extends Activity {

    private ImageView mImgVwPixel;
    private Animation tweenAnimation;
    private Animation animation1,
                        animation2,
                        animation3,
                        animation4,
                        animation5;
    //private int SCREEN_WIDTH=0;
    //private int SCREEN_HEIGHT =0;
    private int X_AXIS_PHASE_LENGTH=0;
    private RelativeLayout relVwPacerContainer;



    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_breathing_pacer);
        configureScreenDimensions();
        initAnimObjects();
        initUI();
        setAnimationListners();
        mImgVwPixel.setAnimation(animation1);
    }


    private void configureScreenDimensions(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width= size.x;
        X_AXIS_PHASE_LENGTH=width/6;
    }

    private void initAnimObjects(){
         animation1 = new TranslateAnimation(0, X_AXIS_PHASE_LENGTH,0, 0);
        animation1.setDuration(1000);
        animation1.setFillAfter(true);
        //PHASE -2 : translate linear-up, from (x1,0)-->(x2,y1)
         animation2 = new TranslateAnimation(X_AXIS_PHASE_LENGTH, (float)(2.5)*X_AXIS_PHASE_LENGTH,0, -200);
        animation2.setDuration(2000);
       // animation2.setStartOffset(1500);
        animation2.setFillAfter(true);
       // mTxtVwPacer.startAnimation(animation2);
        //PHASE -3 : translate horizontal-flat, from (x2,y1)-->(x3,y1)
         animation3 = new TranslateAnimation((float)(2.5)*X_AXIS_PHASE_LENGTH, (float)(3.5)*X_AXIS_PHASE_LENGTH,-200, -200);
        animation3.setDuration(1000);
        //animation2.setStartOffset(3000);
        animation3.setFillAfter(true);
        //mTxtVwPacer.startAnimation(animation3);
        //PHASE -4 : translate linear-downward, from (x3,y1)-->(x4,y0)
         animation4 = new TranslateAnimation((float)(3.5)*X_AXIS_PHASE_LENGTH, 5*X_AXIS_PHASE_LENGTH,-200, 0);
        animation4.setDuration(2000);
        //animation2.setStartOffset(4500);
        animation4.setFillAfter(true);
        //mTxtVwPacer.startAnimation(animation4);
        //PHASE -5 : translate horizontal-flat-end, from (x2,y1)-->(x3,y1)
         animation5 = new TranslateAnimation(5*X_AXIS_PHASE_LENGTH, 6*X_AXIS_PHASE_LENGTH,0, 0);
        animation5.setDuration(1000);
        //animation2.setStartOffset(6000);
        animation5.setFillAfter(false);
        //mTxtVwPacer.startAnimation(animation5);
    }

    private void initUI(){
        mImgVwPixel =(ImageView) findViewById(R.id.imgVwCircle);
        relVwPacerContainer = (RelativeLayout) findViewById(R.id.vwPacerLine);
        DrawView drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.WHITE);
        relVwPacerContainer.addView(drawView);

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

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
