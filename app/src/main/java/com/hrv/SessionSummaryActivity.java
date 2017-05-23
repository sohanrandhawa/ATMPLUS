package com.hrv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.hrv.controller.HRVAppInstance;
import com.hrv.utils.CommonUtils;
import com.hrv.utils.PreferenceHandler;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by manishautomatic on 20/05/17.
 */
public class SessionSummaryActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText mEdtxtSessionActivityType;
    private EditText mEdtxtSessionReadingPosition;
    private Button mBtnSyncSession;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_session_summary);
        initUI();
    }


    private void initUI(){
        mEdtxtSessionReadingPosition=(EditText)findViewById(R.id.edtxtReadingPosition);
        mEdtxtSessionActivityType=(EditText)findViewById(R.id.edtxtActivityType);
        mBtnSyncSession=(Button)findViewById(R.id.btnSync);
        mBtnSyncSession.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view==mBtnSyncSession){
            if(validateinputs()){
                volleySyncSession();
            }
        }

    }


    private boolean validateinputs(){
        if(mEdtxtSessionReadingPosition.getText().toString().trim().equalsIgnoreCase("")){
            CommonUtils.showLongToast(SessionSummaryActivity.this, "please provide session reading position");
            return false;
        }if(mEdtxtSessionActivityType.getText().toString().trim().equalsIgnoreCase("")){
            CommonUtils.showLongToast(SessionSummaryActivity.this, "please provide activity type ");
            return false;
        }
        return true;
    }

 // sync the session to the server
 private void volleySyncSession() {

     CommonUtils.showProgress(SessionSummaryActivity.this, getResources().getString(R.string.sync_in_progress));

     StringRequest sr = new StringRequest(Request.Method.POST,
             HRVAppInstance.getAppInstance().getAPI_ENDPOINT()+HRVAppInstance.getAppInstance().getSYNC_SESSION(),
             new com.android.volley.Response.Listener<String>() {
                 @Override
                 public void onResponse(String response) {
                     CommonUtils.dismissProgress();
                     try {
                         JSONObject jsonObject = new JSONObject(response);
                         int response_code = jsonObject.getInt("response_code");
                         String response_message = jsonObject.getString("response_message");
                         if (response_code == 1) {

                             CommonUtils.showLongToast(SessionSummaryActivity.this, "session synced successfully");
                             finish();
                         } else {
                             CommonUtils.showSmallToast(SessionSummaryActivity.this, response_message);
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                         CommonUtils.showLongToast(SessionSummaryActivity.this,
                                 getResources().getString(R.string.response_parse_error));
                     }

                 }
             }, new com.android.volley.Response.ErrorListener() {
         @Override
         public void onErrorResponse(VolleyError error) {
             CommonUtils.dismissProgress();
             CommonUtils.showLongToast(SessionSummaryActivity.this, getResources().getString(R.string.server_error));
             CommonUtils.myLog("onErrorResponse:  " + error.getMessage(), error.toString());
         }
     }) {
         @Override
         protected Map<String, String> getParams() {
             Map<String, String> params = new HashMap<String, String>();
                Gson gson = new Gson();
             Calendar cal = Calendar.getInstance(Locale.ENGLISH);
             cal.setTimeInMillis(HRVAppInstance.getAppInstance().getCURRENT_SESSION().getStartTime());
              final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
             String formattedDate = dateformat.format(cal.getTime());
             String stringifiedSamples = gson.toJson(HRVAppInstance.getAppInstance().getCURRENT_SESSION().getSamples());
             params.put("athlete_id", PreferenceHandler.readString(SessionSummaryActivity.this, PreferenceHandler.USER_ID,""));
             params.put("duration", Long.toString(HRVAppInstance.getAppInstance().getCURRENT_SESSION().getTimeElapsed()));
             params.put("reading_position", mEdtxtSessionReadingPosition.getText().toString());
             params.put("hrv", Double.toString(HRVAppInstance.getAppInstance().getCURRENT_SESSION().getHrvValue()));
             params.put("rmssd", Double.toString(HRVAppInstance.getAppInstance().getCURRENT_SESSION().getRms()));
             params.put("lnrmssd", Double.toString(HRVAppInstance.getAppInstance().getCURRENT_SESSION().getLnRMS()));
             params.put("heart_rate", Double.toString(HRVAppInstance.getAppInstance().getCURRENT_SESSION().getAvgHeartRate()));
             params.put("sdnn", Double.toString(HRVAppInstance.getAppInstance().getCURRENT_SESSION().getSdNN()));
             params.put("start_time",formattedDate );
             params.put("end_time", HRVAppInstance.getAppInstance().getCURRENT_SESSION().getEndTime());
             params.put("activity", mEdtxtSessionActivityType.getText().toString().trim());
             params.put("samples", stringifiedSamples);


             return params;
         }
     };
     sr.setShouldCache(false);
     HRVAppInstance.getAppInstance().addToRequestQueue(sr, "login");

 }


}
