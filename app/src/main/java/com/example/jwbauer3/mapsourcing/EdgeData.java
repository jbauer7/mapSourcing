package com.example.jwbauer3.mapsourcing;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;

/**
 * Created by njaunich on 10/18/15.
 */
public class EdgeData extends Activity implements SensorEventListener {
    protected ArrayList<Float> stepsThusFar;
    protected ArrayList<Long> timestamps;
    protected ArrayList<Float> direction;
    protected SensorManager sensorManager;

    private float currSteps;
    private float currDirection;

    private boolean logging;


    public EdgeData(){
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
    public void logData() {
        //get spinlock
        logging = true;

        if (stepsThusFar.size() == 0)
        {
            stepsThusFar.add(currSteps);
        } else {
            stepsThusFar.add(stepsThusFar.get(stepsThusFar.size()-1) + currSteps);
        }

        currSteps = 0;
        direction.add(currDirection);
        timestamps.add(System.currentTimeMillis());

        //release spin lock
        logging = false;
    }

    @Override
    public void onSensorChanged (SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            while (logging == true)
            {
                //spin
            }
            currSteps++;
        }
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            while (logging == true)
            {
                //spin
            }
            currDirection = event.values[0];
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
