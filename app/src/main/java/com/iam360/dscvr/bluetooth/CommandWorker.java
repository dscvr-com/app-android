package com.iam360.dscvr.bluetooth;

import android.util.Log;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Created by Lotti on 7/2/2017.
 */

public class CommandWorker extends Thread {

    private Queue<EngineCommandPoint> queue = new LinkedBlockingQueue<>();
    private BluetoothEngineControlService service;
    private Timer timer;
    private static final String TAG = CommandWorker.class.getSimpleName();
    private Semaphore semaphore = new Semaphore(1);


    public CommandWorker(BluetoothEngineControlService service) {
        this.service = service;
        this.timer = new Timer("CommandTimer");
    }

    public void addEngineCommandPoint(EngineCommandPoint point) {
        queue.add(point);
        if (this.getState() == State.WAITING) {
            synchronized (this) {
                this.notify();
            }
        }
    }

    @Override
    public void run() {
        float timeNeededX;
        float timeNeededY;
        while (true) {
            final EngineCommandPoint current = queue.poll();
            if (current == null) {
                lock();

            } else {
                Log.d(TAG, "currentPoint: " + current.getX() + " " + current.getY());
                timeNeededX = (current.getX()!= 0f? ((float) BluetoothEngineControlService.SPEED) / current.getX(): 0f);
                timeNeededY = (current.getY()!= 0f? ((float) BluetoothEngineControlService.SPEED) / current.getY() : 0f);
                Log.d(TAG, "time Needed: x,y" + timeNeededX + " " + timeNeededY);
                service.moveXY(current, BluetoothEngineControlService.SPEEDPOINT);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        synchronized (CommandWorker.this) {
                            CommandWorker.this.notify();
                        }
                    }
                }, ((long) max(timeNeededX, timeNeededY)));
                lock();
            }
        }
    }

    private void lock() {
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "problem pausing thread", e);
            }
        }
    }

    private float max(float timeNeededX, float timeNeededY) {
        return timeNeededX > timeNeededY ? timeNeededX : timeNeededY;
    }
}
