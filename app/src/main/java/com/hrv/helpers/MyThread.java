package com.hrv.helpers;

import android.graphics.Canvas;

/**
 * Created by manishautomatic on 07/12/16.
 */

public class MyThread extends Thread {

    MySurfaceView myView;
    private boolean running = false;

    public MyThread(MySurfaceView view) {
        myView = view;
    }

    public void setRunning(boolean run) {
        running = run;
    }

    @Override
    public void run() {
        while(running){

            Canvas canvas = myView.getHolder().lockCanvas();

            if(canvas != null){
                synchronized (myView.getHolder()) {
                   // myView.drawSomething(canvas);
                }
                myView.getHolder().unlockCanvasAndPost(canvas);
            }

            try {
                sleep(30);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
}
