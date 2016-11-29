package com.iam360.dscvr.record;

import android.util.Log;

/**
 * @author Nilan Marktanner
 * @date 2016-02-10
 */
public class SelectionPoint {
    private float[] extrinsics;
    private int globalId;
    private int ringId;
    private int localId;

    public SelectionPoint(float[] extrinsics, int globalId, int ringId, int localId) {
        this.extrinsics = extrinsics;
        this.globalId = globalId;
        this.ringId = ringId;
        this.localId = localId;
    }

    public long getRingId() {
        return ringId;
    }

    public float[] getExtrinsics() {
        return extrinsics;
    }

    public void setExtrinsics(float[] extrinsics) {
        this.extrinsics = extrinsics;
    }

    public int getGlobalId() {
        return globalId;
    }

    public void setGlobalId(int globalId) {
        this.globalId = globalId;
    }

    public void setRingId(int ringId) {
        this.ringId = ringId;
    }

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }

    @Override
    public String toString() {
        String value = "Selection point : extrinsics=";

        for(int i=0; i<extrinsics.length; i++)
            value += extrinsics[i] + ":";

        value += " globalId=" + globalId + " " +
                "ringId=" + ringId + " " +
                "localId=" + localId + " ";


        Log.d("MARK","SelectionPoint value - "+value);

        return value;
    }
}
