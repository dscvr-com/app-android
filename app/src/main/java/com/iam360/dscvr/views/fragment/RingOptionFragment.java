package com.iam360.dscvr.views.fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.iam360.dscvr.DscvrApp;
import com.iam360.dscvr.R;
import com.iam360.dscvr.bluetooth.BluetoothConnectionReciever;
import com.iam360.dscvr.bluetooth.BluetoothConnector;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

/**
 *
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
    @Bind(R.id.frag_option_record_button)
    ImageButton recordButton;

    BluetoothConnector connector;
    BluetoothConnectionReciever reciever;

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
        updateMode((cache.getInt(Cache.CAMERA_MODE) == Constants.ONE_RING_MODE) ? true : false);
        manualBtn.setOnClickListener(v -> updateMode(true));
        motorBtn.setOnClickListener(v -> updateMode(false));
        recordButton.setOnClickListener(v -> finishSettingMode());

    }

    private void finishSettingMode() {
        if(!isNotCloseable){
            callBackListener.finishSettingModeForRecording();
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callBackListener = (OnModeFinished) context;

    }

    /**
     * Updates 1 or 3 ring on layout and cache
     *
     * @param isManualMode
     */
    private void updateMode(boolean isManualMode) {
        if (isManualMode) {
            manualBtn.setBackgroundResource(R.drawable.manual_icon_orange);
            motorBtn.setBackgroundResource(R.drawable.motor_icon);
            cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE);
        } else {
            if(isNotCloseable){
                return;
            }
            isNotCloseable = true;
            startToSearchEngine();
            showLoading();
            manualBtn.setBackgroundResource(R.drawable.manual_icon);
            motorBtn.setBackgroundResource(R.drawable.motor_icon_orange);
            cache.save(Cache.CAMERA_MODE, Constants.THREE_RING_MODE);
        }
    }

    private void startToSearchEngine() {
        if (BluetoothAdapter.getDefaultAdapter() == null || !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (checkSelfPermission(getContext(),Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_LOCATION);

        }
        connector = new BluetoothConnector(BluetoothAdapter.getDefaultAdapter(),getContext());
        connector.setListener((gatt) -> stopLoading(gatt));
        connector.connect();

    }

    private void stopLoading(BluetoothGatt gatt) {
        ((DscvrApp) (getContext().getApplicationContext())).setBTGatt(gatt);
        isNotCloseable = false;
        //todo
    }

    private void showLoading() {
        //todo
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode ==  PackageManager.PERMISSION_GRANTED){
                    startToSearchEngine();
                }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }




    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode){
            case PERMISSION_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startToSearchEngine();
                }
        }
    }

    public interface OnModeFinished {
        public void finishSettingModeForRecording();
    }
}
