package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * created by Nikhil on 10/11/2015.
 */
public class Edge extends CanvasDrawable {

    private static final int DEFAULTEDGEPRIORITY = 100;
    private static final int DRAWNLINEWIDTH = 25;
    private Node start, end;
    private int weight, direction;
    //protected EdgeData edgeData;

    public Edge(Node start, Node end) {
        super(DEFAULTEDGEPRIORITY);
        this.start = start;
        this.end = end;
        weight = 0;
        direction = 0;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public int getWeight() {
        return weight;
    }

    public int getDirection() {
        return direction;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    //can implement getPriroity;

    @Override
    public void draw(Canvas canvas, int xOffset, int yOffset) {
        //Draw all the edges
        Paint paint = new Paint();
        //Update the paintbrush to make lines (for edges)
        //paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(DRAWNLINEWIDTH);
        if (this.attributes.contains("clicked")) {
            paint.setColor(Color.parseColor("#AAAAAA"));
        } else {
            paint.setColor(Color.parseColor("#7070FF"));
        }

        int xStart = this.getStart().getxPos();
        int yStart = this.getStart().getyPos();
        int xEnd = this.getEnd().getxPos();
        int yEnd = this.getEnd().getyPos();
        canvas.drawLine(xStart + xOffset, yStart + yOffset, xEnd + xOffset, yEnd + yOffset, paint);
    }

    @Override
    public boolean contains(int clickedX, int clickedY, int transXoffset, int transYoffset, float scaleFactor) {
        int xStart = (int) ((this.getStart().getxPos() + transXoffset) * scaleFactor);
        int yStart = (int) ((this.getStart().getyPos() + transYoffset) * scaleFactor);
        int xEnd = (int) ((this.getEnd().getxPos() + transXoffset) * scaleFactor);
        int yEnd = (int) ((this.getEnd().getyPos() + transYoffset) * scaleFactor);

        //we are using herons formula to determine calculate the area of the triangle created.
        //once we have the area we can find the height with respect to our inital line (b) by doing
        //height = 2*Area/b
        //Formula found from http://www.mathopenref.com/heronsformula.html

        //length of each side
        //side a is from start to click
        double a = Math.sqrt(Math.pow(clickedX - xStart, 2) + Math.pow(clickedY - yStart, 2));

        //side b is from start to end
        double b = Math.sqrt(Math.pow(xStart - xEnd, 2) + Math.pow(yStart - yEnd, 2));

        //side c is from clicked to end
        double c = Math.sqrt(Math.pow(clickedX - xEnd, 2) + Math.pow(clickedY - yEnd, 2));

        //ensure that the point clicked is inside the line constraints, ie: not along the infinite line
        //to do this, we check to see if our constructed triangle is acute or obtuse.
        //the former could be inside, the latter cannot.
        //Algorithm is based on pythag formula.
        if(c*c > (b*b) + (a*a)){
            //our triangle is obtuse b/c point a is outside
            return false;
        }
        else if(a*a > (b*b) + (c*c)){
            //our triangle is obtuse b/c point c is outside
            return false;
        }

        //now our point is 'bounded by the edges of the rectangle (inside, either above or below)
        //determine height to see if we are bounded by the other set of edges

        //calculate height
        double height = Math.sqrt((-a - b - c) * (a - b - c) * (a + b - c) * (a - b + c)) / (2 * b);

        //check to see if the height is less than our scaled width of the line.
        //take the half because the drawnlinewidth is the entire line, we only can allow for half of that.
        return (height <= (DRAWNLINEWIDTH/2.0)*scaleFactor);
    }

    public boolean equals(Object toCompare) {
        if (!(toCompare instanceof Edge)) {
            return false;
        }
        Edge comp = (Edge) toCompare;
        return ((comp.getStart().getxPos() == this.getStart().getxPos()) &&
                (comp.getStart().getyPos() == this.getStart().getyPos()) &&
                (comp.getEnd().getxPos() == this.getEnd().getxPos()) &&
                (comp.getEnd().getyPos() == this.getEnd().getyPos()));
    }

}
