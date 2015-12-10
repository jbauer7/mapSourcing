package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * created by Nikhil on 10/11/2015.
 */
public class Node extends BaseNode {

    private static final int DEFAULTNODEPRIORITY = 200;
    private static final int DEFAULTRADIUS = 25;
    private int drawnRadius;
    private boolean stairNode = false;
    private double stairSizeModifier = 1.3; //30% larger

    public Node(int xPos, int yPos, int floor, boolean stair) {
        super(xPos, yPos, floor, DEFAULTNODEPRIORITY);
        drawnRadius = DEFAULTRADIUS;
        options.add(MenuSelection.START);
        options.add(MenuSelection.END);
        stairNode = stair;
    }

    @Override
    public void draw(Canvas canvas, int xOffset, int yOffset) {
        //magic number 100, represents radius of node. Might be passed in from MyView, might be a class var
        Paint paint = new Paint();
        if (this.attributes.contains(Attribute.PATH)) { //apart of the path
            paint.setColor(Color.parseColor("#ff69b4"));
        } else { //default, nothing special about the node
            paint.setColor(Color.parseColor("#CD5C5C"));
        }
        //if clicked, just darken the color, maintain other info, but lets you know its been clicked.
        if (this.attributes.contains(Attribute.CLICKED)) {
            int color = paint.getColor();
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            hsv[2] = hsv[2] * darkenOnClick;
            color = Color.HSVToColor(hsv);
            paint.setColor(color);
        }
        if(stairNode){
            int prevColor = paint.getColor();
            paint.setColor(Color.parseColor("#ff5500"));
            canvas.drawCircle(this.getxPos() + xOffset, this.getyPos() + yOffset, (int) (drawnRadius * (stairSizeModifier)), paint);
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
