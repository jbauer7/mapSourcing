package com.example.jwbauer3.mapsourcing;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private boolean pressed = false;
    private boolean mBound = false;
    private int curFloorNum = 2;
    private EdgeLogService mService;
    private ServiceConnection mConnection = new ServiceConnection(){

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
    Floor floor2;
    Floor floor3;

    MyView myView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nodes2 = new ArrayList<Node>();
        edges2 = new ArrayList<Edge>();
        nodes3 = new ArrayList<Node>();
        edges3 = new ArrayList<Edge>();

        setUp();
        setContentView(R.layout.activity_main);

        floor2 = new Floor(2, nodes2, edges2, new ReferenceState(), ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor2, null));
        floor3 = new Floor(3, nodes3, edges3, new ReferenceState(), ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor3, null));

        myView = (MyView)findViewById(R.id.MyViewTest);
        myView.setFloor(floor2);

    }
    private void setUp() {
        //width, height
        Node test1 = new Node(0, 150,2);
        Node test2 = new Node(400, -350,2);
        Node test3 = new Node(-300, 400,2);
        //Node test4 = new Node(800, 800);
        //Node test5 = new Node(1212, 1911);
        Edge con1 = new Edge(test1, test2);
        //Edge con2 = new Edge(test2, test4);
        Edge con3 = new Edge(test1, test3);
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

        Node test4 = new Node(150,800,3);
        Node test5 = new Node(17,38,3);
        Node test6= new Node(-160,200,3);
        Edge con45 = new Edge(test4,test5);
        Edge con56 = new Edge(test5,test6);
        nodes3.add(test4);
        nodes3.add(test5);
        nodes3.add(test6);
        edges3.add(con45);
        edges3.add(con56);

    }
    public void pressed(View view){
        if(curFloorNum==2){
            myView.setFloor(floor3);
            curFloorNum=3;
        }
        else{
            myView.setFloor(floor2);
            curFloorNum=2;
        }
    }

    public void switchActivity(View view){
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
    public void toggleMesh(View view){
        Button toggleButton = (Button) findViewById(R.id.Button_MeshMode);
        if(toggleButton.getText().equals("Mesh Mode")) {
            toggleButton.setText("Canvas Mode");
        }
        else{
            toggleButton.setText("Mesh Mode");
        }
        myView.toggleMeshMovementMode();
    }
}
