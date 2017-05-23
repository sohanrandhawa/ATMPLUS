package com.hrv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.hrv.controller.HRVAppInstance;
import com.hrv.utils.CommonUtils;
import com.hrv.utils.PreferenceHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by manishautomatic on 19/05/17.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

        private EditText mEdtxtEmail;
        private EditText mEdtxtPassword;
        private Button mBtnLogin;
    private TextView mTxtVwSignUp;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_login);
        // if the user is already logged in then straightaway take the user to the monitor
        String userID = PreferenceHandler.readString(LoginActivity.this,PreferenceHandler.USER_ID,"");
        if(!userID.trim().equalsIgnoreCase("")){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
        initUI();

    }

    @Override
    public void onClick(View view) {
        if(view==mBtnLogin){
           if(validateCredentials()){
               String email = mEdtxtEmail.getText().toString();
               String password = mEdtxtPassword.getText().toString();
               volleyLogin(email,password);
           }
        }if(view==mTxtVwSignUp){
            startActivity(new Intent(LoginActivity.this,SignUpActivity.class));
            finish();
        }
    }

    private void initUI(){
        mEdtxtEmail=(EditText)findViewById(R.id.edtxtEmail);
        mEdtxtPassword=(EditText)findViewById(R.id.edtxtPassword);
        mTxtVwSignUp=(TextView)findViewById(R.id.txtvwSignUp);
        mBtnLogin=(Button)findViewById(R.id.btnLogin);
        mBtnLogin.setOnClickListener(this);
        mTxtVwSignUp.setOnClickListener(this);
    }


// verify if the user has provided valid credentials.
    private boolean validateCredentials(){
        String email = mEdtxtEmail.getText().toString();
        String password = mEdtxtPassword.getText().toString();
        if(!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // do nothing
        }else{
            Toast.makeText(LoginActivity.this,"Please provide a valid email",Toast.LENGTH_LONG).show();
            return false;
        }
        if(password.trim().equalsIgnoreCase("")){
            Toast.makeText(LoginActivity.this,"Please provide a valid password",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // perform login action via Volley...

    private void volleyLogin(final String email, final String password) {


        CommonUtils.showProgress(LoginActivity.this, getResources().getString(R.string.validating_please_wait));

        StringRequest sr = new StringRequest(Request.Method.POST,
                HRVAppInstance.getAppInstance().getAPI_ENDPOINT()+HRVAppInstance.getAppInstance().getPUBLIC_LOGIN(),
                new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                CommonUtils.dismissProgress();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int response_code = jsonObject.getInt("response_code");
                    String response_message = jsonObject.getString("response_message");
                    if (response_code == 1) {
                      String user_id = jsonObject.getString("user_id");
                        PreferenceHandler.writeString(LoginActivity.this,
                                            PreferenceHandler.USER_ID,user_id);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        CommonUtils.showSmallToast(LoginActivity.this, response_message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    CommonUtils.showLongToast(LoginActivity.this,
                                getResources().getString(R.string.response_parse_error));
                }

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CommonUtils.dismissProgress();
                CommonUtils.showLongToast(LoginActivity.this, getResources().getString(R.string.server_error));
                CommonUtils.myLog("onErrorResponse:  " + error.getMessage(), error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", CommonUtils.convertToMD5(password));

                return params;
            }
        };
        sr.setShouldCache(false);
        HRVAppInstance.getAppInstance().addToRequestQueue(sr, "login");

    }
}
