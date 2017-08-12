package com.iam360.dscvr.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Connects the application to a bluetoothdevice
 * Created by Charlotte on 17.03.2017.
 */
public class BluetoothConnector extends BroadcastReceiver {


    public static final String CONNECTED = "com.iam360.bluetooth.BLUETOOTH_CONNECTED";
    public static final String DISCONNECTED = "com.iam360.bluetooth.BLUETOOTH_DISCONNECTED";

    private static final long SCAN_PERIOD = 10000000;//very long time
    private final BluetoothAdapter adapter;
    private final Context context;
    private final Handler stopScanHandler = new Handler();
    private BluetoothConnectionCallback.ButtonValueListener upperButtomListener;
    private BluetoothConnectionCallback.ButtonValueListener lowerButtonListener;
    private BluetoothLoadingListener listener;
    private List<BluetoothDevice> nextDevice = new ArrayList<>();
    private boolean currentlyConnecting = false;
    private BluetoothEngineControlService controlService = new BluetoothEngineControlService();
    private BluetoothLeScanCallback bluetoothLeScanCallback = new BluetoothLeScanCallback((device -> addDeviceFromScan(device)));
    private BluetoothConnectionCallback callback;

    public BluetoothConnector(BluetoothAdapter adapter, Context context) {
        this.adapter = adapter;
        this.context = context;

    }


    public void connect(BluetoothLoadingListener listener, BluetoothConnectionCallback.ButtonValueListener upperButtomListener, BluetoothConnectionCallback.ButtonValueListener lowerButtonListener) {
        this.listener = listener;
        this.upperButtomListener = upperButtomListener;
        this.lowerButtonListener = lowerButtonListener;
        List<BluetoothDevice> bluetoothDevices = searchBondedDevices();
        if (bluetoothDevices.size() > 0) {
            connect(bluetoothDevices.get(0));
            bluetoothDevices.remove(0);
            nextDevice.addAll(bluetoothDevices);
        } else {
            findLeDevice();
        }

    }

    private void findLeDevice() {
        if (adapter == null || !adapter.isEnabled() && adapter.getBluetoothLeScanner() == null) {
            throw new IllegalStateException("adapter is not turned on");
        } else {
            stopScanHandler.postDelayed(() -> adapter.getBluetoothLeScanner().stopScan(bluetoothLeScanCallback), SCAN_PERIOD);
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            ArrayList<ScanFilter> filters = new ArrayList<>();
            filters.add(new ScanFilter.Builder().setServiceUuid(BluetoothEngineControlService.SERVICE_UUID).build());
            adapter.getBluetoothLeScanner().startScan(filters, settings, bluetoothLeScanCallback);
        }
    }
    private void addDeviceFromScan(BluetoothDevice device) {
        nextDevice.add(device);
        if (!currentlyConnecting) {
            if (nextDevice.size() > 0) {
                connect(nextDevice.get(0));
                nextDevice.remove(0);
            }
        }
    }

    private List<BluetoothDevice> searchBondedDevices() {
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        List<BluetoothDevice> contactableList = new ArrayList<>();
        if (bondedDevices.isEmpty()) {
            return new ArrayList<>();
        }
        for (BluetoothDevice device : bondedDevices) {
            if (device.getUuids() == null) {
                continue;
            }
            for (ParcelUuid uuid : device.getUuids()) {
                if (uuid.equals(BluetoothEngineControlService.SERVICE_UUID)) {
                    contactableList.add(device);
                    continue;
                }
            }

        }
        return contactableList;
    }

    private void connect(BluetoothDevice device) {
        currentlyConnecting = true;
        callback = new BluetoothConnectionCallback(gatt -> afterConnecting(gatt), lowerButtonListener, upperButtomListener);
        device.connectGatt(context, true, callback);

    }

    private void afterConnecting(BluetoothGatt gatt) {
        controlService.setBluetoothGatt(gatt);
        if (listener != null) listener.endLoading(gatt);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case CONNECTED:
                break;
            case DISCONNECTED:
                currentlyConnecting = false;
                if (nextDevice.size() > 0) {
                    connect(nextDevice.get(0));
                    nextDevice.remove(0);
                } else {
                    findLeDevice();
                }
                break;

        }
    }

    public boolean hasDevices() {
        if (nextDevice.size() == 0 && !currentlyConnecting) {
            return false;
        }
        return true;
    }

    public boolean isConnected() {
        return controlService.hasBluetoothService();
    }

    public BluetoothEngineControlService getBluetoothService() {
        return controlService;
    }

    public void stop() {
        adapter.cancelDiscovery();
        adapter.getBluetoothLeScanner().stopScan(bluetoothLeScanCallback);
    }

    public void update(BluetoothConnectionCallback.ButtonValueListener upper, BluetoothConnectionCallback.ButtonValueListener lower) {
        this.upperButtomListener = upper;
        this.lowerButtonListener = lower;
        if (callback != null) {
            callback.update(upper, lower);
        }
    }

    public interface BluetoothLoadingListener {
        void endLoading(BluetoothGatt gatt);
    }
}
