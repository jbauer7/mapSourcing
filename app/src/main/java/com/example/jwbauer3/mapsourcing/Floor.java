package com.example.jwbauer3.mapsourcing;

import android.graphics.drawable.Drawable;

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

    public Floor()
    {
        this.floorNum = -1;
        meshReferenceState = null;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public Floor(int floorNum, ArrayList<Node> nodes, ArrayList<Edge> edges, ReferenceState referenceState, Drawable image) {


        this.floorNum = floorNum;
        meshReferenceState = referenceState;
        setImageInfo(image);
        this.nodes = nodes;
        this.edges = edges;
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
