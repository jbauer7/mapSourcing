package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by Eric on 12/2/15.
 */
public class LocationNode extends BaseNode {

    private static final int DEFAULTLOCATIONNODEPRIORITY = 250;
    private static final int DEFAULTRADIUS = 100;
    private int drawnRadius;

    public LocationNode(int xPos, int yPos, int floor) {
        super(xPos, yPos, floor, DEFAULTLOCATIONNODEPRIORITY);
        drawnRadius = DEFAULTRADIUS;

        //todo: fix hardcoding
        //todo: why not just store MenuSelection Enums, and have MyView create them on the fly,
        //todo: that way we can dynamically create them wherever they need to be...
        MenuOption newOpt = new MenuOption(this, 0, MenuSelection.START);
        options.add(newOpt);
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
            paint.setColor(Color.parseColor("#CDCD5C"));
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
        float middleX = (float) (this.getxPos() + xOffset);
        float middleY = (float) (this.getyPos() + yOffset);
        canvas.drawArc(middleX-drawnRadius, middleY-drawnRadius, middleX+drawnRadius, middleY+drawnRadius, 240f, 60f, true, paint);

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

}
