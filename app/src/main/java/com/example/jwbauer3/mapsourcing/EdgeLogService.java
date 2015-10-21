package com.example.jwbauer3.mapsourcing;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import com.example.jwbauer3.mapsourcing.LogData;


/*Service handles the collection of sensor data in the background*/
public class EdgeLogService extends Service implements SensorEventListener{
    protected SensorManager sensorManager;
    private float currSteps;
    private float currDirection;
    private float currPressure;
    private float x, y, z;

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
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    //Logs direction and time when a step has been detected
    public void logData() {
        while(true) {
            try {Thread.sleep(100);}
            catch (InterruptedException e){e.printStackTrace();}
            LogData loggedData = new LogData(currSteps, currDirection, currPressure, System.currentTimeMillis(),
                    x, y, z);
            send(loggedData);
        }
    }

    public void send(LogData loggedData){
        //TODO ADD CODE to send object to SERVER
    }

    @Override
    public void onSensorChanged (SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {currSteps++;}
        else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {currDirection = event.values[0];}
        else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {currPressure = event.values[0];}
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {accellerometerInfo(event);}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    //detect whether a step has been taken
    //if so, increment the step counter
    public void accellerometerInfo(SensorEvent event){
        //movement
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

    }
}
