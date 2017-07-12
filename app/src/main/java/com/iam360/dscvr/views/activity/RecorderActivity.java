package com.iam360.dscvr.views.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.iam360.dscvr.R;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.views.fragment.RecordFragment;
import com.iam360.dscvr.views.fragment.RecorderOverlayFragment;
import com.iam360.dscvr.views.fragment.RingOptionFragment;

import timber.log.Timber;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RecorderActivity extends AppCompatActivity implements RingOptionFragment.OnModeFinished {

    private final int PERMISSION_REQUEST_CAMERA = 2;

    private RecordFragment recordFragment;
    private RecorderOverlayFragment recorderOverlayFragment;
    private RingOptionFragment ringOptionFragment;
    public Cache cache;
    private boolean overlayInitialised = false;
    private boolean shouldDirectlyStart = false;

    private void initialzeRingOptions() {
        Timber.d("Initing camera.");

        Bundle bundle = new Bundle();
        bundle.putInt("mode", cache.getInt(Cache.CAMERA_MODE));
        recordFragment = new RecordFragment();
        ringOptionFragment = new RingOptionFragment();
        recorderOverlayFragment = new RecorderOverlayFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.feed_placeholder, recordFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.feed_placeholder, ringOptionFragment).commit();
    }

    private void initalizeOverlay() {
        overlayInitialised = true;
        getSupportFragmentManager().beginTransaction().add(R.id.feed_placeholder, recorderOverlayFragment).commit();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        cache = Cache.open();
        checkPermissionAndInitialize();

    }

    public void checkPermissionAndInitialize() {
        Timber.d("Checking permission.");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Timber.d("Requesting permission.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            Timber.d("Permission granted.");
            initialzeRingOptions();
        }

    }

    public void startRecording() {
        recordFragment.startRecording();
    }

    public void cancelRecording() {
        recordFragment.cancelRecording();
    }

    public void showLoading() {
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);
        finish();
    }

    public void setAngleRotation(float rotation) {
        recorderOverlayFragment.setAngleRotation(rotation);
    }

    public void setArrowRotation(float rotation) {
        recorderOverlayFragment.setArrowRotation(rotation);
    }

    public void setProgressLocation(float progress) {
        recorderOverlayFragment.setProgress(progress);
    }

    public void setArrowVisible(boolean visible) {
        recorderOverlayFragment.setArrowVisible(visible);
    }

    public void setGuideLinesVisible(boolean visible) {
        recorderOverlayFragment.setGuideLinesVisible(visible);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_cancel_recording)
                .setPositiveButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        cancelRecording();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.dialog_dont_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void finishSettingModeForRecording() {

        getSupportFragmentManager().beginTransaction().remove(ringOptionFragment).commit();
        initalizeOverlay();
    }

    @Override
    public void directlyStartToRecord() {
        if (!overlayInitialised) {
            finishSettingModeForRecording();
            shouldDirectlyStart = true;
        } else {
            recorderOverlayFragment.startRecording();
        }

    }

    public void overlayInitialised() {
        if (shouldDirectlyStart) {
            recorderOverlayFragment.startRecording();
            shouldDirectlyStart = false;
        }
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Timber.d("onRequestPermissionsResult");
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Timber.d("Permission granted.");
                    initialzeRingOptions();

                } else {
                    Timber.d("Permission not granted.");
                    throw new RuntimeException("Need Camera!");
                }
                return;
            }
        }
    }
}
