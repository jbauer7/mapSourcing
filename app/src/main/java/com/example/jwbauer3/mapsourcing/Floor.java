package com.example.jwbauer3.mapsourcing;

import java.util.ArrayList;

/**
 * created by Nikhil on 11/18/2015.
 */
public class Floor {

    protected int floorNum;

    protected ArrayList<Node> nodes;
    protected ArrayList<Edge> edges;
    protected ReferenceState meshReferenceState;

    protected int backgroundImageResId;
    protected float maxMeshScaleFactor = -1f;

    public Floor() {
        this.floorNum = -1;
        meshReferenceState = null;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public Floor(int floorNum, ArrayList<Node> nodes, ArrayList<Edge> edges, ReferenceState referenceState, int backgroundImageResId) {
        this.floorNum = floorNum;
        meshReferenceState = referenceState;
        this.backgroundImageResId = backgroundImageResId;
        this.nodes = nodes;
        this.edges = edges;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    //todo: see how mapping algo passes in drawables, update accordingly
    public void setNodesEdges(ArrayList<Node> nodes, ArrayList<Edge> edges) {
        this.nodes.addAll(nodes);
        this.edges.addAll(edges);
    }

    public ReferenceState getMeshReferenceState() {
        return meshReferenceState;
    }

    public int getBackgroundImageResId(){
        return backgroundImageResId;
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
