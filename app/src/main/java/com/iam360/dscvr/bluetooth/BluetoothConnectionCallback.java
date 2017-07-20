package com.iam360.dscvr.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by Charlotte on 17.03.2017.
 */
public class BluetoothConnectionCallback extends BluetoothGattCallback {

    private final ButtonValueListener bottomButton;
    private final ButtonValueListener topButton;
    private BluetoothConnector.BluetoothLoadingListener listener;

    public BluetoothConnectionCallback(BluetoothConnector.BluetoothLoadingListener listener, ButtonValueListener bottomButton, ButtonValueListener topButton) {
        this.topButton = topButton;
        this.bottomButton = bottomButton;
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
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Timber.d("characteristic changed");
        if (characteristic.getUuid().equals(BluetoothEngineControlService.RESPONSE_UUID)) {
            if (Arrays.equals(characteristic.getValue(),BluetoothEngineControlService.BOTTOMBUTTON)) {
                    if(bottomButton!= null) bottomButton.buttomPressed();
            }else if(Arrays.equals(characteristic.getValue(), BluetoothEngineControlService.TOPBUTTON)){
                    if(bottomButton!= null) topButton.buttomPressed();
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Timber.d("service discovered");
        listener.endLoading(gatt);
    }

    public interface ButtonValueListener {
        void buttomPressed();
    }
}