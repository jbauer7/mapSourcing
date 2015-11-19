package com.example.jwbauer3.mapsourcing;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;


/**
 * created by Nikhil on 10/11/2015.
 */
public class MyView extends View {
    private Floor curFloor;
    //priority queue for drawing (stores lowest elements first)
    private PriorityQueue<CanvasDrawable> drawables_draw;
    //priority queue for search/touching (stores highest elements first)
    private PriorityQueue<CanvasDrawable> drawables_search;

    private int originalXOffset = 0;
    private int originalYOffset = 0;
    private int originalMaxXOffset = 0;
    private int originalMaxYOffset = 0;
    private int xOffset = 0;
    private int yOffset = 0;

    //TODO: use enum
    private static int NONE = 0;
    private static int DRAG = 1;
    private static int ZOOM = 2;
    private int mode = NONE;

    private final float MAXZOOMSCALE = 4f;
    private final float MINSCALEFACTOR = .25f;
    private boolean meshMode = false;

    //hold information on how to draw the canvas, mesh, and what is currently active
    private ReferenceState canvasReferenceState;
    private ReferenceState meshReferenceState;
    private ReferenceState activeReferenceState;

    private ScaleGestureDetector myDetector;
    private int myViewWidth;
    private int myViewHeight;

    private boolean menuActive;
    private ArrayList<MenuOption> opts = new ArrayList<MenuOption>();

    public MyView(Context context) {
        super(context);
        preformSetup();
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        preformSetup();
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        preformSetup();
    }

    public void setFloor(Floor floor) {
        //switch if we already have a floor
        //todo: check that meshreferenceState is saved off.
        if(meshMode) {
            activeReferenceState = floor.getMeshReferenceState();
        }

        this.curFloor = floor;
        meshReferenceState=curFloor.getMeshReferenceState();
        determineOffsets();
        setDrawableQueues();

        xOffset = originalXOffset + (int) (meshReferenceState.transX);
        yOffset = originalYOffset + (int) (meshReferenceState.transY);

        invalidate();
    }
    private void setDrawableQueues(){
        drawables_draw = new PriorityQueue<>((curFloor.getNodes().size() * 2), new Comparator<CanvasDrawable>() {
            @Override
            public int compare(CanvasDrawable lhs, CanvasDrawable rhs) {
                return lhs.getPriority() - rhs.getPriority();
            }
        });
        drawables_search = new PriorityQueue<>((curFloor.getNodes().size()*2), new Comparator<CanvasDrawable>() {
            @Override
            public int compare(CanvasDrawable lhs, CanvasDrawable rhs) {
                return rhs.getPriority() - lhs.getPriority();
            }
        });
        drawables_draw.addAll(curFloor.getNodes());
        drawables_draw.addAll(curFloor.getEdges());
        drawables_search.addAll(curFloor.getNodes());
        drawables_search.addAll(curFloor.getEdges());
    }

    private void preformSetup() {
        setListeners();
        setReferenceStates();
    }

    /*
     Sets both onTouch listener and ScaleGestureDetector
     */
    private void setListeners() {
        this.setOnTouchListener(new MyOnTouchListener());
        myDetector = new ScaleGestureDetector(getContext(), new MyScaleListener());
    }

    /*
    Instantiates the ReferenceStates to be used.
     */
    private void setReferenceStates() {
        canvasReferenceState = new ReferenceState();
        //default active to be canvas (ie: not starting in mesh mode).
        activeReferenceState = canvasReferenceState;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        //update the scale: we zoom equally in both x and y direction
        canvas.scale(canvasReferenceState.scaleFactor, canvasReferenceState.scaleFactor);
        //update the translation: offset by trans(XY)/scaleFactor (how far at what zoom factor)
        canvas.translate(canvasReferenceState.transX / canvasReferenceState.scaleFactor,
                canvasReferenceState.transY / canvasReferenceState.scaleFactor);

        //draw the background image (if there is one)
        curFloor.getBackgroundImage().draw(canvas);

        //draw each drawable element (nodes, edges, etc etc)
        for (CanvasDrawable element : drawables_draw) {
            element.draw(canvas, xOffset, yOffset);
        }

        canvas.restore();

    }

    /*
    Handles the response for this
     */
    private void touchDown(MotionEvent event) {
        CanvasDrawable selectedElement = null;
        int translatedXOffset = xOffset + (int) (canvasReferenceState.transX / canvasReferenceState.scaleFactor);
        int translatedYOffset = yOffset + (int) (canvasReferenceState.transY / canvasReferenceState.scaleFactor);

        for (CanvasDrawable element : drawables_search) {

            if (element.contains((int) event.getX(), (int) event.getY(), translatedXOffset, translatedYOffset, canvasReferenceState.scaleFactor)) {
                selectedElement = element;
                break;
            }
        }
        if (selectedElement != null) {
            if (selectedElement instanceof MenuOption) {

            } else {
                //wasn't a menu option, remove it.
                selectedElement.toggleAttribute("clicked");
                drawables_draw.removeAll(opts);
                drawables_search.removeAll(opts);
                opts = selectedElement.getOptions();
                if (opts != null) {
                    drawables_draw.addAll(opts);
                    drawables_search.addAll(opts);
                }
                invalidate();
            }
        } else {
            drawables_draw.removeAll(opts);
            drawables_search.removeAll(opts);
        }

    }

    /*
    Turn on or off the ability to move the mesh compared to the canvas
     */
    public void toggleMeshMovementMode() {
        if (meshMode) {
            //leaving meshmode
            //save off info in meshReferenceState
            meshReferenceState = activeReferenceState;
            //switch states
            activeReferenceState = canvasReferenceState;
        } else {
            //entering meshmode
            //save off info in canvasReferenceState
            canvasReferenceState = activeReferenceState;
            //switch states
            activeReferenceState = meshReferenceState;
        }
        meshMode = !meshMode;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //todo: do we need this call
        determineOffsets();

        //update information every time this runs to catch phone orientation changes
        LinearLayout wrapper = (LinearLayout) getRootView().findViewById(R.id.LinearLayout_main_wrapper);
        myViewWidth = wrapper.getWidth();
        myViewHeight = wrapper.getHeight();


        //should be height, width. 5k,5k is for displaying the background.
        //TODO: figure out bounds for screen
        setMeasuredDimension(myViewWidth, myViewHeight);

    }

    /*
    Determine the offsets for our list of nodes
     */
    private void determineOffsets() {
        ArrayList<Node> nodes = curFloor.getNodes();
        if (nodes != null) {
            int radius = nodes.get(0).getDrawnRadius();
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

            //Offsets are added, so invert the minimum values here
            xOffset = -minX;
            yOffset = -minY;
            originalXOffset = xOffset;
            originalYOffset = yOffset;

            //Max Values are kept positive
            originalMaxXOffset = maxX;
            originalMaxYOffset = maxY;
        }
    }

    /*
    Custom class to define the onTouch properties of the MyView View.
     */
    class MyOnTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //push one finger to screen
                    mode = DRAG;
                    activeReferenceState.startX = event.getX() - activeReferenceState.prevTransX;
                    activeReferenceState.startY = event.getY() - activeReferenceState.prevTransY;
                    //todo: should we click if we are dragging
                    if (!meshMode)
                        touchDown(event);
                    break;

                case MotionEvent.ACTION_MOVE:
                    //still panning/dragging
                    activeReferenceState.transX = event.getX() - activeReferenceState.startX;
                    activeReferenceState.transY = event.getY() - activeReferenceState.startY;

                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    //push a second finger to screen
                    mode = ZOOM;
                    break;

                case MotionEvent.ACTION_UP:
                    //take both fingers off screen
                    mode = NONE;
                    activeReferenceState.prevTransX = activeReferenceState.transX;
                    activeReferenceState.prevTransY = activeReferenceState.transY;
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    //take one finger off screen
                    mode = DRAG;
                    activeReferenceState.prevTransX = activeReferenceState.transX;
                    activeReferenceState.prevTransY = activeReferenceState.transY;
                    break;
            }
            //alert the scale gesture detector to possible scaling.
            myDetector.onTouchEvent(event);

            //Fix the max scale factor
            if (activeReferenceState.scaleFactor > MAXZOOMSCALE) {
                activeReferenceState.scaleFactor = MAXZOOMSCALE;
            }


            if (meshMode) {
                //TODO: have this be a static method/variable that gets updated
                for (CanvasDrawable element : drawables_draw) {
                    element.setScaleFactor(activeReferenceState.scaleFactor);
                }
                determineOffsets();
                correctMeshBoundaries();
            } else {
                correctCanvasBoundaries();
            }


            return true;
        }

    }

    private void correctMeshBoundaries() {
        //Fix the min scale factor for mesh mode.
        //min scale factor for canvas is defined below
        if (activeReferenceState.scaleFactor < MINSCALEFACTOR) {
            activeReferenceState.scaleFactor = MINSCALEFACTOR;
        }

        //update the offsets of the nodes xoffset, yoffset
        xOffset = originalXOffset + (int) (activeReferenceState.transX);
        yOffset = originalYOffset + (int) (activeReferenceState.transY);

        //don't let nodes go too far to the left
        if (xOffset < originalXOffset) {
            xOffset = originalXOffset;
            activeReferenceState.transX = 0;
            activeReferenceState.prevTransX = 0;
        }
        //too far on top
        if (yOffset < originalYOffset) {
            yOffset = originalYOffset;
            activeReferenceState.transY = 0;
            activeReferenceState.prevTransY = 0;
        }
        //too far right
        if (xOffset + originalMaxXOffset > curFloor.getBackgroundWidth()) {
            xOffset = (curFloor.getBackgroundWidth() - originalMaxXOffset);
            activeReferenceState.transX = xOffset - originalXOffset;
            activeReferenceState.prevTransX = activeReferenceState.transX;
        }
        //too far bottom
        if (yOffset + originalMaxYOffset > curFloor.getBackgroundHeight()) {
            yOffset = (curFloor.getBackgroundHeight() - originalMaxYOffset);
            activeReferenceState.transY = yOffset - originalYOffset;
            activeReferenceState.prevTransY = activeReferenceState.transY;
        }
        invalidate();
    }

    private void correctCanvasBoundaries() {
        //portrait scale fix
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //If our width is smaller than myViewWidth, we have to adjust
            if (canvasReferenceState.scaleFactor * curFloor.getBackgroundWidth() < myViewWidth) {
                canvasReferenceState.scaleFactor = ((float) myViewWidth) / curFloor.getBackgroundWidth();
            }
        }
        //landscape scale fix
        else {
            //If our height is smaller than myViewHeight, we have to adjust
            if (canvasReferenceState.scaleFactor * curFloor.getBackgroundHeight() < myViewHeight) {
                canvasReferenceState.scaleFactor = ((float) myViewHeight) / curFloor.getBackgroundHeight();
            }
        }


        //Two 4 consecutive if statements ensures that the drawing is mapped to the top and
        //left corner as a default.

        //left and right
        //don't let image pan past the right edge
        if (canvasReferenceState.scaleFactor * curFloor.getBackgroundWidth() - myViewWidth + canvasReferenceState.transX < 0) {
            canvasReferenceState.transX = -(canvasReferenceState.scaleFactor * curFloor.getBackgroundWidth() - myViewWidth);
        }
        //don't let image pan past the left edge
        if (canvasReferenceState.transX > 0) {
            canvasReferenceState.transX = 0;
        }
        //top and bottom
        //don't let image pan past the bottom edge
        if (canvasReferenceState.scaleFactor * curFloor.getBackgroundHeight() - myViewHeight + canvasReferenceState.transY < 0) {
            canvasReferenceState.transY = -(canvasReferenceState.scaleFactor * curFloor.getBackgroundHeight() - myViewHeight);
        }
        //don't let image pan past the top edge
        if (canvasReferenceState.transY > 0) {
            canvasReferenceState.transY = 0;
        }

        //update image if we are dragging, or if we are zooming in our out.
        if (mode == DRAG || mode == ZOOM) {
            invalidate();
        }
    }

    /*
    Custom class to handle scaling for our canvas.
     */
    class MyScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //when we are zooming, this updates our scale factor
            //needs to be active because we are using this for both canvas and mesh
            activeReferenceState.scaleFactor *= detector.getScaleFactor();
            //invalidate(); not needed because its called from the ontouchlistener
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    }

}


