package com.iam360.dscvr.bluetooth;

import android.util.Log;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Lotti on 7/2/2017.
 */

public class CommandWorker {

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Runnable runnable;

    private Queue<EngineCommandPoint> queue = new LinkedBlockingQueue<>();
    private BluetoothEngineControlService service;
    private static final String TAG = CommandWorker.class.getSimpleName();


    public CommandWorker(BluetoothEngineControlService service) {
        this.service = service;
        runnable = new CommandWorkerRunnable();
        executor.submit(runnable);
    }

    public void addEngineCommandPoint(EngineCommandPoint point) {
        queue.add(point);
    }


    private class CommandWorkerRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                final EngineCommandPoint current = queue.poll();
                if (current !=null){
                    float timeNeededX = (current.getX() != 0f ? ((float) BluetoothEngineControlService.SPEED) / current.getX() : 0f);
                    float timeNeededY = (current.getY() != 0f ? ((float) BluetoothEngineControlService.SPEED) / current.getY() : 0f);
                    service.moveXY(current, BluetoothEngineControlService.SPEEDPOINT);
                    Log.e(TAG, "moved");
                    try {
                        Log.e(TAG, "sleeped : " + max(timeNeededX, timeNeededY));
                            Thread.sleep((long) max(timeNeededX, timeNeededY));

                    } catch (InterruptedException e) {
                        Log.e(TAG, "interrupted", e);
                    }
                }
            }
        }

        private float max(float timeNeededX, float timeNeededY) {
            return timeNeededX > timeNeededY ? timeNeededX : timeNeededY;
        }

    }
}
