package com.example.jwbauer3.mapsourcing;

import java.io.Serializable;

/**
 * created by Eric on 12/9/15.
 */
public class Edge extends BaseEdge implements Serializable {

    private static final int DEFAULTEDGEPRIORITY = 100;

    public Edge(BaseNode start, BaseNode end) {
        super(DEFAULTEDGEPRIORITY, start, end);
    }

    //For creating from DatabaseHelper
    protected Edge(BaseNode start, BaseNode end, int weight, int direction) {
        super(DEFAULTEDGEPRIORITY, start, end, weight, direction);
    }
}
