package com.iam360.dscvr.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iam360.dscvr.DscvrApp;

/**
 * class to handle Bluetooth Broadcasts
 *
 * Created by Charlotte on 15.11.2016.
 */
public class BluetoothConnectionReciever extends BroadcastReceiver {

    public static final String CONNECTED = "com.iam360.bluetooth.BLUETOOTH_CONNECTED";
    public static final String DISCONNECTED = "com.iam360.bluetooth.BLUETOOTH_DISCONNECTED";
    private static final String TAG = "BluetoothConnectReceive";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case CONNECTED:
                Log.i(TAG, "connected to device");
                break;
            case DISCONNECTED:
                //TODO
                Log.i(TAG, "lost connection to device");
                break;

        }
    }
}
