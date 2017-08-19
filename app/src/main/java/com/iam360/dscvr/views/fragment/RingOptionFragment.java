package com.iam360.dscvr.views.fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.iam360.dscvr.DscvrApp;
import com.iam360.dscvr.R;
import com.iam360.dscvr.bluetooth.BluetoothConnector;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.Constants;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

/**
 * <p>
 * Fragment to decide which Mode should be used.
 * Created by Lotti on 4/25/2017.
 */

public class RingOptionFragment extends Fragment {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_LOCATION = 2;
    private String TAG = RingOptionFragment.class.getSimpleName();
    @Bind(R.id.frag_manual_button)
    ImageButton leftButton;
    @Bind(R.id.frag_motor_button)
    ImageButton rightButton;
    @Bind(R.id.camera_btn)
    ImageButton recordButton;
    @Bind(R.id.record_progress)
    ProgressBar loading;

    BluetoothConnector connector;

    private boolean isNotCloseable = false;


    private Cache cache;

    private OnModeFinished callBackListener;
    private boolean firstTime = true;
    private boolean isCurrentlyRingChoosing = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_ring_option, container, false);
        cache = Cache.open();
        ButterKnife.bind(this, view);
        initButtonListeners();
        setModeToManual();
        stopLoading();
        return view;
    }

    private void initButtonListeners() {
        leftButton.setOnClickListener(v -> {
            if(!isCurrentlyRingChoosing) {
                setModeToManual();
            } else {
                setModeToOneRingMotor();
            }
        });
        rightButton.setOnClickListener(v -> {
            if (!isCurrentlyRingChoosing) {
                setModeToMotor();
            } else {
                setModeToThreeRingMotor();
            }
        });
        recordButton.setOnClickListener(v -> finishSettingMode());
    }

    private void setModeToThreeRingMotor() {
        leftButton.setBackgroundResource(R.drawable.one_ring_icon);
        rightButton.setBackgroundResource(R.drawable.three_ring_icon_orange);
        cache.save(Cache.MOTOR_ON, true);
        cache.save(Cache.CAMERA_MODE, Constants.THREE_RING_MODE);
    }

    private void setModeToMotor() {

        if (!DscvrApp.getInstance().hasConnection() || firstTime) {
            showLoading();
            isNotCloseable = true;
            startToSearchEngine();
            firstTime = false;
        }

        isCurrentlyRingChoosing = true;
        int rings = cache.getInt(Cache.CAMERA_MODE);

        if(rings == Constants.THREE_RING_MODE) {
            setModeToThreeRingMotor();
        } else {
            setModeToOneRingMotor();
        }
    }

    private void setModeToOneRingMotor() {
        leftButton.setBackgroundResource(R.drawable.one_ring_icon_active);
        rightButton.setBackgroundResource(R.drawable.three_ring_icon);
        cache.save(Cache.MOTOR_ON, true);
        cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE);
    }

    private void setModeToManual() {
        leftButton.setBackgroundResource(R.drawable.manual_icon_orange);
        rightButton.setBackgroundResource(R.drawable.motor_inactive_white);
        cache.save(Cache.MOTOR_ON, false);
    }

    private void finishSettingMode() {
        if (!isNotCloseable) {
            callBackListener.finishSettingModeForRecording();
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callBackListener = (OnModeFinished) context;
        isCurrentlyRingChoosing = false;
    }

    private void startToSearchEngine() {
        if (BluetoothAdapter.getDefaultAdapter() == null || !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_LOCATION);
            return;

        }
        this.connector = DscvrApp.getInstance().getConnector();
        if (connector != null && DscvrApp.getInstance().hasConnection()) {
            connector.update(() -> {}, () -> {});
            stopLoading();
            isNotCloseable = false;
        } else {
            this.connector.connect(gatt -> {
                if (gatt != null) {
                    Snackbar.make(getView(), "Motor found. ", Snackbar.LENGTH_SHORT).show();
                }
                stopLoading();
            }, () -> {}, () -> {});
        }

    }

    private void stopLoading() {
        Timber.d("stop bt loading");
        isNotCloseable = false;
        getActivity().runOnUiThread(() -> loading.setVisibility(View.INVISIBLE));
        getActivity().runOnUiThread(() -> recordButton.setBackgroundResource(R.drawable.camera_selector));
    }

    private void showLoading() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (loading.getVisibility() == View.VISIBLE) {
                    Snackbar.make(getView(), "No Motor found.", Snackbar.LENGTH_LONG).show();
                }
            }
        }, 10000);
        loading.setVisibility(View.VISIBLE);
        recordButton.setBackgroundResource(R.drawable.camera_without_icn);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == PackageManager.PERMISSION_GRANTED) {
                    startToSearchEngine();
                }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startToSearchEngine();
                }
        }
    }

    public interface OnModeFinished {
        public void finishSettingModeForRecording();

        public void directlyStartToRecord();
    }
}
