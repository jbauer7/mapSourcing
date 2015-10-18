package com.example.jwbauer3.mapsourcing;

import java.util.ArrayList;

/**
 * Created by njaunich on 10/18/15.
 */
public class EdgeData {
    private ArrayList<Double> stepsThusFar;
    private ArrayList<Double> timestamps;
    private ArrayList<Double> direction;

    public EdgeData(){
        this.stepsThusFar = new ArrayList<>();
        this.timestamps = new ArrayList<>();
        this.direction = new ArrayList<>();

    }
    public void logData() {
        
    }
}
