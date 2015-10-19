package com.example.jwbauer3.mapsourcing;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
/*Service handles the collection of sensor data in the background*/
public class EdgeLogService extends Service implements SensorEventListener{
    protected ArrayList<Float> stepsThusFar;
    protected ArrayList<Long> timestamps;
    protected ArrayList<Float> direction;
    protected SensorManager sensorManager;
    private float currSteps;
    private float currDirection;

    public void onCreate() {
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                    sensorThread();
                    logData();
            }
        };
        thread.start();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void sensorThread() {
        this.stepsThusFar = new ArrayList<>();
        this.timestamps = new ArrayList<>();
        this.direction = new ArrayList<>();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    //Logs direction and time when a step has been detected
    public void logData() {
        for(int i = 0; i<10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stepsThusFar.add(currSteps);
            direction.add(currDirection);
            timestamps.add(System.currentTimeMillis());
        }
    }

    @Override
    public void onSensorChanged (SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            currSteps++;
        }
        else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            currDirection = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
