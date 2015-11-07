package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;

import java.util.ArrayList;

/**
 * Created by Nikhil on 10/18/2015.
 */
public abstract class CanvasDrawable {

    protected int priority;
    protected ArrayList<String> attributes;
    public CanvasDrawable(int priority) {
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

    /*
    Does the element contain the point xPos, yPos given the current OffsetValues and
     */
    public abstract boolean contains(int xPos,int yPos, int xOffset, int yOffset, float canvasScaleFactor);

    public abstract void setScaleFactor(float scaleFactor);

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
