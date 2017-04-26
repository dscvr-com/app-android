package com.iam360.dscvr.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.iam360.dscvr.DscvrApp;

import timber.log.Timber;

/**
 * Created by Charlotte on 17.03.2017.
 */
public class BluetoothConnectionCallback extends BluetoothGattCallback {


    private static final String BLUETOOTH_GATT = "bluetoothGatt";
    private final Context context;
    private BluetoothConnector.BluetoothLoadingListener listener;

    public BluetoothConnectionCallback(Context context, BluetoothConnector.BluetoothLoadingListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setListener(BluetoothConnector.BluetoothLoadingListener listener) {
        this.listener = listener;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.i("onConnectionStateChange", "Status: " + status);
        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
                Timber.d("STATE_CONNECTED");
                gatt.discoverServices();
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                Timber.e("STATE_DISCONNECTED");
                break;
            default:
                Timber.e("STATE_OTHER");
    }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Timber.d("service discovered");
        listener.endLoading(gatt);
    }


}