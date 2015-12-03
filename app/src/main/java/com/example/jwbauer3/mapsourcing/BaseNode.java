package com.example.jwbauer3.mapsourcing;

import java.util.ArrayList;

/**
 * Created by Eric on 12/2/15.
 */
public abstract class BaseNode extends CanvasDrawable {

    private int xPos, yPos, defaultxPos, defaultYPos;
    private ArrayList<Edge> edges;
    private int floor;

    public BaseNode(int xPos, int yPos, int floor, final int nodePriotity) {
        super(nodePriotity);
        this.xPos = xPos;
        this.yPos = yPos;
        defaultxPos = xPos;
        defaultYPos = yPos;
        edges = new ArrayList<>();
        this.floor = floor;
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

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        xPos = (int) (defaultxPos * scaleFactor);
        yPos = (int) (defaultYPos * scaleFactor);
    }

    public boolean equals(Object toCompare) {
        if (!(toCompare instanceof BaseNode)) {
            return false;
        } else if (!(this.getClass().equals(toCompare.getClass()))) {
            return false;
        } else {
            return ((Node) toCompare).getxPos() == this.getxPos() && ((Node) toCompare).getyPos() == this.getyPos();
        }
    }

    public int getMenuStartX() {
        return xPos;
    }

    public int getMenuStartY() {
        return yPos;
    }

    public int getFloorNum() {
        return floor;
    }
}
