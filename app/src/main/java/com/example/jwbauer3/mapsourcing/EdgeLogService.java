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
import android.widget.Toast;
import java.util.ArrayList;


/*Service handles the collection of sensor data in the background*/
public class EdgeLogService extends Service {
    //Service variables
    protected SensorManager sensorManager;
    SensorEventListener sensorListener;
    private final IBinder mBinder = new LocalBinder();

    /// Floor change Variables
    boolean newFloor=false;
    float currPressure;
    private float floorPressure;
    private boolean getFloorReading = true; //THESE ARE FIELDS I NEED FOR DETECTING FLOOR CHANGE
    private int currFloor = 1;              //   -kyle
    private int altCount = 0;
    private boolean up = false;
    private boolean down = false;

    //Mapping variables
    int xPos = 0;
    int yPos = 0;
    int prev_x = 0;
    int prev_y = 1;
    private int prevDegreeRange;
    private int degreeRangeChangedCount;
    private float degreeOffset;
    private boolean noOffset = true;
    boolean lock = false;
    Node firstNode, prevNode;
    ArrayList<Node> nodes = new ArrayList<>();
    ArrayList<Edge> edges = new ArrayList<>();
    boolean running;
    boolean offsetReady;

    //navigation variables
    boolean navigationMode=false;
    BaseNode currNode;
    int[] currentLocation = new int[2];

    //both navigation and mapping
    private float currSteps;


    public void onCreate() {
        running = true;
        Thread thread = new Thread() {
            @Override
            public void run() {
                sensorListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                            if (navigationMode) navigationHandler(event.values[0]);
                            else directionHandler(event.values[0]);
                        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                            checkStep(event);
                        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
                            checkAltitude(event);
                        }

                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }


                };

                // starts required sensors
                sensorThread();

                //TODO: Add floor logic (currently forced to floor 2 for EHall
                //TODO: Add stair node logic (currently set to false)
                firstNode = createNewNode(0, 0, currFloor, newFloor);
                //  nodes = new ArrayList<>();
                // edges = new ArrayList<>();
                nodes.add(firstNode);
                prevNode = firstNode;
                while (running)
                    ;
            }
        };
        thread.start();
    }

    public void onDestroy() {
        super.onDestroy();
        offsetReady = false;
        running = false;
    }


    public class LocalBinder extends Binder {
        EdgeLogService getService() {
            return EdgeLogService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void sendBroadcast() {
        Intent new_intent = new Intent();
        new_intent.setAction("ToActivity");
        sendBroadcast(new_intent);
    }

    public void sensorThread() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }


    /////////////////////////// MAPPING METHODS//////////////////////////////////////////////////////


    //on the sensor change (altitude) I just want to print a value to see what some
    //numbers look like to implement the way we check what floor we're on using the phone's
    //sensors as opposed to just touching a button
    //code from minilab 4 part 2.  It is a step detector based on the readings
    //from the accelerometer
    private void checkStep(SensorEvent event) {
        // Movement
        if (noOffset && !offsetReady && !navigationMode) return;

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


    private void checkAltitude(SensorEvent event) {
        currPressure = event.values[0];
        System.out.println(currPressure);
        if (noOffset && !offsetReady) return;

        if (getFloorReading) {
            getFloorReading = false;
            floorPressure = currPressure;
            return;
        }
        ////all other readings than the floor reading////
        if (floorPressure - currPressure >= 3.2) {
            altCount++;
            if (altCount == 10) {
                currFloor++;
                altCount = 0;
                getFloorReading = true;
                up = true;
            }
        } else if (currPressure - floorPressure >= 3.2) {
            altCount++;
            if (altCount == 10) {
                currFloor--;
                altCount = 0;
                getFloorReading = true;
                down = true;
            }
        } else altCount = 0;

        //IF THE FLOOR IS CHANGING, TOAST

        if (up) {
            up = false;
            System.out.println("UP STAIRS");
            Toast.makeText(getApplicationContext(), "UP STAIRS",
                    Toast.LENGTH_SHORT).show();
        } else if (down) {
            down = false;
            System.out.println("DOWN STAIRS");
            Toast.makeText(getApplicationContext(), "DOWN STAIRS",
                    Toast.LENGTH_SHORT).show();
        }
        //newFloor=true;
    }


    private void getInitialDirectionRange(float newDegree) {
        //  degreeOffset = -270; // These will have to be calculated on a building by building basis
        float currDegreeOffset = 0;
        if ((newDegree + degreeOffset) < 0) {
            currDegreeOffset = (360 + degreeOffset);
        }
        if ((newDegree + currDegreeOffset) % 360 >= 45 && (newDegree + currDegreeOffset) % 360 < 135) { // East
            prevDegreeRange = 1;
        } else if ((newDegree + currDegreeOffset) % 360 >= 135 && (newDegree + currDegreeOffset) % 360 < 225) { // South
            prevDegreeRange = 2;
        } else if ((newDegree + currDegreeOffset) % 360 >= 225 && (newDegree + currDegreeOffset) % 360 < 315) { // West
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

        if (noOffset) {
            if (!offsetReady)
                return;

            degreeOffset = 360 - newDegree;
            noOffset = false;
            prevNode.setAltitude(currPressure);
            return;
        }
        if (prevDegreeRange == 0) {
            getInitialDirectionRange(newDegree);
            return;
        }
        currDegreeOffset = degreeOffset;
        //Log.i("offset", Float.toString(currDegreeOffset));

        //Thresholds are based on standard direction and an offset to match building map
        if ((newDegree + currDegreeOffset) % 360 >= 45 && (newDegree + currDegreeOffset) % 360 < 135) { // East
            degreeRange = 1;
            curr_x = 1;
            curr_y = 0;
        } else if ((newDegree + currDegreeOffset) % 360 >= 135 && (newDegree + currDegreeOffset) % 360 < 225) { // South
            degreeRange = 2;
            curr_y = 1;
            curr_x = 0;
        } else if ((newDegree + currDegreeOffset) % 360 >= 225 && (newDegree + currDegreeOffset) % 360 < 315) { // West
            degreeRange = 3;
            curr_x = -1;
            curr_y = 0;
        } else { // North
            degreeRange = 4;
            curr_x = 0;
            curr_y = -1;
        }
        Log.i("start", Integer.toString(degreeRange));


        //checks if current direction has changed based on the set thresholds
        if (prevDegreeRange == degreeRange) {
            degreeRangeChangedCount = 0;
            return;
        } else {
            degreeRangeChangedCount++;

        }

        if (degreeRangeChangedCount == 1) {
            xPos = prevNode.getxPos() + (int) currSteps * prev_x * 5;
            yPos = prevNode.getyPos() + (int) currSteps * prev_y * 5;
            //currSteps = 0;
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
        if (newNode == null) {
            //TODO: Add floor logic (currently forced to floor 2 for EHall
            //TODO: Add stair node logic (currently set to false)
            newNode = createNewNode(xPos,yPos,currFloor, newFloor);
            newNode.setAltitude(currPressure);
            splitEdge(newNode);
            addNewNode = true;
        }
        Edge newEdge = new Edge(prevNode, newNode);
        newEdge.setWeight((int) currSteps);
        newEdge.setDirection((int) newDegree);
        prevNode.addEdge(newEdge);
        newNode.addEdge(newEdge);
        if (addNewNode) nodes.add(newNode);
        edges.add(newEdge);
        prevNode = newNode;
        prev_x = curr_x;
        prev_y = curr_y;
        currSteps = 0;
        Log.i("New Node", newNode.getxPos() + " , " + newNode.getyPos() + " Dir: " + newDegree);
        //set values as needed
        sendBroadcast();
    }

    //methods for main activity to grab nodes and edges
    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public Node nodeExists(int xPos, int yPos) {
        Node curr;
        for (int i = 0; i < nodes.size(); i++) {
            curr = nodes.get(i);
            if (Math.abs(curr.getxPos() - xPos) < 25 && Math.abs(curr.getyPos() - yPos) < 25) {
                return nodes.get(i);
            }
        }
        return null;
    }

    public void splitEdge(Node newNode) {

        Node curr;
        Node temp;
        boolean xAxis = true;
        for (int i = 0; i < nodes.size(); i++) {
            curr = nodes.get(i);
            //check if the current node is either on the same x or y line
            //as the "new" node
            if (Math.abs(curr.getxPos() - newNode.getxPos()) < 15
                    || Math.abs(curr.getyPos() - newNode.getyPos()) < 15) {

                for (int j = 0; j < nodes.size(); j++) {
                    if (i == j) break;
                    temp = nodes.get(j);
                    if (Math.abs(temp.getxPos() - newNode.getxPos()) < 15
                            || Math.abs(temp.getyPos() - newNode.getyPos()) < 15) {

                        if (Math.abs(temp.getxPos() - newNode.getxPos()) < 15) xAxis = false;

                        Edge currEdge;
                        for (int k = 0; k < edges.size(); k++) {
                            currEdge = edges.get(k);
                            //IF THERE IS AN EDGE BETWEEN THESE 2 NODES THAT ALREADY EXIST
                            //EDGE(CURR , TEMP)
                            if (currEdge.getStart() == curr && currEdge.getEnd() == temp) {
                                curr.getEdges().remove(edges.get(k));
                                temp.getEdges().remove(edges.get(k));
                                Edge oneEdge = new Edge(curr, newNode);
                                oneEdge.setDirection(edges.get(k).getDirection());
                                Edge twoEdge = new Edge(newNode, temp);
                                twoEdge.setDirection(edges.get(k).getDirection());
                                if (xAxis) {
                                    oneEdge.setWeight(Math.abs(curr.getxPos() - newNode.getxPos()) / 5);
                                    twoEdge.setWeight(Math.abs(temp.getxPos() - newNode.getxPos()) / 5);
                                } else {
                                    oneEdge.setWeight(Math.abs(curr.getyPos() - newNode.getyPos()) / 5);
                                    twoEdge.setWeight(Math.abs(temp.getyPos() - newNode.getyPos()) / 5);
                                }
                                edges.add(oneEdge);
                                edges.add(twoEdge);
                                curr.addEdge(oneEdge);
                                temp.addEdge(twoEdge);
                                newNode.addEdge(oneEdge);
                                newNode.addEdge(twoEdge);
                                edges.remove(k);
                            }
                            //Edge (Temp, Curr)
                            else if (currEdge.getStart() == temp && currEdge.getEnd() == curr) {
                                curr.getEdges().remove(edges.get(k));
                                temp.getEdges().remove(edges.get(k));
                                Edge oneEdge = new Edge(temp, newNode);
                                oneEdge.setDirection(edges.get(k).getDirection());
                                Edge twoEdge = new Edge(newNode, curr);
                                oneEdge.setDirection(edges.get(k).getDirection());
                                if (xAxis) {
                                    oneEdge.setWeight(Math.abs(temp.getxPos() - newNode.getxPos()) / 5);
                                    twoEdge.setWeight(Math.abs(curr.getxPos() - newNode.getxPos()) / 5);
                                } else {
                                    oneEdge.setWeight(Math.abs(temp.getyPos() - newNode.getyPos()) / 5);
                                    twoEdge.setWeight(Math.abs(curr.getyPos() - newNode.getyPos()) / 5);
                                }
                                edges.add(oneEdge);
                                edges.add(twoEdge);
                                curr.addEdge(twoEdge);
                                temp.addEdge(oneEdge);
                                newNode.addEdge(oneEdge);
                                newNode.addEdge(twoEdge);
                                edges.remove(k);
                            }
                        }
                    }
                }
            }
        }
    }

    private Node createNewNode(int xPos, int yPos, int floor, boolean stairs){
        Node newNode =new Node(xPos, yPos, floor, stairs);
        return newNode;
    }


    public void setOffsetReady() {
        offsetReady = true;
    }
    public void setOffsetNotReady() {
        offsetReady = false;
        noOffset = true;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }
    ///////////////////////////////////////// Navigation Methods////////////////////////////////////////
    private void navigationHandler(float newDegree){
        int direction;
        Edge closestEdge=null;

        for(Edge currEdge: currNode.getEdges()){
            direction=currEdge.getDirection();
            if(!currEdge.getStart().equals(currNode)) direction=(direction+180)%360;
            if(Math.abs(currEdge.getDirection()-newDegree)<45){
                closestEdge=currEdge;
                //  return;
            }
        }
        if(closestEdge==null) return;

        if(closestEdge.getStart().equals(currNode)){
            currNode=closestEdge.getEnd();
        }

        //currentLocation = currSteps/ (float) closestEdge.getWeight();
        sendBroadcast();
        // send this to main
        if(closestEdge.getWeight()-currSteps <5 ){
            if(closestEdge.getStart().equals(currNode)){
                currNode=closestEdge.getEnd();
            }
            else currNode=closestEdge.getStart();
        }
    }

    public void setNavigationStartnode(Node start){
        if(navigationMode){
            currNode=start;
            currSteps=0;
        }
    }


    public void setNavigationMode(){ navigationMode=true;}
    public void setMappingMode(){ navigationMode=false;}
    //  public float getLocation(){return percentEdge;}



    ///////////////////////////Not Implemented for project/////////////////////////////////////////
    public void save() {
        //TODO ADD CODE to send object to SERVER
    }
}