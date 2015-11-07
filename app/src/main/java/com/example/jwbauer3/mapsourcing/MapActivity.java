package com.example.jwbauer3.mapsourcing;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.lang.Object;


public class MapActivity extends AppCompatActivity {
    private Node firstNode;
    private Node prevNode;
    EdgeLogService mService;
    boolean mBound = false;
    int prevSteps;
    float prevDirection = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //First node will start at the origin
        firstNode = new Node(0,0);
        prevNode = firstNode;
        prevSteps=0;

        //Bind to the EdgeLogService
        Intent intent = new Intent(this, EdgeLogService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void createNode(View vew){
        ArrayList<LogData> currData = mService.getCurrData();
        String turn = "";
        float direction=aveDir(currData);
        int steps= (int) currData.get(currData.size()-1).getSteps() - prevSteps;
        prevSteps = (int) currData.get(currData.size()-1).getSteps();

        //Calculate xy location of the current node
        int xDir =  (prevNode.getxPos() + (int)(steps *  Math.sin((double) direction * Math.PI / 180)));
        int yDir =  (prevNode.getyPos() + (int)(steps *  Math.cos((double) direction * Math.PI / 180)));

        Node newNode = new Node(xDir,yDir);
        Edge currEdge= new Edge(prevNode, newNode);
        currEdge.setDirection((int) direction);
        currEdge.setWeight(steps);
        prevNode.setEdges(currEdge);
        newNode.setEdges(currEdge);

        TextView out = (TextView) findViewById(R.id.output);
        out.setText("Parent Node  x:" + Float.toString(prevNode.getxPos())
                + " y:" + Float.toString(prevNode.getyPos()) + "\nChild Node     x:" + Float.toString(newNode.getxPos())
                + " y:" + Float.toString(newNode.getyPos()) + "\nEdge direction:" + Float.toString(currEdge.getDirection())
                + "\nweight:" + Float.toString(currEdge.getWeight()));
        PackageManager pm = getPackageManager();
       if( pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER))
           Toast.makeText(getApplicationContext(), "Has step counter",Toast.LENGTH_LONG).show();;




        //this doesn't work yet
        //it's supposed to be me testing how to determine what direction of a turn we just made
        if(prevDirection != 1000){
            if(prevDirection - currEdge.getDirection() > 35 || currEdge.getDirection() - prevDirection >35){
                if(prevDirection <= 45 || currEdge.getDirection() <= 45){
                    if(prevDirection > currEdge.getDirection()){
                        turn = "left";
                    }
                    else{
                        turn = "right";
                    }
                }
                else{
                    if(prevDirection > currEdge.getDirection()){
                        turn = "right";
                    }
                    else{
                        turn = "left";
                    }
                }
            }
            else{
                turn = "no";
            }

            Toast.makeText(getApplicationContext(), turn + " turn",Toast.LENGTH_LONG).show();
        }

        prevDirection = currEdge.getDirection();



       /* Toast.makeText(getApplicationContext(), "Parent Node  x:"+Float.toString(prevNode.getxPos())
                        +" y:"+Float.toString(prevNode.getyPos())+" Child Node x:"+Float.toString(newNode.getxPos())
                +" y:"+Float.toString(newNode.getxPos())+" Edge direction:"+Float.toString(currEdge.getDirection())
                +" weight:"+Float.toString(currEdge.getWeight()),Toast.LENGTH_LONG).show();
                */

        prevNode = newNode;
        //clear data and start recording for next edge
        //mService.clearData();
    }

    public void clrButton(View view){
        mService.clearData();
    }

    public float aveDir(ArrayList<LogData> currData){
        int direction=0;
        for(int i=0; i< currData.size(); i++) {
            direction+=currData.get(i).getDirection();
        }
        return direction/currData.size();
    }

    private ServiceConnection mConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName className, IBinder service){
            EdgeLogService.LocalBinder binder = (EdgeLogService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0){
            mBound = false;
        }
    };
}
