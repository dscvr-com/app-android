package com.iam360.iam360.views.new_design;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.UUID;

import com.iam360.iam360.R;
import com.iam360.iam360.util.Cache;

public class RecorderActivity extends AppCompatActivity {

    private  RecordFragment recordFragment;
    private RecorderOverlayFragment recorderOverlayFragment;
    private Cache cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        cache = Cache.open();
        Bundle bundle = new Bundle();
        bundle.putInt("mode", cache.getInt(Cache.CAMERA_MODE));
        recordFragment = new RecordFragment();
        recordFragment.setArguments(bundle);

//        Bundle bundle = new Bundle();
//        bundle.putInt("mode", Constants.MODE_CENTER);
        recorderOverlayFragment = new RecorderOverlayFragment();
//        recordFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.feed_placeholder, recordFragment).commit();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.feed_placeholder, recorderOverlayFragment).commit();

    }

    public void startRecording() {
            recordFragment.startRecording();
    }

    public void cancelRecording() {
        recordFragment.cancelRecording();
        finish();
    }
    public void startPreview(UUID id) {
        Intent intent = new Intent(this, OptoImagePreviewActivity.class);
        intent.putExtra("id", id.toString());
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
//        super.onBackPressed();
    }
}
