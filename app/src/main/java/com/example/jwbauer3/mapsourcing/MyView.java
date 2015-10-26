package com.example.jwbauer3.mapsourcing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;


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
    private PriorityQueue<CanvasDrawable> drawables;

    //TODO: use enum
    private static int NONE = 0;
    private static int DRAG = 1;
    private static int ZOOM = 2;
    private int mode = NONE;
    private float startX=0f;
    private float startY=0f;
    private float transX=0f;
    private float tranxY=0f;
    private float prevTransX=0f;
    private float prevTransY=0f;


    private Float scaleFactor = 1.f;
    private ScaleGestureDetector myDetector;

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
        myDetector = new ScaleGestureDetector(getContext(), new MyScaleListener());
    }

    private void setPriorityQueue() {
        //instantiate Set with a custom comparator that looks at priority.
        //TODO: pick a better magic number for init capacity
        drawables = new PriorityQueue<CanvasDrawable>(10, new Comparator<CanvasDrawable>() {
            @Override
            public int compare(CanvasDrawable lhs, CanvasDrawable rhs) {
                return lhs.getPriority() - rhs.getPriority();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Call the base on draw
        super.onDraw(canvas);

        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);
        canvas.translate(transX/scaleFactor, tranxY/scaleFactor);
        setBackgroundImage(canvas);

        //Create a paintbrush type object
        Paint paint = new Paint();

        //Set the background color
       // paint.setStyle(Paint.Style.FILL);
        //paint.setColor(Color.YELLOW);
        //canvas.drawPaint(paint);

        for (CanvasDrawable element : drawables) {
            element.draw(canvas, xOffset, yOffset);
        }

        canvas.restore();

    }
    private void setBackgroundImage(Canvas canvas){
        //Drawable background = ResourcesCompat.getDrawable(getResources(), R.drawable.eh_floor2, null);
        //background.setBounds(0,0,1500,900);
        //background.draw(canvas);
    }

    /*
    Handles the response for this
     */
    private void touchDown(MotionEvent event) {
        //TODO: always search the entire queue, change to search through a reversed list
        //problem arises from priority queue puts the lowest elements first, but we click highest
        //that are at the end of the list.
        CanvasDrawable selectedElement = null;
        for (CanvasDrawable element : drawables) {
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
        //should be height, width. 5k,5k is for displaying the background.
        //TODO: figure out bounds for screen
        setMeasuredDimension(1312, 2011);

    }

    /*
    Custom class to define the onTouch properties of the MyView View.
     */
    class MyOnTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mode = DRAG;
                    startX = event.getX()-prevTransX;
                    startY = event.getY()-prevTransY;
                    touchDown(event);
                    break;

                case MotionEvent.ACTION_MOVE:
                    transX = event.getX() - startX;
                    tranxY = event.getY() - startY;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = ZOOM;
                    break;

                case MotionEvent.ACTION_UP:
                    // touch up code
                    mode = NONE;
                    prevTransX = transX;
                    prevTransY = tranxY;
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    mode = DRAG;
                    //maybe don't need these.
                    prevTransX = transX;
                    prevTransY = tranxY;
                    break;

            }


            //zooming
            myDetector.onTouchEvent(event);

            if(mode == DRAG && scaleFactor != 1f || mode == ZOOM){
                invalidate();
            }
            return true;
        }

    }

    class MyScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            //.postScale(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            //Canvas canvas = new Canvas();

            //canvas.scale(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            //cubed so it zooms faster
            scaleFactor *= detector.getScaleFactor();// * detector.getScaleFactor() * detector.getScaleFactor();
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            // return drawMatrix != null;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }

}


