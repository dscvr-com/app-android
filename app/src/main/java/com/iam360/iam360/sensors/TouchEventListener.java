package com.iam360.iam360.sensors;

import android.graphics.Point;
import android.util.Log;

import com.iam360.iam360.util.Maths;

import java.util.Arrays;

/**
 * @author Nilan Marktanner
 * @date 2015-12-26
 */
public class TouchEventListener extends RotationMatrixProvider {
    private boolean isTouching;

    private float theta;
    private float phi;


    // note that at the moment we work solely with vfov here
    private float vfov;
    private float hfov;

    // damping
    private float phiDiff;
    private float thetaDiff;
    private float phiDamp;
    private float thetaDamp;
    private float dampFactor;

    private Point touchStartPoint;
    private int sceneWidth;
    private int sceneHeight;

    // depending on optograph format. suitable for Stitcher version <= 7
    private static final float BORDER = (float) Math.PI / 6.45f;
    private float minTheta;
    private float maxTheta;


    public TouchEventListener(float dampFactor, int sceneWidth, int sceneHeight, float vfov) {
        isTouching = false;
        phi = 0.0f;
        theta = 0.0f;
        phiDiff = 0.0f;
        thetaDiff = 0.0f;
        phiDamp = 0.0f;
        thetaDamp = 0.0f;
        this.dampFactor = dampFactor;
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;

        // note that we're getting vfov as input here and not hfov like in iOS - hfov is not in use at the moment
        this.vfov = vfov;
        this.hfov = vfov * sceneWidth / (float) sceneHeight;

        // constraint symmetrical around initial theta
        maxTheta = (float) Maths.PI_2 - BORDER - (vfov * (float) Math.PI / 180.0f) / 2.0f;
        minTheta = -maxTheta;
    }

    public void touchStart(Point point) {
        isTouching = true;
        touchStartPoint = point;
    }

    public void touchMove(Point point) {
        float x0 = sceneWidth / 2.0f;
        float y0 = sceneHeight / 2.0f;
        float flen = (float) (y0 / Math.tan(vfov / 2.0f * Math.PI / 180.0f));

        float startPhi = (float) Math.atan((touchStartPoint.x - x0) / flen);
        float startTheta = (float) Math.atan((touchStartPoint.y - y0) / flen);
        float endPhi = (float) Math.atan((point.x - x0) / flen);
        float endTheta = (float) Math.atan((point.y - y0) / flen);

        phiDiff += startPhi - endPhi;
        thetaDiff += startTheta - endTheta;

        touchStartPoint = point;
    }

    public void touchEnd(Point point) {
        isTouching = false;
        touchStartPoint = null;
    }

    public void reset() {
        phiDiff = 0;
        thetaDiff = 0;
        phi = 0;
        theta = 0;
        phiDamp = 0;
        thetaDamp = 0;
    }

    @Override
    public float[] getRotationMatrix() {
        Log.d("MARK","getRotationMatrix TouchEvent");
        if (!isTouching) {
            // Update from motion and damping
            phiDamp *= dampFactor;
            thetaDamp *= dampFactor;

            phi += phiDamp;
            theta += thetaDamp;
        } else {
            // Update from touch
            phiDamp = phiDiff;
            thetaDamp = thetaDiff;

            phi += phiDiff;
            theta += thetaDiff;

            phiDiff = 0;
            thetaDiff = 0;
        }

        // clamp theta for border effect
        theta = Math.max(minTheta, Math.min(theta, maxTheta));
        Log.d("MARK","theta = "+theta);
        Log.d("MARK","phi = "+phi);

        float[] rotationX = {(float) Math.toDegrees(theta), 1, 0, 0};
        float[] rotationY = {(float) -Math.toDegrees(phi), 0, 1, 0};
        Log.d("MARK","rotationX = "+ Arrays.toString(rotationX));
        Log.d("MARK","rotationY = "+ Arrays.toString(rotationY));
        Log.d("MARK","Maths.buildRotationMatrix(rotationY, rotationX) = "+ Maths.buildRotationMatrix(rotationY, rotationX));

        return Maths.buildRotationMatrix(rotationY, rotationX);
    }

    @Override
    public void getRotationMatrix(float[] target) {
        System.arraycopy(getRotationMatrix(), 0, target, 0, 16);
    }

    public boolean isTouching() {
        return isTouching;
    }
    public float getPhi() {
        return phi;
    }

    public void setPhi(float phi) {
        this.phi = phi;
    }

    public float getTheta() {
        return theta;
    }

    public void setTheta(float theta) {
        this.theta = theta;
    }
}