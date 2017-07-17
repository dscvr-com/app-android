package com.iam360.dscvr.record;

/**
 * Created by Emi on 17/07/2017.
 */

public class RecorderParamInfo {
    double graphHOverlap;
    double graphVOverlap;
    double stereoHBuffer;
    double stereoVBuffer;
    double tolerance;
    boolean halfGraph;

    public RecorderParamInfo(double graphHOverlap, double graphVOverlap, double stereoHBuffer, double stereoVBuffer, double tolerance, boolean halfGraph) {
        this.graphHOverlap = graphHOverlap;
        this.graphVOverlap = graphVOverlap;
        this.stereoHBuffer = stereoHBuffer;
        this.stereoVBuffer = stereoVBuffer;
        this.tolerance = tolerance;
        this.halfGraph = halfGraph;
    }

    public RecorderParamInfo() {
        this(0.9, 0.25, 0.5, -0.05, 2.0, true); // Default params for Samsung Note 4.
    }

    public double getGraphHOverlap() {
        return graphHOverlap;
    }

    public void setGraphHOverlap(double graphHOverlap) {
        this.graphHOverlap = graphHOverlap;
    }

    public double getGraphVOverlap() {
        return graphVOverlap;
    }

    public void setGraphVOverlap(double graphVOverlap) {
        this.graphVOverlap = graphVOverlap;
    }

    public double getStereoHBuffer() {
        return stereoHBuffer;
    }

    public void setStereoHBuffer(double stereoHBuffer) {
        this.stereoHBuffer = stereoHBuffer;
    }

    public double getStereoVBuffer() {
        return stereoVBuffer;
    }

    public void setStereoVBuffer(double stereoVBuffer) {
        this.stereoVBuffer = stereoVBuffer;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public boolean getHalfGraph() {
        return halfGraph;
    }

    public void setHalfGraph(boolean halfGraph) {
        this.halfGraph = halfGraph;
    }

    @Override
    public String toString() {
        return "RecorderParamInfo{" +
                "graphHOverlap=" + getGraphHOverlap() +
                ", graphVOverlap=" + getGraphVOverlap() +
                ", stereoHBuffer=" + getStereoHBuffer() +
                ", stereoVBuffer=" + getStereoVBuffer() +
                ", tolerance=" + getTolerance() +
                ", halfGraph=" + getHalfGraph() +
                '}';
    }
}
