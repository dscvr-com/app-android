package com.iam360.dscvr.views.record;

import android.app.Activity;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.util.List;

import iam360.com.orbit360media.InMemoryImageProvider;
import iam360.com.orbit360media.RecorderPreviewViewBase;
import iam360.com.orbit360media.SurfaceProvider;
import iam360.com.record.RecorderPreviewListener;

/**
 * Created by Emi on 15/05/2017.
 */

public class RecorderPreviewView extends RecorderPreviewViewBase {

    private static final String TAG = "RecorderPreviewView";
    private static final Size OPTIMAL_PREVIEW_SIZE = new Size(720, 1280);
    private InMemoryImageProvider inMemoryRecorder;
    private RecorderPreviewListener
            dataListener;

    private static int DETECTOR_IMAGE_SIZE = 240;

    boolean exposureLocked;

    public RecorderPreviewView(Activity context) {
        super(context);
        inMemoryRecorder = new InMemoryImageProvider();
        exposureLocked = false;
    }

    @Override
    protected Size calculatePreviewSize(StreamConfigurationMap map, Size[] supportedPreviewSizes, Size viewSize) {
        // Sizing is actually simple - the stream has the max resolution/aspect, and we choose by setting the size of our surface.
        // We take the smallest size that fits our aspect and is larger than our preview
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            return chooseOptimalPreviewSize(map.getHighSpeedVideoSizes(), Math.max(viewSize.getHeight(), viewSize.getWidth()), Math.min(viewSize.getHeight(), viewSize.getWidth()), 4, 3);
//
//        } else {
            return chooseOptimalPreviewSize(supportedPreviewSizes, Math.max(viewSize.getHeight(), viewSize.getWidth()), Math.min(viewSize.getHeight(), viewSize.getWidth()), 4, 3);
//        }
    }

    @Override
    protected boolean canUseCamera(CameraCharacteristics characteristics) {
        // Odd comparison because of odd terminology
        return (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK);
    }

    @Override
    protected SurfaceProvider[] createSurfacesProviders() {

        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "createSurfaceProviders");
        return new SurfaceProvider[]{inMemoryRecorder};
    }


    @Override
    public CaptureRequest.Builder setupPreviewSession(CameraDevice device, Surface previewSurface) throws CameraAccessException {

        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "setupPreviewSession");
        CaptureRequest.Builder builder = super.setupPreviewSession(device, previewSurface);
        builder.addTarget(inMemoryRecorder.getSurface());

        if(this.exposureLocked) {
            builder.set(CaptureRequest.CONTROL_AE_LOCK, true);
        }

        return builder;
    }

    public void lockExposure() {
        if(!this.exposureLocked) {
            this.exposureLocked = true;
            startPreview(); // This call should be save, since it re-submits the capture request.
        }
    }
    @Override
    protected void onSessionCreated(CameraCaptureSession currentSession) {
        super.onSessionCreated(currentSession);
        inMemoryRecorder.startFrameFetching(dataListener);
    }

    @Override
    protected void onSessionDestroying(CameraCaptureSession currentSession) {
        super.onSessionDestroying(currentSession);
        inMemoryRecorder.stopFrameFetching();
    }

    public void setPreviewListener(RecorderPreviewListener dataListener) {
        this.dataListener = dataListener;
    }

    @Override
    protected void onCameraOpenend(CameraDevice device) {
        super.onCameraOpenend(device);
        if (null != dataListener) {
            dataListener.cameraOpened(cameraDevice);
        }
    }

    @Override
    protected void onCameraClosed(CameraDevice device) {
        super.onCameraClosed(device);
        if (null != dataListener) {
            dataListener.cameraClosed(cameraDevice);
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "onResume");
        inMemoryRecorder.startBackgroundThread();
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "onPause");
        super.onPause();
        try {
            inMemoryRecorder.stopBackgroundThread();
        } catch (InterruptedException e) {
            Log.e(TAG, "background thread is already stopped", e);
        }
    }

    @Override
    protected void initializeExternalSurfaceProvider(Size optimalSize, SurfaceProvider target, SurfaceProvider.SurfaceProviderCallback externalSurfaceCallback) {
        Log.w(TAG, "Vendor: " + android.os.Build.MANUFACTURER);
//        if (target == inMemoryRecorder /*&& android.os.Build.MANUFACTURER.equals("Huawei")*/) {
//            float aspect = (float) optimalSize.getWidth() / (float) optimalSize.getHeight();
//            super.initializeExternalSurfaceProvider(new Size((int) (DETECTOR_IMAGE_SIZE * Math.min(1, aspect)), (int) (DETECTOR_IMAGE_SIZE * Math.min(1, 1.f / aspect))), target, externalSurfaceCallback);
//        } else {
            super.initializeExternalSurfaceProvider(OPTIMAL_PREVIEW_SIZE, target, externalSurfaceCallback);
//        }

    }

    @Override
    protected void createCaptureSession(List<Surface> sessionSurfaces, CameraCaptureSession.StateCallback callback, CameraDevice cameraDevice, Handler backgroundHandler) throws CameraAccessException {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            cameraDevice.createConstrainedHighSpeedCaptureSession(sessionSurfaces, callback, backgroundHandler);
//        } else {
            super.createCaptureSession(sessionSurfaces, callback, cameraDevice, backgroundHandler);
//        }
    }

}
