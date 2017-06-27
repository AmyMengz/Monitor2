package com.monitor.mz.xx.monitor.Services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Debug;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;

import com.monitor.mz.xx.monitor.Constants;
import com.monitor.mz.xx.monitor.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/24.
 */

public class ReaderService extends Service {
    private List<Float> cpuTotal, cpuAM;
    private List<Integer> memoryAM;
    private List<String> memUsed, memAvailable, memFree, cached, threshold;
    private int maxSamples = 2000;
    private int pId;
    private ActivityManager am;
    private Debug.MemoryInfo[] amMI;
    private ActivityManager.MemoryInfo mi;
    private SharedPreferences mPrefs;
    private int intervalRead, intervalUpdate, intervalWidth;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cpuTotal = new ArrayList<Float>(maxSamples);
        cpuAM = new ArrayList<Float>(maxSamples);
        memoryAM = new ArrayList<Integer>(maxSamples);
        memUsed = new ArrayList<String>(maxSamples);
        memAvailable = new ArrayList<String>(maxSamples);
        memFree = new ArrayList<String>(maxSamples);
        cached = new ArrayList<String>(maxSamples);
        threshold = new ArrayList<String>(maxSamples);
        pId = Process.myPid();
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        amMI = am.getProcessMemoryInfo(new int[]{pId});
        mi = new ActivityManager.MemoryInfo();
        mPrefs = getSharedPreferences(getString(R.string.app_name) + Constants.prefs, MODE_PRIVATE);
        intervalRead = mPrefs.getInt(Constants.intervalRead,Constants.defaultIntervalRead);
        intervalUpdate = mPrefs.getInt(Constants.intervalUpdate, Constants.defaultIntervalUpdate);
        intervalWidth = mPrefs.getInt(Constants.intervalWidth, Constants.defaultIntervalWidth);

    }
}
