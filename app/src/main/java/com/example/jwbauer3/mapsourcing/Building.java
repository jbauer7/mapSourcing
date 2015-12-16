package com.example.jwbauer3.mapsourcing;

/**
 * Created by Eric on 12/16/15.
 */
public class Building {

    private long id;
    private String name;

    public Building(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
