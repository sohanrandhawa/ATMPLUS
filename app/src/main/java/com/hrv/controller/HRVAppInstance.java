package com.hrv.controller;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.hrv.models.SessionTemplate;
import com.mobiprobe.Mobiprobe;
import com.orm.SugarApp;
import com.orm.SugarContext;

import java.util.ArrayList;

/**
 * Created by manishautomatic on 26/09/16.
 */

public class HRVAppInstance extends SugarApp {


    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mContext;

    private static HRVAppInstance instance;
    private final ArrayList<Integer> RR_READINGS = new ArrayList<>();
    private ArrayList<Integer> CURRENT_RR_PACKET = new ArrayList<>();
    private SessionTemplate CURRENT_SESSION = new SessionTemplate();
    private final String API_ENDPOINT = "https://www.mobiprobe.com/hrvb/endpoint/";
    private final String PUBLIC_LOGIN = "publiclogin.php";
    private final String PUBLIC_SIGNUP = "athletesignup.php";
    private final String SYNC_SESSION = "logsession.php";


    public BluetoothDevice getCurrentBLEDevice() {
        return currentBLEDevice;
    }

    public void setCurrentBLEDevice(BluetoothDevice currentBLEDevice) {
        this.currentBLEDevice = currentBLEDevice;
    }

    private BluetoothDevice currentBLEDevice;

    @Override
    public void onCreate(){
        super.onCreate();
        instance = this;
        Mobiprobe.activate(this,"bd49783a");
        mContext=this;
        initVolley();
    }


    public synchronized ArrayList<Integer> getCURRENT_RR_PACKET() {
        return CURRENT_RR_PACKET;
    }

    public synchronized  void setCURRENT_RR_PACKET(ArrayList<Integer> CURRENT_RR_PACKET) {
        this.CURRENT_RR_PACKET = CURRENT_RR_PACKET;
    }

    public static HRVAppInstance getAppInstance(){
        return instance;
    }

    public String getAPI_ENDPOINT() {
        return API_ENDPOINT;
    }

    public String getPUBLIC_LOGIN() {
        return PUBLIC_LOGIN;
    }

    public synchronized ArrayList<Integer> getRR_READINGS() {
        return RR_READINGS;
    }


    public String getPUBLIC_SIGNUP() {
        return PUBLIC_SIGNUP;
    }


    public String getSYNC_SESSION() {
        return SYNC_SESSION;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }

    private void initVolley(){
        mRequestQueue = getRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }


    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public SessionTemplate getCURRENT_SESSION() {
        return CURRENT_SESSION;
    }

    public void setCURRENT_SESSION(SessionTemplate CURRENT_SESSION) {
        this.CURRENT_SESSION = CURRENT_SESSION;
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
