package com.iam360.iam360.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Mariel on 4/25/2016.
 */
public class FaceStatus implements Parcelable {
    private boolean[] defaultStatus = {false,false,false,false,false,false};
    private List<Boolean> defStat = new ArrayList<>(Arrays.asList(false, false, false, false, false, false));
    private boolean[] status = {false,false,false,false,false,false};;

    public FaceStatus() {
        this.status = defaultStatus;
    }

    private FaceStatus(Parcel source) {
        source.readBooleanArray(this.status);
    }

    public boolean[] getStatus() {
        return status;
    }

    public void setStatus(boolean[] status) {
        this.status = status;
    }

    public void setStatusByIndex(int index,boolean bool) {
        this.status[index] = bool;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // SAME ORDER AS IN FaceStatus(Parcel source)!
        dest.writeBooleanArray(this.status);
    }

    public static final Parcelable.Creator<FaceStatus> CREATOR = new Parcelable.Creator<FaceStatus>(){
        @Override
        public FaceStatus createFromParcel(Parcel source) {
            return new FaceStatus(source);
        }

        @Override
        public FaceStatus[] newArray(int size) {
            return new FaceStatus[size];
        }
    };
}
