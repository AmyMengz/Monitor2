package com.monitor.mz.xx.monitor.utils;

import android.os.Parcel;

import java.io.IOException;

/**
 * Created by Administrator on 2017/7/12.
 */

public class Stat extends ProcFile{

    public static Stat get(int pid) throws IOException{
        return new Stat(String.format("/proc/%d/stat",pid));
    }
    private final String[] fields;
    private Stat(String path) throws IOException {
        super(path);
        fields = content.split("\\s+");
    }
    private Stat(Parcel in){
        super(in);
        this.fields = in.createStringArray();
    }
    public int getPid(){
        return Integer.parseInt(fields[0]);
    }
    public String getComm(){
        return fields[1].replace("(","").replace(")","");
    }
    public static final Creator<Stat> CREATOR = new Creator<Stat>() {
        @Override
        public Stat createFromParcel(Parcel source) {
            return new Stat(source);
        }

        @Override
        public Stat[] newArray(int size) {
            return new Stat[size];
        }
    };
}
