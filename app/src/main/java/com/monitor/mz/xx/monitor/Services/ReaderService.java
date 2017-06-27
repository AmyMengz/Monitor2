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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/24.
 */

public class ReaderService extends Service {
    private List<Float> cpuTotal, cpuAM;
    private List<Integer> memoryAM;
    private List<String> memUsed//已用内存
            , memAvailable//可用内存
            , memFree
            , cached
            , threshold;
    private int maxSamples = 2000;
    private int pId;
    private ActivityManager am;
    private Debug.MemoryInfo[] amMI;
    private ActivityManager.MemoryInfo mi;
    private SharedPreferences mPrefs;
    private int intervalRead//读取时间间隔
            , intervalUpdate, intervalWidth;//间隔
    private BufferedReader reader;//读取文件
    private String s;//读取文件一行
    private boolean firstRead = true;
    private int memTotal;//总存储

    private List<Map<String, Object>> mListSelected;

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
        intervalRead = mPrefs.getInt(Constants.intervalRead, Constants.defaultIntervalRead);
        intervalUpdate = mPrefs.getInt(Constants.intervalUpdate, Constants.defaultIntervalUpdate);
        intervalWidth = mPrefs.getInt(Constants.intervalWidth, Constants.defaultIntervalWidth);

    }

    private volatile Thread readThread = new Thread();
    private Runnable readRunnable = new Runnable() {
        @Override
        public void run() {
            Thread thisThread = Thread.currentThread();
            while (readThread == thisThread) {
                read();
                try {
                    Thread.sleep(intervalRead);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void read() {
        try {
            reader = new BufferedReader(new FileReader("/proc/meminfo"));
            s = reader.readLine();
            while (s != null) {
                while (memFree.size() >= maxSamples) {
                    cpuTotal.remove(cpuTotal.size() - 1);
                    cpuAM.remove(cpuAM.size() - 1);
                    memoryAM.remove(memoryAM.size() - 1);
                    memUsed.remove(memUsed.size() - 1);
                    memAvailable.remove(memAvailable.size() - 1);
                    memFree.remove(memFree.size() - 1);
                    cached.remove(cached.size() - 1);
                    threshold.remove(threshold.size() - 1);
                }
                if (mListSelected != null && !mListSelected.isEmpty()) {
                    List<Intent> l = (List<Intent>) ((Map<String, Object>) mListSelected.get(0))
                            .get(Constants.pFinalValue);
                    if (l != null && l.size() >= maxSamples) {
                        for (Map<String, Object> m : mListSelected) {
                            ((List<Intent>) m.get(Constants.pFinalValue)).remove(l.size() - 1);
                            ((List<Integer>) m.get(Constants.pTPD)).remove(((List<Integer>) m.get(Constants.pTPD)).size() - 1);
                        }
                    }
                }
                if (mListSelected != null && !mListSelected.isEmpty()) {
                    for (Map<String, Object> m : mListSelected) {
                        List<Integer> l = (List<Integer>) m.get(Constants.pFinalValue);
                        if (l == null)
                            break;
                        while (l.size() >= maxSamples) {
                            l.remove(l.size() - 1);
                            l = (List<Integer>) m.get(Constants.pTPD);
                            while (l.size() >= maxSamples)
                                l.remove(l.size() - 1);
                        }
                    }
                }
                if (firstRead&&s.startsWith("MemTotal:")){
                    memTotal = Integer.parseInt(s.split("[ ]+",3)[1]);
                    firstRead = false;
                }else if (s.startsWith("MemFree:"))
                    memFree.add(0, s.split("[ ]+", 3)[1]);
                else if (s.startsWith("Cached:"))
                    cached.add(0, s.split("[ ]+", 3)[1]);

                s = reader.readLine();
            }
            reader.close();
            am.getMemoryInfo(mi);
            if(mi == null){
                memUsed.add(0,String.valueOf(0));
                memAvailable.add(0, String.valueOf(0));
                threshold.add(0, String.valueOf(0));
            } else {
                // 空闲内存=free+buffers+cached=total-used
                memUsed.add(0, String.valueOf(memTotal - mi.availMem/1024));//outInfo.availMem即为可用空闲内存
                //可用空闲内存
                memAvailable.add(0, String.valueOf(mi.availMem/1024));
                //
                threshold.add(0, String.valueOf(mi.threshold/1024));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
