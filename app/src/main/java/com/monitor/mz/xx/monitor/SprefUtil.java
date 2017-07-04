package com.monitor.mz.xx.monitor;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Administrator on 2017/6/29.
 */

public class SprefUtil {
    private SharedPreferences mPrefs;
    Context mCotext;
    SharedPreferences.Editor editor;
    public SprefUtil(Context context){
        mCotext = context;
        mPrefs = context.getSharedPreferences(context.getString(R.string.app_name)+Constants.prefs,MODE_PRIVATE);
        editor = mPrefs.edit();
    }
    public int getInt(String key,int defaultValue){
        return mPrefs.getInt(key,defaultValue);
    }
    public void putInt(String key,int value){
        editor.putInt(key,value).commit();
    }
    public boolean getBoolean(String key,boolean defaultValue){
        return mPrefs.getBoolean(key,defaultValue);
    }
    public void putBoolean(String key,boolean value){
        editor.putBoolean(key,value).commit();
    }




}
