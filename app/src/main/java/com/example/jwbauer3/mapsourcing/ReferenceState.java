package com.example.jwbauer3.mapsourcing;

/**
 * Class allows for easy storage/access to a reference state
 * To be used to swap between screen and mesh movement.
 */
public class ReferenceState {
    public float startX;
    public float startY;
    public float transX;
    public float transY;
    public float prevTransX;
    public float prevTransY;
    public float scaleFactor;

    /*
    Instantiates and sets default values.
     */
    public ReferenceState() {
        startX = 0f;
        startY = 0f;
        transX = 0f;
        transY = 0f;
        prevTransX = 0f;
        prevTransY = 0f;
        scaleFactor = 1f;
    }
}
