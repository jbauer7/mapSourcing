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


/*Service handles the collection of sensor data in the background creates a thread that handles
* mapping and navigating algorithms */
public class EdgeLogService extends Service {
    //Service variables
    protected SensorManager sensorManager;
    protected SensorEventListener sensorListener;
    private final IBinder mBinder = new LocalBinder();
    private boolean sensorLock = true;
    private boolean startLock = true;
    private boolean running;
    private boolean offsetReady;

    /// Floor change Variables
    private boolean newFloor = false;
    private float currPressure;
    private float floorPressure;
    private boolean getFloorReading = true; //THESE ARE FIELDS I NEED FOR DETECTING FLOOR CHANGE
    private int currFloor = 1;              //   -kyle
    private int altCount = 0;
    private boolean up = false;
    private boolean down = false;
    private boolean noOffset = true;

    //Mapping variables
    private int xPos = 0;
    private int yPos = 0;
    private int prev_x = 0;
    private int prev_y = 1;
    private int prevDegreeRange;
    private int degreeRangeChangedCount;
    private float degreeOffset;
    boolean lock = false;
    private Node firstNode, prevNode;
    protected ArrayList<Node> nodes; //= new ArrayList<>();
    protected ArrayList<Edge> edges; //= //new ArrayList<>();

    //navigation variables
    private BaseEdge currEdge;
    private int[] currentLocation = new int[2];
    private int step_change = 0;
    private final int NAVIGATION_UPDATE_STEP_THRESHOLD = 3;
    private float navDegree;
    private boolean atNode = false;
    private BaseNode navCurrNode;
    private boolean noStep = true;
    private float edgeDirection;

    //both navigation and mapping
    private float currSteps;
    private boolean navigationMode = false;


    /* start sensor thread*/
    public void onCreate() {
        running = true;
        Thread thread = new Thread() {
            @Override
            public void run() {
                sensorListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        if (sensorLock) return;

                        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                            if (noOffset && !navigationMode) setOffset(event.values[0]);
                            else if (!startLock) {
                                if (navigationMode) navigationHandler(event.values[0]);
                                else directionHandler(event.values[0]);
                            }
                        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                            if (!startLock) checkStep(event);
                        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
                            if (!startLock) checkAltitude(event);
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    }
                };

                //Grabs first Node
                while (nodes == null)
                    ;
                firstNode = nodes.get(0);
                prevNode = firstNode;

                // starts required sensors
                sensorThread();

                while (running)
                    ;
            }
        };
        thread.start();
    }

    /*kill infinite loop onDestroy*/
    public void onDestroy() {
        super.onDestroy();
        offsetReady = false;
        running = false;
    }

    /* Allows service to be bound to */
    public class LocalBinder extends Binder {
        EdgeLogService getService() {
            return EdgeLogService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /* allows service to send messages to main activity */
    private void sendBroadcast() {
        Intent new_intent = new Intent();
        //new_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        new_intent.setAction("ToActivity");
        sendBroadcast(new_intent);
    }

    /* starts sensors */
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

    /*control sensor methods*/
    public void lockSensors() {
        sensorLock = true;
    }

    public void unlockSensors() {
        sensorLock = false;
    }

    public void lockStart() {
        startLock = true;
    }

    public void unlockStart() {
        startLock = false;
    }

    /*set current floor for nodes*/
    public void setCurrFloor(int floor) {
        currFloor = floor;
    }

    /* setNodes and edges that service should use*/
    public void setNodesEdges(ArrayList<Node> nodes, ArrayList<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    /* Set degreeOffset for mapping*/
    private void setOffset(Float newDegree) {
        setOffsetReady();
        degreeOffset = 360 - newDegree;
        noOffset = false;
        // prev_x=0;
        //  prev_y=-1;
        return;
    }

    public void setPrevNode() {
        prevNode = nodes.get(0);
    }


    /////////////////////////// MAPPING METHODS//////////////////////////////////////////////////////
    //on the sensor change (altitude) I just want to print a value to see what some
    //numbers look like to implement the way we check what floor we're on using the phone's
    //sensors as opposed to just touching a button
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
                if (navigationMode) {
                    currSteps = (toEndNode()) ? currSteps + 1 : currSteps - 1;
                    step_change++;
                    noStep = false;
                } else currSteps++;
                lock = true;
            }
        } else {
            lock = false;
        }
    }

    /* currently unused will
    TODO: Add floor logic (currently forced to floor 2 for EHall
    TODO: Add stair node logic (currently set to false) */
    private void checkAltitude(SensorEvent event) {
        currPressure = event.values[0];
        //System.out.println(currPressure);
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

    /* set ranges for mapping screen orientation to real world orientation */
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


    /* handel direction for creating nodes*/
    public void directionHandler(float newDegree) {
        //float change = prevDegreeChange;
        int degreeRange;
        int curr_x = 0;
        int curr_y = 0;
        float currDegreeOffset;
        boolean addNewNode = false;
        //setting initial degree offset


        if (prevDegreeRange == 0) {
            getInitialDirectionRange(newDegree);
            return;
        }
        currDegreeOffset = degreeOffset;

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

        //checks if current direction has changed based on the set thresholds
        if (prevDegreeRange == degreeRange) {
            degreeRangeChangedCount = 0;
            edgeDirection = newDegree;
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
        if (newNode == prevNode) {
            prev_x = curr_x;
            prev_y = curr_y;
            return;
        }
        if (newNode == null) {
            //TODO: Add floor logic (currently forced to floor 2 for EHall
            //TODO: Add stair node logic (currently set to false)
            newNode = createNewNode(xPos, yPos, currFloor, newFloor);
            newNode.setAltitude(currPressure);
            splitEdge(newNode);
            addNewNode = true;
        }
        Edge newEdge = new Edge(prevNode, newNode);
        newEdge.setWeight((int) currSteps);
        newEdge.setDirection((int) edgeDirection);
        prevNode.addEdge(newEdge);
        newNode.addEdge(newEdge);
        if (addNewNode) nodes.add(newNode);
        edges.add(newEdge);
        Log.i("NEW NODE:", newNode.nodeRefString + "prev node;" + prevNode.nodeRefString);

        prevNode = newNode;
        prev_x = curr_x;
        prev_y = curr_y;
        currSteps = 0;

        //Tell display new node has been added to allow real time node creation
        sendBroadcast();
    }

    /* checks if location of newNode is within a threshold of a current node*/
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

    /* splits edge when node is created in the middle of the edge*/
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
                    if (i == j) continue;
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
                                if (xAxis) {
                                    if (curr.getxPos() > temp.getxPos()) {
                                        if (!(newNode.getxPos() > temp.getxPos() && newNode.getxPos() < curr.getxPos()))
                                            continue;
                                    } else {
                                        if (!(newNode.getxPos() > curr.getxPos() && newNode.getxPos() < curr.getxPos()))
                                            continue;
                                    }
                                } else {
                                    if (curr.getyPos() > temp.getyPos()) {
                                        if (!(newNode.getyPos() > temp.getyPos() && newNode.getyPos() < curr.getyPos()))
                                            continue;
                                    } else {
                                        if (!(newNode.getyPos() > curr.getyPos() && newNode.getyPos() < curr.getyPos()))
                                            continue;
                                    }
                                }
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

    /*method to create new node*/
    private Node createNewNode(int xPos, int yPos, int floor, boolean stairs) {
        Node newNode = new Node(xPos, yPos, floor, stairs);
        return newNode;
    }

    /* locks to prevent mapping before offset has been set*/
    public void setOffsetReady() {
        offsetReady = true;
    }

    public void setOffsetNotReady() {
        offsetReady = false;
        noOffset = true;
    }

    //methods for main activity to grab nodes and edges
    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }


    ///////////////////////////////////////// Navigation Methods////////////////////////////////////////
    /* Navigation algo
    TODO FIX small at node error
    TODO add multifloor implementation*/
    private void navigationHandler(float newDegree) {
        if (currEdge == null || noStep) return;
        noStep = true;
        Log.i("DIR", Float.toString(currEdge.getDirection()));
        Log.i("NAVDIR", Float.toString(newDegree));

        //if leaving node get new edge;
        navDegree = newDegree;

        atNode = checkAtNode();
        if (atNode) {
            System.out.println("HERE");
            currEdge = getNextEdge();
           // currSteps = 0;
        }


        navDegree = newDegree;

        // Distance user has walked to the endNode of the edge
        float percentDistance = currSteps / (float) currEdge.getWeight();

        /*/calculate where user is
        if (Math.abs(currEdge.getStart().getDefaultXPos() - (float) currEdge.getEnd().getDefaultXPos()) > 10) {
            currentLocation[0] = ((int) ((((float) currEdge.getStart().getDefaultXPos() - (float) currEdge.getEnd().getDefaultXPos())) * percentDistance))+currEdge.getStart().getDefaultXPos();
        }
        else{
            currentLocation[0] =  currEdge.getStart().getDefaultXPos();
        }
        if (Math.abs(currEdge.getStart().getDefaultYPos() - (float) currEdge.getEnd().getDefaultYPos()) > 10) {
            currentLocation[1] = ((int) ((((float) currEdge.getStart().getDefaultYPos() + (float) currEdge.getEnd().getDefaultYPos())) * percentDistance));
        }
        else {
            currentLocation[0] = currEdge.getStart().getDefaultYPos();
        }
        */
        currentLocation[0] = ((int) ((((float) currEdge.getStart().getDefaultXPos() - (float) currEdge.getEnd().getDefaultXPos())) * -percentDistance))+currEdge.getStart().getDefaultXPos();
        currentLocation[1] = ((int) ((((float) currEdge.getStart().getDefaultYPos() - (float) currEdge.getEnd().getDefaultYPos())) * -percentDistance))+currEdge.getStart().getDefaultYPos();



        System.out.println("Top Node:" + currEdge.getEnd().getDefaultYPos() + "\n percentage:" + Float.toString(percentDistance));

        //Require three steps to update
        if (step_change <= NAVIGATION_UPDATE_STEP_THRESHOLD || atNode) {
            return;
        }
        step_change = 0;

        //Update display
        sendBroadcast();
    }

    /* Checks if user is at node point*/
    private boolean checkAtNode() {
        if (currSteps / (float) currEdge.getWeight() < 0.05) {
            navCurrNode = currEdge.getStart();
            return true;
        } else if (currSteps / (float) currEdge.getWeight() > .95) {
            navCurrNode = currEdge.getEnd();
            return true;
        } else return false;
    }

    /* Returns edge closest to facing direction */
    private BaseEdge getNextEdge() {
        BaseEdge nextEdge = navCurrNode.getEdges().get(0);
        float DegreesAway = 360;

        //find best fit edge
        for (BaseEdge tempEdge : navCurrNode.getEdges()) {
            float tempDegreesAway = Math.abs(tempEdge.getDirection() - navDegree);
           //Degree difference
            if (tempDegreesAway > 180) {
                tempDegreesAway = 360 - tempDegreesAway;
            }
            //accounting for direction of edge
            tempDegreesAway= (tempEdge.getStart()==navCurrNode) ? tempDegreesAway : (tempDegreesAway+180)%360;
            //finding the closest edge
            if (tempDegreesAway < DegreesAway) {
                DegreesAway = tempDegreesAway;
                nextEdge = tempEdge;
            }
        }
        //Setingg the new currSteps
        if(nextEdge!=currEdge) {
            currSteps = (nextEdge.getStart() == navCurrNode) ? 0 : nextEdge.getWeight();
        }
        return nextEdge;
    }

    /* determines if a user is walking towards end node or start node */
    private boolean toEndNode() {
        if (navDegree < (currEdge.getDirection() + 90) % 360 || navDegree > (currEdge.getDirection() + 270) % 360) {
            return true;
        }
        return false;
    }

    /* Sets start node for navigation*/
    private void setNavigationStartEdge(BaseEdge start, float percentComplete) {
        if (navigationMode) {
            currEdge = start;
            currSteps = currEdge.getWeight() * percentComplete;
        }
    }

    /* Ability to get current Edge for canvasView */
    public BaseEdge getCurrEdge() {

        return (BaseEdge) currEdge;
    }

    /* Change EdgeLogService to Navigation Mode */
    private void setNavigationMode() {
        navigationMode = true;
    }

    /* Change EdgeLogService to Mapping Mode */
    public void setMappingMode() {
        navigationMode = false;
    }

    /* accessor to update current location of user */
    public int[] getLocation() {
        return currentLocation;
    }

    /* Set in canvasView.java to start navigation functionality */
    public void setUserLocation(BaseEdge edge, double percentToEnd) {
        setNavigationMode();
        setNavigationStartEdge(edge, (float) percentToEnd);
        unlockSensors();
        unlockStart();
    }
}