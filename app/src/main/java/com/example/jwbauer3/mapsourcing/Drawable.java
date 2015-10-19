package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;

import java.util.ArrayList;

/**
 * Created by Nikhil on 10/18/2015.
 */
public abstract class Drawable {

    protected int priority;
    protected ArrayList<String> attributes;
    public Drawable(int priority) {
        this.priority = priority;
        attributes = new ArrayList<>();
    }

    /*
    Gets the priority of an element
     */
    public int getPriority(){
        return priority;
    }

    /*
    Has the element draw itself upon the canvas
     */
    public abstract void draw(Canvas canvas, int xOffset, int yOffset);


    public abstract boolean contains(int xPos,int yPos, int xOffset, int yOffset);

    /*
    Toggle an attribute on or off,
    TODO: replace with enum?
     */
    public void toggleAttribute(String attribute){
        if(!attributes.contains(attribute)){
            attributes.add(attribute);
        }
        else{
            attributes.remove(attribute);
        }
    }





}
