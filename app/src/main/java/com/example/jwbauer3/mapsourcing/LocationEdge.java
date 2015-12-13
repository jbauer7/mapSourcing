package com.example.jwbauer3.mapsourcing;

/**
 * created by Eric on 12/9/15.
 */
public class LocationEdge extends BaseEdge {

    private static final int DEFAULTLOCATIONEDGEPRIORITY = 150;

    public LocationEdge(BaseNode start, BaseNode end) {
        super(DEFAULTLOCATIONEDGEPRIORITY, start, end);
    }
}
