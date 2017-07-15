package com.iam360.dscvr.views.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iam360.dscvr.R;
import com.iam360.dscvr.record.GlobalState;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.views.activity.RecorderActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class RecorderOverlayFragment extends Fragment {
    public static final int PREVIEW_RECORD = 1;
    public static final int RECORDING = 2;

    private int RECORDING_MODE = Constants.MODE_CENTER;
    private int screenWidth = Constants.getInstance().getDisplayMetrics().widthPixels;
    private int MODE = PREVIEW_RECORD;


    @Bind(R.id.record_button)
    ImageButton recordButton;
    @Bind(R.id.record_progress)
    ProgressBar recordProgress;

    @Bind(R.id.camera_overlay)
    FrameLayout cameraOverlay;
    @Bind(R.id.instruction)
    TextView instruction;
    @Bind(R.id.crosshair)
    View crosshair;
    @Bind(R.id.arrow)
    View arrow;
    @Bind(R.id.line)
    View line;
    @Bind(R.id.angle)
    View angle;
    @Bind(R.id.progress_point)
    View progressPoint;
    @Bind(R.id.arc)
    View arc;
    @Bind(R.id.progress)
    ProgressBar progressLine;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_recorder_overlay, container, false);
        ButterKnife.bind(this, view);

        initializeNavigationButtons(view);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        ((RecorderActivity) getActivity()).overlayInitialised();
    }


    private void initializeNavigationButtons(View view) {

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startRecording();

            }
        });

    }


    public void startRecording() {
        Timber.d("initializeNavigationButtons recordButtonClick");
        MODE = RECORDING_MODE;
        recordButton.setVisibility(View.INVISIBLE);
        recordProgress.setVisibility(View.INVISIBLE);
        cameraOverlay.setVisibility(View.VISIBLE);
        instruction.setText(getActivity().getResources().getText(R.string.record_instruction_follow));

        ((RecorderActivity) getActivity()).startRecording();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getView().setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                }
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public void setAngleRotation(float rotation) {
        line.setRotation((float) Math.toDegrees(rotation));
    }

    public void setArrowRotation(float rotation) {
        arrow.setRotation((float) Math.toDegrees(rotation));
    }

    public void setArrowVisible(boolean visible) {
        if (visible) {
            arrow.setVisibility(View.VISIBLE);
            crosshair.setBackgroundResource(R.drawable.crosshair);
        } else {
            arrow.setVisibility(View.INVISIBLE);
            crosshair.setBackgroundResource(R.drawable.crosshair_red);
        }
    }

    public void setGuideLinesVisible(boolean visible) {
        if (visible) {
            line.setVisibility(View.VISIBLE);
            angle.setVisibility(View.VISIBLE);
        } else {
            line.setVisibility(View.INVISIBLE);
            angle.setVisibility(View.INVISIBLE);
        }
    }

    // sets Max value for progress bar
    private void setProgressMaxValue() {
        progressLine.setMax((Math.round((screenWidth - 100))));
    }

    public void setProgress(float progress) {
        setProgressMaxValue();
        progressPoint.setX((screenWidth - 100) * progress + 15);
        progressLine.setProgress((Math.round((screenWidth - 100) * progress)));
    }

    public boolean isPreviewMode() {
        if (MODE == PREVIEW_RECORD) return true;
        else return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (GlobalState.isAnyJobRunning) {
            recordProgress.setVisibility(View.VISIBLE);
        } else {
            recordProgress.setVisibility(View.INVISIBLE);
        }
    }

}
