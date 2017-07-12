package com.monitor.mz.xx.monitor.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.monitor.mz.xx.monitor.bean.AndroidAppProcess;
import com.monitor.mz.xx.monitor.bean.AndroidProcess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2017/7/12.
 */

public class ProcessManager {
    private ProcessManager(){
        throw  new AssertionError("no instances");
    }

    /**
     *
     * @return
     */
    public static List<AndroidProcess> getRunningProcesses(){
        List<AndroidProcess> processes = new ArrayList<>();
        File[] files = new File("/proc").listFiles();
        for (File file:files){
            if(file.isDirectory()){
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                }catch (NumberFormatException e){
                    continue;
                }
                try{
                    processes.add(new AndroidProcess(pid));
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return processes;
    }

    /**
     *
     * @return
     */
    public static List<AndroidAppProcess> getRunningAppProcesses() {
        List<AndroidAppProcess> processes = new ArrayList<>();
        File[] files = new File("/proc").listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                try {
                    processes.add(new AndroidAppProcess(pid));
                } catch (AndroidAppProcess.NotAndroidAppProcessException ignored) {
                } catch (IOException e) {
                    // If you are running this from a third-party app, then system apps will not be
                    // readable on Android 5.0+ if SELinux is enforcing. You will need root access or an
                    // elevated SELinux context to read all files under /proc.
                    // See: https://su.chainfire.eu/#selinux
                }
            }
        }
        return processes;
    }
    public static List<AndroidAppProcess> getRunningForegroundApps(Context ctx){
        List<AndroidAppProcess> processes = new ArrayList<>();
        File[] files = new File("/proc").listFiles();
        PackageManager pm = ctx.getPackageManager();
        for (File file:files){
            if(file.isDirectory()){
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                try{
                    AndroidAppProcess process = new AndroidAppProcess(pid);
                    if(!process.foreground){
                        continue;
                    }else if(process.uid >=1000&&process.uid<=999){//android中uid从1000到9999都是给系统程序保留的
                        continue;
                    }else if(process.name.contains(":")){
                        continue;
                    }else if (pm.getLaunchIntentForPackage(process.getPackageName())==null){
                        continue;
                    }
                    processes.add(process);
                }catch (AndroidAppProcess.NotAndroidAppProcessException ignored) {
                } catch (IOException e) {

                }
            }
        }
        return processes;
    }

    /**
     *
     * @return {@code true}if this process is in the foreground.
     */
    public static boolean isMyProcessInTheForeground(){
        List<AndroidAppProcess> processes = getRunningAppProcesses();
        int myPid = android.os.Process.myPid();
        int myUid = android.os.Process.myUid();
        for(AndroidAppProcess process:processes){
            if(process.pid == myPid&&process.foreground){
                return true;
            }
        }
        return false;
    }
    public static List<ActivityManager.RunningAppProcessInfo> getRunningAppProcessInfo(Context ctx){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP_MR1){
            List<AndroidAppProcess> runningAppProcesses = getRunningAppProcesses();
            List<ActivityManager.RunningAppProcessInfo> appProcessInfos = new ArrayList<>();
            for (AndroidAppProcess process:runningAppProcesses){
                ActivityManager.RunningAppProcessInfo info = new ActivityManager.RunningAppProcessInfo(
                        process.name,process.pid,null
                );
                info.uid = process.uid;
                appProcessInfos.add(info);
            }
            return appProcessInfos;
        }
        ActivityManager am = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getRunningAppProcesses();
    }
    public static class Filter{
        private String name;
        private int pid = -1;
        private int ppid = -1;
        private boolean apps;
        public Filter setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * @param pid
         *     The process id to filter
         * @return This Filter object to allow for chaining of calls to set methods
         */
        @Deprecated
        public Filter setPid(int pid) {
            this.pid = pid;
            return this;
        }

        /**
         * @param ppid
         *     The parent process id to filter
         * @return This Filter object to allow for chaining of calls to set methods
         */
        @Deprecated
        public Filter setPpid(int ppid) {
            this.ppid = ppid;
            return this;
        }

        /**
         * @param apps
         *     {@code true} to only filter app processes
         * @return This Filter object to allow for chaining of calls to set methods
         */
        @Deprecated
        public Filter setApps(boolean apps) {
            this.apps = apps;
            return this;
        }

        @Deprecated
        public List<AndroidProcess> run() {
            List<AndroidProcess> processes = new ArrayList<>();
            File[] files = new File("/proc").listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    int pid;
                    try {
                        pid = Integer.parseInt(file.getName());
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    if (this.pid != -1 && pid != this.pid) {
                        continue;
                    }
                    try {
                        AndroidProcess process;
                        if (this.apps) {
                            process = new AndroidAppProcess(pid);
                        } else {
                            process = new AndroidProcess(pid);
                        }
                        if (this.name != null && !process.name.contains(this.name)) {
                            continue;
                        }
//                        if (this.ppid != -1 && process.stat().ppid() != this.ppid) {
//                            continue;
//                        }
                        processes.add(process);
                    } catch (IOException e) {
                        // If you are running this from a third-party app, then system apps will not be
                        // readable on Android 5.0+ if SELinux is enforcing. You will need root access or an
                        // elevated SELinux context to read all files under /proc.
                        // See: https://su.chainfire.eu/#selinux
                    } catch (AndroidAppProcess.NotAndroidAppProcessException ignored) {
                    }
                }
            }
            return processes;
        }

    }
    /**
     * Comparator to list processes by name
     */
    public static final class ProcessComparator implements Comparator<AndroidProcess> {

        @Override public int compare(AndroidProcess p1, AndroidProcess p2) {
            return p1.name.compareToIgnoreCase(p2.name);
        }

    }

}
