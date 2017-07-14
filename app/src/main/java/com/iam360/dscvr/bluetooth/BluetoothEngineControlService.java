package com.iam360.dscvr.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;
import android.util.Log;

import com.iam360.dscvr.sensors.RotationMatrixProvider;
import com.iam360.dscvr.util.Maths;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

/**
 * Class to control Motor.
 * Can send MotorCommands to the motor
 * Created by Charlotte on 21.11.2016.
 */
public class BluetoothEngineControlService {

    public static final ParcelUuid SERVICE_UUID = ParcelUuid.fromString("69400001-B5A3-F393-E0A9-E50E24DCCA99");
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("69400002-B5A3-F393-E0A9-E50E24DCCA99");
    public static final UUID RESPONSE_UUID = UUID.fromString("69400003-B5A3-F393-E0A9-E50E24DCCA99");
    protected static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final byte[] TOPBUTTON = new byte[]{(byte) 0xFE, 0x01, (byte) 0x08, (byte) 0x01, (byte) 0x08, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    public static final byte[] BOTTOMBUTTON = new byte[]{(byte) 0xFE, 0x01, 0x08,  0x00, 0x07, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    private static final String TAG = "MotorControl";
    private static final double STEPS_FOR_ONE_ROUND_X = 5111;
    private static final double STEPS_FOR_ONE_ROUND_Y = 15000;
    private static final int STEP_FOR_360 = (int) ((STEPS_FOR_ONE_ROUND_X / 360f) * 380f);


    private CommandWorker worker;

    private BluetoothGattService bluetoothService;
    private BluetoothGatt gatt;
    private EngineCommandPoint movedSteps = new EngineCommandPoint(0, 0);
    private BluetoothEngineMatrixProvider providerInstanz;
    private double yTeta = 0;
    public static final int SPEED = 500;
    public static final EngineCommandPoint SPEEDPOINT = new EngineCommandPoint(SPEED, SPEED);

    public BluetoothEngineControlService() {
        worker = new CommandWorker(this);
    }

    public boolean setBluetoothGatt(BluetoothGatt gatt) {
        if (gatt == null && this.hasBluetoothService()) {
            stop();
        }

        if (gatt == null) {
            bluetoothService = null;
            this.gatt = null;
            return false;
        }

        // set: bluetoothService
        List<BluetoothGattService> services = gatt.getServices();
        Log.i("onServicesDiscovered: ", services.toString());
        BluetoothGattService correctService = null;
        for (BluetoothGattService service : services) {
            if (service.getUuid().equals(SERVICE_UUID.getUuid())) {
                correctService = service;
                break;
            }
        }
        if (correctService == null) {
            return false;
        } else {
            this.gatt = gatt;
            this.bluetoothService = correctService;
            BluetoothGattCharacteristic characteristic = bluetoothService.getCharacteristic(RESPONSE_UUID);
            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE );
            gatt.writeDescriptor(descriptor);
            return true;
        }
    }

    public boolean hasBluetoothService() {
        return bluetoothService != null && gatt != null;
    }

    private void sendCommand(EngineCommand command) {
        BluetoothGattCharacteristic characteristic = bluetoothService.getCharacteristic(CHARACTERISTIC_UUID);
        assert (((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0);
        characteristic.setValue(command.getValue());
        gatt.writeCharacteristic(characteristic);
    }


    public void moveXY(EngineCommandPoint steps, EngineCommandPoint speed) {
        EngineCommand command = EngineCommand.moveXY(steps, speed);
        movedSteps.add(steps);
        sendCommand(command);

    }

    public void createCommands(List<EngineCommandPoint> pointsInDeg){
        List<EngineCommandPoint> pointsInStep = new ArrayList<>();
        for(EngineCommandPoint point: pointsInDeg) {

            EngineCommandPoint stepspoint = new EngineCommandPoint((float) (STEPS_FOR_ONE_ROUND_X / 360) * point.getX(), (float) (STEPS_FOR_ONE_ROUND_Y / 180) * point.getY());
            stepspoint.mul(-1);
            pointsInStep.add(stepspoint);

        }
        worker.setCommandPointsForNewRunnable(pointsInStep);
    }

    private void stop() {
        sendCommand(EngineCommand.stop());
    }

    public BluetoothEngineMatrixProvider getBluetoothEngineMatrixProviderForGatt() {
        if (gatt != null) {
            if (providerInstanz == null) {
                providerInstanz = new BluetoothEngineMatrixProvider();
            }
            return providerInstanz;
        }
        return null;
    }





    public class BluetoothEngineMatrixProvider extends RotationMatrixProvider {
        @Override
        public void getRotationMatrix(float[] target) {
            double xPhi = worker.getTimeForOldCommands()*SPEED / (STEPS_FOR_ONE_ROUND_X/360d);
            Timber.d("xPhi: " + xPhi);
            float[] rotationX = {(float) yTeta + 180, 1, 0, 0};
            float[] rotationY = {(float) Math.toDegrees(xPhi), 0, 1, 0};
            float[] result = Maths.buildRotationMatrix(rotationY, rotationX);
            System.arraycopy(result, 0, target, 0, 16);
        }
    }
}
