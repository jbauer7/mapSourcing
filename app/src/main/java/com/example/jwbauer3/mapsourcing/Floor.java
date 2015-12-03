package com.example.jwbauer3.mapsourcing;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

/**
 * created by Nikhil on 11/18/2015.
 */
public class Floor {
    private int floorNum;

    private ArrayList<Node> nodes;
    private ArrayList<Edge> edges;
    private ReferenceState meshReferenceState;

    private int backgroundWidth; //image
    private int backgroundHeight; //image
    private Drawable backgroundImage;
    private float maxMeshScaleFactor = -1f;

    //todo: do we need to store these? We need to recalculate all the
    //private int originalMinXOffset;
    //private int originalMinYOffset;
    //private int originalMaxX;
    //private int originalMaxY;

    public Floor(int floorNum, ArrayList<Node> nodes, ArrayList<Edge> edges, ReferenceState referenceState, Drawable image) {
        this.floorNum = floorNum;
        meshReferenceState = referenceState;
        setImageInfo(image);
        this.nodes = nodes;
        this.edges = edges;
        //originalMinXOffset=originalMinYOffset=originalMaxX=originalMaxY=0;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    private void setImageInfo(Drawable image) {
        backgroundImage = image;
        backgroundHeight = image.getMinimumHeight();
        backgroundWidth = image.getMinimumWidth();
        backgroundImage.setBounds(0, 0, backgroundWidth, backgroundHeight);
    }

    //todo: see how mapping algo passes in drawables, update accordingly
    public void setNodesEdges(ArrayList<Node> nodes, ArrayList<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public ReferenceState getMeshReferenceState() {
        return meshReferenceState;
    }

    public int getBackgroundWidth(){
        return backgroundWidth;
    }
    public int getBackgroundHeight(){
        return backgroundHeight;
    }
    public Drawable getBackgroundImage(){
        return backgroundImage;
    }
    public int getFloorNum(){
        return floorNum;
    }
    public void setMaxMeshScaleFactor(float sf){
        maxMeshScaleFactor = sf;
    }
    public float getMaxMeshScaleFactor(){
        return maxMeshScaleFactor;
    }


}
