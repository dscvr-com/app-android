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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring_option);

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.record_preview, CameraPreviewFragment.newInstance())
                    .commit();
        }

        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter1);
        this.registerReceiver(mReceiver, filter2);
        this.registerReceiver(mReceiver, filter3);

        checkPermissionAndInitialize();
    }

    // Todo - please clean that code up.
    public void checkPermissionAndInitialize() {
        Timber.d("Checking permission.");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Timber.d("Please show explaination.");
                throw new RuntimeException("Not implemented!");

            } else {

                // No explanation needed, we can request the permission.
                Timber.d("Requesting permission.");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Timber.d("Permission granted.");
            initializeWithPermission();
        }
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
                break;
            case R.id.record_button:
                if(cache.getInt(Cache.CAMERA_MODE) == Constants.ONE_RING_MODE) {
                    Intent intent;
                    intent = new Intent(RingOptionActivity.this, RecorderActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    boolean permissionOK = checkBluetoothPermission();
                    if(permissionOK)
                        enableBluetooth();
                }
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
        boolean permission = false;

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Not supported." , Toast.LENGTH_SHORT).show();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Not supported", Toast.LENGTH_SHORT).show();
            permission = false;
        } else permission = true;

        return permission;
    }

    private void enableBluetooth() {
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

        initializeWithPermission();
    }

    private void scanLeDevice(final boolean enable) {

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
//                Timber.d("Paired Device : " + device.getName());
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
                        cache.save(Cache.BLE_DEVICE_ADDRESS, deviceList.get(which).getAddress());
                        dialog.dismiss();

                        Intent intent;
                        intent = new Intent(RingOptionActivity.this, RecorderActivity.class);
                        startActivity(intent);
                        finish();

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

    private void setDefMotorConfigs(){

        cache.save(Cache.BLE_ROT_COUNT, "5111");
        cache.save(Cache.BLE_BOT_COUNT, "-3998");
        cache.save(Cache.BLE_TOP_COUNT, "1999");
        cache.save(Cache.BLE_PPS_COUNT, "250");
        cache.save(Cache.BLE_BUF_COUNT, "20");
    }

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Do something if connected
//                Toast.makeText(getApplicationContext(), "BT Connected", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Do something if disconnected
//                Toast.makeText(getApplicationContext(), "BT Disconnected", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
