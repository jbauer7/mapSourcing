package com.example.jwbauer3.mapsourcing;

import java.util.ArrayList;

/**
 * Created by Nikhil on 10/11/2015.
 */
public class Node {

    private int xPos, yPos;
    private ArrayList<Edge> edges;
    public Node(int xPos, int yPos){
        this.xPos = xPos;
        this.yPos = yPos;
        edges = new ArrayList<Edge>();
    }
    public int getxPos(){return xPos;}
    public int getyPos(){return  yPos;}

    //could store radius, color, pixel area coverage.
    public ArrayList<Edge> getEdges(){return edges;}
    public void setEdges(Edge e){this.edges.add(e);}

}
