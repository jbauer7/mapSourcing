package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * created by Nikhil on 11/16/2015.
 */
public class MenuOption extends CanvasDrawable {


    private static final int DEFAULTOPTIONPRIORITY = 300;
    private final int elementWidth = 200; //200 pixels wide
    private final int elementHeight = 100; //100 pixels tall
    private int borderPix = 3;
    private int elementNum;
    private MenuSelection displaySelection;
    private CanvasDrawable menuOwner;
    private float scaleFactor = 1f;
    private float defaultTextSize = 48f;
    private int xPos, yPos, defaultXpos, defaultYpos;

    public MenuOption(CanvasDrawable menuOwner, int xPos, int yPos, int elementNum, MenuSelection display) {
        super(DEFAULTOPTIONPRIORITY);
        this.elementNum = elementNum;
        displaySelection = display;
        this.menuOwner = menuOwner;
        this.xPos = xPos;
        this.yPos = yPos;
        defaultXpos = xPos;
        defaultYpos = yPos;
    }

    @Override
    public void draw(Canvas canvas) {
        //update scale factor
        this.scaleFactor = menuOwner.getScaleFactor();

        Paint paint = new Paint();

        int xStart = xPos;
        int yStart = yPos + (int) ((elementHeight * elementNum * scaleFactor) - (borderPix * scaleFactor * elementNum));
        int xEnd = xStart + (int) (elementWidth * scaleFactor);
        int yEnd = yStart + (int) (elementHeight * scaleFactor);
        int border = (int) (borderPix * scaleFactor);

        paint.setColor(Color.BLACK);
        canvas.drawRect(xStart, yStart, xEnd, yEnd, paint);
        paint.setColor(Color.CYAN);
        canvas.drawRect(xStart + border, yStart + border, xEnd - border, yEnd - border, paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(defaultTextSize * scaleFactor);
        canvas.drawText(displaySelection.toString(), xStart, yStart + (scaleFactor * elementHeight / 2), paint);
    }

    @Override
    public boolean contains(int clickedX, int clickedY) {
        int xStart = xPos;
        int yStart = yPos + (int) ((elementHeight * elementNum * scaleFactor) - (borderPix * elementNum * scaleFactor));
        int xEnd = xStart + (int) (elementWidth * scaleFactor);
        int yEnd = yStart + (int) (elementHeight * scaleFactor);
        return (clickedX > xStart && clickedY > yStart && clickedX < xEnd && clickedY < yEnd);
    }


    @Override
    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        xPos = (int) (defaultXpos * scaleFactor);
        yPos = (int) (defaultYpos * scaleFactor);
    }

    public MenuSelection getMenuAttribute() {
        return displaySelection;
    }

    public CanvasDrawable getParent() {
        return menuOwner;
    }

    public int getXpos(){
        return defaultXpos;
    }
    public  int getYpos(){
        return defaultYpos;
    }
}
