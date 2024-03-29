package com.example.jwbauer3.mapsourcing;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * created by Eric on 12/2/15.
 */
public abstract class BaseNode extends CanvasDrawable implements Serializable {

    private int xPos, yPos, defaultXPos, defaultYPos;
    private ArrayList<BaseEdge> edges;
    private int floor;
    private float altitude;
    protected String nodeRefString;
    protected int drawnRadius;
    protected long id = -1; //For saving to DatabaseHelper

    public BaseNode(int xPos, int yPos, int floor, int drawnRadius, final int nodePriority) {
        super(nodePriority);
        this.xPos = xPos;
        this.yPos = yPos;
        defaultXPos = xPos;
        defaultYPos = yPos;
        edges = new ArrayList<>();
        this.floor = floor;
        nodeRefString = floor + "_" + xPos + "_" + yPos + "_" + nodePriority;
        this.drawnRadius = drawnRadius;
    }

    //For creating from DatabaseHelper
    protected BaseNode(final int nodePriority, long id, int defaultXPos, int defaultYPos, int xPos, int yPos, int floorNum) {
        super(nodePriority);
        this.id = id;
        this.defaultXPos = defaultXPos;
        this.defaultYPos = defaultYPos;
        this.xPos = xPos;
        this.yPos = yPos;
        this.edges = new ArrayList<>();
        this.floor = floorNum;
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public void removeEdge(BaseEdge edge) {

        edges.remove(edge);
    }

    public void clearEdges() {
        edges.clear();
    }

    public ArrayList<BaseEdge> getEdges() {
        return edges;
    }

    public void addEdge (BaseEdge e) {
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

    public void setAltitude(float altitude){
        this.altitude=altitude;
    }

    public float getAltitude(){
        return altitude;
    }

    public boolean contains(int mapX, int mapY) {
        return (Math.sqrt(Math.pow(mapX - this.getxPos(), 2) + Math.pow(mapY - this.getyPos(), 2)) <= drawnRadius);
    }
}
