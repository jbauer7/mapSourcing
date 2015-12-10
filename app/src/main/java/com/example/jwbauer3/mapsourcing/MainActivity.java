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
    private int curFloorNum;
    private EdgeLogService mService;
    private Navigator navigator;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            EdgeLogService.LocalBinder binder = (EdgeLogService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };


    private ArrayList<Node> nodes2;
    private ArrayList<Edge> edges2;
    private ArrayList<Node> nodes3;
    private ArrayList<Edge> edges3;
    private Floor floor2;
    private Floor floor3;
    private ArrayList<Floor> floors = new ArrayList<>();
    private String[] floorNames = {"Floor 2", "Floor 3"};
    private Intent serviceIntent;


    MyView myView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null && bundle.getString("mode").equals("navigation"))
        {
            navigationMode=true;
            Toast.makeText(getApplicationContext(), "navigationModeSet",
                    Toast.LENGTH_SHORT).show();
        }


        serviceIntent = new Intent(this, EdgeLogService.class);


        nodes2 = new ArrayList<Node>();
        edges2 = new ArrayList<Edge>();
        nodes3 = new ArrayList<Node>();
        edges3 = new ArrayList<Edge>();


        setContentView(R.layout.activity_main);
        setUp();
        setUpSpinner();
        floor2 = new Floor(2, nodes2, edges2, new ReferenceState(), ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor2, null));
        floor3 = new Floor(3, nodes3, edges3, new ReferenceState(), ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor3, null));
        floors.add(floor2);
        floors.add(floor3);
        ArrayList<BaseNode> graph = new ArrayList<>();
        graph.addAll(floor2.getNodes());
        graph.addAll(floor3.getNodes());
        navigator = new Navigator(graph);

        myView = (MyView) findViewById(R.id.MyViewTest);
        curFloorNum = 0;
        myView.setFloor(floor2);
        setMenuText();
        myView.setNavigator(navigator);
    }

    protected void onPause() {
        super.onPause();
        mService.setOffsetNotReady();
        unregisterReceiver(activityReceiver);
        if (mConnection != null)
            unbindService(mConnection);
    }


    protected void onResume() {
        super.onResume();
        if (activityReceiver != null) {
            //Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_ACTIVITY"
            IntentFilter intentFilter = new IntentFilter("ToActivity");
            //Map the intent filter to the receiver
            registerReceiver(activityReceiver, intentFilter);
        }
        if (serviceIntent != null)
            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        //if(navigationMode) mService.setNavigationMode();
        //else mService.setMappingMode();
    }

    private void setUp() {
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
                    curFloorNum = position;
                    myView.setFloor(floors.get(position));
                    setMenuText();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void pressed(View view) {
        Button mapButton = (Button)findViewById(R.id.Button_MapMode);
        if (!pressed) {
            mapButton.setText("Save Map");
            mService.setOffsetReady();
            pressed=true;
        }
        else {
            // DO STUFF TO SAVE FLOOR
            mService.setOffsetNotReady();
            mapButton.setText("Start Map");
            pressed=false;
        }
    }

    public void updateDisplay() {
        nodes2 = mService.getNodes();
        edges2 = mService.getEdges();
        floor2.setNodesEdges(nodes2, edges2);
        myView.setFloor(floor2);
    }

    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateDisplay();
            Toast.makeText(getApplicationContext(), "New Node Created",
                    Toast.LENGTH_SHORT).show();
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
