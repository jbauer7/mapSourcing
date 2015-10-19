package com.example.jwbauer3.mapsourcing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * created by Nikhil on 10/11/2015.
 */
public class MyView extends View {

    private ArrayList<Node> nodes;
    private ArrayList<Node> clickedNodes = new ArrayList<>();
    private ArrayList<Edge> edges;
    private int xOffset = 0;
    private int yOffset = 0;
    private int radius = 100;
    private PriorityQueue<Drawable> drawables;

    public MyView(Context context) {
        super(context);
        setListener();
        setPriorityQueue();
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setListener();
        setPriorityQueue();
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setListener();
        setPriorityQueue();
    }


    /*
    Allows the nodes/edges to be set from an outside source
     */
    public void setNodesEdges(ArrayList<Node> nodes, ArrayList<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;

        drawables.addAll(nodes);
        drawables.addAll(edges);

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

    private void setPriorityQueue() {
        //instantiate Set with a custom comparator that looks at priority.
        //TODO: pick a better magic number for init capacity
        drawables = new PriorityQueue<Drawable>(10, new Comparator<Drawable>() {
            @Override
            public int compare(Drawable lhs, Drawable rhs) {
                return lhs.getPriority() - rhs.getPriority();
            }
        });
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

        for (Drawable element : drawables) {
            element.draw(canvas, xOffset, yOffset);
        }

    }

    /*
    Handles the response for this
     */
    private void touchDown(MotionEvent event) {
        //TODO: always search the entire queue, change to search through a reversed list
        //problem arises from priority queue puts the lowest elements first, but we click highest
        //that are at the end of the list.
        Drawable selectedElement = null;
        for (Drawable element : drawables) {
            if (element.contains((int) event.getX(), (int) event.getY(), xOffset, yOffset)) {
                selectedElement = element;
                //break;
            }
        }
        if (selectedElement != null) {
            selectedElement.toggleAttribute("clicked");
            invalidate();
        }

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


