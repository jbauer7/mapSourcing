package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import java.util.ArrayList;

/**
 * created by Nikhil on 10/11/2015.
 */
public class Node extends CanvasDrawable {

    private static final int DEFAULTNODEPRIORITY = 200;
    private static final int DEFAULTRADIUS = 25;
    private int drawnRadius;
    private int xPos, yPos, defaultxPos, defaultYPos;
    private ArrayList<Edge> edges;
    private int floor;

    public Node(int xPos, int yPos, int floor) {
        super(DEFAULTNODEPRIORITY);
        this.xPos = xPos;
        this.yPos = yPos;
        defaultxPos = xPos;
        defaultYPos = yPos;
        edges = new ArrayList<>();
        drawnRadius = DEFAULTRADIUS;
        this.floor = floor;
        //todo: fix hardcoding
        MenuOption newOpt = new MenuOption(this, 0, "Start", 4000, 2400);
        MenuOption newOpt2 = new MenuOption(this, 1, "End", 4000, 2400);
        options.add(newOpt);
        options.add(newOpt2);
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
        if (this.attributes.contains("path")) {
            paint.setColor(Color.parseColor("#ff69b4"));
        } else {
            paint.setColor(Color.parseColor("#CD5C5C"));
        }
        //if clicked, just darken the color, maintain other info, but lets you know its been clicked.
        if (this.attributes.contains("clicked")) {
            int color = paint.getColor();
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            //todo: magic number
            hsv[2]=hsv[2]*0.75f;
            color = Color.HSVToColor(hsv);
            paint.setColor(color);
        }
        canvas.drawCircle(this.getxPos() + xOffset, this.getyPos() + yOffset, drawnRadius, paint);
    }

    @Override
    public boolean contains(int clickedX, int clickedY, int transXoffset, int transYoffset, float scaleFactor) {
        //transxoffset and transyoffset include translated and scale factor already
        //transXoffset = xOffset + transX/ScaleFactor, Y is just for Y values
        int displayedRadius = (int) (drawnRadius * scaleFactor);
        int scaledXPosition = (int) ((this.getxPos() + transXoffset) * scaleFactor);
        int scaledYPosition = (int) ((this.getyPos() + transYoffset) * scaleFactor);
        return (Math.sqrt(Math.pow(clickedX - scaledXPosition, 2) + Math.pow(clickedY - scaledYPosition, 2)) <= displayedRadius);
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        drawnRadius = (int) (DEFAULTRADIUS * scaleFactor);
        xPos = (int) (defaultxPos * scaleFactor);
        yPos = (int) (defaultYPos * scaleFactor);
    }

    public ArrayList<MenuOption> getOptions() {
        return options;
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

    public int getMenuStartX() {
        return xPos;
    }

    public int getMenuStartY() {
        return yPos;
    }

    public int getDrawnRadius() {
        return drawnRadius;
    }
}
