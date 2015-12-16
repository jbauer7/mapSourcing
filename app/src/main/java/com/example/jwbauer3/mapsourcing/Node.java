package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.Serializable;

/**
 * created by Nikhil on 10/11/2015.
 */
public class Node extends BaseNode implements Serializable {

    private static final int DEFAULTNODEPRIORITY = 200;
    private static final int DEFAULTRADIUS = 25;
    private boolean stairNode = false;

    private double stairSizeModifier = 1.3; //30% larger

    public Node(int xPos, int yPos, int floor, boolean stair) {
        super(xPos, yPos, floor, DEFAULTRADIUS, DEFAULTNODEPRIORITY);
        drawnRadius = DEFAULTRADIUS;
        //options.add(MenuSelection.START);
        //options.add(MenuSelection.END);
        stairNode = stair;
    }

    public Node(int xPos, int yPos, int floor) {
        this(xPos, yPos, floor, false);
    }

    //For creating from DatabaseHelper
    protected Node(long id, int defaultXPos, int defaultYPos, int xPos, int yPos, boolean stairNode, int floorNum) {
        super(DEFAULTNODEPRIORITY, id, defaultXPos, defaultYPos, xPos, yPos, floorNum);
        this.stairNode = stairNode;
    }

    //For saving to DatabaseHelper
    protected boolean getIsStairNode() {
        return this.stairNode;
    }

    @Override
    public void draw(Canvas canvas) {
        //magic number 100, represents radius of node. Might be passed in from CanvasView, might be a class var
        Paint paint = new Paint();
        if (this.attributes.contains(Attribute.PATH)) { //apart of the path
            paint.setColor(Application.getResColor(R.color.PathNodeColor));
        } else { //default, nothing special about the node
            paint.setColor(Application.getResColor(R.color.DefaultNodeColor));
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
        if (stairNode) {
            int prevColor = paint.getColor();
            paint.setColor(Application.getResColor(R.color.StairNodeBorderColor));
            canvas.drawCircle(this.getxPos(), this.getyPos(), (int) (drawnRadius * (stairSizeModifier)), paint);
            paint.setColor(prevColor);
        }

        canvas.drawCircle(this.getxPos(), this.getyPos(), drawnRadius, paint);

    }

    public void setScaleFactor(float scaleFactor) {
        drawnRadius = (int) (DEFAULTRADIUS * scaleFactor);
        super.setScaleFactor(scaleFactor);
    }

}
