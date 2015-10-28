package com.example.jwbauer3.mapsourcing;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Binder;
import android.util.Log;
import com.example.jwbauer3.mapsourcing.LogData;

import java.util.ArrayList;


/*Service handles the collection of sensor data in the background*/
public class EdgeLogService extends Service implements SensorEventListener{
    protected SensorManager sensorManager;
    private float currSteps;
    private float currDirection;
    private float currPressure;
    private float[] acc, rot;
    private final IBinder mBinder = new LocalBinder();
    private ArrayList<LogData> currData = new ArrayList<>();

    public void onCreate() {
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                    acc = new float[3];
                    rot = new float[3];

                    sensorThread();
            }
        };
        thread.start();
    }

    public class LocalBinder extends Binder {
        EdgeLogService getService() {
            return EdgeLogService.this;
        }
    }


    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public ArrayList<LogData> getCurrData(){
        return currData;
    }

    public void clearData(){
        currData.clear();
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
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }


    //Logs direction and time when a step has been detected
    public void logData() {
          //  try {Thread.sleep(100);}
          //  catch (InterruptedException e){e.printStackTrace();}
            LogData loggedData = new LogData(currSteps, currDirection, currPressure,
                    System.currentTimeMillis(), acc, rot);
            currData.add(loggedData);
      //  }
    }

    public void send(LogData loggedData){
        //TODO ADD CODE to send object to SERVER
    }

    @Override
    public void onSensorChanged (SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            currSteps++;
            logData();
        }
        else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {currDirection = event.values[0];}
        else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {currPressure = event.values[0];}
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, acc, 0, event.values.length);
        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(event.values, 0, rot, 0, event.values.length);
            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
