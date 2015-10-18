package com.example.jwbauer3.mapsourcing;

import com.example.jwbauer3.mapsourcing.Node;

/**
 * Created by Nikhil on 10/11/2015.
 */
public class Edge {


    private Node start, end;
    private int weight, direction;

    public Edge(Node start, Node end){
        this.start = start;
        this.end = end;
        weight = 0;
        direction =0;
    }

    public Node getStart() {return start;}
    public Node getEnd() {return end;}
    public int getWeight(){return weight;}
    public int getDirection(){return direction;}

    public void setWeight(int weight){this.weight=weight;}
    public void setDirection(int direction){this.direction= direction;}

}
