package com.example.jwbauer3.mapsourcing;

import android.os.Looper;
import android.widget.Toast;

import com.example.jwbauer3.mapsourcing.Node;

/**
 * Created by Nikhil on 10/11/2015.
 */
public class Edge {


    private Node start, end;
    private int weight, direction;
    protected EdgeData edgeData;

    public Edge(Node start, Node end){
        this.start = start;
        this.end = end;
        weight = 0;
        direction =0;
    }

    public void startLogEdgeData()
    {
        /*new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                edgeData = new EdgeData();
                int numOfLogs = 10;
                while(true)
                {
                    edgeData.logData();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    numOfLogs--;
                    if (numOfLogs == 0)
                    {
                        break;
                    }
                }
            }
        }).start(); */
    }

    public Node getStart() {return start;}
    public Node getEnd() {return end;}
    public int getWeight(){return weight;}
    public int getDirection(){return direction;}

    public void setWeight(int weight){this.weight=weight;}
    public void setDirection(int direction){this.direction= direction;}

}
