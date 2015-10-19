package com.example.jwbauer3.mapsourcing;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
/*Service handles the collection of sensor data in the background*/
public class EdgeLogService extends IntentService implements SensorEventListener{
    protected ArrayList<Float> stepsThusFar;
    protected ArrayList<Long> timestamps;
    protected ArrayList<Float> direction;
    protected SensorManager sensorManager;
    private float currSteps;
    private float currDirection;


    public EdgeLogService(){
        super("EdgeLogService");
    }

    protected void onHandleIntent(Intent workIntent) {
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
            stepsThusFar.add(currSteps);
            direction.add(currDirection);
            timestamps.add(System.currentTimeMillis());
           // We ended up printing to the log to test
           // Log.i("data", "steps"+currSteps+" direction"+ currDirection);
    }


    @Override
    public void onSensorChanged (SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            currSteps++;
            //we decided to save everything to the log for each step
            logData();
        }
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            currDirection = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
