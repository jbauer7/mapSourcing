package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;

/**
 * created by Nikhil on 10/11/2015.
 */
public class Node extends CanvasDrawable {

    private static final int DEFAULTNODEPRIORITY = 200;
    private static final int DEFAULTRADIUS = 25;
    private static int drawnRadius;
    private int xPos, yPos, defaultxPos, defaultYPos;
    private ArrayList<Edge> edges;

    public Node(int xPos, int yPos) {
        super(DEFAULTNODEPRIORITY);
        this.xPos = xPos;
        this.yPos = yPos;
        defaultxPos = xPos;
        defaultYPos = yPos;
        edges = new ArrayList<>();
        drawnRadius = DEFAULTRADIUS;
        OptionsMenuOption newOpt = new OptionsMenuOption(this, 0, "Test");
        options.add(newOpt);
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
        if (this.attributes.contains("clicked")) {
            //canvas.drawRect(this.getxPos() + xOffset, this.getyPos() + yOffset, this.getxPos() + xOffset + 250, this.getyPos() + yOffset + 250, paint);
            paint.setColor(Color.parseColor("#66FF33"));
            //canvas.drawRect(this.getxPos() + xOffset, this.getyPos() + yOffset, drawnRadius, paint);
        } else {
            paint.setColor(Color.parseColor("#CD5C5C"));
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
        drawnRadius = (int) (DEFAULTRADIUS * scaleFactor);
        xPos = (int) (defaultxPos * scaleFactor);
        yPos = (int) (defaultYPos * scaleFactor);
    }
    public ArrayList<OptionsMenuOption> getOptions(){
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
    public int getMenuStartX(){
        return xPos;
    }
    public int getMenuStartY(){
        return yPos;
    }
    public int getDrawnRadius() {
        return drawnRadius;
    }
}
