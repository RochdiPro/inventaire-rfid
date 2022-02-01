package demo.rfid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by NG on 2016-12-15.
 */

public class SharedPreference {
    private static final String DOTR_PREFERENCES_NAME = "RFID_DEMO_PREF";

    static final String PREF_LAST_DEVICE_MAC = "LastDeviceMac";

    @SuppressLint("StaticFieldLeak")
    static Context mContext;

    SharedPreference(Context c) {
        mContext = c;
    }

    void put(String key, String value) {
        SharedPreferences pref = mContext.getSharedPreferences(DOTR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    void put(String key, boolean value) {
        SharedPreferences pref = mContext.getSharedPreferences(DOTR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    void put(String key, int value) {
        SharedPreferences pref = mContext.getSharedPreferences(DOTR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    String getValue(String key, String dftValue) {
        SharedPreferences pref = mContext.getSharedPreferences(DOTR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        try {
            return pref.getString(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }
    }

    int getValue(String key, int dftValue) {
        SharedPreferences pref = mContext.getSharedPreferences(DOTR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        try {
            return pref.getInt(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }
    }

    boolean getValue(String key, boolean dftValue) {
        SharedPreferences pref = mContext.getSharedPreferences(DOTR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        try {
            return pref.getBoolean(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }
    }
}
