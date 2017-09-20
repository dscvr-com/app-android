package com.iam360.dscvr.bluetooth;

import android.util.Log;

import com.iam360.dscvr.util.AutoResetEvent;

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
    private long currentStart;
    private double currentStepX;
    private double currentStepY;
    private double currentSpeedX;
    private double currentSpeedY;
    private double xPosition;
    private double yPosition;
    private AutoResetEvent event;

    public CommandWorker(BluetoothEngineControlService service) {
        this.service = service;
        this.xPosition = 0;
        this.currentStepX = 0;
        this.event = new AutoResetEvent(false);
    }

    public void setCommandPointsForNewRunnable(List<EngineCommandPoint> points) {
        if (lastSubmitted == null || lastSubmitted.isDone())
            lastSubmitted = executor.submit(new CommandWorkerRunnable(points));
        else{
            throw new  IllegalStateException("there is already a circle Command Running");
        }
    }

    public double getXPosition() {
        return xPosition + Math.signum(currentStepX) * Math.min(Math.abs(currentStepX), currentSpeedX * (System.currentTimeMillis() - currentStart) / 1000.0);
    }
    public double getYPosition() {
        return yPosition + Math.signum(currentStepY) * Math.min(Math.abs(currentStepY), currentSpeedY * (System.currentTimeMillis() - currentStart) / 1000.0);
    }

    public void notifyPictureProcessed() {
        event.set();
    }

    public void stop() {
        lastSubmitted.cancel(true);
    }


    private class CommandWorkerRunnable implements Runnable {
        private List<EngineCommandPoint> points;


        public CommandWorkerRunnable(List<EngineCommandPoint> points) {
            this.points = points;
        }

        @Override
        public void run() {
            try {
                xPosition = 0;
                //Thread.sleep(500);
                EngineCommandPoint last = points.get(points.size() - 1);
                for (EngineCommandPoint current : points) {
                    Log.d("COMMAND THREAD", "Waiting bla");
                    event.waitOne();
                    if(current != last) {
                        event.waitOne();
                    }
                    Log.d("COMMAND THREAD", "Continuing bla");
                    float timeNeededX = (current.getX() != 0f ? (Math.abs(current.getX()) * 1000f / BluetoothEngineControlService.SPEED) : 0f);
                    float timeNeededY = (current.getY() != 0f ? (Math.abs(current.getY()) * 1000f / BluetoothEngineControlService.SPEED) : 0f);
                    Log.d("COMMAND THREAD", String.format("timeX: %f, timeY: %f", timeNeededX, timeNeededY));

                    currentStart = System.currentTimeMillis();
                    currentStepX = current.getX();
                    currentStepY = current.getY();
                    currentSpeedX = BluetoothEngineControlService.SPEED;
                    currentSpeedY = BluetoothEngineControlService.SPEED;
                    service.moveXY(current, BluetoothEngineControlService.SPEEDPOINT);
                    Thread.sleep((long) Math.max(timeNeededX, timeNeededY));
                    currentStepX = 0;
                    currentStepY = 0;
                    xPosition += current.getX();
                    yPosition += current.getY();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }


    }
}
