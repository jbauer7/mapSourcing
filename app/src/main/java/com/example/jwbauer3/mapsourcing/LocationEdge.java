package com.example.jwbauer3.mapsourcing;

import android.graphics.Canvas;

/**
 * created by Eric on 12/9/15.
 */
public class LocationEdge extends BaseEdge {

    private static final int DEFAULTLOCATIONEDGEPRIORITY = 150;

    public LocationEdge(BaseNode start, BaseNode end, float scaleFactor) {
        super(DEFAULTLOCATIONEDGEPRIORITY, start, end);
        setScaleFactor(scaleFactor);
    }

    @Override
    public void draw(Canvas canvas) {
        //TODO: Is there a nicer way to handle this?
        BaseNode start = this.getStart();
        BaseNode end = this.getEnd();
        this.attributes.remove(Attribute.CLICKED);
        if (start instanceof LocationNode) {
            if (((LocationNode) start).getSourceEdge().attributes.contains(Attribute.CLICKED)) {
                    this.attributes.add(Attribute.CLICKED);
            }
        } else if (end instanceof LocationNode) {
            if (((LocationNode) end).getSourceEdge().attributes.contains(Attribute.CLICKED)) {
                this.attributes.add(Attribute.CLICKED);
            }
        }

        //Call the normal draw
        super.draw(canvas);
    }
    //can never click on a locationNode.
    @Override
    public boolean contains(int mapX, int mapY) {
        return false;
    }
}
