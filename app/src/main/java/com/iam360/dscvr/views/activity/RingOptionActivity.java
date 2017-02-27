package com.iam360.dscvr.views.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.iam360.dscvr.R;
import com.iam360.dscvr.views.fragment.CameraPreviewFragment;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.GeneralUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import timber.log.Timber;

public class RingOptionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final int PERMISSION_REQUEST_CAMERA = 2;
    private static final int REQUEST_ENABLE_BT = 1;

    // Bluetooth scan timeout
    private static final long SCAN_PERIOD = 10000;
    private boolean mScanning;

    // background camera view
//    private RecorderPreviewView recordPreview;

    private TextView manualTxt;
    private TextView motorTxt;
    private ImageButton manualBtn;
    private ImageButton motorBtn;
    private ImageButton recordButton;

    private Cache cache;
    private Handler mHandler;

    // contains the names of scanned devices for layout rendering
    private ArrayAdapter<String> arrayAdapter;
    // holder of scanned devices (name and address)
    private ArrayList<BluetoothDevice> deviceList;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mBluetoothService;

    private UUID mServiceUIID;
    private UUID mResponesUIID;
    private UUID mNotifUUID;


    // Todo - please clean that code up.
    public void checkPermissionAndInitialize() {
        Timber.d("Checking permission.");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Timber.d("Please show explaination.");
                throw new RuntimeException("Not implemented!");

            } else {

                // No explanation needed, we can request the permission.

                Timber.d("Requesting permission.");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSION_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Timber.d("Permission granted.");
            initializeWithPermission();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring_option);

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.record_preview, CameraPreviewFragment.newInstance())
                    .commit();
        }

        checkPermissionAndInitialize();
    }

    void initializeWithPermission() {

        manualTxt = (TextView) findViewById(R.id.manual_text);
        motorTxt = (TextView) findViewById(R.id.motor_text);
        manualBtn = (ImageButton) findViewById(R.id.manual_button);
        motorBtn = (ImageButton) findViewById(R.id.motor_button);
        recordButton = (ImageButton) findViewById(R.id.record_button);

        GeneralUtils generalUtils = new GeneralUtils();
        generalUtils.setFont(this, manualTxt, Typeface.BOLD);
        generalUtils.setFont(this, motorTxt, Typeface.BOLD);

        cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);

        mHandler = new Handler();
        deviceList = new ArrayList<BluetoothDevice>();
        arrayAdapter = new ArrayAdapter<String>( RingOptionActivity.this, android.R.layout.select_dialog_item);

        mServiceUIID = UUID.fromString(getString(R.string.bluetooth_serviceuuidlong));
        mResponesUIID = UUID.fromString(getString(R.string.bluetooth_characteristic_response));
        mNotifUUID = UUID.fromString(getString(R.string.bluetooth_serviceuuid_notification));

        int mode = cache.getInt(Cache.CAMERA_MODE);
        updateMode((mode == Constants.ONE_RING_MODE) ? true : false);

        setDefMotorConfigs();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.manual_button:
                updateMode(true);
                break;
            case R.id.motor_button:
//                Snackbar.make(recordButton, "Motor mode available soon.", Snackbar.LENGTH_SHORT).show();
                updateMode(false);
                boolean permissionOK = checkBluetoothPermission();
                if(permissionOK) enableBluetooth();
                break;
            case R.id.record_button:
                Intent intent;
                intent = new Intent(RingOptionActivity.this, RecorderActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    /**
     * Updates 1 or 3 ring on layout and cache
     * @param isManualMode
     */
    private void updateMode(boolean isManualMode) {
        if(isManualMode) {
            manualBtn.setBackground(getResources().getDrawable(R.drawable.manual_icon_orange));
            motorBtn.setBackground(getResources().getDrawable(R.drawable.motor_icon));
            cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE);
        } else {
            manualBtn.setBackground(getResources().getDrawable(R.drawable.manual_icon));
            motorBtn.setBackground(getResources().getDrawable(R.drawable.motor_icon_orange));
            cache.save(Cache.CAMERA_MODE, Constants.THREE_RING_MODE);
        }
    }

    /**
     *
     * @return true if connected to bluetooth, false if not
     */
    private boolean checkBluetoothPermission() {
        Timber.d("checkBluetoothPermission");
        boolean permission = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Timber.d("Permission not granted");

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    }
                });
                builder.show();
            } else {
                Timber.d("Permission granted.");
            }
        }

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Not supported." , Toast.LENGTH_SHORT).show();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Not supported", Toast.LENGTH_SHORT).show();
            permission = false;
        } else permission = true;

        Timber.d("Permission : " + permission);
        return permission;
    }

    private void enableBluetooth() {
        Timber.d("enableBluetooth");
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else scanLeDevice(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Timber.d("onRequestPermissionsResult");


        initializeWithPermission();
//        switch (requestCode) {
//            case PERMISSION_REQUEST_COARSE_LOCATION: {
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                } else {
//                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                    builder.setTitle("Functionality limited");
//                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
//                    builder.setPositiveButton(android.R.string.ok, null);
//                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                        }
//
//                    });
//                    builder.show();
//                }
//            }
//            case PERMISSION_REQUEST_CAMERA: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    Timber.d("Permission granted.");
//                    initializeWithPermission();
//
//                } else {
//                    Timber.d("Permission not granted.");
//                    throw new RuntimeException("Need Camera!");
//                }
//                return;
//            }
//        }
    }

    private void scanLeDevice(final boolean enable) {

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                Timber.d("Paired Device : " + device.getName());
//                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }

        if (enable) {
            arrayAdapter.clear();
            deviceList.clear();
            showDeviceList();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Timber.d("Scanner Device : " + device.getName() + " " + device.getBondState());
                            // check if device is already on the list
                            if(device.getName() != null) {
                                if(arrayAdapter.getPosition(device.getName()) < 0) {
                                    arrayAdapter.add(device.getName());
                                    arrayAdapter.notifyDataSetChanged();
                                    deviceList.add(device);
                                }
                            }
                        }
                    });
                }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult : " + requestCode + " " + resultCode);
        // User chose not to enable Bluetooth.
        // switch back to manual mode
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            updateMode(true);
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            // when bluetooth is turned on, start scanning for device
            scanLeDevice(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showDeviceList() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(RingOptionActivity.this);
        alertBuilder.setTitle(R.string.bluetooth_searching);
        alertBuilder.setNegativeButton(R.string.cancel_label,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertBuilder.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strOS = arrayAdapter.getItem(which);

//                        if (mBluetoothGatt == null) {
                        cache.save(Cache.BLE_DEVICE_ADDRESS, deviceList.get(which).getAddress());
//                        Timber.d("mBluetoothGatt == null");
//                        mBluetoothGatt = deviceList.get(which).connectGatt(RingOptionActivity.this, false, gattCallback);
//                        }
                        dialog.dismiss();
                    }
                });

        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // stop scanning when uses cancels
                scanLeDevice(false);
            }
        });
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Timber.d("mBluetoothGatt Connected");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Timber.d("mBluetoothGatt Disconnected");
                    break;
                default:
                    Timber.d("mBluetoothGatt default");
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
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            Timber.d("onCharacteristicChanged characteristic = "+characteristic.getUuid());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    mResponseData.setText(new String(bytesToHex(characteristic.getValue())));
                }
            });
            //motorRing type = (0:stop, 1:1st ring rotate, 2:toTop 2nd, 3:3rd ring rotate, 4:toBot, 5:toBot, 6:last)
//            byte[] responseValue = characteristic.getValue();
//            char[] charArr = bleCommands.bytesToHex(responseValue);
//            String yPos = String.valueOf(""+charArr[14] + charArr[15] + charArr[16] + charArr[17] + charArr[18] + charArr[19] + charArr[20] + charArr[21]);
//            Log.d("MARK","yPos  == "+yPos);
//            Log.d("MARK","motorRingType  == "+motorRingType);
//            Log.d("MARK","onCharacteristicChanged characteristic.getValue() = "+new String(bleCommands.bytesToHex(characteristic.getValue())));
//            if(motorRingType == 2) {
//                Log.d("MARK2","motorRingType = "+System.currentTimeMillis() / 1000.0);
//                dataHasCome = false;
//                bleCommands.topRing();
//                motorRingType = 3;
//            }else if(motorRingType == 3){
//                dataHasCome = true;
//                bleCommands.rotateRight();
//                motorRingType = 4;
//            }else if(motorRingType == 4){
//                dataHasCome = false;
//                bleCommands.bottomRing();
//                motorRingType = 5;
//            }else if(motorRingType == 5){
//                dataHasCome = true;
//                bleCommands.rotateRight();
//                motorRingType = 6;
//            }else if(motorRingType == 6){
//                dataHasCome = false;
//                bleCommands.topRing();
//                motorRingType = 0;
//            }
        }
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {}
    };

//    private void getMotorConfig(){
//        api2Consumer.getMotorConfig(new retrofit.Callback<List<MotorConfig>>() {
//            @Override
//            public void onResponse(Response<List<MotorConfig>> response, Retrofit retrofit) {
//                if (response.isSuccess()) {
//                    List<MotorConfig> motorConfigs = response.body();
//                    for(int a=0; a < motorConfigs.size(); a++){
////                        Timber.d("getMotorConfig getMotor_configuration_mobile_platform : "+motorConfigs.get(a).getMotor_configuration_mobile_platform());
//                        if(motorConfigs.get(a).getMotor_configuration_mobile_platform().equals("Android")){
////                            Timber.d("getMotorConfig getMotor_configuration_rotate_count : "+motorConfigs.get(a).getMotor_configuration_rotate_count());
////                            Timber.d("getMotorConfig getMotor_configuration_bot_count : "+motorConfigs.get(a).getMotor_configuration_bot_count());
////                            Timber.d("getMotorConfig getMotor_configuration_top_count : "+motorConfigs.get(a).getMotor_configuration_top_count());
////                            Timber.d("getMotorConfig getMotor_configuration_pulse_per_second : "+motorConfigs.get(a).getMotor_configuration_pulse_per_second());
////                            Timber.d("getMotorConfig getMotor_configuration_buff_count : "+motorConfigs.get(a).getMotor_configuration_buff_count());
//
//                            cache.save(Cache.BLE_ROT_COUNT, motorConfigs.get(a).getMotor_configuration_rotate_count());
//                            cache.save(Cache.BLE_BOT_COUNT, motorConfigs.get(a).getMotor_configuration_bot_count());
//                            cache.save(Cache.BLE_TOP_COUNT, motorConfigs.get(a).getMotor_configuration_top_count());
//                            cache.save(Cache.BLE_PPS_COUNT, motorConfigs.get(a).getMotor_configuration_pulse_per_second());
//                            cache.save(Cache.BLE_BUF_COUNT, motorConfigs.get(a).getMotor_configuration_buff_count());
//                        }
//                    }
//                }else{
//                    Timber.d("getMotorConfig failed : "+response.message().toString());
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                Timber.d("getMotorConfig failed : "+t.getMessage().toString());
//            }
//        });
//    }

    private void setDefMotorConfigs(){
        cache.save(Cache.BLE_ROT_COUNT, "5111");
        cache.save(Cache.BLE_BOT_COUNT, "61538");
        cache.save(Cache.BLE_TOP_COUNT, "2000");
        cache.save(Cache.BLE_PPS_COUNT, "100");
        cache.save(Cache.BLE_BUF_COUNT, "0");

//        getMotorConfig();
    }
}
