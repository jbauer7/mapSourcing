package com.example.jwbauer3.mapsourcing;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * created by Nikhil on 11/18/2015.
 */
public class Floor {


    protected int floorNum;

    protected ArrayList<Node> nodes;
    protected ArrayList<Edge> edges;
    protected ReferenceState meshReferenceState;

    protected int backgroundWidth; //image
    protected int backgroundHeight; //image
    protected Drawable backgroundImage;
    protected float maxMeshScaleFactor = -1f;

    //todo: do we need to store these? We need to recalculate all the
    //private int originalMinXOffset;
    //private int originalMinYOffset;
    //private int originalMaxX;
    //private int originalMaxY;

    public Floor()
    {
        this.floorNum = -1;
        meshReferenceState = null;
        this.nodes = new ArrayList<Node>();
        this.edges = new ArrayList<Edge>();
    }

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
        this.nodes = new ArrayList<Node>();
        this.edges = new ArrayList<Edge>();
        this.nodes.addAll(nodes);
        this.edges.addAll(edges);
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
