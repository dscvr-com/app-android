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
    private float hPos;
    private float vPos;

    public SelectionPoint(float[] extrinsics, int globalId, int ringId, int localId, float hPos, float vPos) {
        this.extrinsics = extrinsics;
        this.globalId = globalId;
        this.ringId = ringId;
        this.localId = localId;
        this.hPos = hPos;
        this.vPos = vPos;
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

    public float getHPos() {
        return hPos;
    }

    public float getVPos() {
        return vPos;
    }

    @Override
    public String toString() {
        String value = "Selection point : extrinsics=";

        for(int i=0; i<extrinsics.length; i++)
            value += extrinsics[i] + ":";

        value += " globalId=" + globalId + " " +
                "ringId=" + ringId + " " +
                "localId=" + localId + " " +
                "vPos=" + vPos + " " +
                "hPos=" + hPos + " ";

        return value;
    }
}
