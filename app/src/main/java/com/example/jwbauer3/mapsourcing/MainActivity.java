package com.example.jwbauer3.mapsourcing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ArrayList<Node> nodes;
    private ArrayList<Edge> edges;
    MyView myView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nodes = new ArrayList<Node>();
        edges = new ArrayList<Edge>();
        setUp();
        //setContentView(new MyView(this, nodes, edges));
        setContentView(R.layout.activity_main);
        //ImageView imageView = (ImageView)findViewById(R.id.ImageView);
        //ArrayList<View> test = new ArrayList<View>();
        //test.add(new MyView(this, nodes, edges));
        //imageView.addTouchables(test);
        //imageView.invalidate();



        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.LinearLayout_main_wrapper);
        int height = linearLayout.getHeight();

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
        //MyView myView = (MyView)findViewById(R.id.MyViewTest);
        LinearLayout myView = (LinearLayout)findViewById(R.id.LinearLayout_main_wrapper);
        //myView.touchDown();
        //myView.setNodesEdges(nodes, edges);
        //myView.setListener();
        int width = myView.getWidth();
        int height = myView.getHeight();

        Toast.makeText(getApplicationContext(), "Height = " + height + " : Width = " + width, Toast.LENGTH_SHORT).show();

        //Edge data logging testing (sorry for using your button)
        /*Edge con4 = edges.get(edges.size()-1);
        EdgeData edgeData = con4.edgeData;
*/
    }

    public void switchActivity(View view){
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
    public void toggleMesh(View view){
        myView.toggleMeshMovementMode();
    }
}
