package com.monitor.mz.xx.monitor.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Administrator on 2017/7/12.
 */

public class ProcFile extends File implements Parcelable {

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getAbsolutePath());
        dest.writeString(this.content);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProcFile> CREATOR = new Creator<ProcFile>() {
        @Override
        public ProcFile createFromParcel(Parcel in) {
            return new ProcFile(in);
        }

        @Override
        public ProcFile[] newArray(int size) {
            return new ProcFile[size];
        }
    };

    /**
     * readFile
     * @param path
     * @return
     * @throws IOException
     */
    public  static String readFile(String path) throws IOException{
        BufferedReader reader = null;
        try{
            StringBuilder output = new StringBuilder();
            reader = new BufferedReader(new FileReader(path));
            String line1 = null;
            String newLine = "\n";
            while((line1 = reader.readLine())!=null){
//            for (String line = reader.readLine(),newLine = "";line !=null;line = reader.readLine()){
                output.append(newLine).append(line1);
            }
            return output.toString();
        }finally {
            if(reader !=null){
                reader.close();
            }
        }
    }

//    public static String readFile(String path) throws IOException {
//        BufferedReader reader = null;
//        try {
//            StringBuilder output = new StringBuilder();
//            reader = new BufferedReader(new FileReader(path));
//            for (String line = reader.readLine(), newLine = ""; line != null; line = reader.readLine()) {
//                output.append(newLine).append(line);
//                newLine = "\n";
//            }
//            return output.toString();
//        } finally {
//            if (reader != null) {
//                reader.close();
//            }
//        }
//    }

    public  final String content;
    public ProcFile(String path) throws IOException{
        super(path);
        content = readFile(path);
    }

    public ProcFile(Parcel in){
        super(in.readString());
        this.content = in.readString();
    }

    @Override
    public long length() {
        return content.length();
    }

}
