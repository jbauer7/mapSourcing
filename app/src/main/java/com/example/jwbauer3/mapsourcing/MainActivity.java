package com.example.jwbauer3.mapsourcing;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private boolean pressed = false;
    private boolean mBound = false;
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


    private ArrayList<Node> nodes;
    private ArrayList<Edge> edges;
    MyView myView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nodes = new ArrayList<Node>();
        edges = new ArrayList<Edge>();
        setUp();
        setContentView(R.layout.activity_main);


        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.LinearLayout_main_wrapper);
        //int height = linearLayout.getHeight();

        myView = (MyView)findViewById(R.id.MyViewTest);
        //myView.setMinimumHeight(height);
        //myView.touchDown();
        myView.setNodesEdges(nodes, edges);
        //myView.setListener();
        //MyView myView = new MyView(this, nodes, edges);
        //ScrollView scrollView = (ScrollView) findViewById(R.id.ScrollView_myViewScrollview);
        linearLayout.invalidate();
    }
    private void setUp() {
        //width, height
        Node test1 = new Node(0, 150);
        Node test2 = new Node(400, -350);
        Node test3 = new Node(-300, 400);
        //Node test4 = new Node(800, 800);
        //Node test5 = new Node(1212, 1911);
        Edge con1 = new Edge(test1, test2);
        //Edge con2 = new Edge(test2, test4);
        Edge con3 = new Edge(test1, test3);
        //Edge con4 = new Edge(test3, test4);
        nodes.add(test1);
        nodes.add(test2);
        nodes.add(test3);
        //nodes.add(test4);
        //nodes.add(test5);
        edges.add(con1);
        //edges.add(con2);
        edges.add(con3);
        //edges.add(con4);

        //starts background service for collecting edge data
        //startService(new Intent(this, EdgeLogService.class));
    }
    public void pressed(View view){
        if(!pressed) {
            Intent intent = new Intent(this, EdgeLogService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            pressed = true;
        }
        else{
            nodes = mService.getNodes();
            edges = mService.getEdges();
            myView.setNodesEdges(nodes,edges);
            pressed = false;
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
