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
public class EdgeLogService extends Service implements SensorEventListener {
    protected SensorManager sensorManager;
    private float currSteps;
    private float currDegree;
    int xPos = 0;
    int yPos = 0;
    int prev_x=0;
    int prev_y=1;


    //Introducted by Nick
    private int prevDegreeRange;
    private int degreeRangeChangedCount;
    private float degreeOffset;
    private boolean noOffset =true;

    private final IBinder mBinder = new LocalBinder();
    private ArrayList<LogData> currData = new ArrayList<>();
    boolean lock = false;
    Node firstNode, prevNode;
    ArrayList<Node> nodes;
    ArrayList<Edge> edges;

    public void onCreate() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                sensorThread();
                while(noOffset)
                    ;   //spin

                //TODO: Add floor logic (currently forced to floor 2 for EHall
                //TODO: Add stair node logic (currently set to false)
                firstNode = new Node(0, 0, 2, false);
                nodes = new ArrayList<>();
                edges = new ArrayList<>();
                nodes.add(firstNode);
                prevNode = firstNode;
                logData();
            }
        };
        thread.start();
    }

    //public void onDestroy(){
     //   unregisterReceiver(receiver);
    //}

    public class LocalBinder extends Binder {
        EdgeLogService getService() {
            return EdgeLogService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            directionHandler(event.values[0]);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            checkStep(event);
        }
    }

    public void sensorThread() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    //Logs direction and time when a step has been detected
    public void logData() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

        float accelerationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        if (accelerationSquareRoot >= 1.3) //
        {
            if (!lock) {
                currSteps++;
                lock = true;
            }
        } else {
            lock = false;
        }
    }

    private void getInitialDirectionRange(float newDegree) {
        //  degreeOffset = -270; // These will have to be calculated on a building by building basis
        float currDegreeOffset = 0;
        if ((newDegree + degreeOffset) < 0) {
            currDegreeOffset = (360 + degreeOffset);
        }
        if ((newDegree + currDegreeOffset)%360  >= 45 && (newDegree + currDegreeOffset)%360  < 135) { // East
            prevDegreeRange = 1;
        } else if ((newDegree + currDegreeOffset)%360  >= 135 && (newDegree + currDegreeOffset)%360  < 225) { // South
            prevDegreeRange = 2;
        } else if ((newDegree + currDegreeOffset)%360  >= 225 && (newDegree + currDegreeOffset)%360  < 315) { // West
            prevDegreeRange = 3;
        } else { // North
            prevDegreeRange = 4;
        }
    }

    public void directionHandler(float newDegree) {
        //float change = prevDegreeChange;
        int degreeRange;
        int curr_x = 0;
        int curr_y = 0;
        float currDegreeOffset;
        boolean addNewNode = false;
        //setting initial degree offset

        if(noOffset){
            degreeOffset=360-newDegree;
            noOffset=false;
            return;
        }
        if (prevDegreeRange == 0) {
            getInitialDirectionRange(newDegree);
            return;
        }
        currDegreeOffset = degreeOffset;
        Log.i("offset", Float.toString(currDegreeOffset));

        //Thresholds are based on standard direction and an offset to match building map
        if ((newDegree + currDegreeOffset)%360 >= 45 && (newDegree + currDegreeOffset)%360 < 135) { // East
            degreeRange = 1;
            curr_x = 1;
            curr_y = 0;
        } else if ((newDegree + currDegreeOffset)%360  >= 135 && (newDegree + currDegreeOffset)%360  < 225) { // South
            degreeRange = 2;
            curr_y = 1;
            curr_x = 0;
        } else if ((newDegree + currDegreeOffset)%360  >= 225 && (newDegree + currDegreeOffset)%360  < 315) { // West
            degreeRange = 3;
            curr_x = -1;
            curr_y =  0;
        } else { // North
            degreeRange = 4;
            curr_x=0;
            curr_y = -1;
        }
        Log.i("start", Integer.toString(degreeRange));



        //checks if current direction has changed based on the set thresholds
        if (prevDegreeRange == degreeRange) {
            degreeRangeChangedCount=0;
            return;
        } else {
            degreeRangeChangedCount++;

        }

        if (degreeRangeChangedCount == 1) {
            xPos = prevNode.getxPos() + (int) currSteps * prev_x * 5;
            yPos = prevNode.getyPos() + (int) currSteps * prev_y * 5;
        }

        //checks that degree is within a new threshold long enough to justify a new node
        if (degreeRangeChangedCount < 20) { //This value will have to be determined through testing
            return;
        } else {
            degreeRangeChangedCount = 0;
            prevDegreeRange = degreeRange;
        }


        //creating a new node and adding the corresponding edge between the prev node and new node
        Node newNode = nodeExists(xPos, yPos);
        if(newNode == null) {
            //TODO: Add floor logic (currently forced to floor 2 for EHall
            //TODO: Add stair node logic (currently set to false)
            newNode = new Node(xPos, yPos, 2, false);
            addNewNode = true;
        }
        Edge newEdge = new Edge(prevNode, newNode);
        newEdge.setWeight((int) currSteps);
        newEdge.setDirection((int) newDegree);
        prevNode.setEdges(newEdge);
        newNode.setEdges(newEdge);
        if(addNewNode)nodes.add(newNode);
        edges.add(newEdge);
        prevNode = newNode;
        currSteps = 0;
        prev_x=curr_x;
        prev_y=curr_y;
        Log.i("New Node", newNode.getxPos() + " , " + newNode.getyPos() + " Dir: " + newDegree);
        //set values as needed
        currDegree = newDegree;
        sendBroadcast();
    }

    //methods for main activity to grab nodes and edges
    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public Node nodeExists(int xPos, int yPos){
        Node curr;
        for(int i = 0; i < nodes.size(); i++){
            curr = nodes.get(i);
            if(Math.abs(curr.getxPos() - xPos) < 25 && Math.abs(curr.getyPos() - yPos) < 25){
                return nodes.get(i);
            }
        }
        return null;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public ArrayList<LogData> getCurrData() {
        return currData;
    }

    public void clearData() {
        currSteps = 0;
        currData.clear();
    }

    public void send(LogData loggedData) {
        //TODO ADD CODE to send object to SERVER
    }

    private void sendBroadcast() {
        Intent new_intent = new Intent();
        new_intent.setAction("ToActivity");
        sendBroadcast(new_intent);
    }

}