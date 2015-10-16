package com.example.jwbauer3.mapsourcing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Nikhil on 10/11/2015.
 */
public class MyView extends View {


    private boolean clicked;
    private ArrayList<Node> nodes;
    private ArrayList<Node> clickedNodes = new ArrayList<>();
    private ArrayList<Edge> edges;
    private int xOffset = 0;
    private int yOffset = 0;
    private int radius = 100;

    //Android needed constructor
    public MyView(Context context) {
        super(context);
        setListener();
    }
    //Android needed constructor
    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setListener();
    }
    //Android needed constructor
    public MyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setListener();
    }
    /*
    Allows the nodes/edges to be set from an outside source
     */
    public void setNodesEdges(ArrayList<Node> nodes, ArrayList<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
        invalidate();
    }
    /*
    Sets the OnTouchListener for this view.
     */
    private void setListener() {
        //TODO: is it better to have this be public, and call only when nodes/edges are set?
            //ie: put this line inside of setNodesEdges() method?
        this.setOnTouchListener(new MyOnTouchListener());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Call the base on draw
        super.onDraw(canvas);

        //Create a paintbrush type object
        Paint paint = new Paint();

        //Set the background color
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.YELLOW);
        canvas.drawPaint(paint);

        //Update the paintbrush to make lines (for edges)
        paint.setStrokeWidth(25);
        paint.setColor(Color.parseColor("#7070FF"));

        //Draw all the edges
        for (int ctr = 0; ctr < edges.size(); ctr++) {
            Edge curEdge = edges.get(ctr);
            int xStart = curEdge.getStart().getxPos();
            int yStart = curEdge.getStart().getyPos();
            int xEnd = curEdge.getEnd().getxPos();
            int yEnd = curEdge.getEnd().getyPos();
            canvas.drawLine(xStart + xOffset, yStart + yOffset, xEnd + xOffset, yEnd + yOffset, paint);
        }

        //Update the paintbrush with the color of nodes
        paint.setColor(Color.parseColor("#66FF33"));

        //Draw all the nodes
        for (int ctr = 0; ctr < nodes.size(); ctr++) {
            Node curNode = nodes.get(ctr);
            canvas.drawCircle(curNode.getxPos() + xOffset, curNode.getyPos() + yOffset, radius, paint);
        }

        //Redraw any nodes that have been clicked
        paint.setColor(Color.parseColor("#CD5C5C"));
        for (Node node : clickedNodes) {
            canvas.drawCircle(node.getxPos() + xOffset, node.getyPos() + yOffset, radius, paint);
        }
    }

    /*
    Finds nodes by a given clicked x and y position.
    If no node, then null is returned.
     */
    private Node findNodeByPosition(int xPos, int yPos) {

        //what is the behavior if two nodes can return true?
        for (Node node : nodes) {
            if (Math.sqrt(Math.pow(xPos - (node.getxPos() + xOffset), 2) + Math.pow(yPos - (node.getyPos() + yOffset), 2)) <= radius) {
                return node;
            }
        }

        return null;
    }

    /*
    Handles the response for this
     */
    private boolean touchDown(MotionEvent event) {
        Node found = findNodeByPosition((int) event.getX(), (int) event.getY());
        if (found != null) {
            //if the node was already pressed, remove it.
            if (clickedNodes.contains(found)) {
                clickedNodes.remove(found);
            } else { //node was not currently pressed, add it.
                clickedNodes.add(found);
            }
            //update the image
            invalidate();
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = heightMeasureSpec;
        int width = widthMeasureSpec;
        if (nodes != null) {
            int minX = nodes.get(0).getxPos();
            int minY = nodes.get(0).getyPos();
            int maxX = minX;
            int maxY = minY;
            for (int x = 1; x < nodes.size(); x++) {
                Node curNode = nodes.get(x);
                if (curNode.getxPos() < minX) {
                    minX = curNode.getxPos();
                } else if (curNode.getxPos() > maxX) {
                    maxX = curNode.getxPos();
                }

                if (curNode.getyPos() < minY) {
                    minY = curNode.getyPos();
                } else if (curNode.getyPos() > maxY) {
                    maxY = curNode.getyPos();
                }

            }

            minX -= radius;
            minY -= radius;
            maxX += radius;
            maxY += radius;

            height = maxY - minY;
            width = maxX - minX;

            //Offsets are added, so invert the minimum values here
            xOffset = -minX;
            yOffset = -minY;
        }
        setMeasuredDimension(width, height);

    }

    /*
    Custom class to define the onTouch properties of the MyView View.
     */
    class MyOnTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchDown(event);
                    break;

                case MotionEvent.ACTION_MOVE:
                    // touch move code
                    break;

                case MotionEvent.ACTION_UP:
                    // touch up code
                    break;
            }
            return true;
        }
    }

}


