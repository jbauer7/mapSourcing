package com.example.jwbauer3.mapsourcing;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;


public class MapActivity extends AppCompatActivity {

    private boolean mapping = false;
    private Node firstNode;
    private Node prevNode;
    EdgeLogService mService;
    boolean mBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Node firstNode = new Node(0,0);
        prevNode = firstNode;

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
        float direction=0;
        for(int i=0; i< currData.size(); i++) {
            direction+=currData.get(i).getDirection();
        }
        direction/=currData.size();
        int steps= (int) currData.get(currData.size()-1).getSteps();

      //log data steps direction
        Node newNode = new Node(prevNode.getxPos()-steps*(int)Math.cos((double) direction+90.0),prevNode.getyPos()+ steps*(int)Math.sin((double) direction+90.0));
        Edge currEdge= new Edge(prevNode, newNode);
        currEdge.setDirection((int) direction);
        currEdge.setWeight((int) currData.get(currData.size() - 1).getSteps());
        prevNode.setEdges(currEdge);
        newNode.setEdges(currEdge);
        prevNode=newNode;

      //  Toast.makeText(getApplicationContext(), "CurrSteps : " +
              //  Float.toString(mService.getCurrSteps()),Toast.LENGTH_SHORT).show();
        mService.clearData();
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
