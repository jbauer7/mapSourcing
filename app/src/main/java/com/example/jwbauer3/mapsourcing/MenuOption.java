package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * created by Nikhil on 11/16/2015.
 */
public class MenuOption extends CanvasDrawable {


    private static final int DEFAULTOPTIONPRIORITY = 300;
    private final int elementWidth = 150;
    private final int elementHeight = 100; //100 pixels tall
    private int borderPix = 3;
    private int elementNum;
    private CanvasDrawable menuOwner;
    private MenuSelection displaySelection;
    private float scaleFactor = 1f;
    //TODO: REPLACE FROM GLOBAL VIEW STATE
    private int backgroundWidth;
    private int backgroundHeight;

    public MenuOption(CanvasDrawable owned, int elementNum, MenuSelection display, int backgroundWidth, int backgroundHeight) {
        super(DEFAULTOPTIONPRIORITY);
        this.menuOwner = owned;
        this.elementNum = elementNum;
        displaySelection = display;
        this.backgroundWidth = backgroundWidth;
        this.backgroundHeight = backgroundHeight;
    }

    @Override
    public void draw(Canvas canvas, int xOffset, int yOffset) {
        //update scale factor
        this.scaleFactor = menuOwner.getScaleFactor();

        Paint paint = new Paint();

        int x = menuOwner.getMenuStartX() + xOffset;
        int y = menuOwner.getMenuStartY() + yOffset + (int) (elementHeight * elementNum * scaleFactor) - (int) (borderPix * scaleFactor * elementNum);
        int xEnd = (int) (x + elementWidth * scaleFactor);
        int yEnd = (int) (y + elementHeight * scaleFactor);
        int border = (int) (borderPix * scaleFactor);

        //todo: implement fix with MenuClass, and global view state
        /*
        if (xEnd > backgroundWidth) {
            xEnd = x;
            x -= elementWidth * scaleFactor;
        }
        if(yEnd > backgroundHeight){
            yEnd=y;
            y-=elementHeight*scaleFactor;
        }
        */

        paint.setColor(Color.BLACK);
        canvas.drawRect(x, y, xEnd, yEnd, paint);
        paint.setColor(Color.CYAN);
        canvas.drawRect(x + border, y + border, xEnd - border, yEnd - border, paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(48f * scaleFactor);
        canvas.drawText(displaySelection.toString(), x, y + (scaleFactor * elementHeight / 2), paint);

    }

    @Override
    public boolean contains(int xPos, int yPos, int xOffset, int yOffset, float canvasScaleFactor) {
        int xStart = (int) ((menuOwner.getMenuStartX() + xOffset) * canvasScaleFactor);
        int yStart = (int) ((menuOwner.getMenuStartY() + yOffset + elementHeight * elementNum * scaleFactor - borderPix * scaleFactor * elementNum) * canvasScaleFactor);
        int xEnd = xStart + (int) (elementWidth * scaleFactor * canvasScaleFactor);
        int yEnd = yStart + (int) (elementHeight * scaleFactor * canvasScaleFactor);
        return (xPos > xStart && yPos > yStart && xPos < xEnd && yPos < yEnd);
    }

    @Override
    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    //TODO: what do, maybe throw non-implementor exception
    @Override
    public int getMenuStartX() {
        return 0;
    }

    @Override
    public int getMenuStartY() {
        return 0;
    }
    public MenuSelection getMenuAttribute(){
        return displaySelection;
    }
    public CanvasDrawable getParent(){
        return menuOwner;
    }
}
