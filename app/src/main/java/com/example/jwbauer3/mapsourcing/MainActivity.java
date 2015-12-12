package com.example.jwbauer3.mapsourcing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.res.ResourcesCompat;
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

    private boolean pressed = false;
    private boolean mBound = false;
    private boolean navigationMode;
    private int curFloorNum=0;
    private EdgeLogService mService;
    private Navigator navigator;

    //Persistence variables
    Context mContext;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            EdgeLogService.LocalBinder binder = (EdgeLogService.LocalBinder) service;
            mService = binder.getService();
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
    private Floor currFloor;
    private ArrayList<Floor> floors = new ArrayList<>();
    private String[] floorNames = {"Floor 1", "Floor 2", "Floor 3","Floor 4"};
    private Intent serviceIntent;


    MyView myView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //set static context variable in Floor class (for persistence)
        mContext = this;
        Node.initPersistence(mContext);

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
        floors.get(1).getNodes().add(new Node(0, 0, 1, false));
        floors.get(2).getNodes().add(new Node(0, 0, 2, false));
        floors.get(3).getNodes().add(new Node(0, 0, 3, false));

       // floors.get(0).getEdges().add(new Edge( floors.get(0).getNodes().get(0), floors.get(0).getNodes().get(1)));
     //   floors.get(1).getNodes().add(new Node(0,0,2,false));
      //  floors.get(2).getNodes().add(new Node(0,0,3,false));
       // floors.get(3).getNodes().add(new Node(0,0,4,false));


        currFloor= floors.get(0);


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
    }

    protected void onPause() {
        super.onPause();
        mService.setOffsetNotReady();
        mService.lockSensors();
        unregisterReceiver(activityReceiver);
        if (mConnection != null)
            unbindService(mConnection);
        Node.saveNode("aNode", currFloor.getNodes().get(0));
    }


    protected void onResume() {
        Node.getNode("aNode");
        super.onResume();
        startUp();
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
        else mapButton.setText("Start Map");
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
                    curFloorNum = position;
                    myView.setFloor(floors.get(position));
                    currFloor = floors.get(position);
                    updateDisplay();
                    setMenuText();
                    endFloor();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void pressed(View view) {
        Button mapButton = (Button) findViewById(R.id.Button_MapMode);
        if (!pressed) {
            mService.setNodesEdges(currFloor.getNodes(), currFloor.getEdges());
            mapButton.setText("Save Map");
            if (navigationMode) {
                mService.setNavigationMode();
            } else {
                mService.setMappingMode();
            }
            mService.setCurrFloor(curFloorNum);
            mService.setOffsetReady();
            mService.unlockSensors();
            pressed = true;
        } else {
            //TODO: STUFF TO SAVE FLOOR
            endFloor();
            pressed = false;
        }
    }

    private void endFloor(){
        Button mapButton = (Button) findViewById(R.id.Button_MapMode);
        mService.setOffsetNotReady();
        mapButton.setText("Start Map");
        mService.lockSensors();
        pressed=false;
    }

    private void updateDisplay() {
        currFloor=floors.get(curFloorNum);
     //   Toast.makeText(getApplicationContext(), "Edges:" + Integer.toString(currFloor.getEdges().size()) + "  nodes:" + Integer.toString(currFloor.getNodes().size()),
       //         Toast.LENGTH_SHORT).show();
        myView.setFloor(currFloor);
    }

    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (navigationMode) {
                //TODO: update current location navigation by calling mService.getLocation()
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
    }

    private void setMenuText() {
        //todo: replace hard coded string with fetched name
        TextView textView = (TextView) findViewById(R.id.TextView_MapTitle);
        textView.setText("Engineering Hall: Floor " + floors.get(curFloorNum).getFloorNum());
    }
}
