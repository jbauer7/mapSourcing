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
    //private float currPressure;
    //private float[] acc; //rot;
    private final IBinder mBinder = new LocalBinder();
    private ArrayList<LogData> currData = new ArrayList<>();
    boolean lock = false;
    int x,y;
    Node firstNode, prevNode;
    ArrayList<Node> nodes;
    ArrayList<Edge> edges;

    public void onCreate() {
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                    //acc = new float[3];
                    //rot = new float[3];
                    firstNode = new Node(0,0);
                    nodes = new ArrayList<Node>();
                    edges = new ArrayList<Edge>();
                    nodes.add(firstNode);
                    prevNode=firstNode;
                    sensorThread();
                    logData();
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
        currSteps=0;
        currData.clear();
    }

    public void sensorThread() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
        //sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
       // sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),SensorManager.SENSOR_DELAY_NORMAL);
    }


    //Logs direction and time when a step has been detected
    public void logData() {
       while(true){
        try {Thread.sleep(100);}
            catch (InterruptedException e){e.printStackTrace();}
            LogData loggedData = new LogData(currSteps, currDirection, System.currentTimeMillis());
            currData.add(loggedData);
        }
    }

    //code from minilab 4 part 2.  It is a step detector based on the readings
    //from the accelerometer
    private void checkStep(SensorEvent event) {
        // Movement
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        if (accelationSquareRoot >= 1.3) //
        {
            if(!lock){
                currSteps++;
                lock = true;
            }
        }
        else{
            lock = false;
        }
    }

    public void send(LogData loggedData){
        //TODO ADD CODE to send object to SERVER
    }

    @Override
    public void onSensorChanged (SensorEvent event) {
        //if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {currSteps++;}
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {directionHandler(event.values[0]);}
        //else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {currPressure = event.values[0];}
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //System.arraycopy(event.values, 0, acc, 0, event.values.length);
            checkStep(event);
        }
        //else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {System.arraycopy(event.values, 0, rot, 0, event.values.length);}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void directionHandler(float degree){
        int curr_x = 0;
        int curr_y = 0;

        //Thresholds to determine direction in hallway
        if(degree>=45 && degree<135) curr_x=-1;
        else if(degree>=135 && degree<225) curr_y=1;
        else if(degree>=225 && degree<315) curr_x=1;
        else curr_y=-1;

        //if direction changes create a node
        if(curr_x != x || curr_y != y){
            Node newNode = new Node(prevNode.getxPos() + (int) currSteps*curr_x,
                    prevNode.getyPos()+(int) currSteps*curr_y);
            Edge newEdge = new Edge(prevNode,newNode);
            newEdge.setWeight((int) currSteps);
            newEdge.setDirection((int) degree);
            prevNode.setEdges(newEdge);
            newNode.setEdges(newEdge);
            nodes.add(newNode);
            edges.add(newEdge);
            prevNode=newNode;
        }

        x=curr_x;
        y=curr_y;
    }

    public ArrayList<Node> getNodes(){return nodes;}
    public ArrayList<Edge> getEdges(){ return edges;}
}
