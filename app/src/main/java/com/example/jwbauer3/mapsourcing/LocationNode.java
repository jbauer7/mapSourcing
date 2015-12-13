package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * created by Eric on 12/2/15.
 */
public class LocationNode extends BaseNode {

    private static final int DEFAULTLOCATIONNODEPRIORITY = 250;
    private static final int DEFAULTRADIUS = 100;
    private Edge sourceEdge;
    private LocationEdge startEdge, endEdge;
    private static final double sliceStartAngle = 240;
    private static final double sliceSweepAngle = 60;
    private static final double sliceStartAngleVectorX = Math.cos(Math.toRadians(sliceStartAngle));
    private static final double sliceStartAngleVectorY = Math.sin(Math.toRadians(sliceStartAngle));
    private static final double sliceEndAngleVectorX = Math.cos(Math.toRadians(sliceStartAngle + sliceSweepAngle));
    private static final double sliceEndAngleVectorY = Math.sin(Math.toRadians(sliceStartAngle + sliceSweepAngle));

    public LocationNode(int xPos, int yPos, int floor, Edge sourceEdge, BaseNode before, BaseNode after) {
        super(xPos, yPos, floor, DEFAULTRADIUS, DEFAULTLOCATIONNODEPRIORITY);
        this.drawnRadius = DEFAULTRADIUS;
        setSourceEdge(sourceEdge, before, after);
        this.options.add(MenuSelection.START);
    }


    @Override
    public void draw(Canvas canvas, int xOffset, int yOffset) {
        //magic number 100, represents radius of node. Might be passed in from MyView, might be a class var
        Paint paint = new Paint();
        if (this.attributes.contains(Attribute.USER)) {
            paint.setColor(Color.parseColor("#ff0000")); //red for user
        } else if (this.attributes.contains(Attribute.DESTINATION)) { //green for destination
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
            hsv[2] = hsv[2] * darkenOnClick;
            color = Color.HSVToColor(hsv);
            paint.setColor(color);
        }
        float middleX = (float) (this.getxPos() + xOffset);
        float middleY = (float) (this.getyPos() + yOffset);
        canvas.drawArc(middleX - drawnRadius,
                middleY - drawnRadius,
                middleX + drawnRadius,
                middleY + drawnRadius,
                (float) sliceStartAngle,
                (float) sliceSweepAngle, true, paint);

    }

    private boolean isPointClockwise(double sectorVectorX, double sectorVectorY, float positionX, float positionY) {
        return (((sectorVectorX * positionY) + -(sectorVectorY * positionX)) > 0);
    }

    private boolean isWithinRadius(float positionX, float positionY, float radius) {
        return ((positionX * positionX) + (positionY * positionY)) <= (radius * radius);
    }

    private boolean isInsideSection(float positionX, float positionY, float radius) {
        return isPointClockwise(sliceStartAngleVectorX, sliceStartAngleVectorY, positionX, positionY) &&
                !isPointClockwise(sliceEndAngleVectorX, sliceEndAngleVectorY, positionX, positionY) &&
                isWithinRadius(positionX, positionY, radius);
    }


    @Override
    public boolean contains(int mapX, int mapY, float scaleFactor) {
        float displayedRadius = (drawnRadius * scaleFactor);
        return isInsideSection(mapX - this.getxPos(), mapY - this.getyPos(), displayedRadius);
    }


    public Edge getSourceEdge() {
        return sourceEdge;
    }

    public void setSourceEdge(Edge source, BaseNode before, BaseNode after) {
        this.sourceEdge = source;

        this.startEdge = new LocationEdge(before, this);
        this.endEdge = new LocationEdge(this, after);

        this.addEdge(startEdge);
        this.addEdge(endEdge);
        sourceEdge.getStart().addEdge(startEdge);
        sourceEdge.getEnd().addEdge(endEdge);
    }

    public void setStartEdge(LocationEdge startEdge) {
        this.startEdge = startEdge;
        this.addEdge(startEdge);
    }

    public LocationEdge getStartEdge() {
        return startEdge;
    }

    public void setEndEdge(LocationEdge endEdge) {
        this.endEdge = endEdge;
        this.addEdge(endEdge);
    }

    public LocationEdge getEndEdge() {
        return endEdge;
    }

    public void setScaleFactor(float scaleFactor) {
        drawnRadius = (int) (DEFAULTRADIUS * scaleFactor);
        super.setScaleFactor(scaleFactor);
    }

}
