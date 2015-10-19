package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Looper;
import android.widget.Toast;

import com.example.jwbauer3.mapsourcing.Node;

/**
 * created by Nikhil on 10/11/2015.
 */
public class Edge extends Drawable {

    private static final int DEFAULTEDGEPRIORITY = 100;
    private Node start, end;
    private int weight, direction;
    protected EdgeData edgeData;

    public Edge(Node start, Node end) {
        super(DEFAULTEDGEPRIORITY);
        this.start = start;
        this.end = end;
        weight = 0;
        direction = 0;
    }



    public void startLogEdgeData()
    {
        new Thread(new Runnable() {

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
        }).start();
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public int getWeight() {
        return weight;
    }

    public int getDirection() {
        return direction;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    //can implement getPriroity;

    @Override
    public void draw(Canvas canvas, int xOffset, int yOffset) {
        //Draw all the edges
        Paint paint = new Paint();
        //Update the paintbrush to make lines (for edges)
        //paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(25);
        if(this.attributes.contains("clicked")) {
            paint.setColor(Color.parseColor("#AAAAAA"));
        }else{
            paint.setColor(Color.parseColor("#7070FF"));
        }

        int xStart = this.getStart().getxPos();
        int yStart = this.getStart().getyPos();
        int xEnd = this.getEnd().getxPos();
        int yEnd = this.getEnd().getyPos();
        canvas.drawLine(xStart + xOffset, yStart + yOffset, xEnd + xOffset, yEnd + yOffset, paint);
    }

    @Override
    public boolean contains(int xPos, int yPos, int xOffset, int yOffset) {
        //TODO: implement algorithm that can determine if the point is on the line
        //TODO: get access to the width of the line
        return false;
    }

    public boolean equals(Object toCompare) {
        if (!(toCompare instanceof Edge)) {
            return false;
        }
        Edge comp = (Edge) toCompare;
        if (comp.getStart().getxPos() == this.getStart().getxPos() &&
                comp.getStart().getyPos() == this.getStart().getyPos() &&
                comp.getEnd().getxPos() == this.getEnd().getxPos() &&
                comp.getEnd().getyPos() == this.getEnd().getyPos()) {
            return true;
        }
        return false;
    }

}
