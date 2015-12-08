package com.example.jwbauer3.mapsourcing;

import java.util.ArrayList;

/**
 * created by Eric on 12/2/15.
 */
public abstract class BaseNode extends CanvasDrawable {

    private int xPos, yPos, defaultXPos, defaultYPos;
    private ArrayList<Edge> edges;
    private int floor;

    public BaseNode(int xPos, int yPos, int floor, final int nodePriority) {
        super(nodePriority);
        this.xPos = xPos;
        this.yPos = yPos;
        defaultXPos = xPos;
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
        xPos = (int) (defaultXPos * scaleFactor);
        yPos = (int) (defaultYPos * scaleFactor);
    }

    public boolean equals(Object toCompare) {
        if (!(toCompare instanceof BaseNode)) {
            return false;
        } else if (!(this.getClass().equals(toCompare.getClass()))) {
            return false;
        } else {
            return ((BaseNode) toCompare).getxPos() == this.getxPos() && ((BaseNode) toCompare).getyPos() == this.getyPos();
        }
    }

    public int getDefaultXPos() {
        return defaultXPos;
    }

    public int getDefaultYPos() {
        return defaultYPos;
    }

    public int getFloorNum() {
        return floor;
    }
}
