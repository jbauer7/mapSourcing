package com.example.jwbauer3.mapsourcing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
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
    private int buildingId = 0;
    private EdgeLogService mService;
    private Navigator navigator;

    //Persistence variables
    Context mContext;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            EdgeLogService.LocalBinder binder = (EdgeLogService.LocalBinder) service;
            mService = binder.getService();
            canvasView.connectEdgeLogService(mService);

            updateDisplay();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    protected static Floor currFloor;
    protected static ArrayList<Floor> floors = new ArrayList<>();
    private String[] floorNames = {"Floor 1", "Floor 2", "Floor 3", "Floor 4"};
    private Intent serviceIntent;


    CanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //set static context variable in Floor class (for persistence)
        mContext = this;

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("mode").equals("navigation")) {
                navigationMode = true;
                if (bundle.getString("presentationMode").equals("true"))
                {
                    //floor = new Persistence(mContext, 4, "ehall", 1);
                } else {
                   // floor = new Persistence(mContext, 2, "ehall", 1);
                }
                Toast.makeText(getApplicationContext(), "Navigation Mode",
                        Toast.LENGTH_SHORT).show();
            } else if (bundle.getString("mode").equals("map")) {
                if (bundle.getString("presentationMode").equals("true"))
                {
                   // floor = new Persistence(mContext, 3, "ehall", 1);
                } else {
                   // floor = new Persistence(mContext, 1, "ehall", 1);
                }
                navigationMode = false;
                Toast.makeText(getApplicationContext(), "Map Mode",
                        Toast.LENGTH_SHORT).show();
            }
            buildingId = bundle.getInt("buildingId");
        }
        serviceIntent = new Intent(this, EdgeLogService.class);

    }

    private void databaseHelperStartUp() {
        //Load all floors
        floors = DatabaseHelper.getAllFloors(buildingId);

        //Init floors if it doesn't exist
        if (floors.size() == 0) {
            floors.add(new Floor(1, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                    R.drawable.eh_floor1));
            floors.add(new Floor(2, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                    R.drawable.eh_floor2));
            floors.add(new Floor(3, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                    R.drawable.eh_floor3));
            floors.add(new Floor(4, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                    R.drawable.eh_floor4));

            floors.get(0).getNodes().add(new Node(0, 0, 1));
            floors.get(1).getNodes().add(new Node(0, 0, 2));
            floors.get(2).getNodes().add(new Node(0, 0, 3));
            floors.get(3).getNodes().add(new Node(0, 0, 4));

            DatabaseHelper.saveFloor(buildingId, floors.get(0));
            DatabaseHelper.saveFloor(buildingId, floors.get(1));
            DatabaseHelper.saveFloor(buildingId, floors.get(2));
            DatabaseHelper.saveFloor(buildingId, floors.get(3));
        }

        //Load the last floor we were on
        SharedPreferences mPrefs = getSharedPreferences("mapSourcingCurrentFloor", MODE_PRIVATE);
        int curFloorNum = mPrefs.getInt("currentFloorNum", 1);
        currFloor = floors.get(curFloorNum - 1);

        setContentView(R.layout.activity_main);
        setUpSpinner();

        ArrayList<BaseNode> graph = new ArrayList<>();
        for (Floor floor : floors) {
            graph.addAll(floor.getNodes());
        }

        navigator = new Navigator(graph);
        canvasView = (CanvasView) findViewById(R.id.MyViewTest);
        canvasView.connectEdgeLogService(mService);
        canvasView.setFloor(currFloor);
        setMenuText();
        canvasView.setNavigator(navigator);
    }

    /*
    private void startUp(){
        floors.add(new Floor(1, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                R.drawable.eh_floor1));
        floors.add(new Floor(2, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                R.drawable.eh_floor2));
        floors.add(new Floor(3, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                R.drawable.eh_floor3));
        floors.add(new Floor(4, new ArrayList<Node>(), new ArrayList<Edge>(), new ReferenceState(),
                R.drawable.eh_floor4));

        // I did this so it would stop crashing gets overwritten later anyway -Joey
        floors.get(0).getNodes().add(new Node(0, 0, 0, false));
        floors.get(0).getNodes().add(new Node(100, 0, 0, false));
        floors.get(0).getNodes().add(new Node(100, 100, 0, false));

        floors.get(1).getNodes().add(new Node(0, 0, 1, false));
        floors.get(1).getNodes().add(new Node(0, 100, 1, false));
        floors.get(1).getNodes().add(new Node(100, 100, 1, false));

        floors.get(2).getNodes().add(new Node(100, 100, 2, false));
        floors.get(3).getNodes().add(new Node(100, 100, 3, false));

        floors.get(0).getEdges().add(new Edge(floors.get(0).getNodes().get(0), floors.get(0).getNodes().get(1)));
        floors.get(0).getEdges().add(new Edge(floors.get(0).getNodes().get(1), floors.get(0).getNodes().get(2)));

        floors.get(1).getEdges().add(new Edge(floors.get(1).getNodes().get(0), floors.get(1).getNodes().get(1)));
        floors.get(1).getEdges().add(new Edge(floors.get(1).getNodes().get(1), floors.get(1).getNodes().get(2)));

        currFloor = floors.get(0);


        setContentView(R.layout.activity_main);
        setUpSpinner();


        ArrayList<BaseNode> graph = new ArrayList<>();
        graph.addAll(floors.get(0).getNodes());
        graph.addAll(floors.get(1).getNodes());
        graph.addAll(floors.get(2).getNodes());
        graph.addAll(floors.get(3).getNodes());

        navigator = new Navigator(graph);
        canvasView = (CanvasView) findViewById(R.id.MyViewTest);
        canvasView.connectEdgeLogService(mService);
        canvasView.setFloor(currFloor);
        setMenuText();
        canvasView.setNavigator(navigator);
    }
    */

    protected void onPause() {
        super.onPause();
        endFloor();
        curFloorNum = 0;
        unregisterReceiver(activityReceiver);
        if (mConnection != null)
            unbindService(mConnection);

        //Save the floor
        DatabaseHelper.saveFloor(buildingId, currFloor);

        //Save which floor we were on
        SharedPreferences mPrefs = getSharedPreferences("mapSourcingCurrentFloor", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.clear();
        mEditor.putInt("currentFloorNum", currFloor.getFloorNum());
        mEditor.commit();
    }


    protected void onResume() {
        super.onResume();

        databaseHelperStartUp();

        if (activityReceiver != null) {
            //Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_ACTIVITY"
            IntentFilter intentFilter = new IntentFilter("ToActivity");
            //Map the intent filter to the receiver
            registerReceiver(activityReceiver, intentFilter);
        }
        if (serviceIntent != null)
            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

        Button mapButton = (Button) findViewById(R.id.Button_MapMode);
        if(navigationMode) mapButton.setText("Start Nav");
        // else mapButton.setText("Start Map");
    }


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
                    DatabaseHelper.saveFloor(buildingId, currFloor);

                    curFloorNum = position;
                    canvasView.setFloor(floors.get(position));
                    currFloor = floors.get(position);
                    updateDisplay();
                    setMenuText();
                }
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

            }
            else{

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
                    //save floor
                    DatabaseHelper.saveFloor(buildingId, currFloor);
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
        canvasView.setFloor(currFloor);
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
                canvasView.updateUserLocation((Edge) mService.getCurrEdge(), mService.getLocation()[0], mService.getLocation()[1]);
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
        canvasView.toggleMeshMovementMode();
    }

    private void setMenuText() {
        //todo: replace hard coded string with fetched name
        TextView textView = (TextView) findViewById(R.id.TextView_MapTitle);
        textView.setText("Engineering Hall: Floor " + floors.get(curFloorNum).getFloorNum());
    }
}