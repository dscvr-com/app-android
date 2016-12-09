package com.iam360.dscvr.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.iam360.dscvr.R;

import java.util.UUID;

import timber.log.Timber;

public class BLECommands {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattService mBluetoothService;
    private Activity mMainActivity;
    private UUID mServiceUIID;
    private UUID mWriteUIID;

    private Cache cache;

    public BLECommands(BluetoothAdapter mBluetoothAdapter, BluetoothGatt mBluetoothGatt, BluetoothGattService mBluetoothService, Activity mMainActivity){
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.mBluetoothGatt = mBluetoothGatt;
        this.mBluetoothService = mBluetoothService;
        this.mMainActivity = mMainActivity;
        this.mServiceUIID = UUID.fromString(mMainActivity.getString(R.string.bluetooth_serviceuuidlong));
        this.mWriteUIID = UUID.fromString(mMainActivity.getString(R.string.bluetooth_characteristic_write));

        cache = Cache.open();
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
//        String data = "fe070200000c12012c00";
        String data = "fe07";
        data += "02"; //motor type
        data += "0000"; //
        data += convertToHex(Integer.parseInt(cache.getString(Cache.BLE_TOP_COUNT)));//"07cf"; //rotate count
        data += convertToHex(Integer.parseInt(cache.getString(Cache.BLE_PPS_COUNT)));//"012c"; //speed
        data += "00"; //steps
        data += CalculateCheckSum(hexStringToByteArray(data)); //crc
        data += "ffffffffffff"; //padding

        Timber.d("topRing = "+data);

//        fe0702000007cf00640041ffffffffffff
//        fe0702000007cf0012c00affffffffffff

        writeData(hexStringToByteArray(data));
//        writeData(hexStringToByteArray("fe0702000007cf00640041ffffffffffff"));
    }
    public void bottomRing(){
//        String data = "fe0702fffff060012c00";

        String data = "fe07";
        data += "02"; //motor type
        data += "ffff"; //
        data += convertToHex(Integer.parseInt(cache.getString(Cache.BLE_BOT_COUNT)));//"f062"; //rotate count
        data += convertToHex(Integer.parseInt(cache.getString(Cache.BLE_PPS_COUNT)));//"012c"; //speed
        data += "00"; //steps

        data += CalculateCheckSum(hexStringToByteArray(data));
        data += "ffffffffffff";

        Timber.d("bottomRing = "+data);

        writeData(hexStringToByteArray(data));
//        writeData(hexStringToByteArray("fe0702fffff062006400bbffffffffffff"));
    }
    public void rotateRight(){
        String data = "fe07";
        data += "01"; //motor type
        data += "0000"; //
        data += convertToHex(Integer.parseInt(cache.getString(Cache.BLE_ROT_COUNT)));//"13f7"; //rotate count
        data += convertToHex(Integer.parseInt(cache.getString(Cache.BLE_PPS_COUNT)));//"00c8"; //speed; PPS
        data += "00"; //steps

        Timber.d("rotateRight = "+convertToHex(200));
        data += CalculateCheckSum(hexStringToByteArray(data));
        data += "ffffffffffff";
        writeData(hexStringToByteArray(data));
//        writeData(hexStringToByteArray("fe0701000013f700640074ffffffffffff"));
    }

    private byte[] hexStringToByteArray(final String s) {

        int len = s.length();

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private void writeData(byte[] data) {
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

    private String CalculateCheckSum(byte[] bytes){
        short CheckSum = 0, i;
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

    private String convertToHex(int val){
        String retVal = Integer.toString(val, 16);

        int pad = (4 - retVal.length());
        for(int i = 1; i <= pad; i++){
            retVal = "0"+retVal;
        }
        Timber.d("convertToHex = "+retVal);
        return retVal;
    }
}