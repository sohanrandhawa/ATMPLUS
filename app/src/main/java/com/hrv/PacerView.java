package com.hrv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

/**
 * Created by manishautomatic on 06/12/16.
 */

public class PacerView extends View {


    private int width;
    private int height;
    Paint paint = new Paint();
    private int xIncrement, yMaxHeight;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint;



    public PacerView(Context context, int X_INCREMENT, int Y_MAX_HEIGHT) {
        super(context);
        this.xIncrement=X_INCREMENT;
        paint.setColor(Color.GREEN);
        this.yMaxHeight=Y_MAX_HEIGHT;
        mBitmapPaint = new Paint();
        mBitmapPaint.setColor(Color.GREEN);
        mBitmapPaint.setStrokeWidth(1);
        mBitmap = Bitmap.createBitmap(6*xIncrement, 300, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);


    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        Log.v("SIZE CHANGED", "CALLED IN CANVAS");

    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        //drawLine(float startX, float startY, float stopX, float stopY, Paint paint)
        //start-flat

    }


    public void drawLine(){
        mCanvas.drawLine(0, 0, xIncrement, 0, paint);
        // ramp-up
        mCanvas.drawLine(xIncrement, 0, (float)2.5*xIncrement,-yMaxHeight , paint);
        // translate-flat
        mCanvas.drawLine((float)2.5*xIncrement, -yMaxHeight,(float)3.5*xIncrement ,-yMaxHeight , paint);
        //ramp-down
        mCanvas.drawLine((float)3.5*xIncrement, -yMaxHeight,(float)5*xIncrement ,0 , paint);
        // end-flat
        mCanvas.drawLine((float)5*xIncrement, 0,(float)6*xIncrement ,0 , paint);
    }
}
