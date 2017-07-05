package com.iam360.dscvr.bluetooth;

import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Lotti on 7/2/2017.
 */

public class CommandWorker {

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Runnable runnable;
    private Future<?> lastSubmitted = null;
    private BluetoothEngineControlService service;
    private static final String TAG = CommandWorker.class.getSimpleName();


    public CommandWorker(BluetoothEngineControlService service) {
        this.service = service;
    }

    public void setCommandPointsForNewRunnable(List<EngineCommandPoint> points) {
        if (lastSubmitted == null || lastSubmitted.isDone())
            lastSubmitted = executor.submit(new CommandWorkerRunnable(points));
        else{
            throw new  IllegalStateException("there is already a circle Command Running");
        }
    }


    private class CommandWorkerRunnable implements Runnable {
        private List<EngineCommandPoint> points;


        public CommandWorkerRunnable(List<EngineCommandPoint> points) {
            this.points = points;
        }

        @Override
        public void run() {
            for (EngineCommandPoint current : points) {
                float timeNeededX = (current.getX() != 0f ? ((float) BluetoothEngineControlService.SPEED *1000) / current.getX() : 0f);
                float timeNeededY = (current.getY() != 0f ? ((float) BluetoothEngineControlService.SPEED *1000) / current.getY() : 0f);
                service.moveXY(current, BluetoothEngineControlService.SPEEDPOINT);
                try {
                    Thread.sleep((long) max(timeNeededX, timeNeededY) );

                } catch (InterruptedException e) {
                    Log.e(TAG, "interrupted!", e);
                }
            }
        }

        private float max(float timeNeededX, float timeNeededY) {
            return timeNeededX > timeNeededY ? timeNeededX : timeNeededY;
        }

    }
}
