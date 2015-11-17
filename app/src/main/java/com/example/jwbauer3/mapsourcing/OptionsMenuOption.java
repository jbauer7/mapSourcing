package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * created by Nikhil on 11/16/2015.
 */
public class OptionsMenuOption extends CanvasDrawable {


    private static final int DEFAULTOPTIONPRIORITY = 300;
    private final int elementWidth = 150;
    private final int elementHeight = 100; //100 pixels tall
    private int elementNum;
    private CanvasDrawable menuOwner;
    String displayText;

    public OptionsMenuOption(CanvasDrawable owned, int elementNum, String display) {
        super(DEFAULTOPTIONPRIORITY);
        this.menuOwner = owned;
        this.elementNum = elementNum;
        displayText = display;
    }

    @Override
    public void draw(Canvas canvas, int xOffset, int yOffset) {
        Paint paint = new Paint();
        paint.setColor(Color.CYAN);
        int x = menuOwner.getMenuStartX() + xOffset;
        int y = menuOwner.getMenuStartY() + yOffset + elementHeight * elementNum;
        canvas.drawRect(x, y, x + elementWidth, y + elementHeight, paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(48f);
        //TODO: magic nums
        canvas.drawText(displayText, x, y + (elementHeight / 2), paint);

    }

    @Override
    public boolean contains(int xPos, int yPos, int xOffset, int yOffset, float canvasScaleFactor) {
        int xstart = (int) ((menuOwner.getMenuStartX() + xOffset) * canvasScaleFactor);
        int ystart = (int) ((menuOwner.getMenuStartY() + yOffset + elementHeight * elementNum) * canvasScaleFactor);
        int xend = xstart + (int)(elementWidth*canvasScaleFactor);
        int yend = ystart + (int)(elementHeight*canvasScaleFactor);
        return (xPos > xstart && yPos > ystart && xPos < xend && yPos < yend);
    }

    @Override
    public void setScaleFactor(float scaleFactor) {

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
}
