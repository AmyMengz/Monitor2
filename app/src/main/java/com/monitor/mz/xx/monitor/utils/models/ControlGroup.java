package com.monitor.mz.xx.monitor.utils.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/7/12.
 */

public class ControlGroup implements Parcelable {
    public final int id;
    public final String subsystems;
    public final String group;

    public ControlGroup(String line) throws NumberFormatException,IndexOutOfBoundsException{
        String[] filelds = line.split(":");
        id = Integer.parseInt(filelds[0]);
        subsystems = filelds[1];
        group = filelds[2];
    }
    protected ControlGroup(Parcel in) {
        this.id = in.readInt();
        this.subsystems = in.readString();
        this.group = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.subsystems);
        dest.writeString(this.group);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ControlGroup> CREATOR = new Creator<ControlGroup>() {
        @Override
        public ControlGroup createFromParcel(Parcel in) {
            return new ControlGroup(in);
        }

        @Override
        public ControlGroup[] newArray(int size) {
            return new ControlGroup[size];
        }
    };
}
