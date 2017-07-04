package com.monitor.mz.xx.monitor.Services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.monitor.mz.xx.monitor.Constants;
import com.monitor.mz.xx.monitor.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
            , memFree, cached, threshold;
    private int maxSamples = 2000;
    private int pId;
    private ActivityManager am;
    private Debug.MemoryInfo[] amMI;
    private ActivityManager.MemoryInfo mi;
    private SharedPreferences mPrefs;
    private int intervalRead//读取时间间隔
            , intervalUpdate, intervalWidth;//间隔
    private BufferedReader reader;//读取文件
    private BufferedWriter mWriter;
    private File mFile;
    private String s;//读取文件一行
    private String[] sa;
    private long work,//work cpu
            workAM,
            totalBefore,
            totalT, workT, workBefore, workAMBefore, workAMT;

    private long total;//cpu total
    private boolean firstRead = true, recording,topRow = true;
    private int memTotal;//总存储

    private List<Map<String, Object>> mListSelected;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ReaderServiceBinder();
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
        readThread.start();
//        registerReceiver(receiverStartRecord, new IntentFilter(C.actionStartRecord));
//        registerReceiver(receiverStopRecord, new IntentFilter(C.actionStopRecord));
//        registerReceiver(receiverClose, new IntentFilter(C.actionClose));

    }

    private volatile Thread readThread =
            new Thread(new Runnable() {
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
            }
                    , Constants.readThread);
            //new Thread(readRunnable,Constants.readThread);
//    private Runnable readRunnable = new Runnable() {
//        @Override
//        public void run() {
//            Thread thisThread = Thread.currentThread();
//            while (readThread == thisThread) {
//                read();
//                try {
//                    Thread.sleep(intervalRead);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    };

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
                if (firstRead && s.startsWith("MemTotal:")) {
                    memTotal = Integer.parseInt(s.split("[ ]+", 3)[1]);
                    firstRead = false;
                } else if (s.startsWith("MemFree:"))
                    memFree.add(0, s.split("[ ]+", 3)[1]);
                else if (s.startsWith("Cached:"))
                    cached.add(0, s.split("[ ]+", 3)[1]);

                s = reader.readLine();
            }
            reader.close();
            am.getMemoryInfo(mi);
            if (mi == null) {
                memUsed.add(0, String.valueOf(0));
                memAvailable.add(0, String.valueOf(0));
                threshold.add(0, String.valueOf(0));
            } else {
                // 空闲内存=free+buffers+cached=total-used
                memUsed.add(0, String.valueOf(memTotal - mi.availMem / 1024));//outInfo.availMem即为可用空闲内存
                //可用空闲内存
                memAvailable.add(0, String.valueOf(mi.availMem / 1024));
                //
                threshold.add(0, String.valueOf(mi.threshold / 1024));
            }
            memoryAM.add(amMI[0].getTotalPrivateDirty());
            reader = new BufferedReader(new FileReader("proc/stat"));
            sa = reader.readLine().split("[ ]+", 9);
            work = Long.parseLong(sa[1]) + Long.parseLong(sa[2]) + Long.parseLong(sa[3]);
            total = work + Long.parseLong(sa[4]) + Long.parseLong(sa[5]) +
                    Long.parseLong(sa[6]) + Long.parseLong(sa[7]);
            reader.close();
            reader = new BufferedReader(new FileReader("/proc/" + pId + "/stat"));
            sa = reader.readLine().split("[ ]+", 9);
            workAM = Long.parseLong(sa[13]) + Long.parseLong(sa[14]) +
                    Long.parseLong(sa[15]) + Long.parseLong(sa[16]);
            reader.close();
            if (mListSelected != null && !mListSelected.isEmpty()) {
                int[] arrayPIds = new int[mListSelected.size()];
                synchronized (mListSelected) {
                    int n = 0;
                    for (Map<String, Object> p : mListSelected) {
                        try {
                            if (p.get(Constants.pDead) == null) {
                                reader = new BufferedReader(new FileReader("/proc/" + p.get(Constants.pId) + "/stat"));
                                arrayPIds[n] = Integer.valueOf((String) p.get(Constants.pTPD));
                                ++n;
                                sa = reader.readLine().split("[ ]+", 18);
                                p.put(Constants.work, (float) Long.parseLong(sa[13]) + Long.parseLong(sa[14]) + Long.parseLong(sa[15])
                                        + Long.parseLong(sa[16]));
                                reader.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            p.put(Constants.pDead, Boolean.TRUE);
                            Intent intent = new Intent(Constants.actionDeadProcess);
                            intent.putExtra(Constants.process, (Serializable) p);
                            sendBroadcast(intent);
                        }
                    }
                }
                Debug.MemoryInfo[] mip = am.getProcessMemoryInfo(arrayPIds);
                int n = 0;
                for (Map<String, Object> entry : mListSelected) {
                    List<Integer> l = (List<Integer>) entry.get(Constants.pTPD);
                    if (l == null) {
                        l = new ArrayList<Integer>();
                        entry.put(Constants.pTPD, l);
                    }
                    if (entry.get(Constants.pDead) == null) {
//						if (mip[n].getTotalPrivateDirty() !=0
//								&& mip[n].getTotalPss() != 0
//								&& mip[n].getTotalSharedDirty() != 0) // To avoid dead processes
                        l.add(0, mip[n].getTotalPrivateDirty());
                        ++n;
                    } else l.add(0, 0);
                }
            }
            if (totalBefore != 0) {
                totalT = total - totalBefore;
                workT = work - workBefore;
                workAMT = workAM - workAMBefore;
                cpuTotal.add(0, restrictPercentage(workT * 100 / (float) totalT));
                cpuAM.add(0, restrictPercentage(workAMT * 100 / (float) totalT));
                if (mListSelected != null && !mListSelected.isEmpty()) {
                    int workPT = 0;
                    List<Float> l;

                    synchronized (mListSelected) {
                        for (Map<String, Object> p : mListSelected) {
                            if (p.get(Constants.workBefore) == null)
                                break;
                            l = (List<Float>) p.get(Constants.pFinalValue);
                            if (l == null) {
                                l = new ArrayList<Float>();
                                p.put(Constants.pFinalValue, l);
                            }
                            while (l.size() >= maxSamples)
                                l.remove(l.size() - 1);

                            workPT = (int) ((Float) p.get(Constants.work) - (Float) p.get(Constants.workBefore));
                            l.add(0, restrictPercentage(workPT * 100 / (float) totalT));
                        }
                    }
                }
            }
            totalBefore = total;
            workBefore = work;
            workAMBefore = workAM;

            if (mListSelected != null && !mListSelected.isEmpty())
                for (Map<String, Object> p : mListSelected)
                    p.put(Constants.workBefore, p.get(Constants.work));

            reader.close();

            if (recording)
                record();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void record() {
        if (mWriter == null) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Montitor");
            dir.mkdirs();
            mFile = new File(dir,new StringBuilder().append(getString(R.string.app_name))
            .append("Record-").append(getDate()).append(".csv").toString());
            try{

                mWriter = new BufferedWriter(new FileWriter(mFile));
            }catch (Exception e){
                e.printStackTrace();
                notifyError(e);
                return;
            }
            try {
                if(topRow){
                    StringBuilder sb = new StringBuilder()
                            .append(getString(R.string.app_name))
                            .append(" Record,Starting date and time:,")
                            .append(getDate())
                            .append(",Read interval (ms):,")
                            .append(intervalRead)
                            .append(",MemTotal (kB),")
                            .append(memTotal)
                            .append("\nTotal CPU usage (%),AnotherMonitor (Pid ")
                            .append(Process.myPid())
                            .append(") CPU usage (%),AnotherMonitor Memory (kB)");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void notifyError(final Exception e) {
        e.printStackTrace();
        if(mWriter!=null){
            stopRecord();
        }else {
            recording = false;
            sendBroadcast(new Intent(Constants.actionSetIconRecord));
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ReaderService.this, getString(R.string.notify_toast_error_2) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

//            mNM.notify(10, mNotificationRead);
        }
    }
    void startRecord() {
        recording = true;
        sendBroadcast(new Intent(Constants.actionSetIconRecord));
    }
    void stopRecord() {
        recording = false;
        sendBroadcast(new Intent(Constants.actionSetIconRecord));
    }

    private String getDate() {
        Calendar c = Calendar.getInstance();
        DecimalFormat df = new DecimalFormat("00");
        return new StringBuilder()
                .append(df.format(c.get(Calendar.YEAR))).append("-")
                .append(df.format(c.get(Calendar.MONTH) + 1)).append("-")
                .append(df.format(c.get(Calendar.DATE))).append("-")
                .append(df.format(c.get(Calendar.HOUR_OF_DAY))).append("-")
                .append(df.format(c.get(Calendar.MINUTE))).append("-")
                .append(df.format(c.get(Calendar.SECOND))).toString();
    }

    private float restrictPercentage(float percentage) {
        if (percentage > 100)
            return 100;
        else if (percentage < 0)
            return 0;
        else return percentage;
    }
    List<Float> getCPUTotalP() {
        return cpuTotal;
    }


    public class ReaderServiceBinder extends Binder{
        public ReaderService getService(){
            return ReaderService.this;
        }
    }
    public int getMemTotal(){
        return memTotal;
    }
}
