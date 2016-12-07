package com.hrv.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hrv.R;

/**
 * Created by manishautomatic on 07/12/16.
 */

public class MySurfaceView extends SurfaceView {

    private SurfaceHolder surfaceHolder;
    private Bitmap bmpIcon;
    private MyThread myThread;
    int xPos = 0;
    int yPos = 0;
    int deltaX = 5;
    int deltaY = 5;
    int iconWidth;
    int iconHeight;

    public MySurfaceView(Context context) {
        super(context);
        init();
    }

    public MySurfaceView(Context context,
                         AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySurfaceView(Context context,
                         AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){

        myThread = new MyThread(this);

        surfaceHolder = getHolder();
        bmpIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.add);

        iconWidth = bmpIcon.getWidth();
        iconHeight = bmpIcon.getHeight();

        surfaceHolder.addCallback(new SurfaceHolder.Callback(){

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                myThread.setRunning(true);
                myThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder,
                                       int format, int width, int height) {
                // TODO Auto-generated method stub

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                myThread.setRunning(false);
                while (retry) {
                    try {
                        myThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                    }
                }
            }});
    }

    protected void drawSomething(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmpIcon,
                getWidth()/2, getHeight()/2, null);

        xPos += deltaX;
        if(deltaX > 0){
            if(xPos >= getWidth() - iconWidth){
                deltaX *= -1;
            }
        }else{
            if(xPos <= 0){
                deltaX *= -1;
            }
        }

        yPos += deltaY;
        if(deltaY > 0){
            if(yPos >= getHeight() - iconHeight){
                deltaY *= -1;
            }
        }else{
            if(yPos <= 0){
                deltaY *= -1;
            }
        }

        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmpIcon,
                xPos, yPos, null);

    }

}
