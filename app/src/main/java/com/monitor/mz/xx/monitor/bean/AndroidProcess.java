package com.monitor.mz.xx.monitor.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.monitor.mz.xx.monitor.utils.models.Cgroup;
import com.monitor.mz.xx.monitor.utils.models.ProcFile;
import com.monitor.mz.xx.monitor.utils.models.Stat;

import java.io.IOException;

/**
 * Created by Administrator on 2017/7/12.
 */

public class AndroidProcess implements Parcelable {
    public final String name;
    public final int pid;

    public AndroidProcess(int pid) throws IOException {
        this.pid = pid;
        this.name = getProcessName(pid);
    }

    public AndroidProcess(Parcel in) {
        this.name = in.readString();
        this.pid = in.readInt();
    }

    public static final Creator<AndroidProcess> CREATOR = new Creator<AndroidProcess>() {
        @Override
        public AndroidProcess createFromParcel(Parcel in) {
            return new AndroidProcess(in);
        }

        @Override
        public AndroidProcess[] newArray(int size) {
            return new AndroidProcess[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.pid);
    }

    public static String getProcessName(int pid) throws IOException {
        String cmdline = null;
        try {
            cmdline = ProcFile.readFile(String.format("/proc/%d/cmdline", pid)).trim();
            System.out.println(cmdline);
        } catch (IOException ignored) {
        }
        if (TextUtils.isEmpty(cmdline)) {
            return Stat.get(pid).getComm();
        }
        return cmdline;
    }


    public String read(String filename) throws IOException {
        return ProcFile.readFile(String.format("/proc/%d/%s", pid, filename));
    }

    public String attr_current() throws IOException {
        return read("attr/current");
    }

    public String cmdline() throws IOException {
        return read("cmdline");
    }

    public Cgroup cgroup() throws IOException {
        return Cgroup.get(pid);
    }

    public int oom_adj() throws IOException {
        return Integer.parseInt(read("oom_adj"));
    }

    public int oom_score_adj() throws IOException {
        return Integer.parseInt(read("oom_score_adj"));
    }

    public Stat stat() throws IOException {
        return Stat.get(pid);
    }

    //    public Statm statm() throws IOException {
//        return Statm.get(pid);
//    }
//    public Status status() throws IOException {
//    return Status.get(pid);
//    }
    public String wchan() throws IOException {
        return read("wchan");
    }
}
