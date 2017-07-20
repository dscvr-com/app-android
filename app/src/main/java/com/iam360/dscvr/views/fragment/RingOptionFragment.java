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
    ImageButton manualBtn;
    @Bind(R.id.frag_motor_button)
    ImageButton motorBtn;
    @Bind(R.id.camera_btn)
    ImageButton recordButton;
    @Bind(R.id.record_progress)
    ProgressBar loading;

    BluetoothConnector connector;

    private boolean isNotCloseable = false;


    private Cache cache;

    private OnModeFinished callBackListener;

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
        return view;
    }

    private void initButtonListeners() {
        updateMode(!cache.getBoolean(Cache.MOTOR_ON));
        manualBtn.setOnClickListener(v -> updateMode(true));
        motorBtn.setOnClickListener(v -> updateMode(false));
        recordButton.setOnClickListener(v -> finishSettingMode());

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

    }

    /**
     * update engine or manual mode
     *
     * @param isManualMode
     */
    private void updateMode(boolean isManualMode) {
        if (isManualMode) {
            stopLoading(null);
            manualBtn.setBackgroundResource(R.drawable.manual_icon_orange);
            motorBtn.setBackgroundResource(R.drawable.one_ring_inactive_icn);
            cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE);//FIXME remove this later
            cache.save(Cache.MOTOR_ON, !isManualMode);
            if (isNotCloseable || DscvrApp.getInstance().hasConnection()) {
                connector.stop();
            }
        } else {

            if (!DscvrApp.getInstance().hasConnection()) {
                startToSearchEngine();
                showLoading();
                isNotCloseable = true;
            }
            cache.save(Cache.MOTOR_ON, !isManualMode);
            manualBtn.setBackgroundResource(R.drawable.manual_icon);
            motorBtn.setBackgroundResource(R.drawable.one_ring_active_icn);
            cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE); // FIXME remove this later
        }
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

        connector = new BluetoothConnector(BluetoothAdapter.getDefaultAdapter(), getContext());
        DscvrApp.getInstance().getConnector();

        connector.connect(gatt -> stopLoading(gatt), () -> reactForUpperButton(), () -> reactForLowerButton());

    }

    private void reactForLowerButton() {
        Timber.d("lowerBotton");
        //TODO
    }

    private void reactForUpperButton() {
        if (!isNotCloseable) {
            updateMode(false);
            callBackListener.directlyStartToRecord();
    }
    }

    private void stopLoading(BluetoothGatt gatt) {
        Timber.d("stop bt loading");
        isNotCloseable = false;
        if (gatt != null) {
            Snackbar.make(getView(), "Motor found. ", Snackbar.LENGTH_SHORT).show();
        }
        getActivity().runOnUiThread(() -> loading.setVisibility(View.INVISIBLE));
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
