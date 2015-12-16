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

    //Function control variables
    private boolean navigationMode;
    protected static int curFloorNum = 0;
    private long selectedBuildingId = 0;
    private Navigator navigator;
    protected static Floor currFloor;
    protected static ArrayList<Floor> floors = new ArrayList<>();
    private String[] floorNames = {"Floor 1", "Floor 2", "Floor 3", "Floor 4"};
    CanvasView canvasView;

    //Button Control variables
    private boolean gotNorth = false;
    private boolean pressed = false;

    //Service variables
    private EdgeLogService mService;
    private boolean mBound = false;
    private Intent serviceIntent;

    //Persistence variables
    Context mContext;


    /*Connect to back ground service that handles sensors and mapping/navigation algorithms*/
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            EdgeLogService.LocalBinder binder = (EdgeLogService.LocalBinder) service;
            mService = binder.getService();
            //connect MyView to service
            canvasView.connectEdgeLogService(mService);
            //update Display after connection
            updateDisplay();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //set static context variable in Floor class (for persistence)
        mContext = this;
        super.onCreate(savedInstanceState);

        //Force screen to not sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Set current state of app
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
            selectedBuildingId = bundle.getLong("selectedBuildingId");
        }
        //start background service
        serviceIntent = new Intent(this, EdgeLogService.class);
    }


    /*Set up andriod SQLlite interface*/
    private void databaseHelperStartUp() {
        //Load all floors
        floors = DatabaseHelper.getAllFloors(selectedBuildingId);

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

            DatabaseHelper.saveFloor(selectedBuildingId, floors.get(0));
            DatabaseHelper.saveFloor(selectedBuildingId, floors.get(1));
            DatabaseHelper.saveFloor(selectedBuildingId, floors.get(2));
            DatabaseHelper.saveFloor(selectedBuildingId, floors.get(3));
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


    //properly close back ground services and save current data
    protected void onPause() {
        super.onPause();
        //handle background service
        endFloor();
        curFloorNum = 0;
        //properly unregister receiver
        unregisterReceiver(activityReceiver);
        //kill service
        if (mConnection != null)
            unbindService(mConnection);

        //Save the floor
        DatabaseHelper.saveFloor(selectedBuildingId, currFloor);

        //Save which floor we were on
        SharedPreferences mPrefs = getSharedPreferences("mapSourcingCurrentFloor", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.clear();
        mEditor.putInt("currentFloorNum", currFloor.getFloorNum());
        mEditor.commit();
    }


    //Restart background services and set message receiver
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
        if (navigationMode) mapButton.setText("Start Nav");
        else mapButton.setText("Start Map");
    }


    //Set up floor selection spinner
    private void setUpSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.Spinner_ToggleFloors);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, floorNames);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //spinner being used
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (curFloorNum == position) {
                    //do nothing
                } else {
                    endFloor();
                    DatabaseHelper.saveFloor(selectedBuildingId, currFloor);

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

    //handle bottom right button
    public void pressed(View view) {
        Button mapButton = (Button) findViewById(R.id.Button_MapMode);
        if (navigationMode) {
            //do nothing
            return;
        } else {
            //map mode set offset
            if (!gotNorth) {
                mService.setNodesEdges(currFloor.getNodes(), currFloor.getEdges());
                mService.setCurrFloor(curFloorNum);
                gotNorth = true;
                mService.unlockSensors();
                mapButton.setText("Start Map");
            } else {
                //start mapping
                if (!pressed) {

                    mService.setPrevNode();
                    mapButton.setText("Save Map");
                    mService.setMappingMode();
                    mService.unlockStart();
                    pressed = true;
                } else {
                    //stop mapping and save floor
                    DatabaseHelper.saveFloor(selectedBuildingId, currFloor);
                    endFloor();
                }
            }
        }
    }

    //end floor safely and update control variables
    private void endFloor() {
        Button mapButton = (Button) findViewById(R.id.Button_MapMode);
        mService.setOffsetNotReady();
        if (!navigationMode) mapButton.setText("Set Offset");
        else mapButton.setText("START NAV");
        mService.lockSensors();
        mService.lockStart();
        pressed = false;
        gotNorth = false;
    }

    //signal myView to update display
    private void updateDisplay() {
        mService.setNodesEdges(currFloor.getNodes(), currFloor.getEdges());
        currFloor = floors.get(curFloorNum);
        canvasView.setFloor(currFloor);
    }

    //receive messages from EdgeLogServices
    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (navigationMode) {
                //update userloction
                mService.setNodesEdges(currFloor.getNodes(), currFloor.getEdges());
                Toast.makeText(getApplicationContext(), "x:" + mService.getLocation()[0] + "\ny:" + mService.getLocation()[1],
                        Toast.LENGTH_SHORT).show();
                canvasView.updateUserLocation((Edge) mService.getCurrEdge(), mService.getLocation()[0], mService.getLocation()[1]);
            } else {
                //show new node and edges
                updateDisplay();
                Toast.makeText(getApplicationContext(), "New Node Created",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };


    //handle mesh mode button
    public void toggleMesh(View view) {
        Button toggleButton = (Button) findViewById(R.id.Button_SwitchMode);
        if (toggleButton.getText().equals("Mesh Mode")) {
            toggleButton.setText("Canvas Mode");
        } else {
            toggleButton.setText("Mesh Mode");
        }
        canvasView.toggleMeshMovementMode();
    }

    //modify title text
    private void setMenuText() {
        //todo: replace hard coded string with fetched name
        TextView textView = (TextView) findViewById(R.id.TextView_MapTitle);
        textView.setText("Engineering Hall: Floor " + floors.get(curFloorNum).getFloorNum());
    }
}