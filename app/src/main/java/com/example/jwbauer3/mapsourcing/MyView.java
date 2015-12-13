package com.example.jwbauer3.mapsourcing;

import android.content.Context;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * created by Nikhil on 10/11/2015.
 */
public class MyView extends View {
    private Floor curFloor;
    //priority queue for drawing (stores lowest elements first)
    private SortedArrayList<CanvasDrawable> drawables_draw;
    //priority queue for search/touching (stores highest elements first)
    private SortedArrayList<CanvasDrawable> drawables_search;

    private int originalXOffset = 0;
    private int originalYOffset = 0;
    private int originalMaxXOffset = 0;
    private int originalMaxYOffset = 0;
    private int xOffset = 0;
    private int yOffset = 0;

    private static int NONE = 0;
    private static int DRAG = 1;
    private static int ZOOM = 2;
    private int mode = NONE;

    //end all be all zoom constraints
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

    private Navigator navigator = null;
    private ArrayList<CanvasDrawable> path = null;
    private BaseNode startNode = null;
    private BaseNode endNode = null;

    private LocationNode userLocation = null;
    private LocationNode searchLocation = null;

    private ArrayList<MenuOption> opts = new ArrayList<>();

    //Service Connection
    private EdgeLogService mService;


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
        this.curFloor = floor;
        //switch if we already have a floor
        if (meshMode) {
            activeReferenceState = curFloor.getMeshReferenceState();
        }


        meshReferenceState = curFloor.getMeshReferenceState();
        determineOffsets();
        setDrawableQueues();

        if (userLocation != null) {
            if (userLocation.getFloorNum() == curFloor.getFloorNum()) {
                addLocationNodeToDrawables(userLocation);
            } else {
                removeLocationNodeFromDrawables(userLocation);
            }
        }
        if (searchLocation != null) {
            if (searchLocation.getFloorNum() == curFloor.getFloorNum()) {
                addLocationNodeToDrawables(searchLocation);
            } else {
                removeLocationNodeFromDrawables(searchLocation);
            }
        }

        xOffset = originalXOffset + (int) (meshReferenceState.transX);
        yOffset = originalYOffset + (int) (meshReferenceState.transY);

        invalidate();
    }

    /*
    Allows the service to update the location of the user in real time.
    usersEdge: Edge the user is currently on.
    mapX: the map X coordinate the user is at
    mapY: the map Y coordinate the user is at
     */
    public void updateUserLocation(Edge usersEdge, int mapX, int mapY) {
        //this method should never be called if userLocation is null
        setLocationNode(usersEdge, mapX, mapY, userLocation, searchLocation);
    }

    /*
    To be called when userLocation has been set by the user themselves.
    Will update the services knowledge of the users location.
    Wants the % to endNode: IE on endNode, return 1, on startNode return 0.
     */
    private void alertServiceToUserLocationUpdate() {
        //handler to some service object
        //should never be called if userLocation is null
        BaseNode startNode = userLocation.getSourceEdge().getStart();
        BaseNode endNode = userLocation.getSourceEdge().getEnd();
        int mapXpos = userLocation.getDefaultXPos();
        int mapYpos = userLocation.getDefaultYPos();

        //calculate the distance to the startNode and to the endNode. Using pythag formula.
        //diff in x^2 + diff in y^2. Sqrt the result.
        double distToStart = Math.sqrt(Math.pow((mapXpos - startNode.getDefaultXPos()), 2) + Math.pow((mapYpos - startNode.getDefaultYPos()), 2));
        double distToEnd = Math.sqrt(Math.pow((mapXpos - endNode.getDefaultXPos()), 2) + Math.pow((mapYpos - endNode.getDefaultYPos()), 2));

        //percent to end will be startDist/(totalDist)
        double percentToEnd = distToStart / (distToEnd + distToStart);

        //service.setUserLocation(userLocation.getSourceEdge(), percentToEnd);
    }

    /*
    Public access to set the navigator for this instance. Set in the main activity.
     */
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    private void setDrawableQueues() {
        int approxSize = (curFloor.getNodes().size() * 2); //Typically 2 edges per node
        drawables_draw = new SortedArrayList<>(approxSize, new Comparator<CanvasDrawable>() {
            @Override
            public int compare(CanvasDrawable lhs, CanvasDrawable rhs) {
                return (lhs.getPriority() - rhs.getPriority());
            }
        });
        drawables_search = new SortedArrayList<>(approxSize, new Comparator<CanvasDrawable>() {
            @Override
            public int compare(CanvasDrawable lhs, CanvasDrawable rhs) {
                return rhs.getPriority() - lhs.getPriority();
            }
        });
        drawables_draw.addAllSorted(curFloor.getNodes());
        drawables_draw.addAllSorted(curFloor.getEdges());
        drawables_search.addAllSorted(curFloor.getNodes());
        drawables_search.addAllSorted(curFloor.getEdges());
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

        //TODO: try to incorporate the x,y offsets into the translate
        //get rid of need to pass offsets to canvasdrawables
        //TODO: http://stackoverflow.com/questions/10303578/how-to-offset-bitmap-drawn-on-a-canvas

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

        int mapX = convertPixelToMapX((int) event.getX());
        int mapY = convertPixelToMapY((int) event.getY());

        for (CanvasDrawable element : drawables_search) {

            if (element.contains(mapX, mapY, canvasReferenceState.scaleFactor)) {
                selectedElement = element;
                break;
            }
        }
        if (selectedElement != null) {
            if (selectedElement instanceof MenuOption) {
                MenuOption opt = (MenuOption) selectedElement;
                if (opt.getMenuAttribute().equals(MenuSelection.START)) {

                } else if (opt.getMenuAttribute().equals(MenuSelection.END)) {

                } else if (opt.getMenuAttribute().equals(MenuSelection.LOCATE)) {
                    //todo: only Edge has access Locate menuOption
                    setLocationNode((Edge) opt.getParent(), opt.getXpos(), opt.getYpos(), userLocation, searchLocation);
                    userLocation.toggleAttribute(Attribute.USER);
                    //Set the start node for the navigator
                    startNode = userLocation;
                    navigator.setStartNode(startNode);
                    if (endNode != null) {
                        updatePath();
                    }

                    alertServiceToUserLocationUpdate();

                } else if (opt.getMenuAttribute().equals(MenuSelection.SEARCH)) {
                    //todo: only Edge has access to Search menuOption
                    setLocationNode((Edge) opt.getParent(), opt.getXpos(), opt.getYpos(), searchLocation, userLocation);
                    searchLocation.toggleAttribute(Attribute.DESTINATION);
                    //Set the end node for the navigator
                    endNode = searchLocation;
                    navigator.setEndNode(endNode);
                    if (startNode != null) {
                        updatePath();
                    }
                }
                //Remove the menu since an option was clicked
                drawables_draw.removeAll(opts);
                drawables_search.removeAll(opts);

            } else {
                //wasn't a menu option, remove it.
                selectedElement.toggleAttribute(Attribute.CLICKED);
                drawables_draw.removeAll(opts);
                drawables_search.removeAll(opts);
                //opts = selectedElement.getOptions();
                if (selectedElement.getOptions() != null) {
                    determineMenuOptions(event, selectedElement);
                }
                invalidate();
            }
        } else {
            drawables_draw.removeAll(opts);
            drawables_search.removeAll(opts);
        }

    }

    private void determineMenuOptions(MotionEvent event, CanvasDrawable selectedElement) {
        int xpos, ypos;
        opts.clear();
        if (selectedElement instanceof Node) {
            //node, make it at the center
            //need the default position
            xpos = ((Node) selectedElement).getDefaultXPos();
            ypos = ((Node) selectedElement).getDefaultYPos();
        } else {
            //edge, make it where you click. Need to divide out the scale Factor for the original point.
            xpos = (int) (convertPixelToMapX((int) event.getX()) / meshReferenceState.scaleFactor);
            ypos = (int) (convertPixelToMapY((int) event.getY()) / meshReferenceState.scaleFactor);
        }
        for (int x = 0; x < selectedElement.getOptions().size(); x++) {
            MenuOption menuOption = new MenuOption(selectedElement, xpos, ypos, x, selectedElement.getOptions().get(x));
            menuOption.setScaleFactor(meshReferenceState.scaleFactor);
            opts.add(menuOption);
        }
        drawables_draw.addAllSorted(opts);
        drawables_search.addAllSorted(opts);
    }

    private void updatePath() {
        //remove all canvas drawables states that they are in the old path
        if (path != null) {
            for (CanvasDrawable drawable : path) {
                drawable.toggleAttribute(Attribute.PATH);
            }
        }
        navigator.calculatePath();
        path = navigator.getPath();

        if (path != null) {
            for (CanvasDrawable drawable : path) {
                drawable.toggleAttribute(Attribute.PATH);
            }
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
        //added in temp
        xOffset = originalXOffset + (int) (meshReferenceState.transX);
        yOffset = originalYOffset + (int) (meshReferenceState.transY);
        meshMode = !meshMode;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //update information every time this runs to catch phone orientation changes
        LinearLayout wrapper = (LinearLayout) getRootView().findViewById(R.id.LinearLayout_main_wrapper);
        myViewWidth = wrapper.getWidth();
        myViewHeight = wrapper.getHeight();
        setMeasuredDimension(myViewWidth, myViewHeight);
    }

    /*
    Determine the offsets for our list of nodes
     */
    private void determineOffsets() {
        ArrayList<Node> nodes = curFloor.getNodes();
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

            //Offsets are added, so invert the minimum values here
            xOffset = -minX;
            yOffset = -minY;
            originalXOffset = xOffset;
            originalYOffset = yOffset;

            //Max Values are kept positive
            originalMaxXOffset = maxX;
            originalMaxYOffset = maxY;

            //only run once per floor.
            //TODO: when a new node is added, also check this.
            if (curFloor.getMaxMeshScaleFactor() == -1f) {
                float tbScale = curFloor.getBackgroundHeight() / (float) (maxY - minY);
                float lrScale = curFloor.getBackgroundWidth() / (float) (maxX - minX);
                if (tbScale > lrScale) {
                    curFloor.setMaxMeshScaleFactor(lrScale);
                } else {
                    curFloor.setMaxMeshScaleFactor(tbScale);
                }

            }
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
        if (meshReferenceState.scaleFactor < MINSCALEFACTOR) {
            meshReferenceState.scaleFactor = MINSCALEFACTOR;
        } //already checked the absolute zoom scale cap.
        else if (meshReferenceState.scaleFactor > curFloor.getMaxMeshScaleFactor()) {
            meshReferenceState.scaleFactor = curFloor.getMaxMeshScaleFactor();
        }

        //update the offsets of the nodes xOffset, yOffset
        xOffset = originalXOffset + (int) (meshReferenceState.transX);
        yOffset = originalYOffset + (int) (meshReferenceState.transY);

        //don't let nodes go too far to the left
        if (xOffset < originalXOffset) {
            xOffset = originalXOffset;
            meshReferenceState.transX = 0;
            meshReferenceState.prevTransX = 0;
        }
        //too far right
        if (xOffset + originalMaxXOffset > curFloor.getBackgroundWidth()) {
            xOffset = (curFloor.getBackgroundWidth() - originalMaxXOffset);
            meshReferenceState.transX = xOffset - originalXOffset;
            meshReferenceState.prevTransX = meshReferenceState.transX;
        }
        //too far on top
        if (yOffset < originalYOffset) {
            yOffset = originalYOffset;
            meshReferenceState.transY = 0;
            meshReferenceState.prevTransY = 0;
        }
        //too far bottom
        if (yOffset + originalMaxYOffset > curFloor.getBackgroundHeight()) {
            yOffset = (curFloor.getBackgroundHeight() - originalMaxYOffset);
            meshReferenceState.transY = yOffset - originalYOffset;
            meshReferenceState.prevTransY = meshReferenceState.transY;
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

        //update the offsets of the nodes xoffset, yoffset
        xOffset = originalXOffset + (int) (meshReferenceState.transX);
        yOffset = originalYOffset + (int) (meshReferenceState.transY);


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

    /*
    Method to convert a clicked pixel's X coordinate to the maps X coordinate
     */
    private int convertPixelToMapX(int pixelPoint) {
        int translatedXOffset = xOffset + (int) (canvasReferenceState.transX / canvasReferenceState.scaleFactor);

        return (int) (pixelPoint / canvasReferenceState.scaleFactor) - translatedXOffset;
    }

    /*
    Method to convert a clicked pixel's Y coordinate to the maps Y coordinate
     */
    private int convertPixelToMapY(int pixelPoint) {
        int translatedYOffset = yOffset + (int) (canvasReferenceState.transY / canvasReferenceState.scaleFactor);

        return (int) (pixelPoint / canvasReferenceState.scaleFactor) - translatedYOffset;
    }

    private void addLocationNodeToDrawables(LocationNode locationNode) {
        drawables_draw.addSorted(locationNode);
        drawables_search.addSorted(locationNode);
        drawables_draw.addSorted(locationNode.getStartEdge());
        drawables_draw.addSorted(locationNode.getEndEdge());
    }

    private void removeLocationNodeFromDrawables(LocationNode locationNode) {
        drawables_draw.remove(locationNode);
        drawables_search.remove(locationNode);
        drawables_draw.remove(locationNode.getStartEdge());
        drawables_draw.remove(locationNode.getEndEdge());
    }

    /*
    Method to set both user and search locationNode.
    Will delete/update all references to the LocationNode 'update'
     */
    private void setLocationNode(Edge userSourceEdge, int xPos, int yPos, LocationNode update, LocationNode other) {
        //toSet is the LocationNode that needs to be set.
        LocationNode toSet = update;
        //other is the 'other' LocationNode that isn't being currently being set.

        BaseNode nodeBefore = userSourceEdge.getStart();
        BaseNode nodeAfter = userSourceEdge.getEnd();
        if (toSet != null) {
            //Remove the old user location and its edges
            removeLocationNodeFromDrawables(toSet);
            userSourceEdge.getStart().removeEdge(toSet.getStartEdge());
            userSourceEdge.getEnd().removeEdge(toSet.getEndEdge());
        }
        if ((other != null) && (other.getSourceEdge().equals(userSourceEdge))) {
            //This search node is on this edge too, make both edges point to it
            //TODO: should only set one edge to the other.... not both.
            nodeBefore = other;
            nodeAfter = other;
            drawables_draw.remove(other.getStartEdge());
            drawables_draw.remove(other.getEndEdge());
        }

        //Create the new user location and its edges
        toSet = new LocationNode(xPos, yPos, curFloor.getFloorNum(), userSourceEdge, nodeBefore, nodeAfter);
        toSet.setScaleFactor(meshReferenceState.scaleFactor);

        addLocationNodeToDrawables(toSet);

        //Update the searchLocation's edges as well
        if (other != null) {
            //Remove old edges from collections
            Edge searchSourceEdge = other.getSourceEdge();
            drawables_draw.remove(other.getStartEdge());
            drawables_draw.remove(other.getEndEdge());
            searchSourceEdge.getStart().removeEdge(other.getStartEdge());
            searchSourceEdge.getEnd().removeEdge(other.getEndEdge());
            other.clearEdges();

            if (other.getSourceEdge().equals(userSourceEdge)) {
                //This search node is on this edge too, connect them directly
                other.setEndEdge(toSet.getStartEdge());
                other.setStartEdge(toSet.getEndEdge());
            } else {
                //Revert edges to nodes on each side of the source edge
                LocationEdge searchStartEdge = new LocationEdge(searchSourceEdge.getStart(), other);
                LocationEdge searchEndEdge = new LocationEdge(other, searchSourceEdge.getEnd());

                //Add new edges to collections
                other.setStartEdge(searchStartEdge);
                other.setEndEdge(searchEndEdge);
                searchSourceEdge.getStart().addEdge(searchStartEdge);
                searchSourceEdge.getEnd().addEdge(searchEndEdge);
                if (other.getFloorNum() == curFloor.getFloorNum()) {
                    drawables_draw.addSorted(searchStartEdge);
                    drawables_draw.addSorted(searchEndEdge);
                }
            }
        }
        if (update == userLocation) {
            userLocation = toSet;
        } else {
            searchLocation = toSet;
        }
    }

    public void connectEdgeLogService(EdgeLogService mService){
        this.mService=mService;
    }
}


