package com.iam360.dscvr.views.activity;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.iam360.dscvr.R;
import com.iam360.dscvr.views.fragment.RecordFragment;
import com.iam360.dscvr.views.fragment.RecorderOverlayFragment;
import com.iam360.dscvr.util.BLECommands;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.views.fragment.RingOptionFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RecorderActivity extends AppCompatActivity implements RingOptionFragment.OnModeFinished {

    private final int PERMISSION_REQUEST_CAMERA = 2;

    private RecordFragment recordFragment;
    private RecorderOverlayFragment recorderOverlayFragment;
    private RingOptionFragment ringOptionFragment;
    public Cache cache;

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

    RecorderActivity act;

    private void initialzeRingOptions() {
        Timber.d("Initing camera.");

        Bundle bundle = new Bundle();
        bundle.putInt("mode", cache.getInt(Cache.CAMERA_MODE));
        recordFragment = new RecordFragment();
        ringOptionFragment = new RingOptionFragment();
        recorderOverlayFragment = new RecorderOverlayFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.feed_placeholder, recordFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.feed_placeholder, ringOptionFragment).commit();
    }

    private void initalizeOverlay() {
        getSupportFragmentManager().beginTransaction().add(R.id.feed_placeholder, recorderOverlayFragment).commit();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        cache = Cache.open();
        checkPermissionAndInitialize();

    }

    public void checkPermissionAndInitialize() {
        Timber.d("Checking permission.");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Timber.d("Requesting permission.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            Timber.d("Permission granted.");
            initialzeRingOptions();
        }

    }

    public void startRecording() {
        recordFragment.startRecording();

        if (cache.getInt(Cache.CAMERA_MODE) == Constants.THREE_RING_MODE) {
            bleCommands = new BLECommands(mBluetoothAdapter, mBluetoothGatt, mBluetoothService, RecorderActivity.this);
            Log.d("MARK2", "startRot = " + System.currentTimeMillis() / 1000.0);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dataHasCome = true;
                    bleCommands.rotateRight();
                }
            }, 2000);
            motorRingType = 2; //top ring
        }
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


//
//    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            switch (newState) {
//                case BluetoothProfile.STATE_CONNECTED:
//                    gatt.discoverServices();
//                    break;
//                case BluetoothProfile.STATE_DISCONNECTED:
//                    break;
//                default:
//                    Log.e("gattCallback", "Unknown State: " + newState);
//            }
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            List<BluetoothGattService> services = gatt.getServices();
//            // Hardcoded for now, uuid filtering not working
//            for (int a = 0; a < services.size(); a++) {
//                if (services.get(a).getUuid().equals(mServiceUIID)) {
//                    mBluetoothService = services.get(a);
//                    for (int b = 0; b < mBluetoothService.getCharacteristics().size(); b++) {
//                        if (mBluetoothService.getCharacteristics().get(b).getUuid().equals(mResponesUIID)) {
//                            BluetoothGattCharacteristic characteristic = mBluetoothService.getCharacteristics().get(b);
//                            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
//                            BluetoothGattDescriptor d = characteristic.getDescriptor(mNotifUUID);
//                            d.setValue(true ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00, 0x00});
//                            mBluetoothGatt.writeDescriptor(d);
//                        }
//                    }
//                }
//            }
//        }
//
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            gatt.disconnect();
//        }
//
//        @Override
//                public void onCharacteristicChanged(BluetoothGatt gatt,
//                final BluetoothGattCharacteristic characteristic) {
//                    Log.d("MARK", "onCharacteristicChanged characteristic = " + characteristic.getUuid());
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                    mResponseData.setText(new String(bytesToHex(characteristic.getValue())));
//                        }
//                    });
//                    //motorRing type = (0:stop, 1:1st ring rotate, 2:toTop 2nd, 3:3rd ring rotate, 4:toBot, 5:toBot, 6:last)
//                    byte[] responseValue = characteristic.getValue();
//                    char[] charArr = bleCommands.bytesToHex(responseValue);
//                    String yPos = String.valueOf("" + charArr[14] + charArr[15] + charArr[16] + charArr[17] + charArr[18] + charArr[19] + charArr[20] + charArr[21]);
//                    Log.d("MARK", "yPos  == " + yPos);
//                    Log.d("MARK", "motorRingType  == " + motorRingType);
//                    Log.d("MARK", "onCharacteristicChanged characteristic.getValue() = " + new String(bleCommands.bytesToHex(characteristic.getValue())));
//                    if (motorRingType == 2) {
//                        dataHasCome = false;
//                        bleCommands.topRing();
//                        motorRingType = 3;
//                    } else if (motorRingType == 3) {
////                dataHasCome = true;
////                bleCommands.rotateRight();
////                motorRingType = 4;
//
//                        act.runOnUiThread(new Runnable() {
//                    public void run() {
//                        Handler handler = new Handler();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                dataHasCome = true;
//                                bleCommands.rotateRight();
//                                motorRingType = 4;
//
//                                Timber.d("onCharacteristicChanged motorRingType 3");
//                            }
//                        }, 2000);
//                    }
//                });
//            } else if (motorRingType == 4) {
//                dataHasCome = false;
//                bleCommands.bottomRing();
//                motorRingType = 5;
//            } else if (motorRingType == 5) {
////                dataHasCome = true;
////                bleCommands.rotateRight();
////                motorRingType = 6;
//                act.runOnUiThread(new Runnable() {
//                    public void run() {
//                        Handler handler = new Handler();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                dataHasCome = true;
//                                bleCommands.rotateRight();
//                                motorRingType = 6;
//
//                                Timber.d("onCharacteristicChanged motorRingType 5");
//                            }
//                        }, 2000);
//                    }
//                });
//            } else if (motorRingType == 6) {
//                dataHasCome = false;
//                bleCommands.topRing();
//                motorRingType = 0;
//            }
//        }
//
//        @Override
//        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
//                                     int status) {
//        }
//    };
//
//
    @Override
    public void finishSettingModeForRecording() {
        getSupportFragmentManager().beginTransaction().remove(ringOptionFragment).commit();
        initalizeOverlay();
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Timber.d("onRequestPermissionsResult");
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Timber.d("Permission granted.");
                    initialzeRingOptions();

                } else {
                    Timber.d("Permission not granted.");
                    throw new RuntimeException("Need Camera!");
                }
                return;
            }
        }
    }
}
