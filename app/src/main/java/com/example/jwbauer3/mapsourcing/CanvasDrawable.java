package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;

import java.util.ArrayList;

/**
 * created by Nikhil on 10/18/2015.
 */
public abstract class CanvasDrawable {

    protected int priority;
    protected ArrayList<MenuSelection> options;
    protected ArrayList<Attribute> attributes;
    //default scaleFactor is 1f.
    protected float scaleFactor = 1f;
    protected float darkenOnClick = .85f;

    public CanvasDrawable(int priority) {
        //this.options = options;
        this.priority = priority;
        attributes = new ArrayList<>();
        options = new ArrayList<>();
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
    public abstract void draw(Canvas canvas);

    /*
    Does the element contain the mapX and mapY point.
     */
    public abstract boolean contains(int mapX, int mapY);

    public abstract void setScaleFactor(float scaleFactor);

    public float getScaleFactor(){
        return scaleFactor;
    }

    /*
    Toggle an attribute on or off,
     */
    public void toggleAttribute(Attribute attribute){
        if(!attributes.contains(attribute)){
            attributes.add(attribute);
        }
        else{
            attributes.remove(attribute);
        }
    }
    public ArrayList<MenuSelection> getOptions(){
        return options;
    }

}
