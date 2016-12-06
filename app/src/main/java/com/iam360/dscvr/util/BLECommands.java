package com.iam360.dscvr.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.iam360.dscvr.R;

import java.util.UUID;

public class BLECommands {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattService mBluetoothService;
    private Activity mMainActivity;
    private UUID mServiceUIID;
    private UUID mWriteUIID;

    public BLECommands(BluetoothAdapter mBluetoothAdapter, BluetoothGatt mBluetoothGatt, BluetoothGattService mBluetoothService, Activity mMainActivity){
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.mBluetoothGatt = mBluetoothGatt;
        this.mBluetoothService = mBluetoothService;
        this.mMainActivity = mMainActivity;
        this.mServiceUIID = UUID.fromString(mMainActivity.getString(R.string.bluetooth_serviceuuidlong));
        this.mWriteUIID = UUID.fromString(mMainActivity.getString(R.string.bluetooth_characteristic_write));
    }

    public static char[] bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return hexChars;
    }

    public void topRing(){
        String data = "fe0702fffff9f7012c00";
        data += CalculateCheckSum(hexStringToByteArray(data));
        data += "ffffffffffff";
        writeData(hexStringToByteArray(data));
    }
    public void bottomRing(){
        String data = "fe070200000c12012c00";
        data += CalculateCheckSum(hexStringToByteArray(data));
        data += "ffffffffffff";
        writeData(hexStringToByteArray(data));
    }
    public void rotateRight(){
//                String data = "fe070100001c48012c00"; //012c - 300 speed
//                String data = "fe070100001c48006400"; // 0064 - 100 speed
//                String data = "fe0701000013F7006400"; // changed number of steps
//                String data = "fe0701000013F700c800"; // changed number of steps
                String data = "fe0701000013ec00c800"; // changed number of steps
                data += CalculateCheckSum(hexStringToByteArray(data));
                data += "ffffffffffff";
                writeData(hexStringToByteArray(data));
    }

    public byte[] hexStringToByteArray(final String s) {
        mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mSendData.setText(s);
//                mSendDataContainer.setVisibility(View.VISIBLE);
            }
        });

        int len = s.length();

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public void writeData(byte[] data) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothService = mBluetoothGatt.getService(mServiceUIID);
        if(mBluetoothService != null){
            BluetoothGattCharacteristic characteristic = mBluetoothService.getCharacteristic(mWriteUIID);

            characteristic.setValue(data);
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }


    public String CalculateCheckSum( byte[] bytes ){
        short CheckSum = 0, i = 0;
        for( i = 0; i < bytes.length; i++){
            CheckSum += (short)(bytes[i] & 0xFF);
        }

        CheckSum = (short) ( CheckSum & 0xff);
        if(CheckSum < 9){
            return "0"+Integer.toHexString(CheckSum);
        }else{
            return Integer.toHexString(CheckSum);
        }
    }
}