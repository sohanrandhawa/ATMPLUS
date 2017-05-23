package com.hrv.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class is used to store data locally
 */
public class PreferenceHandler {


    public static final String REMEMBER_ME = "remember_me";
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    public static final String EMAIL_ID = "email_id";
    public static final String PASSWORD = "password";
    public static final String LOGGED_IN = "logged_in";

    public static final String HEIGHT = "height";
    public static final String WEIGHT = "weight";
    public static final String GENDER = "gender";
    public static final String BMI = "bmi";



    private static final String PREF_NAME = "HRV_APP_PREFS";
    private static final int MODE = Context.MODE_PRIVATE;
    private static final Method sApplyMethod = findApplyMethod();

    // boolean
    public static void writeBoolean(Context context, String key, boolean value) {
        getEditor(context).putBoolean(key, value).commit();
    }

    public static boolean readBoolean(Context context, String key, boolean defValue) {
        return getPreferences(context).getBoolean(key, defValue);
    }

    // integer
    public static void writeInteger(Context context, String key, int value) {
        getEditor(context).putInt(key, value).commit();
    }

    public static int readInteger(Context context, String key, int defValue) {
        return getPreferences(context).getInt(key, defValue);
    }

    // String
    public static void writeString(Context context, String key, String value) {
        getEditor(context).putString(key, value).commit();
    }

    public static String readString(Context context, String key, String defValue) {
        return getPreferences(context).getString(key, defValue);
    }

    // float
    public static void writeFloat(Context context, String key, float value) {
        getEditor(context).putFloat(key, value).commit();
    }

    public static float readFloat(Context context, String key, float defValue) {
        return getPreferences(context).getFloat(key, defValue);
    }

    // long
    public static void writeLong(Context context, String key, long value) {
        getEditor(context).putLong(key, value).commit();
    }

    public static long readLong(Context context, String key, long defValue) {
        return getPreferences(context).getLong(key, defValue);
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, MODE);
    }

    private static Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

    public static void SavePreference(SharedPreferences sharedPreferences, String key, String value) {
        CommonUtils.myLog("SavePreference", "key" + key + " , value " + value);
        Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String LoadPreferences(SharedPreferences sharedPreferences, String param, String defvalue) {

        String firstcheck = sharedPreferences.getString(param, defvalue);
        CommonUtils.myLog("LoadPreferences " + param + ": shared preferecce Load userid==", firstcheck);

        return firstcheck;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Method findApplyMethod() {
        try {
            Class cls = Editor.class;
            return cls.getMethod("apply");
        } catch (NoSuchMethodException unused) {
            // fall through
        }
        return null;
    }

    public static void apply(Editor editor) {
        if (sApplyMethod != null) {
            try {
                sApplyMethod.invoke(editor);
                return;
            } catch (InvocationTargetException unused) {
                // fall through
            } catch (IllegalAccessException unused) {
                // fall through
            }
        }
        editor.commit();
    }

}
