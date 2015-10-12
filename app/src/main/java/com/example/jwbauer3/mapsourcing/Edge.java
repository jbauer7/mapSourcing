package com.example.jwbauer3.mapsourcing;

import com.example.jwbauer3.mapsourcing.Node;

/**
 * Created by Nikhil on 10/11/2015.
 */
public class Edge {


    private Node start, end;

    public Edge(Node start, Node end){
        this.start = start;
        this.end = end;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }
}
