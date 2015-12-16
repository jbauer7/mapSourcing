package com.example.jwbauer3.mapsourcing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private boolean gotNorth = false;
    private boolean pressed = false;
    private boolean mBound = false;
    private boolean navigationMode;
    protected static int curFloorNum=0;
    private EdgeLogService mService;
    private Navigator navigator;

    //Persistence variables
    Context mContext;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            EdgeLogService.LocalBinder binder = (EdgeLogService.LocalBinder) service;
            mService = binder.getService();
            myView.connectEdgeLogService(mService);

            updateDisplay();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };


    //private ArrayList<Node> currNodes;
    //private ArrayList<Edge> currEdges;
    protected static Floor currFloor;
    protected static ArrayList<Floor> floors = new ArrayList<>();
    private String[] floorNames = {"Floor 1", "Floor 2", "Floor 3", "Floor 4"};
    private Intent serviceIntent;


    MyView myView;

    protected static Persistence floor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //set static context variable in Floor class (for persistence)
        mContext = this;

        //Persistence(Context context, int type, String building, int floorNumber)
        floor = new Persistence(mContext, 1, "ehall", 1);

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("mode").equals("navigation")) {
                navigationMode = true;
                Toast.makeText(getApplicationContext(), "Navigation Mode",
                        Toast.LENGTH_SHORT).show();
            } else if (bundle.getString("mode").equals("map")) {
                navigationMode = false;
                Toast.makeText(getApplicationContext(), "Map Mode",
                        Toast.LENGTH_SHORT).show();
            }
        }
        serviceIntent = new Intent(this, EdgeLogService.class);

    }

    private void startUp(){
        //hard coded the intialization of floors TODO generalize this -Joey
        floors.add(new Floor(1, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor2, null)));
        floors.add(new Floor(2, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor2, null)));
        floors.add(new Floor(3, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor3, null)));
        floors.add(new Floor(4, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor3, null)));

        // I did this so it would stop crashing gets overwritten later anyway -Joey
        floors.get(0).getNodes().add(new Node(0, 0, 0, false));
        floors.get(0).getNodes().add(new Node(100, 0, 0, false));
        floors.get(0).getNodes().add(new Node(100, 100, 0, false));

        floors.get(1).getNodes().add(new Node(0, 0, 1, false));
        floors.get(2).getNodes().add(new Node(0, 0, 2, false));
        floors.get(3).getNodes().add(new Node(0, 0, 3, false));

        floors.get(0).getEdges().add(new Edge( floors.get(0).getNodes().get(0), floors.get(0).getNodes().get(1)));
        floors.get(0).getEdges().add(new Edge( floors.get(0).getNodes().get(1), floors.get(0).getNodes().get(2)));
        floors.get(1).getNodes().add(new Node(0,0,2,false));
        floors.get(2).getNodes().add(new Node(0,0,3,false));
        floors.get(3).getNodes().add(new Node(0,0,4,false));


        currFloor = floors.get(0);


        setContentView(R.layout.activity_main);
        setUpSpinner();


        ArrayList<BaseNode> graph = new ArrayList<>();
        graph.addAll(floors.get(0).getNodes());
        graph.addAll(floors.get(1).getNodes());
        graph.addAll(floors.get(2).getNodes());
        graph.addAll(floors.get(3).getNodes());

        navigator = new Navigator(graph);
        myView = (MyView) findViewById(R.id.MyViewTest);
        myView.connectEdgeLogService(mService);
        myView.setFloor(currFloor);
        setMenuText();
        myView.setNavigator(navigator);
    }

    /* private void tempStartUp(Floor savedFloor){
        //hard coded the intialization of floors TODO generalize this -Joey
        floors.add(new Floor(1, savedFloor.nodes, savedFloor.edges, savedFloor.meshReferenceState,
                ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor2, null)));
        floors.add(new Floor(2, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor2, null)));
        floors.add(new Floor(3, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor3, null)));
        floors.add(new Floor(4, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor3, null)));

        // I did this so it would stop crashing gets overwritten later anyway -Joey
        floors.get(0).getNodes().add(new Node(0, 0, 0, false));
        floors.get(1).getNodes().add(new Node(0, 0, 1, false));
        floors.get(2).getNodes().add(new Node(0, 0, 2, false));
        floors.get(3).getNodes().add(new Node(0, 0, 3, false));

        // floors.get(0).getEdges().add(new Edge( floors.get(0).getNodes().get(0), floors.get(0).getNodes().get(1)));
        //   floors.get(1).getNodes().add(new Node(0,0,2,false));
        //  floors.get(2).getNodes().add(new Node(0,0,3,false));
        // floors.get(3).getNodes().add(new Node(0,0,4,false));


        currFloor = floors.get(0);


        setContentView(R.layout.activity_main);
        setUpSpinner();


        ArrayList<BaseNode> graph = new ArrayList<>();
        graph.addAll(floors.get(0).getNodes());
        graph.addAll(floors.get(1).getNodes());
        graph.addAll(floors.get(2).getNodes());
        graph.addAll(floors.get(3).getNodes());

        navigator = new Navigator(graph);
        myView = (MyView) findViewById(R.id.MyViewTest);

        myView.setFloor(currFloor);
        setMenuText();
        myView.setNavigator(navigator);

        if (!floor.isFloorSaved())
        {
            floor.saveFloor(currFloor);
        }
    } */

    public void getCurrentFloorSavedVersion() {
        floor.setCurrFloor(curFloorNum + 1);
        if (floor.getSavedFloor() == 1) {
            Floor savedFloor = floor.returnSavedFloor();
            if (savedFloor.getNodes().size() > 0)
            {
                floors.add(curFloorNum, savedFloor);
                myView.setFloor(floors.get(curFloorNum));
                currFloor = floors.get(curFloorNum);
            } else {
                myView.setFloor(floors.get(curFloorNum));
                currFloor = floors.get(curFloorNum);
            }
        } else {
            myView.setFloor(floors.get(curFloorNum));
            currFloor = floors.get(curFloorNum);
        }
        mService.setNodesEdges(currFloor.getNodes(), currFloor.getEdges());
    }

    private void persistenceStartUp(){
        Log.d("Persistence", "persistenceStartUp");
        //Persistence test
        floors = new ArrayList<>();
        curFloorNum = 0;
        for (int i = 0; i < 4; i++)
        {
            floor.setCurrFloor(i + 1);
            int drawable = R.drawable.eh_floor2;
            if (i + 1 > 2)
            {
                drawable = R.drawable.eh_floor3;
            }
            if (floor.getSavedFloor() == 1)
            {
                Floor savedFloor = floor.returnSavedFloor();
                if (savedFloor.edges.size() > 0 && savedFloor.nodes.size() > 0) {
                    floors.add(new Floor(i + 1, savedFloor.nodes, savedFloor.edges, savedFloor.meshReferenceState,
                            ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor2, null)));
                } else {
                    floors.add(new Floor(i + 1, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                            ResourcesCompat.getDrawable(getResources(), drawable, null)));
                    floors.get(i).getNodes().add(new Node(0, 0, i, false));
                }
            } else {
                floors.add(new Floor(i + 1, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                        ResourcesCompat.getDrawable(getResources(), drawable, null)));
                floors.get(i).getNodes().add(new Node(0, 0, i, false));
            }
        }
        Log.d("Persistence", "persistenceStartUp after for-loop");
        floor.setCurrFloor(1);
        currFloor= floors.get(0);


        //floors.get(1).getNodes().add(new Node(0, 0, 1, false));

        setContentView(R.layout.activity_main);
        setUpSpinner();


        ArrayList<BaseNode> graph = new ArrayList<>();
        graph.addAll(floors.get(0).getNodes());
        graph.addAll(floors.get(1).getNodes());
        graph.addAll(floors.get(2).getNodes());
        graph.addAll(floors.get(3).getNodes());

        navigator = new Navigator(graph);
        myView = (MyView) findViewById(R.id.MyViewTest);

        myView.setFloor(currFloor);
        setMenuText();
        myView.setNavigator(navigator);

        if (!floor.isFloorSaved())
        {
            floor.saveFloor(currFloor);
        }
    }

    protected void onPause() {
        super.onPause();
        endFloor();
        curFloorNum = 0;
        unregisterReceiver(activityReceiver);
        if (mConnection != null)
            unbindService(mConnection);
        //Node.saveNode("aNode", currFloor.getNodes().get(0));

        floor.saveFloor(currFloor);
    }


    protected void onResume() {
        super.onResume();

        //TODO: Persistence TESTING
        //Persistence test
        persistenceStartUp();
        //startUp();
        /*if (floor.getSavedFloor() == 1)
        {
            Floor savedFloor = floor.returnSavedFloor();
            if (savedFloor.edges.size() > 0 && savedFloor.nodes.size() > 0)
            {
                tempStartUp(savedFloor);
                //persistenceStartUp();
            } else {
                startUp();
            }
        } else {
            startUp();
        } */
        //TODO: END Persistence TESTING

        if (activityReceiver != null) {
            //Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_ACTIVITY"
            IntentFilter intentFilter = new IntentFilter("ToActivity");
            //Map the intent filter to the receiver
            registerReceiver(activityReceiver, intentFilter);
        }
        if (serviceIntent != null)
            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

        /*if (floor.isFloorSaved())
        {
            Log.d("onResume", "currFloor.getNodes().size() = "
                    + currFloor.getNodes().size()
                    + "currFloor.getEdges().size() = "
                    + currFloor.getEdges().size());
            if (mService == null)
            {
                Log.d("onResume", "mService == null");
            }
            mService.setNodesEdges(currFloor.getNodes(), currFloor.getEdges());
        } */


        Button mapButton = (Button) findViewById(R.id.Button_MapMode);
        if(navigationMode) mapButton.setText("Start Nav");
        // else mapButton.setText("Start Map");
    }

  /*  private void setUp() {
        //width, height
        Node test1 = new Node(0, 150, 2, true);
        Node test2 = new Node(400, -350, 2, false);
        Node test3 = new Node(-300, 400, 2, false);
        //Node test4 = new Node(800, 800);
        //Node test5 = new Node(1212, 1911);
        Edge con1 = new Edge(test1, test2);
        test1.addEdge(con1);
        test2.addEdge(con1);
        //Edge con2 = new Edge(test2, test4);
        Edge con3 = new Edge(test1, test3);
        test1.addEdge(con3);
        test3.addEdge(con3);
        //Edge con4 = new Edge(test3, test4);
        nodes2.add(test1);
        nodes2.add(test2);
        nodes2.add(test3);
        //nodes2.add(test4);
        //nodes2.add(test5);
        edges2.add(con1);
        //edges2.add(con2);
        edges2.add(con3);
        //edges2.add(con4);

        Node test4 = new Node(150, 800, 3, false);
        Node test5 = new Node(17, 38, 3, false);
        Node test6 = new Node(-160, 200, 3, true);
        Edge con45 = new Edge(test4, test5);
        test4.addEdge(con45);
        test5.addEdge(con45);
        Edge con56 = new Edge(test5, test6);
        test5.addEdge(con56);
        test6.addEdge(con56);
        nodes3.add(test4);
        nodes3.add(test5);
        nodes3.add(test6);
        edges3.add(con45);
        edges3.add(con56);

        //cross floor
        Edge xFloor = new Edge(test1, test6);
        test1.addEdge(xFloor);
        test6.addEdge(xFloor);


    }*/



    private void setUpSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.Spinner_ToggleFloors);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, floorNames);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (curFloorNum == position) {
                    //do nothing
                } else {
                    endFloor();
                    floor.setCurrFloor(curFloorNum + 1);
                    floor.saveFloor(currFloor);
                    curFloorNum = position;
                    floor.setCurrFloor(curFloorNum + 1);
                    myView.setFloor(floors.get(position));
                    currFloor = floors.get(position);
                    updateDisplay();
                    setMenuText();
                }
//                mService.setNodesEdges(currFloor.getNodes(), currFloor.getEdges());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void pressed(View view) {
        Button mapButton = (Button) findViewById(R.id.Button_MapMode);

        if(navigationMode){
            if(!pressed){
             //   mService.setNodesEdges(currFloor.getNodes(), currFloor.getEdges());

                //pressed=true;
                //mapButton.setText("END NAV");
            }
            else{
              //endFloor();
            }

        }
        else {
            if (!gotNorth) {
                mService.setNodesEdges(currFloor.getNodes(), currFloor.getEdges());
                mService.setCurrFloor(curFloorNum);
                gotNorth = true;
                mService.unlockSensors();
                mapButton.setText("Start Map");
            } else {
                if (!pressed) {
                    mService.setPrevNode();
                    mapButton.setText("Save Map");
                    mService.setMappingMode();
                    mService.unlockStart();
                    pressed = true;
                } else {
                    //TODO: STUFF TO SAVE FLOOR
                    floor.setCurrFloor(curFloorNum + 1);
                    floor.saveFloor(currFloor);
                    endFloor();
                }
            }
        }
    }

    private void endFloor(){
        Button mapButton = (Button) findViewById(R.id.Button_MapMode);
        mService.setOffsetNotReady();
        if(!navigationMode)mapButton.setText("Set Offset");
        else mapButton.setText("START NAV");
        mService.lockSensors();
        mService.lockStart();
        pressed=false;
        gotNorth=false;
    }

    private void updateDisplay() {
        mService.setNodesEdges(currFloor.getNodes(), currFloor.getEdges());
        currFloor=floors.get(curFloorNum);
        floor.setCurrFloor(curFloorNum + 1);
        floor.saveFloor(currFloor);
        //   Toast.makeText(getApplicationContext(), "Edges:" + Integer.toString(currFloor.getEdges().size()) + "  nodes:" + Integer.toString(currFloor.getNodes().size()),
        //         Toast.LENGTH_SHORT).show();
        myView.setFloor(currFloor);
    }

    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (navigationMode) {
                //getCurrentFloorSavedVersion();
                mService.setNodesEdges(currFloor.getNodes(), currFloor.getEdges());
                //********************* UNCOMMENT THIS TO UPDATE DISPLAY WHEN READY ********************////
                Toast.makeText(getApplicationContext(), "x:" + mService.getLocation()[0]+ "\ny:"+ mService.getLocation()[1],
                        Toast.LENGTH_SHORT).show();
                myView.updateUserLocation((Edge) mService.getCurrEdge(), mService.getLocation()[0], mService.getLocation()[1]);
            } else {
                updateDisplay();
                Toast.makeText(getApplicationContext(), "New Node Created",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void toggleMesh(View view) {
        Button toggleButton = (Button) findViewById(R.id.Button_SwitchMode);
        if (toggleButton.getText().equals("Mesh Mode")) {
            toggleButton.setText("Canvas Mode");
        } else {
            toggleButton.setText("Mesh Mode");
        }
        myView.toggleMeshMovementMode();
        floor.saveFloor(currFloor);
    }

    private void setMenuText() {
        //todo: replace hard coded string with fetched name
        TextView textView = (TextView) findViewById(R.id.TextView_MapTitle);
        textView.setText("Engineering Hall: Floor " + floors.get(curFloorNum).getFloorNum());
    }
}