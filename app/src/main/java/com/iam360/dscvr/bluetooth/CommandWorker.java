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
    private Future<?> lastSubmitted = null;
    private BluetoothEngineControlService service;
    private static final String TAG = CommandWorker.class.getSimpleName();
    private double timeForOldCommands = 0d;
    private double currentSystimeOfDot = 0d;

    public double getTimeForOldCommands() {
        return timeForOldCommands + (currentSystimeOfDot - System.currentTimeMillis())/1000f;
    }

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
            timeForOldCommands = 0d;
            currentSystimeOfDot = System.currentTimeMillis();
            for (EngineCommandPoint current : points) {
                float timeNeededX = (current.getX() != 0f ? (current.getX() / BluetoothEngineControlService.SPEED) * 1000f: 0f);
                float timeNeededY = (current.getY() != 0f ? (current.getY() / BluetoothEngineControlService.SPEED) * 1000f: 0f);
                service.moveXY(current, BluetoothEngineControlService.SPEEDPOINT);
                currentSystimeOfDot = System.currentTimeMillis();
                try {
                    Thread.sleep((long) max(timeNeededX + 500, timeNeededY));

                } catch (InterruptedException e) {
                    Log.e(TAG, "interrupted!", e);
                }
                timeForOldCommands += timeNeededX;
            }
        }

        private float max(float timeNeededX, float timeNeededY) {
            return timeNeededX > timeNeededY ? timeNeededX : timeNeededY;
        }

    }
}
