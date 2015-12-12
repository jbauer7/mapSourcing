package com.example.jwbauer3.mapsourcing;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * created by Nikhil on 10/11/2015.
 */
public class Node extends BaseNode implements Serializable {

    private static final int DEFAULTNODEPRIORITY = 200;
    private static final int DEFAULTRADIUS = 25;
    private int drawnRadius;
    private boolean stairNode = false;

    //Persistence variables
    private static SharedPreferences sharedPreferences;
    private static String PREF_NAME = "FloorStorage";

    public Node(int xPos, int yPos, int floor, boolean stair) {
        super(xPos, yPos, floor, DEFAULTNODEPRIORITY);
        drawnRadius = DEFAULTRADIUS;
        //todo: fix hardcoding
        //todo: why not just store MenuSelection Enums, and have MyView create them on the fly,
        //todo: that way we can dynamically create them wherever they need to be...
        //MenuOption newOpt = new MenuOption(this, 0, MenuSelection.START);
        //MenuOption newOpt2 = new MenuOption(this, 1, MenuSelection.END);
        options.add(MenuSelection.START);
        options.add(MenuSelection.END);
        //todo: determine if this is a stairNode here, or from passed in.
        stairNode = stair;
    }

    @Override
    public void draw(Canvas canvas, int xOffset, int yOffset) {
        //magic number 100, represents radius of node. Might be passed in from MyView, might be a class var
        Paint paint = new Paint();
        if (this.attributes.contains(Attribute.TERMINAL)) { //either start node or end node
            paint.setColor(Color.parseColor("#00ff00"));
        } else if (this.attributes.contains(Attribute.PATH)) { //apart of the path
            paint.setColor(Color.parseColor("#ff69b4"));
        } else { //default, nothing special about the node
            paint.setColor(Color.parseColor("#CD5C5C"));
        }
        //if clicked, just darken the color, maintain other info, but lets you know its been clicked.
        if (this.attributes.contains(Attribute.CLICKED)) {
            int color = paint.getColor();
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            //todo: magic number
            hsv[2] = hsv[2] * 0.85f;
            color = Color.HSVToColor(hsv);
            paint.setColor(color);
        }
        if(stairNode){
            int prevColor = paint.getColor();
            paint.setColor(Color.parseColor("#ff5500"));
            //todo: is 30% larger enough? Also hardcoding fix needed.
            canvas.drawCircle(this.getxPos() + xOffset, this.getyPos() + yOffset, (int) (drawnRadius * (1.3)), paint);
            paint.setColor(prevColor);
        }
        canvas.drawCircle(this.getxPos() + xOffset, this.getyPos() + yOffset, drawnRadius, paint);

    }

    @Override
    public boolean contains(int clickedX, int clickedY, int transXoffset, int transYoffset, float scaleFactor) {
        //transxoffset and transyoffset include translated and scale factor already
        //transXoffset = xOffset + transX/ScaleFactor, Y is just for Y values
        int displayedRadius = (int) (drawnRadius * scaleFactor);
        int scaledXPosition = (int) ((this.getxPos() + transXoffset) * scaleFactor);
        int scaledYPosition = (int) ((this.getyPos() + transYoffset) * scaleFactor);
        return (Math.sqrt(Math.pow(clickedX - scaledXPosition, 2) + Math.pow(clickedY - scaledYPosition, 2)) <= displayedRadius);
    }

    public void setScaleFactor(float scaleFactor) {
        drawnRadius = (int) (DEFAULTRADIUS * scaleFactor);
        super.setScaleFactor(scaleFactor);
    }

}
