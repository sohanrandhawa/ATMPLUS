package com.hrv.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.hrv.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



/**
 * THIS CLASS CONTAINS ALL the common methods/values used throughout the
 * application
 */
public class CommonUtils {


    private static ProgressDialog progress_Dialog = null;

    /**
     * A dialog showing a progress indicator and an optional text message or
     * view. Only a text message or a view can be used at the same time.
     *
     * @param context - context of the activity on which Progress dialog need to be
     *                shown
     * @param message - message to be shown in progress Dialog
     */
    public static void showProgress(Context context, String message) {

        // check if progress dialog is already visible. If visible, then remove it.
        dismissProgress();

        progress_Dialog = new ProgressDialog(context);
        progress_Dialog.setCancelable(false);
        progress_Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress_Dialog.setMessage(message);
        progress_Dialog.show();
    }

    /**
     * Dismiss this dialog, removing it from the screen. This method can be
     * invoked safely from any thread. Note that you should not override this
     * method to do cleanup when the dialog is dismissed
     */
    public static void dismissProgress() {

        if (progress_Dialog != null) {
            try {
                if (progress_Dialog.isShowing()) {
                    progress_Dialog.dismiss();
                    progress_Dialog = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method to close the soft keypad
     *
     * @param activity current activity
     */
    public static void closeKeyBoard(FragmentActivity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
    }

    /**
     * Method used to show Small Toast.
     *
     * @param context context of the current activity
     * @param message message to be shown on Toast
     */
    public static void showSmallToast(Context context, String message) {
        Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Method used to show Long Toast.
     *
     * @param context context of the current activity
     * @param message message to be shown on Toast
     */
    public static void showLongToast(Context context, String message) {
        Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }





    /**
     * Method for displaying Logs. Set "isDebuggerOn" to false if you do not
     * want to display Logs else set it to true
     *
     * @param tag     Tag of the Log
     * @param message message to be displayed in Log
     * @author ngoyal
     */
    public static void myLog(String tag, String message) {
        boolean isDebuggerOn = true;
        if (isDebuggerOn) {
            try {
                Log.d(tag, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Method to return empty string in case the result from server is null or empty
     *
     * @param string the string to be examined
     * @return empty string if the provided value is empty or null, else the value itself
     */
    public static String checkData(String string) {
        if (string == null || TextUtils.isEmpty(string.toString().trim())) {
            return "";
        }

        return string.trim();
    }


    /**
     * This method check the network availability in the device weather its from
     * mobile data or Wi-Fi or any other medium
     *
     * @param context context of the current Activity
     * @return TRUE, if Internet connection is available, FALSE otherwise.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }


    /**
     * This method is used to enable vertical scrolling for multiple line edit text.
     * If inside a scroll view then this method will disable the parent scroll and will allow the user to scroll the data inside the edit text.
     *
     * @param view Name of the view for which scrolling is to be enabled
     * @param id   ID of the view
     */
    public static void enableVerticalScroll(View view, final int id) {

        view.setVerticalScrollBarEnabled(true);
        view.setHorizontalScrollBarEnabled(true);
        view.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (v.getId() == id) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Method to check email is valid or not
     *
     * @param email
     * @return
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * password validation
     * Password should be at least eight characters long containing at least one uppercase letter, one lowercase letter one number and one symbol.
     *
     * @param password password to validate
     * @return TRUE if password is valid otherwise FALSE
     */
    public final static boolean isValidPassword(String password) {
//        String pattern = "^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,20})$";

        String pattern = "^((?=.*\\d)(?=.*[a-z]).{8,20})$";

        // ^[a-zA-Z0-9\\S]$
        //^(?=.*[a-zA-Z0-9])(?=\\S+$).{5,}$
        // ^(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{5,}$
        // ^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{5,}$
        /**
         * ^ # start-of-string
         * (?=.*[0-9]) # a digit must occur at least once
         * (?=.*[a-z]) # a lower case letter must occur at least once
         * (?=.*[A-Z]) # an upper case letter must occur at least once
         * (?=.*[@#$%^&+=]) # a special character must occur at least once
         * (?=\\S+$) # no whitespace allowed in the entire string
         * .{8,20} # length at least 8 characters and maximum of 20
         * $ # end-of-string
         */

        return password.matches(pattern);
    }


    /**
     * Get Date from Date-Time
     *
     * @param dateString date/time string from server
     * @return date in required date format (dd MMM yyyy)
     */
    public static String getDate(String dateString) {

        myLog("convertDateFormat", "Source DateTime: " + dateString);

        if (dateString == null || dateString.equalsIgnoreCase(""))
            return dateString;

        SimpleDateFormat srcDateFormat;

        srcDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 2016-11-28 16:22:48

        String requiredDate = "";
        SimpleDateFormat reqDateTimeFormat = new SimpleDateFormat("dd MMM yyyy");  // output: 18 July 2016

        try {
            Date varDate = srcDateFormat.parse(dateString);
            requiredDate = reqDateTimeFormat.format(varDate);

        } catch (Exception e) {
            e.printStackTrace();
            requiredDate = "";
        }

        myLog("convertDateFormat", "requiredDate DateTime: " + requiredDate);
        return requiredDate;

    }

    /**
     * Get Time from Date-Time
     *
     * @param dateString date/time string from server
     * @return Time in required date format (dd MMM yyyy)
     */
    public static String getTime(String dateString) {

        myLog("convertDateFormat", "Source DateTime: " + dateString);

        if (dateString == null || dateString.equalsIgnoreCase(""))
            return dateString;

        SimpleDateFormat srcDateFormat;

        srcDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 2016-11-28 16:22:48

        String requiredDate = "";
        SimpleDateFormat reqDateTimeFormat = new SimpleDateFormat("hh:mm a");  // output: 4:22 pm

        try {
            Date varDate = srcDateFormat.parse(dateString);
            requiredDate = reqDateTimeFormat.format(varDate);

        } catch (Exception e) {
            e.printStackTrace();
            requiredDate = "";
        }

        myLog("convertDateFormat", "requiredDate DateTime: " + requiredDate);
        return requiredDate;

    }


    /**
     * Method for converting date/time to required format.
     *
     * @param dateString date/time string from server
     * @return date in required date format (dd-MMM-yyyy hh:mm a)
     */
    public static String convertDateTimeFormat(String dateString) {

        myLog("convertDateTimeFormat", "Source DateTime: " + dateString);

        if (dateString == null || dateString.equalsIgnoreCase("") || dateString.contains(","))
            return dateString;

        SimpleDateFormat srcDateFormat;

            srcDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");// 2016-08-01 10:46:01

        String requiredDate = "";
        SimpleDateFormat reqDateTimeFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");  // output: July 18, 2016 09:50 PM

        try {
            Date varDate = srcDateFormat.parse(dateString);
            requiredDate = reqDateTimeFormat.format(varDate);

        } catch (Exception e) {
            e.printStackTrace();
           requiredDate=dateString;
        }

        myLog("convertDateTimeFormat", "requiredDate DateTime: " + requiredDate);
        return requiredDate;

    }

    /**
     * Method for converting string value to MD5 hash
     *
     * @param value normal string value
     * @return MD5 hash code
     */
    public static String convertToMD5(String value) {
        try {

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(value.getBytes());
            byte messageDigest[] = digest.digest();

            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Method rounding the double value upto requred number of places
     *
     * @param value
     * @param places
     * @return
     */
    public static double round(double value, int places) {

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Get Date of past 7days from current Date
     *
     * @return
     */
    public static String getLastWeekDate() {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, -7);
        return df.format(cal.getTime()).toString();

    }

    /**
     * Get current Date
     *
     * @return
     */
    public static String getCurrentDate() {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();

        return df.format(cal.getTime()).toString();

    }
}
