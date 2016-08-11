package com.iam360.iam360.views.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.iam360.iam360.R;
import com.iam360.iam360.ble.BLECommands;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.views.fragment.RecordFragment;
import com.iam360.iam360.views.fragment.RecorderOverlayFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RecorderActivity extends AppCompatActivity {

    private RecordFragment recordFragment;
    private RecorderOverlayFragment recorderOverlayFragment;
    private Cache cache;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLE_LIST = 1000;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mBluetoothService;
    private ScanSettings mScanSettings;
    private List<ScanFilter> mScanFilters;
    private BLECommands bleCommands;

    private UUID mServiceUIID;
    private UUID mResponesUIID;
    private UUID mNotifUUID;

    private boolean m3ringFlag = true;
    private int motorRingType = 1;
    public boolean useBLE = false;
    public boolean dataHasCome = false;

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

//        Intent data = getIntent();
//        if(data != null && data.getStringExtra("DEVICE_NAME") != null && !data.getStringExtra("DEVICE_NAME").equals("")){
            useBLE = true;
            mServiceUIID = UUID.fromString(getString(R.string.btle_serviceuuidlong));
            mResponesUIID = UUID.fromString(getString(R.string.btle_characteristic_response));
            mNotifUUID = UUID.fromString(getString(R.string.btle_serviceuuid_notification));

            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, R.string.error_btle_notsupport, Toast.LENGTH_SHORT).show();
                finish();
            }

            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                mScanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                mScanFilters = new ArrayList<ScanFilter>();
            }

//            String devName = data.getStringExtra("DEVICE_NAME");
//            String devAddrss = data.getStringExtra("DEVICE_ADDRESS");
            String devAddrss = "44:A6:E5:03:88:4F";
            endBT();
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setDeviceAddress(devAddrss);
            mScanFilters.add(builder.build());
            beginBT();
//        }else{
//            useBLEDeviceDialog();
//        }
    }

    public void startRecording() {
        recordFragment.startRecording();
        bleCommands = new BLECommands(mBluetoothAdapter, mBluetoothGatt, mBluetoothService, RecorderActivity.this);
        Log.d("MARK2","startRot = "+System.currentTimeMillis() / 1000.0);
        bleCommands.rotateRight();
        motorRingType = 2; //top ring
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
//            finish();
            return;
        }else {
            startLeScan();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            stopLeScan();
        }
    }

    @Override
    protected void onDestroy() {
        endBT();
        super.onDestroy();
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice btDevice = result.getDevice();
            connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d("MARK","onBatchScanResults = "+results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("MARK","Scan Failed Error Code: " + errorCode);
        }
    };

    public void connectToDevice(BluetoothDevice device) {
        if (mBluetoothGatt == null) {
            mBluetoothGatt = device.connectGatt(this, false, gattCallback);
            stopLeScan();
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    break;
                default:
                    Log.e("gattCallback", "Unknown State: " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            // Hardcoded for now, uuid filtering not working
            for(int a=0; a < services.size(); a++){
                if(services.get(a).getUuid().equals(mServiceUIID)){
                    mBluetoothService = services.get(a);
                    for (int b=0; b<mBluetoothService.getCharacteristics().size(); b++){
                        if(mBluetoothService.getCharacteristics().get(b).getUuid().equals(mResponesUIID)){
                            BluetoothGattCharacteristic characteristic = mBluetoothService.getCharacteristics().get(b);
                            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                            BluetoothGattDescriptor d = characteristic.getDescriptor(mNotifUUID);
                            d.setValue(true ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[] { 0x00, 0x00 });
                            mBluetoothGatt.writeDescriptor(d);
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            gatt.disconnect();
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            final BluetoothGattCharacteristic characteristic) {
            Log.d("MARK","onCharacteristicChanged characteristic = "+characteristic.getUuid());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    mResponseData.setText(new String(bytesToHex(characteristic.getValue())));
                }
            });
            //motorRing type = (0:stop, 1:1st ring rotate, 2:toTop 2nd, 3:3rd ring rotate, 4:toBot, 5:toBot, 6:last)
            byte[] responseValue = characteristic.getValue();
            char[] charArr = bleCommands.bytesToHex(responseValue);
            String yPos = String.valueOf(""+charArr[14] + charArr[15] + charArr[16] + charArr[17] + charArr[18] + charArr[19] + charArr[20] + charArr[21]);
            Log.d("MARK","yPos  == "+yPos);
            Log.d("MARK","motorRingType  == "+motorRingType);
            Log.d("MARK","onCharacteristicChanged characteristic.getValue() = "+new String(bleCommands.bytesToHex(characteristic.getValue())));
            if(motorRingType == 2) {
                Log.d("MARK2","motorRingType = "+System.currentTimeMillis() / 1000.0);
                dataHasCome = false;
                bleCommands.topRing();
                motorRingType = 3;
            }else if(motorRingType == 3){
                dataHasCome = true;
                bleCommands.rotateRight();
                motorRingType = 4;
            }else if(motorRingType == 4){
                dataHasCome = false;
                bleCommands.bottomRing();
                motorRingType = 5;
            }else if(motorRingType == 5){
                dataHasCome = true;
                bleCommands.rotateRight();
                motorRingType = 6;
            }else if(motorRingType == 6){
                dataHasCome = false;
                bleCommands.topRing();
                motorRingType = 0;
            }

//            switch (yPos) {
//                case "FFFFEE99":  //after ng open
//                    if (m3ringFlag && motorRingType == 1) {
//                        bleCommands.topRing();
//                        motorRingType = 2;
//                    }
//                    break;
//                case "FFFFE890":  //after ng move top
//                    if (m3ringFlag) {
//                        if (motorRingType == 2) {
//                            bleCommands.rotateRight();
//                            motorRingType = 3;
//                        } else {
//                            bleCommands.bottomRing(); //after ng 2nd ring
//                        }
//                    }
//                    break;
//                case "FFFFF4A2":  //after ng move to bottom
//                    if (m3ringFlag) {
//                        if (motorRingType == 3) {
//                            bleCommands.rotateRight();
//                            motorRingType = 4;
//                        } else {
//                            bleCommands.topRing(); //after ng 3rd ring
//                        }
//                    }
//                    break;
//            }
        }
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
        }
    };

    private void beginBT() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // Request to Enable BT
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            startLeScan();
        }
    }

    private void endBT() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                mBluetoothGatt = null;
                mBluetoothService = null;
            }
            stopLeScan();
        }
    }

    private void startLeScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mLEScanner.startScan(mScanFilters, mScanSettings, mScanCallback);
        }
    }

    private void stopLeScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mLEScanner.stopScan(mScanCallback);
        }
    }

    private void useBLEDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to use a motor?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(RecorderActivity.this, BLEListActivity.class);
                        startActivityForResult(intent,REQUEST_BLE_LIST);
                        dialog.cancel();
                        useBLE = true;
                        finish();
                    }
                });
        builder.setNegativeButton("NO",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                useBLE = false;
                dialog.cancel();
            }
        });
        AlertDialog alert1 = builder.create();
        alert1.show();
    }
}
