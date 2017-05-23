package com.hrv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hrv.controller.HRVAppInstance;
import com.hrv.utils.CommonUtils;
import com.hrv.utils.PreferenceHandler;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by manishautomatic on 20/05/17.
 */
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener  {

    private EditText mEdtxtEmail;
    private EditText mEdtxtPassword, mEdtxtConfirmPassword;
    private Button mBtnSignUp;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_signup);
        initUI();
    }


    @Override
    public void onClick(View view) {
        if(view==mBtnSignUp){
            if(validateCredentials()){
                String email = mEdtxtEmail.getText().toString();
                String password = mEdtxtPassword.getText().toString();
                String confirmPassword = mEdtxtConfirmPassword.getText().toString();
                volleySignUp(email,password);
            }
        }

    }


    private void initUI(){
        mEdtxtEmail=(EditText)findViewById(R.id.edtxtEmail);
        mEdtxtPassword=(EditText)findViewById(R.id.edtxtPassword);
        mEdtxtConfirmPassword=(EditText)findViewById(R.id.edtxtConfirmPassword);
        mBtnSignUp=(Button)findViewById(R.id.btnSignUp);
        mBtnSignUp.setOnClickListener(this);
    }


    // verify if the user has provided valid credentials.
    private boolean validateCredentials(){
        String email = mEdtxtEmail.getText().toString();
        String password = mEdtxtPassword.getText().toString();
        String confirmPassword = mEdtxtConfirmPassword.getText().toString();
        if(!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // do nothing
        }else{
            Toast.makeText(SignUpActivity.this, "Please provide a valid email", Toast.LENGTH_LONG).show();
            return false;
        }
        if(password.trim().equalsIgnoreCase("")){
            Toast.makeText(SignUpActivity.this,"Please provide a valid password",Toast.LENGTH_LONG).show();
            return false;
        }if(!password.equalsIgnoreCase(confirmPassword)){
            Toast.makeText(SignUpActivity.this,
                    "Password and confirm password do not match...",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }




    private void volleySignUp(final String email, final String password) {


        CommonUtils.showProgress(SignUpActivity.this, getResources().getString(R.string.signing_up_please_wait));

        StringRequest sr = new StringRequest(Request.Method.POST,
                HRVAppInstance.getAppInstance().getAPI_ENDPOINT()+HRVAppInstance.getAppInstance().getPUBLIC_SIGNUP(),
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        CommonUtils.dismissProgress();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int response_code = jsonObject.getInt("response_code");
                            String response_message = jsonObject.getString("response_message");
                            if (response_code == 1) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                builder.setMessage("Signup Successfull, please login using your email and password.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                startActivity(new Intent(SignUpActivity.this,LoginActivity.class));
                                                finish();
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            } else {

                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                builder.setMessage(response_message)
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            CommonUtils.showLongToast(SignUpActivity.this,
                                    getResources().getString(R.string.response_parse_error));
                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CommonUtils.dismissProgress();
                CommonUtils.showLongToast(SignUpActivity.this, getResources().getString(R.string.server_error));
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
