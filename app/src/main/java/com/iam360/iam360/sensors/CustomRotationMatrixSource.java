package com.iam360.iam360.sensors;

import android.opengl.Matrix;
import android.util.Log;

import com.iam360.iam360.util.Maths;

import java.util.Arrays;

/**
 * Created by Joven on 8/11/2016.
 */
public class CustomRotationMatrixSource extends RotationMatrixProvider {
    private float theta;
    private float phi;

    public CustomRotationMatrixSource(float t, float p){
        theta = t;
        phi = p;
    }

    @Override
    public void getRotationMatrix(float[] target) {
        System.arraycopy(getRotationMatrix(), 0, target, 0, 16);
    }

    @Override
    public float[] getRotationMatrix(){
        // clamp theta for border effect
        Log.d("MARK","theta = "+theta);
        Log.d("MARK","phi = "+phi);

        float[] baseCorrection = Maths.buildRotationMatrix(new float[]{0, 1, 0, 0});

        float[] rotationX = {(float) Math.toDegrees(theta), 1, 0, 0};
        float[] rotationY = {(float) -Math.toDegrees(phi), 0, 1, 0};

        Log.d("MARK","rotationX = "+ Arrays.toString(rotationX));
        Log.d("MARK","rotationY = "+ Arrays.toString(rotationY));
        Log.d("MARK","Maths.buildRotationMatrix(rotationY, rotationX) = "+ Maths.buildRotationMatrix(rotationY, rotationX));

        float[] rotationMatrix = new float[16];
        float[] coreMotionMatrix = Maths.buildRotationMatrix(rotationY, rotationX);
        Matrix.multiplyMM(rotationMatrix, 0, baseCorrection, 0, coreMotionMatrix, 0);

        return rotationMatrix;
    }
}