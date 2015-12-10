package com.example.jwbauer3.mapsourcing;

/**
 * Created by Eric on 12/9/15.
 */
public class Edge extends BaseEdge {

    private static final int DEFAULTEDGEPRIORITY = 100;

    public Edge(BaseNode start, BaseNode end) {
        super(DEFAULTEDGEPRIORITY, start, end);
    }
}
