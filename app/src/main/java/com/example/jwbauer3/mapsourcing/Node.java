package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by Nikhil on 10/11/2015.
 */
public class Node extends Drawable {

    private static final int DEFAULTNODEPRIORITY = 200;
    private int xPos, yPos;
    private ArrayList<Edge> edges;

    public Node(int xPos, int yPos) {
        super(DEFAULTNODEPRIORITY);
        this.xPos = xPos;
        this.yPos = yPos;
        edges = new ArrayList<Edge>();
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    //could store radius, color, pixel area coverage.
    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void setEdges(Edge e) {
        this.edges.add(e);
    }

    @Override
    public void draw(Canvas canvas, int xOffset, int yOffset) {
        //magic number 100, represents radius of node. Might be passed in from MyView, might be a class var
        Paint paint = new Paint();
        if(this.attributes.contains("clicked")) {
            paint.setColor(Color.parseColor("#66FF33"));
        }
        else{
            paint.setColor(Color.parseColor("#CD5C5C"));
        }
        canvas.drawCircle(this.getxPos() + xOffset, this.getyPos() + yOffset, 100, paint);
    }

    @Override
    public boolean contains(int xPos, int yPos, int xOffset, int yOffset) {
        //TODO: MAGIC NUMBER FOR RADIUS
        return (Math.sqrt(Math.pow(xPos - (this.getxPos() + xOffset), 2) + Math.pow(yPos - (this.getyPos() + yOffset), 2)) <= 100);
    }

    public boolean equals(Object toCompare) {
        if (!(toCompare instanceof Node)) {
            return false;
        } else if (((Node) toCompare).getxPos() == this.getxPos() && ((Node) toCompare).getyPos() == this.getyPos()) {
            return true;
        } else {
            return false;
        }
    }
}
