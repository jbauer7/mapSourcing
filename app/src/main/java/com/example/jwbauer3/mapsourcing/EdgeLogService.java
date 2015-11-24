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
    private float currDegree;
    private float prevDegreeTime;
    private float prevDegreeChange;
    private int prevDegreeRange;
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
                    //todo: get correct floor

                //todo: hardcoded that this is a non-stair node
                    firstNode = new Node(0,0,2,false);
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

    private void getInitialDirectionRange(float newDegree)
    {
        if(newDegree>=45 && newDegree<135) {

            prevDegreeRange = 1;
        } else if(newDegree>=135 && newDegree<225) {

            prevDegreeRange=2;
        } else if(newDegree>=225 && newDegree<315) {

            prevDegreeRange=3;
        } else {

            prevDegreeRange=4;
        }
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
            LogData loggedData = new LogData(currSteps, currDegree, System.currentTimeMillis());
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

    public void directionHandler(float newDegree) {
        //float change = prevDegreeChange;
        int degreeRange = 0;
        if (prevDegreeRange == 0)
        {
            getInitialDirectionRange(newDegree);
            return;
        }
        /* if (newDegree > currDegree)
        {
            change = change + (newDegree - currDegree);
        } else {
            change = change + (currDegree - newDegree);
        } */
        float currDegreeTime = System.currentTimeMillis();
        /* if ( !((currDegreeTime - prevDegreeTime) > 800) ) {
            return;
        } */
        /* if ( !((change > 30) && (currDegreeTime - prevDegreeTime) > 800) )
        {
            prevDegreeChange = change;
            return;
        }
        prevDegreeChange = 0; */
        int curr_x = 0;
        int curr_y = 0;

        if(newDegree>=45 && newDegree<135) {

            degreeRange = 1;
            curr_x=-1;
        } else if(newDegree>=135 && newDegree<225) {

            degreeRange=2;
            curr_y=1;
        } else if(newDegree>=225 && newDegree<315) {

            degreeRange=3;
            curr_x=1;
        } else {

            degreeRange=4;
            curr_y=-1;
        }

        if (prevDegreeRange == degreeRange) {
            return;
        }
        prevDegreeRange = degreeRange;


        //todo: hardcoded that this is a non-stair node
        Node newNode = new Node(prevNode.getxPos() + (int) currSteps*curr_x,
                prevNode.getyPos()+(int) currSteps*curr_y,2,false);

        Edge newEdge = new Edge(prevNode,newNode);
        newEdge.setWeight((int) currSteps);
        newEdge.setDirection((int) newDegree);
        prevNode.setEdges(newEdge);
        newNode.setEdges(newEdge);
        nodes.add(newNode);
        edges.add(newEdge);
        prevNode=newNode;
        currSteps = 0;
        Log.i("New Node", newNode.getxPos() + " , " + newNode.getyPos() +  " Dir: " + newDegree );

        //set values as needed
        currDegree = newDegree;
        prevDegreeTime = currDegreeTime;
    }

    public void directionHandler2(float degree){
        int curr_x = 0;
        int curr_y = 0;

        //Thresholds to determine direction in hallway
        if(degree>=45 && degree<135) curr_x=-1;
        else if(degree>=135 && degree<225) curr_y=1;
        else if(degree>=225 && degree<315) curr_x=1;
        else curr_y=-1;


        //todo: hardcoded that this is a non-stair node
        //if direction changes create a node
        if(curr_x != x || curr_y != y){
            Node newNode = new Node(prevNode.getxPos() + (int) currSteps*curr_x,
                    prevNode.getyPos()+(int) currSteps*curr_y,2,false);

            Edge newEdge = new Edge(prevNode,newNode);
            newEdge.setWeight((int) currSteps);
            newEdge.setDirection((int) degree);
            prevNode.setEdges(newEdge);
            newNode.setEdges(newEdge);
            nodes.add(newNode);
            edges.add(newEdge);
            prevNode=newNode;
            currSteps = 0;
            Log.i("New Node", newNode.getxPos() + " , " + newNode.getyPos() +  " Dir: " + degree );

        }

        x=curr_x;
        y=curr_y;
    }

    public ArrayList<Node> getNodes(){return nodes;}
    public ArrayList<Edge> getEdges(){ return edges;}
}
